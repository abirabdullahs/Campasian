package com.abir.demo.models;

/**
 * Post model class for user feed items
 */
public class Post {
    private String postId;
    private String userId;
    private String content;
    private String imageUrl;
    private long timestamp;
    private int likes;
    private int comments;

    public Post() {
    }

    public Post(String postId, String userId, String content, String imageUrl) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
        this.likes = 0;
        this.comments = 0;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }
}
