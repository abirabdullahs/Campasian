package com.campasian.service;

import com.campasian.model.User;
import com.google.gson.JsonObject;

/**
 * Handles authentication: signup, login, and EIN-to-university mapping.
 */
public final class AuthService {

    private static final AuthService INSTANCE = new AuthService();
    private final ApiService apiService = ApiService.getInstance();

    private AuthService() {}

    public static AuthService getInstance() {
        return INSTANCE;
    }

    /**
     * Maps EIN prefix to university name. Extend as needed for real EIN schemes.
     */
    public String resolveUniversityByEin(String ein) {
        if (ein == null || ein.isBlank()) return "";
        String prefix = ein.trim().substring(0, Math.min(3, ein.trim().length()));
        return switch (prefix) {
            case "101" -> "AIUB";
            case "102" -> "BUET";
            case "103" -> "DU";
            case "104" -> "NSU";
            case "105" -> "BRAC";
            case "106" -> "EWU";
            case "107" -> "IUB";
            case "108" -> "SUB";
            default -> "Unknown University";
        };
    }

    /**
     * Registers a new user via Supabase Auth (HTTPS /auth/v1/signup).
     */
    public void signup(String fullName, String email, String einNumber, String department,
                       String password) throws ApiException {
        String universityName = resolveUniversityByEin(einNumber);

        JsonObject meta = new JsonObject();
        meta.addProperty("full_name", fullName);
        meta.addProperty("ein_number", einNumber);
        meta.addProperty("university_name", universityName);
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
            if (e.isInvalidCredentials()) {
                return null;
            }
            throw e;
        }
    }
}
