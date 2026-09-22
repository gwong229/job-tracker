package com.example.jobtracker.dto;

import java.time.LocalDate;

public record JobApplicationResponse(
        Long id,
        String link,
        String company,
        String location,
        String salary,
        String jobType,
        LocalDate applicationDeadline,
        String postingPlatform
) {}