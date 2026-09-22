package com.example.jobtracker.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.jobtracker.dto.CreateUserApplicationRequest;
import com.example.jobtracker.dto.UpdateUserApplicationRequest;
import com.example.jobtracker.dto.UserApplicationResponse;
import com.example.jobtracker.service.UserApplicationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/my-applications")
public class UserApplicationController {

    private final UserApplicationService userApplicationService;

    public UserApplicationController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PostMapping
    public ResponseEntity<UserApplicationResponse> apply(@Valid @RequestBody CreateUserApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userApplicationService.apply(request));
    }

    @GetMapping
    public ResponseEntity<List<UserApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(userApplicationService.getMyApplications());
    }
    @PatchMapping("/{id}")
    public ResponseEntity<UserApplicationResponse> update(@PathVariable Long id,
                                                        @RequestBody UpdateUserApplicationRequest request) {
        return ResponseEntity.ok(userApplicationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}