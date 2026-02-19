package com.campasian.service;

import com.campasian.model.User;
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
                    // Profile table may not exist or RLS may block; user_metadata is already stored
                    System.err.println("Profile insert failed (user_metadata is stored): " + e.getMessage());
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
}
