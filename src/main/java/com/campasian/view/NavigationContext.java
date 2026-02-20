package com.campasian.view;

/**
 * Shared context for navigation (e.g. which profile or chat partner to view).
 */
public final class NavigationContext {

    private static String viewingProfileUserId;
    private static String chatPartnerUserId;
    private static String chatPartnerName;

    private NavigationContext() {}

    /** Set before loading profile-view to show another user's profile. */
    public static void setViewingProfileUserId(String userId) {
        viewingProfileUserId = userId;
    }

    /** Null = show current user's profile. */
    public static String getViewingProfileUserId() {
        return viewingProfileUserId;
    }

    /** Clear after loading (optional, for next load). */
    public static void clearViewingProfile() {
        viewingProfileUserId = null;
    }

    /** Set before loading chat to open conversation with a specific user. */
    public static void setChatPartner(String userId, String name) {
        chatPartnerUserId = userId;
        chatPartnerName = name;
    }

    public static String getChatPartnerUserId() { return chatPartnerUserId; }
    public static String getChatPartnerName() { return chatPartnerName; }

    public static void clearChatPartner() {
        chatPartnerUserId = null;
        chatPartnerName = null;
    }
}
