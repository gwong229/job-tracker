package com.example.jobtracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;

@Entity
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String link;
    @NotBlank
    private String company;
    private String transitTime;
    private String location;
    private String salary;
    private String jobType;
    private LocalDate applicationDeadline;
    private LocalDate dateApplied;
    private String applicationStatus;
    private String postingPlatform;
    private String notes;

    public JobApplication() {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public String getTransitTime() {
        return transitTime;
    }
    public void setTransitTime(String transitTime) {
        this.transitTime = transitTime;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getSalary() {
        return salary;
    }
    public void setSalary(String salary) {
        this.salary = salary;
    }
    public String getJobType() {
        return jobType;
    }
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }
    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    public LocalDate getDateApplied() {
        return dateApplied;
    }
    public void setDateApplied(LocalDate dateApplied) {
        this.dateApplied = dateApplied;
    }
    public String getApplicationStatus() {
        return applicationStatus;
    }
    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }
    public String getPostingPlatform() {
        return postingPlatform;
    }
    public void setPostingPlatform(String postingPlatform) {
        this.postingPlatform = postingPlatform;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
}