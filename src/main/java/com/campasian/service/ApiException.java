package com.campasian.service;

/**
 * Exception thrown for Supabase REST/API failures (HTTP error or network failure).
 */
public final class ApiException extends Exception {

    private final int statusCode;
    private final String error;
    private final String errorDescription;
    private final String responseBody;

    public ApiException(int statusCode, String message, String error, String errorDescription, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.error = error;
        this.errorDescription = errorDescription;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public boolean isInvalidCredentials() {
        if (statusCode != 400) return false;
        if (equalsIgnoreCase(error, "invalid_grant")) return true;
        String desc = errorDescription != null ? errorDescription : getMessage();
        return containsIgnoreCase(desc, "invalid login credentials");
    }

    public boolean isUserAlreadyRegistered() {
        if (statusCode != 400) return false;
        if (equalsIgnoreCase(error, "user_already_exists")) return true;
        String desc = errorDescription != null ? errorDescription : getMessage();
        return containsIgnoreCase(desc, "already registered") || containsIgnoreCase(desc, "user already registered");
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) return false;
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }
}
