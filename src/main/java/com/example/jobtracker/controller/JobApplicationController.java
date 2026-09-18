package com.example.jobtracker.controller;

import com.example.jobtracker.model.JobApplication;
import com.example.jobtracker.repository.JobApplicationRepository;
import com.example.jobtracker.service.JobApplicationService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationRepository repository, JobApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public Page<JobApplication> getAllApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String company,
            Pageable pageable) {

        if (status != null) {
            return service.getApplicationsByStatus(status, pageable);
        }
        if (company != null) {
            return service.searchApplicationsByCompany(company, pageable);
        }
        return service.getAllApplications(pageable);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobApplication> getApplicationById(@PathVariable Long id) {
        return service.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public JobApplication createApplication(@Valid @RequestBody JobApplication application) {
        return service.createApplication(application);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<JobApplication> updateApplication(
            @PathVariable Long id,
            @RequestBody JobApplication updatedApplication) {

        return service.updateApplication(id, updatedApplication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<JobApplication> patchApplication(
            @PathVariable Long id,
            @RequestBody JobApplication updatedApplication) {

        return service.patchApplication(id, updatedApplication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        if (!service.deleteApplication(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}