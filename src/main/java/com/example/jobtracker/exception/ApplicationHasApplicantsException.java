package com.example.jobtracker.exception;

public class ApplicationHasApplicantsException extends RuntimeException {

    public ApplicationHasApplicantsException() {
        super("Cannot delete this posting: one or more users have already applied to it");
    }
}