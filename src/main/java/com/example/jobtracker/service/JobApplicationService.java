package com.example.jobtracker.service;

import com.example.jobtracker.model.JobApplication;
import com.example.jobtracker.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.Optional;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public Page<JobApplication> getAllApplications(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<JobApplication> getApplicationById(Long id) {
        return repository.findById(id);
    }

    public JobApplication createApplication(JobApplication application) {
        return repository.save(application);
    }
    public Optional<JobApplication> updateApplication(Long id, JobApplication updatedApplication) {
        return repository.findById(id)
                .map(application -> {
                    application.setLink(updatedApplication.getLink());
                    application.setCompany(updatedApplication.getCompany());
                    application.setTransitTime(updatedApplication.getTransitTime());
                    application.setLocation(updatedApplication.getLocation());
                    application.setSalary(updatedApplication.getSalary());
                    application.setJobType(updatedApplication.getJobType());
                    application.setApplicationDeadline(updatedApplication.getApplicationDeadline());
                    application.setDateApplied(updatedApplication.getDateApplied());
                    application.setApplicationStatus(updatedApplication.getApplicationStatus());
                    application.setPostingPlatform(updatedApplication.getPostingPlatform());
                    application.setNotes(updatedApplication.getNotes());
                    return repository.save(application);
                });
    }
    public Optional<JobApplication> patchApplication(Long id, JobApplication updatedApplication) {
        return repository.findById(id)
                .map(application -> {
                    if (updatedApplication.getLink() != null) {
                        application.setLink(updatedApplication.getLink());
                    }
                    if (updatedApplication.getCompany() != null) {
                        application.setCompany(updatedApplication.getCompany());
                    }
                    if (updatedApplication.getTransitTime() != null) {
                        application.setTransitTime(updatedApplication.getTransitTime());
                    }
                    if (updatedApplication.getLocation() != null) {
                        application.setLocation(updatedApplication.getLocation());
                    }
                    if (updatedApplication.getSalary() != null) {
                        application.setSalary(updatedApplication.getSalary());
                    }
                    if (updatedApplication.getJobType() != null) {
                        application.setJobType(updatedApplication.getJobType());
                    }
                    if (updatedApplication.getApplicationDeadline() != null) {
                        application.setApplicationDeadline(updatedApplication.getApplicationDeadline());
                    }
                    if (updatedApplication.getDateApplied() != null) {
                        application.setDateApplied(updatedApplication.getDateApplied());
                    }
                    if (updatedApplication.getApplicationStatus() != null) {
                        application.setApplicationStatus(updatedApplication.getApplicationStatus());
                    }
                    if (updatedApplication.getPostingPlatform() != null) {
                        application.setPostingPlatform(updatedApplication.getPostingPlatform());
                    }
                    if (updatedApplication.getNotes() != null) {
                        application.setNotes(updatedApplication.getNotes());
                    }
                    return repository.save(application);
                });
    }
    public boolean deleteApplication(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
    public Page<JobApplication> getApplicationsByStatus(String status, Pageable pageable) {
        return repository.findByApplicationStatus(status, pageable);
    }

    public Page<JobApplication> searchApplicationsByCompany(String company, Pageable pageable) {
        return repository.findByCompanyContainingIgnoreCase(company, pageable);
    }
}