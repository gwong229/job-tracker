package com.example.jobtracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jobtracker.model.UserApplication;

public interface UserApplicationRepository extends JpaRepository<UserApplication, Long> {

    List<UserApplication> findByUserId(Long userId);

    Optional<UserApplication> findByUserIdAndApplicationId(Long userId, Long applicationId);

    boolean existsByUserIdAndApplicationId(Long userId, Long applicationId);
    boolean existsByApplicationId(Long applicationId);
}