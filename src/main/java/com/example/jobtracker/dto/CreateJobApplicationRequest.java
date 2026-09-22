package com.example.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateJobApplicationRequest(
        String link,
        @NotBlank String company,
        String location,
        String salary,
        String jobType,
        LocalDate applicationDeadline,
        String postingPlatform
) {}