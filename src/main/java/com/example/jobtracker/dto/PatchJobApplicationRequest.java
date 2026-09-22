package com.example.jobtracker.dto;

import java.time.LocalDate;

public record PatchJobApplicationRequest(
        String link,
        String company,
        String location,
        String salary,
        String jobType,
        LocalDate applicationDeadline,
        String postingPlatform
) {}