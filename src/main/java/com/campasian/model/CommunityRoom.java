package com.campasian.model;

/**
 * Describes a student community room shown in the sidebar.
 */
public class CommunityRoom {
    private final String id;
    private final String name;
    private final String description;
    private final String scopeLabel;
    private final int memberCount;
    private final boolean verified;
    private final boolean autoJoined;

    public CommunityRoom(String id, String name, String description, String scopeLabel,
                         int memberCount, boolean verified, boolean autoJoined) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.scopeLabel = scopeLabel;
        this.memberCount = memberCount;
        this.verified = verified;
        this.autoJoined = autoJoined;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getScopeLabel() {
        return scopeLabel;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isAutoJoined() {
        return autoJoined;
    }
}
