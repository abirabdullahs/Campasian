package com.campasian.service;

import java.util.prefs.Preferences;

/**
 * Persists session tokens for Remember Me. Uses java.util.prefs.Preferences.
 */
public final class TokenManager {

    private static final String PREF_NODE = "com.campasian";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";

    private static Preferences prefs() {
        return Preferences.userRoot().node(PREF_NODE);
    }

    public static void saveTokens(String accessToken, String refreshToken, String userId) {
        Preferences p = prefs();
        if (accessToken != null) p.put(KEY_ACCESS_TOKEN, accessToken);
        else p.remove(KEY_ACCESS_TOKEN);
        if (refreshToken != null) p.put(KEY_REFRESH_TOKEN, refreshToken);
        else p.remove(KEY_REFRESH_TOKEN);
        if (userId != null) p.put(KEY_USER_ID, userId);
        else p.remove(KEY_USER_ID);
    }

    public static String getAccessToken() {
        return prefs().get(KEY_ACCESS_TOKEN, null);
    }

    public static String getRefreshToken() {
        return prefs().get(KEY_REFRESH_TOKEN, null);
    }

    public static String getUserId() {
        return prefs().get(KEY_USER_ID, null);
    }

    public static boolean hasStoredSession() {
        String at = getAccessToken();
        String uid = getUserId();
        return at != null && !at.isBlank() && uid != null && !uid.isBlank();
    }

    public static void clearTokens() {
        Preferences p = prefs();
        p.remove(KEY_ACCESS_TOKEN);
        p.remove(KEY_REFRESH_TOKEN);
        p.remove(KEY_USER_ID);
    }
}
