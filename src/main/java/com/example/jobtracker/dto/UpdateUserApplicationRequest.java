package com.example.jobtracker.dto;

import java.time.LocalDate;

public record UpdateUserApplicationRequest(
        String transitTime,
        LocalDate dateApplied,
        String applicationStatus,
        String notes
) {}