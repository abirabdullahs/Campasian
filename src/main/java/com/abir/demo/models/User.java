package com.abir.demo.models;

/**
 * User model class for the Campasian application
 */
public class User {
    private String userId;
    private String email;
    private String fullName;
    private String university;
    private String bio;
    private String profileImage;
    private long createdAt;
    private int followers;
    private int following;

    public User() {
    }

    public User(String userId, String email, String fullName, String university) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.university = university;
        this.createdAt = System.currentTimeMillis();
        this.followers = 0;
        this.following = 0;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", university='" + university + '\'' +
                '}';
    }
}
