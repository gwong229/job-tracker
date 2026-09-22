package com.example.jobtracker.service;

import com.example.jobtracker.dto.CreateUserApplicationRequest;
import com.example.jobtracker.dto.UpdateUserApplicationRequest;
import com.example.jobtracker.dto.UserApplicationResponse;
import com.example.jobtracker.exception.DuplicateApplicationException;
import com.example.jobtracker.exception.UnauthorizedActionException;
import com.example.jobtracker.model.JobApplication;
import com.example.jobtracker.model.User;
import com.example.jobtracker.model.UserApplication;
import com.example.jobtracker.repository.JobApplicationRepository;
import com.example.jobtracker.repository.UserApplicationRepository;
import com.example.jobtracker.repository.UserRepository;
import com.example.jobtracker.security.CurrentUserProvider;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private UserApplicationRepository userApplicationRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    private UserApplicationService newService() {
        return new UserApplicationService(
                userApplicationRepository, jobApplicationRepository, userRepository, currentUserProvider);
    }

    private User userWithId(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private JobApplication postingWithId(Long id, String company) {
        JobApplication posting = new JobApplication();
        posting.setId(id);
        posting.setCompany(company);
        return posting;
    }

    @Test
    void apply_savesAndReturnsResponse_whenNoDuplicate() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(userApplicationRepository.existsByUserIdAndApplicationId(1L, 5L)).thenReturn(false);
        when(jobApplicationRepository.findById(5L)).thenReturn(Optional.of(postingWithId(5L, "Shopify")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithId(1L)));
        when(userApplicationRepository.save(any(UserApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateUserApplicationRequest request =
                new CreateUserApplicationRequest(5L, "30 min", null, "Applied", null);

        UserApplicationService service = newService();
        UserApplicationResponse result = service.apply(request);

        assertEquals("Shopify", result.company());
        assertEquals("Applied", result.applicationStatus());
        verify(userApplicationRepository).save(any(UserApplication.class));
    }

    @Test
    void apply_throwsDuplicateException_whenAlreadyApplied() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(userApplicationRepository.existsByUserIdAndApplicationId(1L, 5L)).thenReturn(true);

        CreateUserApplicationRequest request =
                new CreateUserApplicationRequest(5L, null, null, null, null);

        UserApplicationService service = newService();

        assertThrows(DuplicateApplicationException.class, () -> service.apply(request));
        verify(userApplicationRepository, never()).save(any());
    }

    @Test
    void apply_throwsNotFound_whenPostingDoesNotExist() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(userApplicationRepository.existsByUserIdAndApplicationId(1L, 99L)).thenReturn(false);
        when(jobApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        CreateUserApplicationRequest request =
                new CreateUserApplicationRequest(99L, null, null, null, null);

        UserApplicationService service = newService();

        assertThrows(EntityNotFoundException.class, () -> service.apply(request));
    }

    @Test
    void getMyApplications_returnsOnlyCurrentUsersApplications() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        UserApplication ua = new UserApplication();
        ua.setId(10L);
        ua.setUser(userWithId(1L));
        ua.setApplication(postingWithId(5L, "Wealthsimple"));
        ua.setApplicationStatus("Applied");

        when(userApplicationRepository.findByUserId(1L)).thenReturn(List.of(ua));

        UserApplicationService service = newService();
        List<UserApplicationResponse> result = service.getMyApplications();

        assertEquals(1, result.size());
        assertEquals("Wealthsimple", result.get(0).company());
    }

    @Test
    void update_updatesOnlyProvidedFields_whenOwnedByCurrentUser() {
        UserApplication existing = new UserApplication();
        existing.setId(10L);
        existing.setUser(userWithId(1L));
        existing.setApplication(postingWithId(5L, "Shopify"));
        existing.setApplicationStatus("Applied");
        existing.setNotes("Initial notes");

        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(userApplicationRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userApplicationRepository.save(existing)).thenReturn(existing);

        UpdateUserApplicationRequest request =
                new UpdateUserApplicationRequest(null, null, "Interviewing", null);

        UserApplicationService service = newService();
        UserApplicationResponse result = service.update(10L, request);

        assertEquals("Interviewing", result.applicationStatus());
        assertEquals("Initial notes", result.notes()); // unchanged
    }

    @Test
    void update_throwsUnauthorized_whenNotOwnedByCurrentUser() {
        UserApplication existing = new UserApplication();
        existing.setId(10L);
        existing.setUser(userWithId(1L)); // owned by user 1
        existing.setApplication(postingWithId(5L, "Shopify"));

        when(currentUserProvider.getCurrentUserId()).thenReturn(2L); // request made by user 2
        when(userApplicationRepository.findById(10L)).thenReturn(Optional.of(existing));

        UpdateUserApplicationRequest request =
                new UpdateUserApplicationRequest(null, null, "Interviewing", null);

        UserApplicationService service = newService();

        assertThrows(UnauthorizedActionException.class, () -> service.update(10L, request));
        verify(userApplicationRepository, never()).save(any());
    }

    @Test
    void update_throwsNotFound_whenIdDoesNotExist() {
        when(userApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateUserApplicationRequest request =
                new UpdateUserApplicationRequest(null, null, "Interviewing", null);

        UserApplicationService service = newService();

        assertThrows(EntityNotFoundException.class, () -> service.update(99L, request));
    }

    @Test
    void delete_deletes_whenOwnedByCurrentUser() {
        UserApplication existing = new UserApplication();
        existing.setId(10L);
        existing.setUser(userWithId(1L));
        existing.setApplication(postingWithId(5L, "Shopify"));

        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(userApplicationRepository.findById(10L)).thenReturn(Optional.of(existing));

        UserApplicationService service = newService();
        service.delete(10L);

        verify(userApplicationRepository).delete(existing);
    }

    @Test
    void delete_throwsUnauthorized_whenNotOwnedByCurrentUser() {
        UserApplication existing = new UserApplication();
        existing.setId(10L);
        existing.setUser(userWithId(1L)); // owned by user 1
        existing.setApplication(postingWithId(5L, "Shopify"));

        when(currentUserProvider.getCurrentUserId()).thenReturn(2L); // request made by user 2
        when(userApplicationRepository.findById(10L)).thenReturn(Optional.of(existing));

        UserApplicationService service = newService();

        assertThrows(UnauthorizedActionException.class, () -> service.delete(10L));
        verify(userApplicationRepository, never()).delete(any());
    }

    @Test
    void delete_throwsNotFound_whenIdDoesNotExist() {
        when(userApplicationRepository.findById(99L)).thenReturn(Optional.empty());

        UserApplicationService service = newService();

        assertThrows(EntityNotFoundException.class, () -> service.delete(99L));
    }
}