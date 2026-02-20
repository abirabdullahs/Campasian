package com.campasian.view;

/**
 * Shared context for navigation (e.g. which profile to view).
 */
public final class NavigationContext {

    private static String viewingProfileUserId;

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
}
