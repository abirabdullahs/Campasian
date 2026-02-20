package com.campasian.service;

import com.campasian.config.SupabaseConfig;
import com.campasian.model.Comment;
import com.campasian.model.FriendRequest;
import com.campasian.model.LostFoundItem;
import com.campasian.model.MarketplaceItem;
import com.campasian.model.Message;
import com.campasian.model.Notification;
import com.campasian.model.Post;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        TokenManager.clearTokens();
    }

    /**
     * Restores session from TokenManager. Call on app start for Remember Me.
     */
    public boolean restoreSession() {
        String at = TokenManager.getAccessToken();
        String rt = TokenManager.getRefreshToken();
        String uid = TokenManager.getUserId();
        if (at == null || at.isBlank() || uid == null || uid.isBlank()) return false;
        accessToken = at;
        refreshToken = rt != null && !rt.isBlank() ? rt : null;
        currentUserId = uid;
        return true;
    }

    /**
     * Persists current session to TokenManager (for Remember Me).
     */
    public void persistSession() {
        TokenManager.saveTokens(accessToken, refreshToken, currentUserId);
    }

    /**
     * Refreshes access token using stored refresh token. Call when access token may be expired.
     * Updates in-memory tokens and TokenManager. Returns true if refresh succeeded.
     */
    public boolean refreshAccessToken() throws ApiException {
        String rt = refreshToken != null ? refreshToken : TokenManager.getRefreshToken();
        if (rt == null || rt.isBlank()) return false;
        JsonObject payload = new JsonObject();
        payload.addProperty("refresh_token", rt);
        JsonObject root = postJson(authUrl("/token?grant_type=refresh_token"), payload);
        storeTokensIfPresent(root);
        String at = root != null ? asString(root.get("access_token")) : null;
        if (at != null && !at.isBlank()) {
            accessToken = at;
            persistSession();
            return true;
        }
        return false;
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
                        p.setBio(asString(obj.get("bio")));
                        p.setAvatarUrl(asString(obj.get("avatar_url")));
                        p.setBloodGroup(asString(obj.get("blood_group")));
                        p.setSession(asString(obj.get("session")));
                        p.setBatch(asString(obj.get("batch")));
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
    /**
     * Inserts profile into public.profiles. Matches base schema: id, full_name, university_name, ein_number, department.
     * (bio is omitted - add via social_extensions if that migration was run)
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

    /**
     * Creates a new post. Uses current user's id, name, and university from profile.
     * @param imageUrl optional public URL from Supabase Storage (post-images bucket)
     */
    public void sendPost(String content, String imageUrl) throws ApiException {
        String userId = currentUserId;
        if (userId == null || userId.isBlank()) {
            throw new ApiException(-1, "Not logged in", null, null, null);
        }
        UserProfile profile = getProfile(userId);
        String userName = profile != null && profile.getFullName() != null ? profile.getFullName() : "Anonymous";
        String university = profile != null && profile.getUniversityName() != null ? profile.getUniversityName() : "";

        JsonObject body = new JsonObject();
        body.addProperty("user_id", userId);
        body.addProperty("user_name", userName);
        body.addProperty("content", content != null ? content : "");
        body.addProperty("university", university);
        if (imageUrl != null && !imageUrl.isBlank()) body.addProperty("image_url", imageUrl);

        String url = restUrl("/posts");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        postJsonWithAuth(url, body, token);
    }

    /**
     * Creates a new post without image. Convenience for sendPost(content, null).
     */
    public void sendPost(String content) throws ApiException {
        sendPost(content, null);
    }

    /**
     * Fetches posts. If followingOnly is true, only posts from users the current user follows.
     */
    public List<Post> getFeed(boolean followingOnly) throws ApiException {
        if (followingOnly && (currentUserId == null || currentUserId.isBlank())) {
            return Collections.emptyList();
        }
        String url;
        if (followingOnly) {
            List<String> followingIds = getFollowingIds();
            if (followingIds.isEmpty()) return Collections.emptyList();
            url = restUrl("/posts?user_id=in.(" + String.join(",", followingIds) + ")&order=created_at.desc");
        } else {
            url = restUrl("/posts?order=created_at.desc");
        }
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        List<Post> posts = getPostsWithAuth(url, token);
        enrichPostsWithLikes(posts);
        return posts;
    }

    /**
     * Fetches all posts (global feed). Convenience wrapper for getFeed(false).
     */
    public List<Post> getAllPosts() throws ApiException {
        return getFeed(false);
    }

    private List<String> getFollowingIds() throws ApiException {
        if (currentUserId == null || currentUserId.isBlank()) return Collections.emptyList();
        String url = restUrl("/follows?follower_id=eq." + currentUserId + "&select=following_id");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(url, token);
            if (body == null || body.isBlank()) return Collections.emptyList();
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            var arr = parsed.getAsJsonArray();
            List<String> ids = new ArrayList<>();
            for (JsonElement el : arr) {
                if (el != null && el.isJsonObject()) {
                    String id = asString(el.getAsJsonObject().get("following_id"));
                    if (id != null && !id.isBlank()) ids.add(id);
                }
            }
            return ids;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Follows fetch failed: " + e.getMessage(), null, null, null);
        }
    }

    private void enrichPostsWithLikes(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return;
        List<String> postIds = new ArrayList<>();
        for (Post p : posts) {
            if (p.getId() != null) postIds.add(String.valueOf(p.getId()));
        }
        if (postIds.isEmpty()) return;
        Map<Long, Integer> countByPost = new HashMap<>();
        Map<Long, Boolean> likedByPost = new HashMap<>();
        try {
            String idsParam = String.join(",", postIds);
            String url = restUrl("/likes?post_id=in.(" + idsParam + ")&select=post_id,user_id");
            String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
            String body = getRawWithAuth(url, token);
            if (body != null && !body.isBlank()) {
                var parsed = JsonParser.parseString(body);
                if (parsed != null && parsed.isJsonArray()) {
                    for (JsonElement el : parsed.getAsJsonArray()) {
                        if (el != null && el.isJsonObject()) {
                            JsonObject o = el.getAsJsonObject();
                            Long pid = asLong(o.get("post_id"));
                            String uid = asString(o.get("user_id"));
                            if (pid != null) {
                                countByPost.merge(pid, 1, Integer::sum);
                                if (currentUserId != null && currentUserId.equals(uid)) {
                                    likedByPost.put(pid, true);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        for (Post p : posts) {
            Long pid = p.getId();
            p.setLikeCount(countByPost.getOrDefault(pid, 0));
            p.setLikedByMe(Boolean.TRUE.equals(likedByPost.get(pid)));
        }
    }

    private void enrichPostsWithCommentCount(List<Post> posts) throws ApiException {
        if (posts == null || posts.isEmpty()) return;
        List<Long> postIds = new ArrayList<>();
        for (Post p : posts) {
            if (p.getId() != null) postIds.add(p.getId());
        }
        if (postIds.isEmpty()) return;
        Map<Long, Integer> countByPost = new HashMap<>();
        try {
            String idsParam = postIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
            String url = restUrl("/comments?post_id=in.(" + idsParam + ")&select=post_id");
            String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
            String body = getRawWithAuth(url, token);
            if (body != null && !body.isBlank()) {
                var parsed = JsonParser.parseString(body);
                if (parsed != null && parsed.isJsonArray()) {
                    for (JsonElement el : parsed.getAsJsonArray()) {
                        if (el != null && el.isJsonObject()) {
                            Long pid = asLong(el.getAsJsonObject().get("post_id"));
                            if (pid != null) countByPost.merge(pid, 1, Integer::sum);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        for (Post p : posts) {
            p.setCommentCount(countByPost.getOrDefault(p.getId(), 0));
        }
    }

    private String getRawWithAuth(String url, String bearerToken) throws ApiException {
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
            if (status >= 200 && status < 300) {
                return body;
            }
            if ("true".equalsIgnoreCase(System.getProperty("campasian.log.api"))) {
                System.err.println("[Campasian API Error] GET " + url + " -> " + status + " " + (body != null ? body : ""));
            }
            throw new ApiException(status, "HTTP " + status, null, null, body);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Request failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Toggles like on a post. If already liked, unlikes; otherwise likes.
     */
    public void toggleLike(Long postId) throws ApiException {
        if (postId == null || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        String pid = String.valueOf(postId);
        String checkUrl = restUrl("/likes?post_id=eq." + pid + "&user_id=eq." + currentUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        String body = getRawWithAuth(checkUrl, token);
        boolean alreadyLiked = false;
        if (body != null && !body.isBlank()) {
            try {
                var parsed = JsonParser.parseString(body);
                if (parsed != null && parsed.isJsonArray() && parsed.getAsJsonArray().size() > 0) {
                    alreadyLiked = true;
                }
            } catch (Exception ignored) {}
        }
        if (alreadyLiked) {
            deleteWithAuth(restUrl("/likes?post_id=eq." + pid + "&user_id=eq." + currentUserId), token);
        } else {
            JsonObject payload = new JsonObject();
            payload.addProperty("post_id", postId);
            payload.addProperty("user_id", currentUserId);
            postJsonWithAuth(restUrl("/likes"), payload, token);
        }
    }

    private void deleteWithAuth(String url, String bearerToken) throws ApiException {
        try {
            String anonKey = SupabaseConfig.getAnonKey();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + bearerToken)
                .header("Prefer", "return=minimal")
                .DELETE()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(response.statusCode(), "Delete failed", null, null, response.body());
            }
        } catch (ApiException e) { throw e; }
        catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Delete failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Adds a comment on a post.
     */
    public void addComment(Long postId, String text) throws ApiException {
        if (postId == null || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        UserProfile profile = getProfile(currentUserId);
        String userName = profile != null && profile.getFullName() != null ? profile.getFullName() : "Anonymous";
        JsonObject payload = new JsonObject();
        payload.addProperty("post_id", postId);
        payload.addProperty("user_id", currentUserId);
        payload.addProperty("user_name", userName);
        payload.addProperty("content", text != null ? text.trim() : "");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        postJsonWithAuth(restUrl("/comments"), payload, token);
    }

    /**
     * Fetches comments for a post.
     */
    public List<Comment> fetchComments(Long postId) throws ApiException {
        if (postId == null) return Collections.emptyList();
        String url = restUrl("/comments?post_id=eq." + postId + "&order=created_at.asc");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(url, token);
            if (body == null || body.isBlank()) return Collections.emptyList();
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            List<Comment> comments = new ArrayList<>();
            for (JsonElement el : parsed.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    Comment c = new Comment();
                    c.setId(asLong(o.get("id")));
                    c.setPostId(asLong(o.get("post_id")));
                    c.setUserId(asString(o.get("user_id")));
                    c.setUserName(asString(o.get("user_name")));
                    c.setContent(asString(o.get("content")));
                    c.setCreatedAt(asString(o.get("created_at")));
                    comments.add(c);
                }
            }
            return comments;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Comments fetch failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Follows a user.
     */
    public void followUser(String targetId) throws ApiException {
        if (targetId == null || targetId.isBlank() || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        if (targetId.equals(currentUserId)) return;
        JsonObject payload = new JsonObject();
        payload.addProperty("follower_id", currentUserId);
        payload.addProperty("following_id", targetId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        postJsonWithAuth(restUrl("/follows"), payload, token);
    }

    /**
     * Unfollows a user.
     */
    public void unfollowUser(String targetUserId) throws ApiException {
        if (targetUserId == null || targetUserId.isBlank() || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        String url = restUrl("/follows?follower_id=eq." + currentUserId + "&following_id=eq." + targetUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        deleteWithAuth(url, token);
    }

    /**
     * Fetches follower count for a user.
     */
    public int getFollowerCount(String userId) throws ApiException {
        if (userId == null || userId.isBlank()) return 0;
        return getCountFromTable("follows", "following_id", userId);
    }

    /**
     * Fetches following count for a user.
     */
    public int getFollowingCount(String userId) throws ApiException {
        if (userId == null || userId.isBlank()) return 0;
        return getCountFromTable("follows", "follower_id", userId);
    }

    private int getCountFromTable(String table, String column, String value) throws ApiException {
        String url = restUrl("/" + table + "?" + column + "=eq." + value + "&select=" + column);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("apikey", SupabaseConfig.getAnonKey())
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .header("Prefer", "count=exact")
                .header("Range", "0-0")
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String range = response.headers().firstValue("Content-Range").orElse("");
            if (range.contains("/")) {
                String total = range.split("/")[1].trim();
                return Integer.parseInt(total);
            }
            return 0;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return 0;
        }
    }

    /**
     * Checks if current user follows targetUserId.
     */
    public boolean isFollowing(String targetUserId) throws ApiException {
        if (targetUserId == null || targetUserId.isBlank() || currentUserId == null || currentUserId.isBlank()) return false;
        String url = restUrl("/follows?follower_id=eq." + currentUserId + "&following_id=eq." + targetUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        String body = getRawWithAuth(url, token);
        if (body == null || body.isBlank()) return false;
        try {
            var parsed = JsonParser.parseString(body);
            return parsed != null && parsed.isJsonArray() && parsed.getAsJsonArray().size() > 0;
        } catch (Exception e) { return false; }
    }

    /**
     * Fetches notifications for the current user.
     */
    public List<Notification> fetchNotifications() throws ApiException {
        if (currentUserId == null || currentUserId.isBlank()) return Collections.emptyList();
        String url = restUrl("/notifications?user_id=eq." + currentUserId + "&order=created_at.desc&select=id,user_id,type,actor_id,actor_name,post_id,created_at,read_at");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(url, token);
            if (body == null || body.isBlank()) return Collections.emptyList();
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            List<Notification> list = new ArrayList<>();
            for (JsonElement el : parsed.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    Notification n = new Notification();
                    n.setId(asString(o.get("id")));
                    n.setUserId(asString(o.get("user_id")));
                    n.setType(asString(o.get("type")));
                    n.setActorId(asString(o.get("actor_id")));
                    n.setActorName(asString(o.get("actor_name")));
                    n.setPostId(asLong(o.get("post_id")));
                    n.setCreatedAt(asString(o.get("created_at")));
                    n.setReadAt(asString(o.get("read_at")));
                    list.add(n);
                }
            }
            return list;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Notifications fetch failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Fetches all profiles (for People discovery). Requires RLS policy allowing read.
     * Table: public.profiles. Select uses base schema; bio optional (run social_extensions to add).
     */
    public List<UserProfile> getAllProfiles() throws ApiException {
        String url = restUrl("/profiles?select=id,full_name,university_name,ein_number,department,avatar_url,blood_group,session,batch");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        return getProfilesList(url, token);
    }

    /**
     * Searches profiles by name or university. Uses ilike for partial match.
     */
    public List<UserProfile> searchProfiles(String nameQuery, String universityQuery) throws ApiException {
        StringBuilder q = new StringBuilder("/profiles?select=id,full_name,university_name,ein_number,department,avatar_url,blood_group,session,batch");
        if (nameQuery != null && !nameQuery.isBlank()) {
            q.append("&full_name=ilike.*").append(encode(nameQuery)).append("*");
        }
        if (universityQuery != null && !universityQuery.isBlank()) {
            q.append("&university_name=ilike.*").append(encode(universityQuery)).append("*");
        }
        String url = restUrl(q.toString());
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        return getProfilesList(url, token);
    }

    private static String encode(String s) {
        if (s == null) return "";
        return s.replace(" ", "%20").replace("&", "%26");
    }

    private List<UserProfile> getProfilesList(String url, String bearerToken) throws ApiException {
        try {
            String body = getRawWithAuth(url, bearerToken);
            if (body == null || body.isBlank()) return Collections.emptyList();
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            List<UserProfile> list = new ArrayList<>();
            for (JsonElement el : parsed.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    UserProfile p = new UserProfile();
                    p.setId(asString(o.get("id")));
                    p.setFullName(asString(o.get("full_name")));
                    p.setUniversityName(asString(o.get("university_name")));
                    p.setEinNumber(asString(o.get("ein_number")));
                    p.setBio(asString(o.get("bio")));
                    p.setAvatarUrl(asString(o.get("avatar_url")));
                    p.setBloodGroup(asString(o.get("blood_group")));
                    p.setSession(asString(o.get("session")));
                    p.setBatch(asString(o.get("batch")));
                    p.setDepartment(asString(o.get("department")));
                    list.add(p);
                }
            }
            return list;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Profiles fetch failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Fetches profiles filtered by department (e.g. CSE, EEE, BBA).
     */
    public List<UserProfile> getProfilesByDepartment(String department) throws ApiException {
        if (department == null || department.isBlank()) return getAllProfiles();
        String url = restUrl("/profiles?department=ilike.*" + encode(department) + "*&select=id,full_name,university_name,ein_number,department,avatar_url,blood_group,session,batch");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        return getProfilesListFromUrl(url, token);
    }

    /**
     * Fetches profiles by blood group for emergency donor search.
     */
    public List<UserProfile> getProfilesByBloodGroup(String bloodGroup) throws ApiException {
        if (bloodGroup == null || bloodGroup.isBlank()) return Collections.emptyList();
        String encoded = bloodGroup.replace("+", "%2B");
        String url = restUrl("/profiles?blood_group=ilike." + encoded + "&select=id,full_name,university_name,department,session,batch,blood_group");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        return getProfilesListFromUrl(url, token);
    }

    private List<UserProfile> getProfilesListFromUrl(String url, String bearerToken) throws ApiException {
        return getProfilesList(url, bearerToken);
    }

    /**
     * Marks a notification as read.
     */
    public void markNotificationAsRead(String notificationId) throws ApiException {
        if (notificationId == null || notificationId.isBlank() || currentUserId == null || currentUserId.isBlank()) return;
        JsonObject payload = new JsonObject();
        payload.addProperty("read_at", java.time.OffsetDateTime.now().toString());
        String url = restUrl("/notifications?id=eq." + notificationId + "&user_id=eq." + currentUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        patchJsonWithAuth(url, payload, token);
    }

    /**
     * Uploads bytes to Supabase Storage and returns the public URL.
     * Bucket and path are required (e.g. avatars, avatars/userId/avatar.png).
     */
    public String uploadToStorage(String bucket, String path, byte[] data, String contentType) throws ApiException {
        if (bucket == null || path == null || data == null) {
            throw new ApiException(-1, "Invalid storage params", null, null, null);
        }
        String base = SupabaseConfig.getSupabaseUrl();
        String storageUrl = base + "/storage/v1/object/" + bucket + "/" + path.replace(" ", "%20");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        String anonKey = SupabaseConfig.getAnonKey();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(storageUrl))
                .timeout(REQUEST_TIMEOUT)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", contentType != null ? contentType : "image/png")
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return base + "/storage/v1/object/public/" + bucket + "/" + path;
            }
            throw new ApiException(response.statusCode(), "Upload failed", null, null, response.body());
        } catch (ApiException e) { throw e; }
        catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Upload failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Updates profile avatar_url after upload.
     */
    public void updateProfileAvatar(String avatarUrl) throws ApiException {
        if (currentUserId == null || currentUserId.isBlank()) return;
        JsonObject payload = new JsonObject();
        payload.addProperty("avatar_url", avatarUrl != null ? avatarUrl : "");
        String url = restUrl("/profiles?id=eq." + currentUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        patchJsonWithAuth(url, payload, token);
    }

    /**
     * Sends a friend request to target user. Creates record in friend_requests table.
     */
    public void sendFriendRequest(String targetId) throws ApiException {
        if (targetId == null || targetId.isBlank() || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        if (targetId.equals(currentUserId)) return;
        JsonObject payload = new JsonObject();
        payload.addProperty("from_id", currentUserId);
        payload.addProperty("to_id", targetId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        postJsonWithAuth(restUrl("/friend_requests"), payload, token);
    }

    /**
     * Fetches posts by a specific user.
     */
    public List<Post> getPostsByUserId(String userId) throws ApiException {
        if (userId == null || userId.isBlank()) return Collections.emptyList();
        String url = restUrl("/posts?user_id=eq." + userId + "&order=created_at.desc");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        List<Post> posts = getPostsWithAuth(url, token);
        enrichPostsWithLikes(posts);
        enrichPostsWithCommentCount(posts);
        return posts;
    }

    /**
     * Returns friend request status between current user and target: "none", "pending", or "accepted".
     */
    public String getFriendRequestStatus(String targetId) throws ApiException {
        if (targetId == null || targetId.isBlank() || currentUserId == null || currentUserId.isBlank()) return "none";
        String url = restUrl("/friend_requests?or=(and(from_id.eq." + currentUserId + ",to_id.eq." + targetId + "),and(from_id.eq." + targetId + ",to_id.eq." + currentUserId + "))&select=from_id,status");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(url, token);
            if (body == null || body.isBlank()) return "none";
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return "none";
            var arr = parsed.getAsJsonArray();
            if (arr.isEmpty()) return "none";
            for (JsonElement el : arr) {
                if (el != null && el.isJsonObject()) {
                    String status = asString(el.getAsJsonObject().get("status"));
                    if ("accepted".equalsIgnoreCase(status)) return "accepted";
                    if ("pending".equalsIgnoreCase(status)) return "pending";
                }
            }
            return "none";
        } catch (ApiException e) {
            return "none";
        }
    }

    /**
     * Fetches incoming friend requests (to_id = current user, status = pending).
     */
    public List<FriendRequest> getIncomingFriendRequests() throws ApiException {
        if (currentUserId == null || currentUserId.isBlank()) return Collections.emptyList();
        String url = restUrl("/friend_requests?to_id=eq." + currentUserId + "&status=eq.pending&order=created_at.desc&select=id,from_id,to_id,status,created_at");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(url, token);
            if (body == null || body.isBlank()) return Collections.emptyList();
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            List<FriendRequest> list = new ArrayList<>();
            for (JsonElement el : parsed.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    FriendRequest fr = new FriendRequest();
                    fr.setId(asString(o.get("id")));
                    fr.setFromId(asString(o.get("from_id")));
                    fr.setToId(asString(o.get("to_id")));
                    fr.setStatus(asString(o.get("status")));
                    fr.setCreatedAt(asString(o.get("created_at")));
                    UserProfile p = getProfile(fr.getFromId());
                    fr.setFromName(p != null && p.getFullName() != null ? p.getFullName() : "Someone");
                    list.add(fr);
                }
            }
            return list;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    /**
     * Accepts a friend request. Recipient (to_id) can update status to accepted.
     */
    public void acceptFriendRequest(String requestId) throws ApiException {
        if (requestId == null || requestId.isBlank() || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("status", "accepted");
        String url = restUrl("/friend_requests?id=eq." + requestId + "&to_id=eq." + currentUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        patchJsonWithAuth(url, payload, token);
    }

    /**
     * Returns list of accepted friends (users with status=accepted in friend_requests).
     */
    public List<UserProfile> getFriends() throws ApiException {
        if (currentUserId == null || currentUserId.isBlank()) return Collections.emptyList();
        String url = restUrl("/friend_requests?or=(from_id.eq." + currentUserId + ",to_id.eq." + currentUserId + ")&status=eq.accepted&select=from_id,to_id");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(url, token);
            if (body == null || body.isBlank()) return Collections.emptyList();
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            List<String> friendIds = new ArrayList<>();
            for (JsonElement el : parsed.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    String from = asString(o.get("from_id"));
                    String to = asString(o.get("to_id"));
                    String other = from != null && from.equals(currentUserId) ? to : from;
                    if (other != null && !other.isBlank() && !friendIds.contains(other)) friendIds.add(other);
                }
            }
            List<UserProfile> friends = new ArrayList<>();
            for (String fid : friendIds) {
                UserProfile p = getProfile(fid);
                if (p != null) friends.add(p);
            }
            return friends;
        } catch (ApiException e) { throw e; }
        catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    /**
     * Fetches messages between current user and partner (either direction).
     */
    public List<Message> getMessages(String partnerId) throws ApiException {
        if (currentUserId == null || currentUserId.isBlank() || partnerId == null || partnerId.isBlank()) return Collections.emptyList();
        String url = restUrl("/messages?or=(and(sender_id.eq." + currentUserId + ",receiver_id.eq." + partnerId + "),and(sender_id.eq." + partnerId + ",receiver_id.eq." + currentUserId + "))&order=created_at.asc");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(url, token);
            if (body == null || body.isBlank()) return Collections.emptyList();
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            List<Message> list = new ArrayList<>();
            for (JsonElement el : parsed.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    Message m = new Message();
                    m.setId(asString(o.get("id")));
                    m.setSenderId(asString(o.get("sender_id")));
                    m.setReceiverId(asString(o.get("receiver_id")));
                    m.setContent(asString(o.get("content")));
                    m.setCreatedAt(asString(o.get("created_at")));
                    list.add(m);
                }
            }
            return list;
        } catch (ApiException e) { throw e; }
        catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    /**
     * Sends a message to another user.
     */
    public void sendMessage(String receiverId, String content) throws ApiException {
        if (receiverId == null || receiverId.isBlank() || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("sender_id", currentUserId);
        payload.addProperty("receiver_id", receiverId);
        payload.addProperty("content", content != null ? content : "");
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        postJsonWithAuth(restUrl("/messages"), payload, token);
    }

    /**
     * Rejects a friend request (deletes the pending record).
     */
    public void rejectFriendRequest(String requestId) throws ApiException {
        if (requestId == null || requestId.isBlank() || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        String url = restUrl("/friend_requests?id=eq." + requestId + "&to_id=eq." + currentUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        deleteWithAuth(url, token);
    }

    /**
     * Fetches marketplace items. category: Books, Electronics, Stationery, or null for all.
     */
    public List<MarketplaceItem> getMarketplaceItems(String category) throws ApiException {
        StringBuilder url = new StringBuilder("/marketplace_items?order=created_at.desc");
        if (category != null && !category.isBlank()) url.append("&category=eq.").append(encode(category));
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(restUrl(url.toString()), token);
            return parseMarketplaceItems(body);
        } catch (ApiException e) { throw e; }
        catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public void createMarketplaceItem(String title, String description, String price, String condition, String category) throws ApiException {
        if (currentUserId == null || currentUserId.isBlank()) throw new ApiException(-1, "Not logged in", null, null, null);
        UserProfile p = getProfile(currentUserId);
        String userName = p != null && p.getFullName() != null ? p.getFullName() : "Anonymous";
        JsonObject payload = new JsonObject();
        payload.addProperty("user_id", currentUserId);
        payload.addProperty("user_name", userName);
        payload.addProperty("title", title != null ? title : "");
        payload.addProperty("description", description != null ? description : "");
        payload.addProperty("price", price != null ? price : "");
        payload.addProperty("condition", condition != null ? condition : "");
        payload.addProperty("category", category != null ? category : "");
        postJsonWithAuth(restUrl("/marketplace_items"), payload, accessToken != null ? accessToken : SupabaseConfig.getAnonKey());
    }

    private List<MarketplaceItem> parseMarketplaceItems(String body) {
        if (body == null || body.isBlank()) return Collections.emptyList();
        try {
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            List<MarketplaceItem> list = new ArrayList<>();
            for (JsonElement el : parsed.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    MarketplaceItem m = new MarketplaceItem();
                    m.setId(asString(o.get("id")));
                    m.setUserId(asString(o.get("user_id")));
                    m.setUserName(asString(o.get("user_name")));
                    m.setTitle(asString(o.get("title")));
                    m.setDescription(asString(o.get("description")));
                    m.setPrice(asString(o.get("price")));
                    m.setCondition(asString(o.get("condition")));
                    m.setCategory(asString(o.get("category")));
                    m.setCreatedAt(asString(o.get("created_at")));
                    list.add(m);
                }
            }
            return list;
        } catch (Exception e) { return Collections.emptyList(); }
    }

    /**
     * Fetches lost & found items. type: "lost", "found", or null for all.
     */
    public List<LostFoundItem> getLostFoundItems(String type) throws ApiException {
        StringBuilder url = new StringBuilder("/lost_found?order=created_at.desc");
        if (type != null && !type.isBlank()) url.append("&type=eq.").append(type.toLowerCase());
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        try {
            String body = getRawWithAuth(restUrl(url.toString()), token);
            return parseLostFoundItems(body);
        } catch (ApiException e) { throw e; }
        catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public void createLostFoundItem(String type, String title, String description, String location) throws ApiException {
        if (currentUserId == null || currentUserId.isBlank()) throw new ApiException(-1, "Not logged in", null, null, null);
        UserProfile p = getProfile(currentUserId);
        String userName = p != null && p.getFullName() != null ? p.getFullName() : "Anonymous";
        JsonObject payload = new JsonObject();
        payload.addProperty("user_id", currentUserId);
        payload.addProperty("user_name", userName);
        payload.addProperty("type", "lost".equalsIgnoreCase(type) ? "lost" : "found");
        payload.addProperty("title", title != null ? title : "");
        payload.addProperty("description", description != null ? description : "");
        payload.addProperty("location", location != null ? location : "");
        postJsonWithAuth(restUrl("/lost_found"), payload, accessToken != null ? accessToken : SupabaseConfig.getAnonKey());
    }

    private List<LostFoundItem> parseLostFoundItems(String body) {
        if (body == null || body.isBlank()) return Collections.emptyList();
        try {
            var parsed = JsonParser.parseString(body);
            if (parsed == null || !parsed.isJsonArray()) return Collections.emptyList();
            List<LostFoundItem> list = new ArrayList<>();
            for (JsonElement el : parsed.getAsJsonArray()) {
                if (el != null && el.isJsonObject()) {
                    JsonObject o = el.getAsJsonObject();
                    LostFoundItem l = new LostFoundItem();
                    l.setId(asString(o.get("id")));
                    l.setUserId(asString(o.get("user_id")));
                    l.setUserName(asString(o.get("user_name")));
                    l.setType(asString(o.get("type")));
                    l.setTitle(asString(o.get("title")));
                    l.setDescription(asString(o.get("description")));
                    l.setLocation(asString(o.get("location")));
                    l.setCreatedAt(asString(o.get("created_at")));
                    list.add(l);
                }
            }
            return list;
        } catch (Exception e) { return Collections.emptyList(); }
    }

    /**
     * Updates current user's profile. Pass only fields to update.
     */
    public void updateProfile(String fullName, String universityName, String bio) throws ApiException {
        updateProfile(fullName, universityName, bio, null, null, null);
    }

    /**
     * Updates current user's profile with extended campus fields.
     */
    public void updateProfile(String fullName, String universityName, String bio,
                             String bloodGroup, String session, String batch) throws ApiException {
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Not logged in", null, null, null);
        }
        JsonObject payload = new JsonObject();
        if (fullName != null) payload.addProperty("full_name", fullName);
        if (universityName != null) payload.addProperty("university_name", universityName);
        if (bio != null) payload.addProperty("bio", bio);
        if (bloodGroup != null) payload.addProperty("blood_group", bloodGroup);
        if (session != null) payload.addProperty("session", session);
        if (batch != null) payload.addProperty("batch", batch);
        if (payload.size() == 0) return;
        String url = restUrl("/profiles?id=eq." + currentUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        patchJsonWithAuth(url, payload, token);
    }

    private void patchJsonWithAuth(String url, JsonObject payload, String bearerToken) throws ApiException {
        try {
            String anonKey = SupabaseConfig.getAnonKey();
            String body = gson.toJson(payload);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Prefer", "return=minimal")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(response.statusCode(), "Update failed", null, null, response.body());
            }
        } catch (ApiException e) { throw e; }
        catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Update failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Deletes a post. Only for posts owned by current user.
     */
    public void deletePost(Long postId) throws ApiException {
        if (postId == null || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        String url = restUrl("/posts?id=eq." + postId + "&user_id=eq." + currentUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        deleteWithAuth(url, token);
    }

    /**
     * Updates a post's content. Only for posts owned by current user.
     */
    public void updatePost(Long postId, String content) throws ApiException {
        if (postId == null || currentUserId == null || currentUserId.isBlank()) {
            throw new ApiException(-1, "Invalid request", null, null, null);
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("content", content != null ? content : "");
        String url = restUrl("/posts?id=eq." + postId + "&user_id=eq." + currentUserId);
        String token = accessToken != null && !accessToken.isBlank() ? accessToken : SupabaseConfig.getAnonKey();
        patchJsonWithAuth(url, payload, token);
    }

    private List<Post> getPostsWithAuth(String url, String bearerToken) throws ApiException {
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
                    List<Post> posts = new ArrayList<>();
                    for (JsonElement el : arr) {
                        if (el != null && el.isJsonObject()) {
                            JsonObject obj = el.getAsJsonObject();
                            Post p = new Post();
                            p.setId(asLong(obj.get("id")));
                            p.setUserId(asString(obj.get("user_id")));
                            p.setUserName(asString(obj.get("user_name")));
                            p.setContent(asString(obj.get("content")));
                            p.setUniversity(asString(obj.get("university")));
                            p.setCreatedAt(asString(obj.get("created_at")));
                            p.setImageUrl(asString(obj.get("image_url")));
                            posts.add(p);
                        }
                    }
                    return posts;
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new ApiException(-1, "Posts fetch failed: " + e.getMessage(), null, null, null);
        }
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
        logApiError("POST", url, body, status, responseBody);
        throw new ApiException(status, message, null, message, responseBody);
    }

    /**
     * Logs Supabase API errors for debugging. Enable via system property: -Dcampasian.log.api=true
     */
    private static void logApiError(String method, String url, String requestBody, int status, String responseBody) {
        if (!"true".equalsIgnoreCase(System.getProperty("campasian.log.api"))) return;
        System.err.println("[Campasian API Error] " + method + " " + url);
        System.err.println("  Status: " + status);
        if (requestBody != null && !requestBody.isBlank()) {
            System.err.println("  Request: " + requestBody);
        }
        if (responseBody != null && !responseBody.isBlank()) {
            System.err.println("  Response: " + responseBody);
        }
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

    private static Long asLong(JsonElement element) {
        if (element == null || element.isJsonNull()) return null;
        try {
            if (element.isJsonPrimitive()) {
                var prim = element.getAsJsonPrimitive();
                if (prim.isNumber()) return prim.getAsLong();
                if (prim.isString()) {
                    String s = prim.getAsString();
                    return s == null || s.isBlank() ? null : Long.parseLong(s);
                }
            }
            return null;
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
