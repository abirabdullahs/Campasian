package com.campasian.config;

/**
 * Supabase configuration loaded from environment variables or JVM system properties.
 *
 * Required:
 * - SUPABASE_URL (e.g. https://xyzcompany.supabase.co)
 * - SUPABASE_ANON_KEY (public anon key JWT)
 */
public final class SupabaseConfig {

    private SupabaseConfig() {}

    public static String getSupabaseUrl() {
        String url = readRequired("SUPABASE_URL");
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String getAnonKey() {
        return readRequired("SUPABASE_ANON_KEY");
    }

    private static String readRequired(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Missing required configuration: " + key
                    + ". Set it as an environment variable or JVM property (-D" + key + "=...)."
            );
        }
        return value.trim();
    }
}
