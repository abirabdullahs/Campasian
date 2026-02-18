package com.abir.demo.models;

/**
 * Message model class for direct messaging
 */
public class Message {
    private String messageId;
    private String from;
    private String to;
    private String text;
    private long timestamp;
    private boolean read;

    public Message() {
    }

    public Message(String messageId, String from, String to, String text) {
        this.messageId = messageId;
        this.from = from;
        this.to = to;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
