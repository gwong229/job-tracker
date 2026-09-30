"""
Import job postings + per-person applications from the shared Excel tracker
into the job-tracker backend, via its real HTTP API (not direct DB writes).

Why via the API, not SQL:
- Respects validation (e.g. @NotBlank on company)
- Respects the CSRF + cookie-auth setup already built into the backend
- Respects the unique (user_id, application_id) constraint, so re-running
  is safe for the per-person step (duplicates are skipped, not errored)

WHAT THIS DOES NOT DO:
- It does NOT de-duplicate postings themselves. If you run this script
  twice, you'll get two copies of every posting. Only run it once against
  a given database, or clear the `application` table first if you need to
  re-run it.

SETUP (run once):
    pip install pandas requests openpyxl

USAGE:
    1. Edit EXCEL_PATH below to point at your actual .xlsx file.
    2. Make sure the backend is running (./mvnw spring-boot:run).
    3. Run: python import_job_data.py
    4. Enter Geoffrey's and Dean's login passwords when prompted
       (never hardcode passwords into this file).
"""

import getpass
import sys

import pandas as pd
import requests

# ---- Configuration: edit these two lines for your setup ----
EXCEL_PATH = "Job Tracker (Dean & Geoff).xlsx"  # path to your spreadsheet
API_BASE = "http://localhost:8080/api"

GEOFFREY_EMAIL = "geoffrey@example.com"
DEAN_EMAIL = "dean@deanmail.com"

# Statuses that mean "this person did NOT apply" -- skipped entirely.
NOT_APPLIED_STATUSES = {"Not Started", "Not Accepting", None}


def make_authenticated_session(email, password):
    """Logs in and returns a requests.Session that carries the jwt cookie
    and knows how to attach the X-XSRF-TOKEN header on state-changing calls."""
    session = requests.Session()

    # Prime the CSRF cookie first (same reason the frontend does this on load).
    csrf_resp = session.get(f"{API_BASE}/csrf")
    csrf_resp.raise_for_status()

    login_resp = session.post(
        f"{API_BASE}/auth/login",
        json={"email": email, "password": password},
    )
    if login_resp.status_code != 200:
        raise RuntimeError(
            f"Login failed for {email}: {login_resp.status_code} {login_resp.text}"
        )

    return session


def relogin(session, email, password):
    """Re-authenticates an existing session in place (refreshes its jwt cookie)."""
    session.get(f"{API_BASE}/csrf")
    resp = session.post(f"{API_BASE}/auth/login", json={"email": email, "password": password})
    if resp.status_code != 200:
        raise RuntimeError(f"Re-login failed for {email}: {resp.status_code} {resp.text}")


def fetch_existing_postings(session):
    """
    Returns a dict of {(company, link): id} for every posting already in the
    database, so the script can skip creating duplicates on a re-run after
    a partial failure.
    """
    existing = {}
    page = 0
    while True:
        resp = session.get(f"{API_BASE}/applications", params={"page": page, "size": 100})
        resp.raise_for_status()
        data = resp.json()
        for item in data["content"]:
            existing[(item["company"], item["link"])] = item["id"]
        if page >= data["totalPages"] - 1:
            break
        page += 1
    return existing


def get_fresh_csrf_headers(session):
    """
    Fetches a brand-new CSRF token right before use and returns headers
    with it attached, instead of trusting a cookie cached earlier in the
    session. This costs one extra cheap GET per state-changing call, but
    sidesteps an issue where a session's cached XSRF-TOKEN cookie stopped
    being valid after its first successful use.
    """
    resp = session.get(f"{API_BASE}/csrf")
    resp.raise_for_status()
    token = session.cookies.get("XSRF-TOKEN")
    if not token:
        raise RuntimeError("No XSRF-TOKEN cookie found even right after calling /csrf")
    return {"X-XSRF-TOKEN": token}


def to_iso_date_or_none(value):
    """Converts a pandas Timestamp/NaT to 'YYYY-MM-DD' string or None."""
    if pd.isna(value):
        return None
    return value.date().isoformat()


def to_str_or_none(value):
    if pd.isna(value):
        return None
    return str(value).strip()


def load_postings(path):
    df = pd.read_excel(path)
    # Real posting rows only -- everything from row 33 onward in this sheet
    # is a summary/stats section (application counts, rejection tallies, etc.)
    df = df.iloc[:33]
    # Drop fully blank trailing rows just in case.
    df = df.dropna(subset=["Company"])
    return df


