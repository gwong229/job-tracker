package com.example.jobtracker.service;

import com.example.jobtracker.dto.CreateJobApplicationRequest;
import com.example.jobtracker.dto.JobApplicationResponse;
import com.example.jobtracker.dto.PatchJobApplicationRequest;
import com.example.jobtracker.exception.ApplicationHasApplicantsException;
import com.example.jobtracker.model.JobApplication;
import com.example.jobtracker.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.jobtracker.repository.UserApplicationRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;
    @Mock
    private UserApplicationRepository userApplicationRepository; 

    @Test
    void getApplicationById_returnsApplication_whenIdExists() {
        JobApplication application = new JobApplication();
        application.setCompany("Shopify");

        when(repository.findById(1L)).thenReturn(Optional.of(application));

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);
        Optional<JobApplicationResponse> result = service.getApplicationById(1L);

        assertTrue(result.isPresent());
        assertEquals("Shopify", result.get().company());
    }

    @Test
    void getApplicationById_returnsEmpty_whenIdDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);
        Optional<JobApplicationResponse> result = service.getApplicationById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createApplication_savesAndReturnsApplication() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest(
                null, "Wealthsimple", null, null, null, null, null);

        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);
        JobApplicationResponse result = service.createApplication(request);

        assertEquals("Wealthsimple", result.company());
        verify(repository).save(any(JobApplication.class));
    }

    @Test
    void updateApplication_updatesAndReturnsApplication_whenIdExists() {
        JobApplication existing = new JobApplication();
        existing.setCompany("OldCompany");

        CreateJobApplicationRequest request = new CreateJobApplicationRequest(
                null, "NewCompany", null, null, null, null, null);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);
        Optional<JobApplicationResponse> result = service.updateApplication(1L, request);

        assertTrue(result.isPresent());
        assertEquals("NewCompany", result.get().company());
    }

    @Test
    void updateApplication_returnsEmpty_whenIdDoesNotExist() {
        CreateJobApplicationRequest request = new CreateJobApplicationRequest(
                null, "Anything", null, null, null, null, null);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);
        Optional<JobApplicationResponse> result = service.updateApplication(99L, request);

        assertTrue(result.isEmpty());
    }

    @Test
    void patchApplication_updatesOnlyProvidedFields() {
        JobApplication existing = new JobApplication();
        existing.setCompany("Shopify");
        existing.setJobType("Full-time");

        PatchJobApplicationRequest patch = new PatchJobApplicationRequest(
                null, null, null, null, "Contract", null, null); // only jobType provided

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);
        Optional<JobApplicationResponse> result = service.patchApplication(1L, patch);

        assertTrue(result.isPresent());
        assertEquals("Contract", result.get().jobType());
        assertEquals("Shopify", result.get().company()); // unchanged
    }

    @Test
    void deleteApplication_returnsTrue_whenIdExists() {
        when(repository.existsById(1L)).thenReturn(true);
        when(userApplicationRepository.existsByApplicationId(1L)).thenReturn(false);

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);
        boolean result = service.deleteApplication(1L);

        assertTrue(result);
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteApplication_returnsFalse_whenIdDoesNotExist() {
        when(repository.existsById(99L)).thenReturn(false);

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);
        boolean result = service.deleteApplication(99L);

        assertFalse(result);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteApplication_throwsException_whenApplicantsExist() {
        when(repository.existsById(1L)).thenReturn(true);
        when(userApplicationRepository.existsByApplicationId(1L)).thenReturn(true);

        JobApplicationService service = new JobApplicationService(repository, userApplicationRepository);

        assertThrows(ApplicationHasApplicantsException.class, () -> service.deleteApplication(1L));
        verify(repository, never()).deleteById(any());
    }
    
}