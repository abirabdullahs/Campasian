package com.campasian.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads and filters universities from database/university.json.
 */
public final class UniversityService {

    private static final String RESOURCE = "/database/university.json";
    private static List<String> universities;

    public static List<String> loadUniversities() {
        if (universities != null) return universities;
        try (var reader = new InputStreamReader(
                UniversityService.class.getResourceAsStream(RESOURCE), StandardCharsets.UTF_8)) {
            JsonArray arr = new Gson().fromJson(reader, JsonArray.class);
            List<String> list = new ArrayList<>();
            for (var e : arr) {
                if (e != null && e.isJsonPrimitive()) {
                    String s = e.getAsString();
                    if (s != null && !s.isBlank()) list.add(s.trim());
                }
            }
            universities = Collections.unmodifiableList(list);
            return universities;
        } catch (IOException | NullPointerException e) {
            universities = List.of("AIUB", "NSU", "BRAC", "DU", "BUET");
            return universities;
        }
    }

    public static List<String> search(String query) {
        List<String> all = loadUniversities();
        if (query == null || query.isBlank()) return all;
        String lower = query.toLowerCase().trim();
        return all.stream()
            .filter(u -> u.toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }
}
