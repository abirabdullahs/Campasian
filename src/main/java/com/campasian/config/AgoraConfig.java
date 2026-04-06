package com.campasian.config;

/**
 * Agora browser call configuration.
 */
public final class AgoraConfig {

    private AgoraConfig() {
    }

    private static final String DEFAULT_APP_ID = "e03adf7ac7b6454182c647b146183cd2";

    public static String getAppId() {
        String value = System.getProperty("AGORA_APP_ID");
        if (value == null || value.isBlank()) {
            value = System.getenv("AGORA_APP_ID");
        }
        return value != null && !value.isBlank() ? value.trim() : DEFAULT_APP_ID;
    }
}
