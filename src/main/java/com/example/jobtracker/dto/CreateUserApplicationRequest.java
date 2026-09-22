package com.example.jobtracker.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateUserApplicationRequest(
        @NotNull Long applicationId,
        String transitTime,
        LocalDate dateApplied,
        String applicationStatus,
        String notes
) {}
