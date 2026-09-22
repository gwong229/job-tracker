package com.example.jobtracker.dto;

public record LoginResponse(String token, UserResponse user) {}