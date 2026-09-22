package com.example.jobtracker.dto;

import java.time.LocalDate;

public record UserApplicationResponse(
        Long id,
        Long applicationId,
        String company,
        String transitTime,
        LocalDate dateApplied,
        String applicationStatus,
        String notes
) {}