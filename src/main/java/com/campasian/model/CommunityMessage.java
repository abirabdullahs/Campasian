package com.campasian.model;

/**
 * UI-facing group chat message model.
 */
public class CommunityMessage {
    private final String roomId;
    private final String senderId;
    private final String senderName;
    private final String content;
    private final String createdAt;

    public CommunityMessage(String roomId, String senderId, String senderName, String content, String createdAt) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
