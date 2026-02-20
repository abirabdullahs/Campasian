package com.campasian.service;

import com.campasian.model.User;
import com.campasian.model.UserProfile;
import com.google.gson.JsonObject;

/**
 * Handles authentication via Supabase REST API.
 */
public final class AuthService {

    private static final AuthService INSTANCE = new AuthService();
    private final ApiService apiService = ApiService.getInstance();

    private AuthService() {}

    public static AuthService getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a new user via Supabase Auth, stores metadata, and inserts profile.
     * 1. Auth signup with user_metadata (full_name, university_name, ein_number, department)
     * 2. Insert into public.profiles table for queryable profile data
     */
    public void signup(String fullName, String email, String universityName, String number,
                       String department, String password) throws ApiException {
        JsonObject meta = new JsonObject();
        meta.addProperty("full_name", fullName);
        meta.addProperty("university_name", universityName);
        meta.addProperty("ein_number", number);
        meta.addProperty("department", department);

        JsonObject response = apiService.signUp(email, password, meta);
        var userEl = response.get("user");
        if (userEl != null && userEl.isJsonObject()) {
            var userJson = userEl.getAsJsonObject();
            if (userJson.has("id")) {
                String userId = userJson.get("id").getAsString();
                try {
                    apiService.createProfile(userId, fullName, universityName, number, department);
                } catch (ApiException e) {
                    // Profile insert failed - log full Supabase error for debugging
                    System.err.println("[Campasian] Profile insert failed (user_metadata is stored in Auth): " + e.getMessage());
                    if (e.getResponseBody() != null && !e.getResponseBody().isBlank()) {
                        System.err.println("[Campasian] Supabase response: " + e.getResponseBody());
                    }
                    System.err.println("[Campasian] Run with -Dcampasian.log.api=true to log all API errors.");
                    // User can still log in; profile can be created later or fixed via migration
                }
            }
        }
    }

    /**
     * Verifies credentials via Supabase Auth and returns the user if valid.
     */
    public User login(String email, String password) throws ApiException {
        try {
            return apiService.login(email, password);
        } catch (ApiException e) {
            if (e.isInvalidCredentials()) return null;
            throw e;
        }
    }

    /**
     * Fetches the current user's profile from the profiles table.
     * Uses the session's userId. Returns null if not found or no session.
     */
    public UserProfile getCurrentUserProfile() throws ApiException {
        String userId = apiService.getCurrentUserId();
        if (userId == null || userId.isBlank()) return null;
        return apiService.getProfile(userId);
    }

    /**
     * Clears session and navigates to login. Call from logout.
     */
    public void logout() {
        apiService.clearSession();
    }

    /**
     * Tries to restore session from TokenManager. Returns true if valid session restored.
     * If access token is expired, attempts refresh via refresh token before failing.
     */
    public boolean tryRestoreSession() {
        if (!apiService.restoreSession()) return false;
        try {
            if (getCurrentUserProfile() != null) return true;
        } catch (Exception e) {
            // Profile fetch may fail if access token expired; try refresh
            try {
                if (apiService.refreshAccessToken() && getCurrentUserProfile() != null) return true;
            } catch (Exception ignored) {}
            apiService.clearSession();
        }
        return false;
    }
}
