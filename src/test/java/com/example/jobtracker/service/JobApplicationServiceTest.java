package com.example.jobtracker.service;

import com.example.jobtracker.model.JobApplication;
import com.example.jobtracker.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Test
    void getApplicationById_returnsApplication_whenIdExists() {
        JobApplication application = new JobApplication();
        application.setCompany("Shopify");

        when(repository.findById(1L)).thenReturn(Optional.of(application));

        JobApplicationService service = new JobApplicationService(repository);
        Optional<JobApplication> result = service.getApplicationById(1L);

        assertTrue(result.isPresent());
        assertEquals("Shopify", result.get().getCompany());
    }

    @Test
    void getApplicationById_returnsEmpty_whenIdDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        JobApplicationService service = new JobApplicationService(repository);
        Optional<JobApplication> result = service.getApplicationById(99L);

        assertTrue(result.isEmpty());
    }
    @Test
    void createApplication_savesAndReturnsApplication() {
        JobApplication application = new JobApplication();
        application.setCompany("Wealthsimple");

        when(repository.save(application)).thenReturn(application);

        JobApplicationService service = new JobApplicationService(repository);
        JobApplication result = service.createApplication(application);

        assertEquals("Wealthsimple", result.getCompany());
        verify(repository).save(application);
    }

    @Test
    void updateApplication_updatesAndReturnsApplication_whenIdExists() {
        JobApplication existing = new JobApplication();
        existing.setCompany("OldCompany");

        JobApplication updated = new JobApplication();
        updated.setCompany("NewCompany");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        JobApplicationService service = new JobApplicationService(repository);
        Optional<JobApplication> result = service.updateApplication(1L, updated);

        assertTrue(result.isPresent());
        assertEquals("NewCompany", result.get().getCompany());
    }

    @Test
    void updateApplication_returnsEmpty_whenIdDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        JobApplicationService service = new JobApplicationService(repository);
        Optional<JobApplication> result = service.updateApplication(99L, new JobApplication());

        assertTrue(result.isEmpty());
    }

    @Test
    void patchApplication_updatesOnlyProvidedFields() {
        JobApplication existing = new JobApplication();
        existing.setCompany("Shopify");
        existing.setJobType("Full-time");

        JobApplication patch = new JobApplication();
        patch.setJobType("Contract"); // only jobType provided, company left null

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        JobApplicationService service = new JobApplicationService(repository);
        Optional<JobApplication> result = service.patchApplication(1L, patch);

        assertTrue(result.isPresent());
        assertEquals("Contract", result.get().getJobType());
        assertEquals("Shopify", result.get().getCompany()); // unchanged
    }

    @Test
    void deleteApplication_returnsTrue_whenIdExists() {
        when(repository.existsById(1L)).thenReturn(true);

        JobApplicationService service = new JobApplicationService(repository);
        boolean result = service.deleteApplication(1L);

        assertTrue(result);
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteApplication_returnsFalse_whenIdDoesNotExist() {
        when(repository.existsById(99L)).thenReturn(false);

        JobApplicationService service = new JobApplicationService(repository);
        boolean result = service.deleteApplication(99L);

        assertFalse(result);
        verify(repository, never()).deleteById(any());
    }
}