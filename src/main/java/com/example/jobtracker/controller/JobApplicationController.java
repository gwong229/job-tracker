package com.example.jobtracker.controller;

import com.example.jobtracker.dto.CreateJobApplicationRequest;
import com.example.jobtracker.dto.JobApplicationResponse;
import com.example.jobtracker.dto.PatchJobApplicationRequest;
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

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public Page<JobApplicationResponse> getAllApplications(
            @RequestParam(required = false) String company,
            Pageable pageable) {

        if (company != null) {
            return service.searchApplicationsByCompany(company, pageable);
        }
        return service.getAllApplications(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getApplicationById(@PathVariable Long id) {
        return service.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public JobApplicationResponse createApplication(@Valid @RequestBody CreateJobApplicationRequest request) {
        return service.createApplication(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody CreateJobApplicationRequest request) {

        return service.updateApplication(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> patchApplication(
            @PathVariable Long id,
            @RequestBody PatchJobApplicationRequest request) {

        return service.patchApplication(id, request)
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