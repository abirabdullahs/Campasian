package com.abir.demo.models;

/**
 * Club model class for university clubs and groups
 */
public class Club {
    private String clubId;
    private String name;
    private String description;
    private String category;
    private long createdAt;
    private int members;
    private String image;

    public Club() {
    }

    public Club(String clubId, String name, String description, String category) {
        this.clubId = clubId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.createdAt = System.currentTimeMillis();
        this.members = 0;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
