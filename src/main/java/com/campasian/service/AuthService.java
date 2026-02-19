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
     * Registers a new user via Supabase Auth (HTTPS /auth/v1/signup).
     */
    public void signup(String fullName, String email, String universityName, String number,
                       String department, String password) throws ApiException {
        JsonObject meta = new JsonObject();
        meta.addProperty("full_name", fullName);
        meta.addProperty("university_name", universityName);
        meta.addProperty("number", number);
        meta.addProperty("department", department);
        apiService.signUp(email, password, meta);
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
