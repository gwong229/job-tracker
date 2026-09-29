package com.example.jobtracker.service;

import com.example.jobtracker.dto.ApplicantResponse;
import com.example.jobtracker.dto.CreateJobApplicationRequest;
import com.example.jobtracker.dto.JobApplicationResponse;
import com.example.jobtracker.dto.PatchJobApplicationRequest;
import com.example.jobtracker.model.JobApplication;
import com.example.jobtracker.model.UserApplication;
import com.example.jobtracker.repository.JobApplicationRepository;
import com.example.jobtracker.repository.UserApplicationRepository;
import com.example.jobtracker.exception.ApplicationHasApplicantsException;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final UserApplicationRepository userApplicationRepository;

    public JobApplicationService(JobApplicationRepository repository, UserApplicationRepository userApplicationRepository) {
        this.repository = repository;
        this.userApplicationRepository = userApplicationRepository;
    }

    public Page<JobApplicationResponse> getAllApplications(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    public Optional<JobApplicationResponse> getApplicationById(Long id) {
        return repository.findById(id).map(this::toResponse);
    }

    public JobApplicationResponse createApplication(CreateJobApplicationRequest request) {
        JobApplication application = new JobApplication();
        application.setLink(request.link());
        application.setCompany(request.company());
        application.setLocation(request.location());
        application.setSalary(request.salary());
        application.setJobType(request.jobType());
        application.setApplicationDeadline(request.applicationDeadline());
        application.setPostingPlatform(request.postingPlatform());

        return toResponse(repository.save(application));
    }

    public Optional<JobApplicationResponse> updateApplication(Long id, CreateJobApplicationRequest request) {
        return repository.findById(id)
                .map(application -> {
                    application.setLink(request.link());
                    application.setCompany(request.company());
                    application.setLocation(request.location());
                    application.setSalary(request.salary());
                    application.setJobType(request.jobType());
                    application.setApplicationDeadline(request.applicationDeadline());
                    application.setPostingPlatform(request.postingPlatform());
                    return toResponse(repository.save(application));
                });
    }

    public Optional<JobApplicationResponse> patchApplication(Long id, PatchJobApplicationRequest request) {
        return repository.findById(id)
                .map(application -> {
                    if (request.link() != null) {
                        application.setLink(request.link());
                    }
                    if (request.company() != null) {
                        application.setCompany(request.company());
                    }
                    if (request.location() != null) {
                        application.setLocation(request.location());
                    }
                    if (request.salary() != null) {
                        application.setSalary(request.salary());
                    }
                    if (request.jobType() != null) {
                        application.setJobType(request.jobType());
                    }
                    if (request.applicationDeadline() != null) {
                        application.setApplicationDeadline(request.applicationDeadline());
                    }
                    if (request.postingPlatform() != null) {
                        application.setPostingPlatform(request.postingPlatform());
                    }
                    return toResponse(repository.save(application));
                });
    }

    public boolean deleteApplication(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        if (userApplicationRepository.existsByApplicationId(id)) {
            throw new ApplicationHasApplicantsException();
        }
        repository.deleteById(id);
        return true;
    }

    public Page<JobApplicationResponse> searchApplicationsByCompany(String company, Pageable pageable) {
        return repository.findByCompanyContainingIgnoreCase(company, pageable).map(this::toResponse);
    }

    public Optional<List<ApplicantResponse>> getApplicants(Long id) {
        if (!repository.existsById(id)) {
            return Optional.empty();
        }
        List<ApplicantResponse> applicants = userApplicationRepository.findByApplicationId(id).stream()
                .map(this::toApplicantResponse)
                .toList();
        return Optional.of(applicants);
    }

    private JobApplicationResponse toResponse(JobApplication application) {
        return new JobApplicationResponse(
                application.getId(),
                application.getLink(),
                application.getCompany(),
                application.getLocation(),
                application.getSalary(),
                application.getJobType(),
                application.getApplicationDeadline(),
                application.getPostingPlatform()
        );
    }

    private ApplicantResponse toApplicantResponse(UserApplication userApplication) {
        return new ApplicantResponse(
                userApplication.getUser().getUsername(),
                userApplication.getTransitTime(),
                userApplication.getDateApplied(),
                userApplication.getApplicationStatus(),
                userApplication.getNotes()
        );
    }
}