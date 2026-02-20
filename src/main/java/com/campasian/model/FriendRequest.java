package com.campasian.model;

/**
 * Friend request from friend_requests table.
 */
public class FriendRequest {
    private String id;
    private String fromId;
    private String toId;
    private String status;    // pending, accepted, rejected
    private String fromName;  // joined from profiles
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromId() { return fromId; }
    public void setFromId(String fromId) { this.fromId = fromId; }

    public String getToId() { return toId; }
    public void setToId(String toId) { this.toId = toId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
