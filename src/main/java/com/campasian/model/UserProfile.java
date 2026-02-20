package com.campasian.model;

/**
 * Profile data from the public.profiles table.
 */
public class UserProfile {

    private String id;
    private String fullName;
    private String universityName;
    private String einNumber;
    private String bio;
    private String avatarUrl;
    private String bloodGroup;
    private String session;
    private String batch;
    private String department;

    public UserProfile() {}

    public UserProfile(String id, String fullName, String universityName, String einNumber) {
        this.id = id;
        this.fullName = fullName;
        this.universityName = universityName;
        this.einNumber = einNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getEinNumber() { return einNumber; }
    public void setEinNumber(String einNumber) { this.einNumber = einNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getSession() { return session; }
    public void setSession(String session) { this.session = session; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
