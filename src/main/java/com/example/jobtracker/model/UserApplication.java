package com.example.jobtracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import jakarta.persistence.Column;

@Entity
@Table(
    name = "user_application",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "application_id"})
)
public class UserApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    private String transitTime;

    private LocalDate dateApplied;

    private String applicationStatus;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public UserApplication() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public JobApplication getApplication() { return application; }
    public void setApplication(JobApplication application) { this.application = application; }

    public String getTransitTime() { return transitTime; }
    public void setTransitTime(String transitTime) { this.transitTime = transitTime; }

    public LocalDate getDateApplied() { return dateApplied; }
    public void setDateApplied(LocalDate dateApplied) { this.dateApplied = dateApplied; }

    public String getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(String applicationStatus) { this.applicationStatus = applicationStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}