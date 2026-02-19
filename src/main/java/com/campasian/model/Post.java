package com.campasian.model;

/**
 * Post data from the public.posts table.
 */
public class Post {

    private String id;
    private String userId;
    private String userName;
    private String content;
    private String university;
    private String createdAt;

    public Post() {}

    public Post(String id, String userId, String userName, String content, String university, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.university = university;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
