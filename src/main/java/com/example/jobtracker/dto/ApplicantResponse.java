package com.example.jobtracker.dto;

import java.time.LocalDate;

public record ApplicantResponse(
        String username,
        String transitTime,
        LocalDate dateApplied,
        String applicationStatus,
        String notes
) {}