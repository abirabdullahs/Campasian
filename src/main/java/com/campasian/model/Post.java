package com.campasian.model;

/**
 * Post data from the public.posts table.
 */
public class Post {

    private Long id;
    private String userId;
    private String userName;
    private String content;
    private String university;
    private String createdAt;
    private int likeCount;
    private boolean likedByMe;
    private int commentCount;
    private String imageUrl;

    public Post() {}

    public Post(Long id, String userId, String userName, String content, String university, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.university = university;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
