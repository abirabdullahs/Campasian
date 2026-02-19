package com.campasian.service;

import com.campasian.config.SupabaseConfig;
import com.campasian.model.User;
import com.campasian.model.UserProfile;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Minimal Supabase REST client for authentication.
 * Uses Supabase Auth endpoints over HTTPS (port 443) to avoid direct DB (5432) connectivity.
 */
public final class ApiService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private static final ApiService INSTANCE = new ApiService();

    private ApiService() {}
    private final Gson gson = new Gson();

    private volatile String accessToken;
    private volatile String refreshToken;
    private volatile String currentUserId;

    public static ApiService getInstance() {
        return INSTANCE;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Clears session (tokens and userId). Call on logout.
     */
    public void clearSession() {
        accessToken = null;
        refreshToken = null;
        currentUserId = null;
    }

    /**
     * Fetches profile from /rest/v1/profiles?id=eq.{userId}.
     * Returns null if not found or error.
     */
    public UserProfile getProfile(String userId) throws ApiException {
        if (userId == null || userId.isBlank()) return null;
        String url = restUrl("/profiles?id=eq." + userId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        return getJsonWithAuth(url, token);
    }

    private UserProfile getJsonWithAuth(String url, String bearerToken) throws ApiException {
        try {
            String anonKey = SupabaseConfig.getAnonKey();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + bearerToken)
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            String body = response.body();

            if (status >= 200 && status < 300 && body != null && !body.isBlank()) {
                var parsed = JsonParser.parseString(body);
                if (parsed != null && parsed.isJsonArray()) {
                    var arr = parsed.getAsJsonArray();
                    if (arr.size() > 0) {
                        JsonObject obj = arr.get(0).getAsJsonObject();
                        UserProfile p = new UserProfile();
                        p.setId(asString(obj.get("id")));
                        p.setFullName(asString(obj.get("full_name")));
                        p.setUniversityName(asString(obj.get("university_name")));
                        p.setEinNumber(asString(obj.get("ein_number")));
                        return p;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Profile fetch failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Signs up a user via Supabase Auth. Also sends user_metadata for backup.
     * Returns the full response including the created user (with id).
     */
    public JsonObject signUp(String email, String password, JsonObject userMetadata) throws ApiException {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);
        if (userMetadata != null && !userMetadata.isEmpty()) {
            payload.add("data", userMetadata);
        }

        JsonObject root = postJson(authUrl("/signup"), payload);
        storeTokensIfPresent(root);
        storeCurrentUserId(root);
        return root;
    }

    /**
     * Inserts profile data into the public.profiles table via Supabase REST.
     * Column names must match the table schema: id, full_name, university_name, ein_number, department.
     */
    public void createProfile(String userId, String fullName, String universityName, String einNumber,
                              String department) throws ApiException {
        JsonObject body = new JsonObject();
        body.addProperty("id", userId);
        body.addProperty("full_name", fullName != null ? fullName : "");
        body.addProperty("university_name", universityName != null ? universityName : "");
        body.addProperty("ein_number", einNumber != null ? einNumber : "");
        body.addProperty("department", department != null ? department : "");

        String url = restUrl("/profiles");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        postJsonWithAuth(url, body, token);
    }

    private static String restUrl(String path) throws ApiException {
        String base;
        try {
            base = SupabaseConfig.getSupabaseUrl();
        } catch (IllegalStateException e) {
            throw new ApiException(-1, e.getMessage(), null, e.getMessage(), null);
        }
        return base + "/rest/v1" + (path.startsWith("/") ? path : "/" + path);
    }

    private void postJsonWithAuth(String url, JsonObject payload, String bearerToken) throws ApiException {
        String anonKey;
        try {
            anonKey = SupabaseConfig.getAnonKey();
        } catch (IllegalStateException e) {
            throw new ApiException(-1, e.getMessage(), null, e.getMessage(), null);
        }

        String body = gson.toJson(payload);
        var builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(REQUEST_TIMEOUT)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer " + bearerToken)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Prefer", "return=minimal")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        HttpRequest request = builder.build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ApiException(-1, "Network/API request failed: " + e.getMessage(), null, e.getMessage(), null);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) return;

        String responseBody = response.body();
        String message = "HTTP " + status;
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonElement parsed = JsonParser.parseString(responseBody);
                JsonObject obj = parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
                if (obj != null) {
                    String msg = asString(obj.get("message"));
                    if (msg != null && !msg.isBlank()) message = msg;
                }
            } catch (Exception ignored) {}
        }
        throw new ApiException(status, message, null, message, responseBody);
    }

    public User login(String email, String password) throws ApiException {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", password);

        JsonObject root = postJson(authUrl("/token?grant_type=password"), payload);
        storeTokensIfPresent(root);
        storeCurrentUserId(root);

        JsonObject userJson = asObject(root.get("user"));
        return toUser(userJson);
    }

    private void storeCurrentUserId(JsonObject root) {
        if (root == null) return;
        JsonObject userJson = asObject(root.get("user"));
        if (userJson != null && userJson.has("id")) {
            currentUserId = userJson.get("id").getAsString();
        }
    }

    private JsonObject postJson(String url, JsonObject payload) throws ApiException {
        String anonKey;
        try {
            anonKey = SupabaseConfig.getAnonKey();
        } catch (IllegalStateException e) {
            throw new ApiException(-1, e.getMessage(), null, e.getMessage(), null);
        }

        String body = gson.toJson(payload);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(REQUEST_TIMEOUT)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer " + anonKey)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ApiException(-1, "Network/API request failed: " + e.getMessage(), null, e.getMessage(), null);
        }

        int status = response.statusCode();
        String responseBody = response.body();
        if (status >= 200 && status < 300) {
            if (responseBody == null || responseBody.isBlank()) {
                return new JsonObject();
            }
            JsonElement parsed = JsonParser.parseString(responseBody);
            return parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
        }

        String error = null;
        String errorDescription = null;
        String message = "HTTP " + status;

        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonElement parsed = JsonParser.parseString(responseBody);
                JsonObject obj = parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
                if (obj != null) {
                    error = asString(obj.get("error"));
                    errorDescription = firstNonBlank(
                        asString(obj.get("error_description")),
                        asString(obj.get("message")),
                        asString(obj.get("msg")),
                        asString(obj.get("error"))
                    );
                    if (errorDescription != null && !errorDescription.isBlank()) {
                        message = errorDescription;
                    }
                }
            } catch (Exception ignored) {
                // Non-JSON error body; fall back to HTTP status.
            }
        }

        throw new ApiException(status, message, error, errorDescription, responseBody);
    }

    private static String authUrl(String pathAndQuery) throws ApiException {
        String base;
        try {
            base = SupabaseConfig.getSupabaseUrl();
        } catch (IllegalStateException e) {
            throw new ApiException(-1, e.getMessage(), null, e.getMessage(), null);
        }
        if (pathAndQuery == null || pathAndQuery.isBlank()) {
            pathAndQuery = "/";
        }
        if (!pathAndQuery.startsWith("/")) {
            pathAndQuery = "/" + pathAndQuery;
        }
        return base + "/auth/v1" + pathAndQuery;
    }

    private void storeTokensIfPresent(JsonObject root) {
        if (root == null) return;
        String at = asString(root.get("access_token"));
        String rt = asString(root.get("refresh_token"));
        if (at != null && !at.isBlank()) accessToken = at;
        if (rt != null && !rt.isBlank()) refreshToken = rt;
    }

    private static User toUser(JsonObject userJson) {
        if (userJson == null) return null;

        User user = new User();
        user.setEmail(asString(userJson.get("email")));

        JsonObject meta = asObject(userJson.get("user_metadata"));
        if (meta != null) {
            user.setFullName(asString(meta.get("full_name")));
            user.setEinNumber(firstNonBlank(asString(meta.get("ein_number")), asString(meta.get("number"))));
            user.setUniversityName(asString(meta.get("university_name")));
            user.setDepartment(asString(meta.get("department")));
        }
        return user;
    }

    private static String asString(JsonElement element) {
        if (element == null || element.isJsonNull()) return null;
        try {
            return element.getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static JsonObject asObject(JsonElement element) {
        if (element == null || element.isJsonNull()) return null;
        return element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