def create_posting(session, row, email, password):
    payload = {
        "link": to_str_or_none(row["Job Position"]),
        "company": to_str_or_none(row["Company"]),
        "location": to_str_or_none(row["Location"]),
        "salary": to_str_or_none(row["Salary "]),  # note trailing space in header
        "jobType": to_str_or_none(row["Type of Job"]),
        "applicationDeadline": to_iso_date_or_none(row["Application Deadline"]),
        "postingPlatform": to_str_or_none(row["Platform of the Job Listing"]),
    }
    headers = get_fresh_csrf_headers(session)
    resp = session.post(f"{API_BASE}/applications", json=payload, headers=headers)
    if resp.status_code == 401:
        # Transient auth failure -- re-login once and retry before giving up.
        relogin(session, email, password)
        headers = get_fresh_csrf_headers(session)
        resp = session.post(f"{API_BASE}/applications", json=payload, headers=headers)
    if resp.status_code not in (200, 201):
        raise RuntimeError(f"Failed to create posting: {resp.status_code} {resp.text}")
    return resp.json()["id"]


def create_user_application(session, application_id, transit_time, date_applied, status, notes, email, password):
    payload = {
        "applicationId": application_id,
        "transitTime": transit_time,
        "dateApplied": date_applied,
        "applicationStatus": status,
        "notes": notes,
    }
    headers = get_fresh_csrf_headers(session)
    resp = session.post(f"{API_BASE}/my-applications", json=payload, headers=headers)
    if resp.status_code == 401:
        relogin(session, email, password)
        headers = get_fresh_csrf_headers(session)
        resp = session.post(f"{API_BASE}/my-applications", json=payload, headers=headers)
    if resp.status_code == 409:
        return "skipped (already exists)"
    if resp.status_code not in (200, 201):
        raise RuntimeError(f"{resp.status_code} {resp.text}")
    return "created"


def main():
    print("This will create real data in your database via the running backend.")
    confirm = input("Type 'yes' to continue: ")
    if confirm.strip().lower() != "yes":
        print("Aborted.")
        sys.exit(0)

    geoffrey_password = getpass.getpass("Geoffrey's password: ")
    dean_password = getpass.getpass("Dean's password: ")

    print("Logging in as Geoffrey and Dean...")
    geoffrey_session = make_authenticated_session(GEOFFREY_EMAIL, geoffrey_password)
    dean_session = make_authenticated_session(DEAN_EMAIL, dean_password)

    print(f"Reading {EXCEL_PATH}...")
    df = load_postings(EXCEL_PATH)
    print(f"Found {len(df)} posting rows.")

    print("Checking for postings already created by a previous run...")
    existing_postings = fetch_existing_postings(geoffrey_session)
    print(f"Found {len(existing_postings)} postings already in the database.")

    created_postings = 0
    skipped_postings = 0
    created_applications = 0
    errors = []

    for idx, row in df.iterrows():
        company = row["Company"]
        link = to_str_or_none(row["Job Position"])
        try:
            existing_id = existing_postings.get((company, link))
            if existing_id is not None:
                application_id = existing_id
                skipped_postings += 1
            else:
                application_id = create_posting(geoffrey_session, row, GEOFFREY_EMAIL, geoffrey_password)
                existing_postings[(company, link)] = application_id
                created_postings += 1
        except Exception as e:
            errors.append(f"Row {idx} ({company}): FAILED to create posting -- {e}")
            continue  # can't attach applications to a posting that doesn't exist

        d_status = to_str_or_none(row["Application Status (D)"])
        if d_status and d_status.strip() not in NOT_APPLIED_STATUSES:
            try:
                result = create_user_application(
                    dean_session,
                    application_id,
                    to_str_or_none(row["Transit Time (Dean)"]),
                    to_iso_date_or_none(row["Date Applied (Dean)"]),
                    d_status.strip(),
                    to_str_or_none(row["Notes"]),  # all notes attributed to Dean, per your instruction
                    DEAN_EMAIL,
                    dean_password,
                )
                created_applications += 1
                print(f"Row {idx} ({company}): Dean -> {result}")
            except Exception as e:
                errors.append(f"Row {idx} ({company}): FAILED Dean's application -- {e}")

        g_status = to_str_or_none(row["Application Status (G)"])
        if g_status and g_status.strip() not in NOT_APPLIED_STATUSES:
            try:
                result = create_user_application(
                    geoffrey_session,
                    application_id,
                    to_str_or_none(row["Transit Time (Geoffrey)"]),
                    to_iso_date_or_none(row["Date Applied (Geoffrey)"]),
                    g_status.strip(),
                    None,  # notes column is entirely Dean's per your instruction
                    GEOFFREY_EMAIL,
                    geoffrey_password,
                )
                created_applications += 1
                print(f"Row {idx} ({company}): Geoffrey -> {result}")
            except Exception as e:
                errors.append(f"Row {idx} ({company}): FAILED Geoffrey's application -- {e}")

    print("\n--- Summary ---")
    print(f"Postings created: {created_postings}")
    print(f"Postings skipped (already existed): {skipped_postings}")
    print(f"User-applications created/skipped: {created_applications}")
    if errors:
        print(f"\n{len(errors)} error(s):")
        for err in errors:
            print(f"  - {err}")
    else:
        print("No errors.")


if __name__ == "__main__":
    main()