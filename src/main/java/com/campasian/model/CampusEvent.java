package com.campasian.model;

/**
 * Campus event from campus_events table.
 */
public class CampusEvent {
    private String id;
    private String title;
    private String description;
    private String eventDate;
    private String venue;
    private int interestedCount;
    private boolean userInterested;
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public int getInterestedCount() { return interestedCount; }
    public void setInterestedCount(int interestedCount) { this.interestedCount = interestedCount; }
    public boolean isUserInterested() { return userInterested; }
    public void setUserInterested(boolean userInterested) { this.userInterested = userInterested; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
