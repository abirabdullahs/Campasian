package com.campasian.config;

/**
 * Supabase configuration for Campasian.
 * Uses direct values; override via SUPABASE_URL and SUPABASE_ANON_KEY if needed.
 */
public final class SupabaseConfig {

    private SupabaseConfig() {}

    private static final String DEFAULT_URL = "https://ixcmrdmkdlqtnhefjnsx.supabase.co";
    private static final String DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml4Y21yZG1rZGxxdG5oZWZqbnN4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE0Njg5NDMsImV4cCI6MjA4NzA0NDk0M30.k0COnTWJiUd47P4V5hG77m4LBAFKbQsKHrz4hf8nvg4";

    public static String getSupabaseUrl() {
        String url = override("SUPABASE_URL", DEFAULT_URL);
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return url;
    }

    public static String getAnonKey() {
        return override("SUPABASE_ANON_KEY", DEFAULT_ANON_KEY);
    }

    private static String override(String envKey, String defaultValue) {
        String v = System.getProperty(envKey);
        if (v == null || v.isBlank()) v = System.getenv(envKey);
        return (v != null && !v.isBlank()) ? v.trim() : defaultValue;
    }
}
