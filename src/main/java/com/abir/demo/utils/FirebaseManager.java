package com.abir.demo.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Utility class to interact with Firebase Realtime Database via REST API
 */
public class FirebaseManager {
    
    // Firebase configuration
    private static final String DATABASE_URL = "https://campasian.firebaseio.com";
    private static final String API_KEY = "AIzaSyDtNDvHczMn1nkUEHorrGMcZbUxAAiKbbE";
    
    private static Gson gson = new Gson();

    /**
     * Create a new user in database
     */
    public static boolean createUser(String userId, String email, String fullName, String university) {
        try {
            String url = DATABASE_URL + "/users/" + userId + ".json?auth=" + API_KEY;
            
            JsonObject userData = new JsonObject();
            userData.addProperty("email", email);
            userData.addProperty("fullName", fullName);
            userData.addProperty("university", university);
            userData.addProperty("createdAt", System.currentTimeMillis());
            userData.addProperty("profileImage", "");
            userData.addProperty("bio", "");
            userData.addProperty("followers", 0);
            userData.addProperty("following", 0);
            
            return putRequest(url, userData.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get user data from database
     */
    public static JsonObject getUser(String userId) {
        try {
            String url = DATABASE_URL + "/users/" + userId + ".json?auth=" + API_KEY;
            return getRequest(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a post/feed item
     */
    public static boolean createPost(String userId, String content, String imageUrl) {
        try {
            String postId = System.currentTimeMillis() + "_" + userId;
            String url = DATABASE_URL + "/posts/" + postId + ".json?auth=" + API_KEY;
            
            JsonObject postData = new JsonObject();
            postData.addProperty("userId", userId);
            postData.addProperty("content", content);
            postData.addProperty("imageUrl", imageUrl);
            postData.addProperty("timestamp", System.currentTimeMillis());
            postData.addProperty("likes", 0);
            postData.addProperty("comments", 0);
            
            return putRequest(url, postData.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all posts
     */
    public static String getAllPosts() {
        try {
            String url = DATABASE_URL + "/posts.json?auth=" + API_KEY;
            JsonObject response = getRequest(url);
            return response != null ? response.toString() : "{}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * Create an event
     */
    public static boolean createEvent(String eventName, String description, String dateTime, String location) {
        try {
            String eventId = System.currentTimeMillis() + "";
            String url = DATABASE_URL + "/events/" + eventId + ".json?auth=" + API_KEY;
            
            JsonObject eventData = new JsonObject();
            eventData.addProperty("name", eventName);
            eventData.addProperty("description", description);
            eventData.addProperty("dateTime", dateTime);
            eventData.addProperty("location", location);
            eventData.addProperty("createdAt", System.currentTimeMillis());
            eventData.addProperty("attendees", 0);
            
            return putRequest(url, eventData.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all events
     */
    public static String getAllEvents() {
        try {
            String url = DATABASE_URL + "/events.json?auth=" + API_KEY;
            JsonObject response = getRequest(url);
            return response != null ? response.toString() : "{}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * Create a club/group
     */
    public static boolean createClub(String clubName, String description, String category) {
        try {
            String clubId = System.currentTimeMillis() + "";
            String url = DATABASE_URL + "/clubs/" + clubId + ".json?auth=" + API_KEY;
            
            JsonObject clubData = new JsonObject();
            clubData.addProperty("name", clubName);
            clubData.addProperty("description", description);
            clubData.addProperty("category", category);
            clubData.addProperty("createdAt", System.currentTimeMillis());
            clubData.addProperty("members", 0);
            clubData.addProperty("image", "");
            
            return putRequest(url, clubData.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all clubs
     */
    public static String getAllClubs() {
        try {
            String url = DATABASE_URL + "/clubs.json?auth=" + API_KEY;
            JsonObject response = getRequest(url);
            return response != null ? response.toString() : "{}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * Send a message
     */
    public static boolean sendMessage(String fromUserId, String toUserId, String message) {
        try {
            String messageId = System.currentTimeMillis() + "";
            String url = DATABASE_URL + "/messages/" + fromUserId + "_" + toUserId + "/" + messageId + ".json?auth=" + API_KEY;
            
            JsonObject messageData = new JsonObject();
            messageData.addProperty("from", fromUserId);
            messageData.addProperty("to", toUserId);
            messageData.addProperty("text", message);
            messageData.addProperty("timestamp", System.currentTimeMillis());
            messageData.addProperty("read", false);
            
            return putRequest(url, messageData.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method for PUT requests
     */
    private static boolean putRequest(String urlString, String data) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = data.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        return responseCode >= 200 && responseCode < 300;
    }

    /**
     * Helper method for GET requests
     */
    private static JsonObject getRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        Scanner scanner = new Scanner(connection.getInputStream()).useDelimiter("\\A");
        String result = scanner.hasNext() ? scanner.next() : "";
        
        if (result.isEmpty()) {
            return new JsonObject();
        }
        
        return gson.fromJson(result, JsonObject.class);
    }
}
