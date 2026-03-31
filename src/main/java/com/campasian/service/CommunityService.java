package com.campasian.service;

import com.campasian.model.CommunityMessage;
import com.campasian.model.CommunityRoom;
import com.campasian.model.UserProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Supabase-backed community room service.
 */
public final class CommunityService {

    private static final CommunityService INSTANCE = new CommunityService();

    private CommunityService() {}

    public static CommunityService getInstance() {
        return INSTANCE;
    }

    public void ensureAutoJoin(String userKey, String email, String universityName,
                               String department, String universityId, String displayName) throws ApiException {
        String universityKey = normalizeUniversity(universityName);
        if (universityKey.isBlank()) return;

        upsertBuiltInRooms(universityName, department, universityKey, 1, 1);
    }

    public List<CommunityRoom> buildCommunities(UserProfile currentUser, List<UserProfile> allProfiles) throws ApiException {
        if (currentUser == null) return List.of();

        String universityName = safeName(currentUser.getUniversityName());
        String universityKey = normalizeUniversity(universityName);
        if (universityKey.isBlank()) return List.of();

        List<UserProfile> sameUniversity = allProfiles == null ? List.of() : allProfiles.stream()
            .filter(profile -> isSameUniversity(currentUser, profile))
            .sorted(Comparator.comparing(profile -> safeName(profile.getFullName())))
            .toList();

        String department = safeName(currentUser.getDepartment());
        int universityCount = Math.max(sameUniversity.size(), 1);
        int departmentCount = Math.max((int) sameUniversity.stream()
            .filter(profile -> safeName(profile.getDepartment()).equalsIgnoreCase(department))
            .count(), 1);

        upsertBuiltInRooms(universityName, department, universityKey, universityCount, departmentCount);

        List<CommunityRoom> rooms = new ArrayList<>(ApiService.getInstance().getCommunityRooms(universityKey));
        rooms.sort(Comparator
            .comparingInt(this::roomPriority)
            .thenComparing(CommunityRoom::getName, String.CASE_INSENSITIVE_ORDER));
        return rooms;
    }

    public CommunityRoom createCustomRoom(String currentUserId, String universityName, String roomName,
                                          String description, int memberCount) throws ApiException {
        String universityKey = normalizeUniversity(universityName);
        String normalizedName = normalizeDepartment(roomName);
        String id = roomId(universityKey, "custom-" + normalizedName + "-" + System.currentTimeMillis());
        CommunityRoom room = new CommunityRoom(
            id,
            safeName(roomName, "Custom Community"),
            safeName(description, "Student-led custom community."),
            "Custom",
            Math.max(memberCount, 1),
            true,
            false,
            true,
            currentUserId,
            universityKey
        );
        ApiService.getInstance().upsertCommunityRoom(room);
        List<CommunityMessage> messages = ApiService.getInstance().getCommunityMessages(id);
        if (messages.isEmpty()) {
            ApiService.getInstance().sendCommunityMessage(id, currentUserId, "Community Creator", "Custom community created. Start the discussion here.");
        }
        return room;
    }

    public CommunityRoom updateCustomRoom(String roomId, String currentUserId, String roomName, String description) throws ApiException {
        CommunityRoom existing = ApiService.getInstance().getCommunityRoom(roomId);
        if (existing == null || !canManage(existing, currentUserId)) return existing;
        CommunityRoom updated = new CommunityRoom(
            existing.getId(),
            safeName(roomName, existing.getName()),
            safeName(description, existing.getDescription()),
            existing.getScopeLabel(),
            existing.getMemberCount(),
            existing.isVerified(),
            existing.isAutoJoined(),
            existing.isCustom(),
            existing.getOwnerUserId(),
            existing.getUniversityKey()
        );
        ApiService.getInstance().upsertCommunityRoom(updated);
        return updated;
    }

    public boolean deleteCustomRoom(String roomId, String currentUserId) throws ApiException {
        if (roomId == null || roomId.isBlank()) return false;
        ApiService.getInstance().deleteCommunityRoom(roomId);
        return true;
    }

