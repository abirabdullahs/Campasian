package com.campasian.service;

import com.campasian.model.CommunityMessage;
import com.campasian.model.CommunityRoom;
import com.campasian.model.UserProfile;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory community room service.
 * The controller uses ObservableList on top of this service so the UI can later be reused with DB-backed data.
 */
public final class CommunityService {

    private static final CommunityService INSTANCE = new CommunityService();
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final Map<String, List<CommunityMessage>> messagesByRoom = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomMembers = new ConcurrentHashMap<>();

    private CommunityService() {}

    public static CommunityService getInstance() {
        return INSTANCE;
    }

    public void ensureAutoJoin(String userKey, String email, String universityName,
                               String department, String universityId, String displayName) {
        String normalizedUniversity = normalizeUniversity(universityName);
        if (normalizedUniversity.isBlank()) return;

        String memberKey = memberKey(userKey, email, universityId, displayName);
        String generalRoomId = roomId(normalizedUniversity, "general");
        addMembership(generalRoomId, memberKey);
        seedRoom(
            generalRoomId,
            "Campus Community Bot",
            "Welcome to the " + safeName(universityName) + " university hub. " +
                "Students are auto-joined here after signup."
        );

        String cleanedDepartment = normalizeDepartment(department);
        if (!cleanedDepartment.isBlank()) {
            String deptRoomId = roomId(normalizedUniversity, cleanedDepartment);
            addMembership(deptRoomId, memberKey);
            seedRoom(
                deptRoomId,
                "Department Coordinator",
                "Department space ready for " + safeName(department) + " students."
            );
        }
    }

    public List<CommunityRoom> buildCommunities(UserProfile currentUser, List<UserProfile> allProfiles) {
        if (currentUser == null) return List.of();

        ensureAutoJoin(
            currentUser.getId(),
            null,
            currentUser.getUniversityName(),
            currentUser.getDepartment(),
            currentUser.getEinNumber(),
            currentUser.getFullName()
        );

        String universityName = safeName(currentUser.getUniversityName());
        String universityKey = normalizeUniversity(universityName);
        if (universityKey.isBlank()) return List.of();

        List<UserProfile> sameUniversity = allProfiles == null ? List.of() : allProfiles.stream()
            .filter(profile -> isSameUniversity(currentUser, profile))
            .sorted(Comparator.comparing(profile -> safeName(profile.getFullName())))
            .toList();

        List<CommunityRoom> rooms = new ArrayList<>();

        String generalRoomId = roomId(universityKey, "general");
        addMembersFromProfiles(generalRoomId, sameUniversity);
        rooms.add(new CommunityRoom(
            generalRoomId,
            universityName + " Community",
            "Messenger-style university room for announcements, study help, and cross-department discussion.",
            "University Hub",
            roomMembers.getOrDefault(generalRoomId, Set.of()).size(),
            true,
            true
        ));

        String department = safeName(currentUser.getDepartment());
        if (!department.isBlank()) {
            String departmentKey = normalizeDepartment(department);
            String departmentRoomId = roomId(universityKey, departmentKey);
            List<UserProfile> sameDepartment = sameUniversity.stream()
                .filter(profile -> safeName(profile.getDepartment()).equalsIgnoreCase(department))
                .toList();
            addMembersFromProfiles(departmentRoomId, sameDepartment);
            rooms.add(new CommunityRoom(
                departmentRoomId,
                department + " Lounge",
                "Verified room for " + department + " students from " + universityName + ".",
                "Department",
                roomMembers.getOrDefault(departmentRoomId, Set.of()).size(),
                true,
                false
            ));
        }

        String supportRoomId = roomId(universityKey, "freshers");
        seedRoom(
            supportRoomId,
            "Campus Mentor",
            "Use this room for orientation questions, class routines, and first-week help."
        );
        addMembersFromProfiles(supportRoomId, sameUniversity);
        rooms.add(new CommunityRoom(
            supportRoomId,
            "Freshers Help Desk",
            "Structured support room for onboarding, campus logistics, and student Q&A.",
            "Support",
            roomMembers.getOrDefault(supportRoomId, Set.of()).size(),
            true,
            false
        ));

        return rooms;
    }

    public List<CommunityMessage> getMessages(String roomId) {
        return new ArrayList<>(messagesByRoom.getOrDefault(roomId, List.of()));
    }

    public CommunityMessage sendMessage(String roomId, String senderId, String senderName, String content) {
        CommunityMessage message = new CommunityMessage(
            roomId,
            senderId != null ? senderId : "anonymous",
            safeName(senderName, "Student"),
            content,
            OffsetDateTime.now().format(ISO)
        );
        messagesByRoom.computeIfAbsent(roomId, key -> new CopyOnWriteArrayList<>()).add(message);
        return message;
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

    private void addMembersFromProfiles(String roomId, List<UserProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) return;
        for (UserProfile profile : profiles) {
            addMembership(roomId, memberKey(profile.getId(), null, profile.getEinNumber(), profile.getFullName()));
        }
    }

    private void addMembership(String roomId, String memberKey) {
        roomMembers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet()).add(memberKey);
    }

    private void seedRoom(String roomId, String senderName, String content) {
        List<CommunityMessage> roomMessages = messagesByRoom.computeIfAbsent(roomId, key -> new CopyOnWriteArrayList<>());
        if (roomMessages.isEmpty()) {
            roomMessages.add(new CommunityMessage(
                roomId,
                "system",
                senderName,
                content,
                OffsetDateTime.now().minusMinutes(5).format(ISO)
            ));
        }
    }

    private static String roomId(String universityKey, String scope) {
        return universityKey + "::" + scope;
    }

    private static String memberKey(String userKey, String email, String universityId, String displayName) {
        if (userKey != null && !userKey.isBlank()) return "user:" + userKey.trim().toLowerCase(Locale.ROOT);
        if (universityId != null && !universityId.isBlank()) return "id:" + universityId.trim().toLowerCase(Locale.ROOT);
        if (email != null && !email.isBlank()) return "email:" + email.trim().toLowerCase(Locale.ROOT);
        return "name:" + safeName(displayName, "student").toLowerCase(Locale.ROOT);
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
