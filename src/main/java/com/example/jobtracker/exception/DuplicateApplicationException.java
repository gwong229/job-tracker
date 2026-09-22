package com.example.jobtracker.exception;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException() {
        super("You have already applied to this job posting");
    }
}