    public boolean canManage(CommunityRoom room, String currentUserId) {
        return room != null && room.isCustom() && currentUserId != null && currentUserId.equals(room.getOwnerUserId());
    }

    public List<CommunityMessage> getMessages(String roomId) throws ApiException {
        return ApiService.getInstance().getCommunityMessages(roomId);
    }

    public CommunityMessage sendMessage(String roomId, String senderId, String senderName, String content) throws ApiException {
        return ApiService.getInstance().sendCommunityMessage(roomId, senderId, senderName, content);
    }

    private void upsertBuiltInRooms(String universityName, String department, String universityKey,
                                    int universityCount, int departmentCount) throws ApiException {
        ApiService api = ApiService.getInstance();

        api.upsertCommunityRoom(new CommunityRoom(
            roomId(universityKey, "general"),
            safeName(universityName) + " Community",
            "Messenger-style university room for announcements, study help, and cross-department discussion.",
            "University Hub",
            universityCount,
            true,
            true,
            false,
            null,
            universityKey
        ));
        ensureSeedMessage(roomId(universityKey, "general"), "Campus Community Bot",
            "Welcome to the " + safeName(universityName) + " university hub. Students can chat here by default.");

        api.upsertCommunityRoom(new CommunityRoom(
            roomId(universityKey, "freshers"),
            "Freshers Help Desk",
            "Structured support room for onboarding, campus logistics, and student Q&A.",
            "Support",
            universityCount,
            true,
            false,
            false,
            null,
            universityKey
        ));
        ensureSeedMessage(roomId(universityKey, "freshers"), "Campus Mentor",
            "Use this room for orientation questions, class routines, and first-week help.");

        String cleanedDepartment = normalizeDepartment(department);
        if (!cleanedDepartment.isBlank()) {
            api.upsertCommunityRoom(new CommunityRoom(
                roomId(universityKey, cleanedDepartment),
                safeName(department) + " Lounge",
                "Verified room for " + safeName(department) + " students from " + safeName(universityName) + ".",
                "Department",
                departmentCount,
                true,
                false,
                false,
                null,
                universityKey
            ));
            ensureSeedMessage(roomId(universityKey, cleanedDepartment), "Department Coordinator",
                "Department space ready for " + safeName(department) + " students.");
        }
    }

    private void ensureSeedMessage(String roomId, String senderName, String content) throws ApiException {
        if (ApiService.getInstance().getCommunityMessages(roomId).isEmpty()) {
            ApiService.getInstance().sendCommunityMessage(roomId, "system", senderName, content);
        }
    }

    private int roomPriority(CommunityRoom room) {
        if (room == null) return 99;
        if (room.getId() != null && room.getId().endsWith("::general")) return 0;
        if (!room.isCustom() && "Department".equalsIgnoreCase(room.getScopeLabel())) return 1;
        if (room.getId() != null && room.getId().endsWith("::freshers")) return 2;
        if (room.isCustom()) return 3;
        return 4;
    }

    private boolean isSameUniversity(UserProfile currentUser, UserProfile candidate) {
        if (candidate == null) return false;
        String currentEin = safeName(currentUser.getEinNumber());
        String candidateEin = safeName(candidate.getEinNumber());
        if (!currentEin.isBlank() && !candidateEin.isBlank() && currentEin.length() >= 3 && candidateEin.length() >= 3) {
            if (currentEin.substring(0, 3).equalsIgnoreCase(candidateEin.substring(0, 3))) {
                return true;
            }
        }
        return normalizeUniversity(currentUser.getUniversityName())
            .equalsIgnoreCase(normalizeUniversity(candidate.getUniversityName()));
    }

    private static String roomId(String universityKey, String scope) {
        return universityKey + "::" + scope;
    }

    private static String normalizeUniversity(String universityName) {
        return safeName(universityName)
            .toLowerCase(Locale.ROOT)
            .replace("&", "and")
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    }

    private static String normalizeDepartment(String department) {
        return safeName(department)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    }

    private static String safeName(String value) {
        return safeName(value, "");
    }

    private static String safeName(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }
}
