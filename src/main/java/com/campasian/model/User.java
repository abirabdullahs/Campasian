package com.campasian.model;

import java.time.LocalDateTime;

/**
 * Domain model representing a Campasian user.
 */
public class User {

    private int id;
    private String fullName;
    private String email;
    private String einNumber;
    private String universityName;
    private String department;
    private String passwordHash;
    private LocalDateTime createdAt;

    public User() {}

    public User(String fullName, String email, String einNumber, String universityName,
                String department, String passwordHash) {
        this.fullName = fullName;
        this.email = email;
        this.einNumber = einNumber;
        this.universityName = universityName;
        this.department = department;
        this.passwordHash = passwordHash;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEinNumber() { return einNumber; }
    public void setEinNumber(String einNumber) { this.einNumber = einNumber; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
