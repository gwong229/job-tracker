package com.example.jobtracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.jobtracker.dto.CreateUserApplicationRequest;
import com.example.jobtracker.dto.UserApplicationResponse;
import com.example.jobtracker.exception.DuplicateApplicationException;
import com.example.jobtracker.model.JobApplication;
import com.example.jobtracker.model.User;
import com.example.jobtracker.model.UserApplication;
import com.example.jobtracker.repository.JobApplicationRepository;
import com.example.jobtracker.repository.UserApplicationRepository;
import com.example.jobtracker.repository.UserRepository;
import com.example.jobtracker.security.CurrentUserProvider;
import com.example.jobtracker.dto.UpdateUserApplicationRequest;
import com.example.jobtracker.exception.UnauthorizedActionException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserApplicationService {

    private final UserApplicationRepository userApplicationRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    public UserApplicationService(UserApplicationRepository userApplicationRepository,
                                  JobApplicationRepository jobApplicationRepository,
                                  UserRepository userRepository,
                                  CurrentUserProvider currentUserProvider) {
        this.userApplicationRepository = userApplicationRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public UserApplicationResponse apply(CreateUserApplicationRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();

        if (userApplicationRepository.existsByUserIdAndApplicationId(currentUserId, request.applicationId())) {
            throw new DuplicateApplicationException();
        }

        JobApplication posting = jobApplicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new EntityNotFoundException("Job posting not found: " + request.applicationId()));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + currentUserId));

        UserApplication userApplication = new UserApplication();
        userApplication.setUser(user);
        userApplication.setApplication(posting);
        userApplication.setTransitTime(request.transitTime());
        userApplication.setDateApplied(request.dateApplied());
        userApplication.setApplicationStatus(request.applicationStatus());
        userApplication.setNotes(request.notes());

        UserApplication saved = userApplicationRepository.save(userApplication);
        return toResponse(saved);
    }

    public List<UserApplicationResponse> getMyApplications() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        return userApplicationRepository.findByUserId(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserApplicationResponse toResponse(UserApplication ua) {
        return new UserApplicationResponse(
                ua.getId(),
                ua.getApplication().getId(),
                ua.getApplication().getCompany(),
                ua.getTransitTime(),
                ua.getDateApplied(),
                ua.getApplicationStatus(),
                ua.getNotes()
        );
    }
    public UserApplicationResponse update(Long id, UpdateUserApplicationRequest request) {
        UserApplication userApplication = userApplicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + id));

        assertOwnedByCurrentUser(userApplication);

        if (request.transitTime() != null) {
            userApplication.setTransitTime(request.transitTime());
        }
        if (request.dateApplied() != null) {
            userApplication.setDateApplied(request.dateApplied());
        }
        if (request.applicationStatus() != null) {
            userApplication.setApplicationStatus(request.applicationStatus());
        }
        if (request.notes() != null) {
            userApplication.setNotes(request.notes());
        }

        UserApplication saved = userApplicationRepository.save(userApplication);
        return toResponse(saved);
    }

    public void delete(Long id) {
        UserApplication userApplication = userApplicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found: " + id));

        assertOwnedByCurrentUser(userApplication);

        userApplicationRepository.delete(userApplication);
    }

    private void assertOwnedByCurrentUser(UserApplication userApplication) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (!userApplication.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedActionException("You can only modify your own applications");
        }
    }
}