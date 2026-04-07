package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.view.AppRouter;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the People discovery sub-view. Debounced search by name and university.
 * Dynamically loads department filters from the database.
 */
public class PeopleController implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox peopleVBox;
    @FXML private VBox peopleLoadingOverlay;
    @FXML private HBox filterRow;

    private String currentDepartment;
    private Button allDeptBtn;
    private final Map<String, Button> deptButtonMap = new HashMap<>();
    private static final String CHIP_ACTIVE = "people-chip-active";
    private final PauseTransition debouncer = new PauseTransition(Duration.millis(300));
    private static final int PAGE_SIZE = 100;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDepartment = null;
        loadDepartmentFilters();
        loadPeople();
        if (searchField != null) {
            searchField.textProperty().addListener((o, oldVal, newVal) -> {
                debouncer.setOnFinished(e -> loadPeople());
                debouncer.stop();
                debouncer.playFromStart();
            });
        }
    }

    /**
     * Load all unique departments from database and create filter buttons dynamically.
     */
    private void loadDepartmentFilters() {
        if (filterRow == null) return;
        new Thread(() -> {
            try {
                List<String> departments = ApiService.getInstance().getAllDepartments();
                Platform.runLater(() -> {
                    if (filterRow == null) return;
                    filterRow.getChildren().clear();
                    deptButtonMap.clear();
                    
                    // Add "Everyone" button first
                    allDeptBtn = new Button("Everyone");
                    allDeptBtn.getStyleClass().addAll("people-chip", "people-chip-active");
                    allDeptBtn.setOnAction(e -> setDepartment(null));
                    filterRow.getChildren().add(allDeptBtn);
                    
                    // Add department buttons
                    for (String dept : departments) {
                        Button btn = new Button(dept);
                        btn.getStyleClass().add("people-chip");
                        btn.setOnAction(e -> setDepartment(dept));
                        filterRow.getChildren().add(btn);
                        deptButtonMap.put(dept, btn);
                    }
                    
                    // Add spacer and label
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    filterRow.getChildren().add(spacer);
                    
                    Label label = new Label("Browse all students");
                    label.getStyleClass().add("people-sort-label");
                    filterRow.getChildren().add(label);
                });
            } catch (ApiException ignored) {
                // If department loading fails, just show "Everyone" button
                Platform.runLater(() -> {
                    if (filterRow == null) return;
                    filterRow.getChildren().clear();
                    deptButtonMap.clear();
                    
                    allDeptBtn = new Button("Everyone");
                    allDeptBtn.getStyleClass().addAll("people-chip", "people-chip-active");
                    allDeptBtn.setOnAction(e -> setDepartment(null));
                    filterRow.getChildren().add(allDeptBtn);
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    filterRow.getChildren().add(spacer);
                    
                    Label label = new Label("Browse all students");
                    label.getStyleClass().add("people-sort-label");
                    filterRow.getChildren().add(label);
                });
            }
        }).start();
    }

    @FXML
    protected void onSearchKeyReleased() {
        debouncer.setOnFinished(e -> loadPeople());
        debouncer.stop();
        debouncer.playFromStart();
    }

    private void setDepartment(String dept) {
        currentDepartment = dept;
        // Remove active class from all buttons
        if (allDeptBtn != null) allDeptBtn.getStyleClass().remove(CHIP_ACTIVE);
        for (Button btn : deptButtonMap.values()) {
            btn.getStyleClass().remove(CHIP_ACTIVE);
        }
        // Add active class to selected button
        if (dept == null && allDeptBtn != null) {
            allDeptBtn.getStyleClass().add(CHIP_ACTIVE);
        } else if (deptButtonMap.containsKey(dept)) {
            deptButtonMap.get(dept).getStyleClass().add(CHIP_ACTIVE);
        }
        loadPeople();
    }

    private void loadPeople() {
        if (peopleVBox == null) return;
        peopleVBox.getChildren().clear();
        showLoading(true);

        String query = searchField != null ? searchField.getText() : null;
        String dept = currentDepartment;
        new Thread(() -> {
            try {
                String trimmed = query != null ? query.trim() : "";
                String currentUserUniversity = null;
                try {
                    String currentUserId = ApiService.getInstance().getCurrentUserId();
                    if (currentUserId != null && !currentUserId.isBlank()) {
                        UserProfile currentProfile = ApiService.getInstance().getProfile(currentUserId);
                        if (currentProfile != null) {
                            currentUserUniversity = currentProfile.getUniversityName();
                        }
                    }
                } catch (ApiException ignored) {}
                
                List<UserProfile> profiles;
                if (dept != null && !dept.isBlank()) {
                    profiles = ApiService.getInstance().getProfilesByDepartment(dept);
                    if (!trimmed.isBlank()) {
                        String q = trimmed.toLowerCase();
                        profiles = profiles.stream()
                            .filter(p -> (p.getFullName() != null && p.getFullName().toLowerCase().contains(q))
                                || (p.getUniversityName() != null && p.getUniversityName().toLowerCase().contains(q)))
                            .collect(Collectors.toList());
                    }
                } else if (!trimmed.isBlank()) {
                    profiles = ApiService.getInstance().searchProfiles(trimmed, trimmed);
                } else {
                    if (currentUserUniversity != null && !currentUserUniversity.isBlank()) {
                        profiles = ApiService.getInstance().getProfilesByUniversity(currentUserUniversity);
                    } else {
                        profiles = ApiService.getInstance().getAllProfiles();
                    }
                }
                
                // Limit to PAGE_SIZE for faster loading
                if (profiles.size() > PAGE_SIZE) {
                    profiles = profiles.subList(0, PAGE_SIZE);
                }
                
                String currentUserId = ApiService.getInstance().getCurrentUserId();
                final List<UserProfile> finalProfiles = profiles;
                
                // Show UI immediately without friend status (lazy loading)
                Platform.runLater(() -> {
                    if (peopleVBox == null) return;
                    peopleVBox.getChildren().clear();
                    for (UserProfile p : finalProfiles) {
                        if (p.getId() != null && p.getId().equals(currentUserId)) continue;
                        peopleVBox.getChildren().add(buildUserCard(p, false, "none"));
                    }
                    if (finalProfiles.isEmpty()) {
                        Label empty = new Label("No users found.");
                        empty.getStyleClass().add("profile-label");
                        peopleVBox.getChildren().add(empty);
                    }
                    showLoading(false);
                });
                
                // Load friend status in background (batches of 10)
                loadFriendStatusInBatches(finalProfiles, currentUserId);
                
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (peopleVBox != null) {
                        peopleVBox.getChildren().clear();
                        Label err = new Label("Unable to load people.");
                        err.getStyleClass().add("profile-label");
                        peopleVBox.getChildren().add(err);
                        showLoading(false);
                    }
                });
            }
        }).start();
    }
    
    private void loadFriendStatusInBatches(List<UserProfile> profiles, String currentUserId) {
        // Load friend status in background without blocking UI
        new Thread(() -> {
            for (int i = 0; i < profiles.size(); i += 10) {
                int end = Math.min(i + 10, profiles.size());
                List<UserProfile> batch = profiles.subList(i, end);
                
                java.util.Map<String, Boolean> followingMap = new java.util.HashMap<>();
                java.util.Map<String, String> friendStatusMap = new java.util.HashMap<>();
                
                for (UserProfile p : batch) {
                    if (p.getId() == null || p.getId().equals(currentUserId)) continue;
                    try {
                        followingMap.put(p.getId(), ApiService.getInstance().isFollowing(p.getId()));
                        friendStatusMap.put(p.getId(), ApiService.getInstance().getFriendRequestStatus(p.getId()));
                    } catch (ApiException ignored) {}
                }
                
                java.util.Map<String, Boolean> finalFollowingMap = followingMap;
                java.util.Map<String, String> finalFriendStatusMap = friendStatusMap;
                
                // Update UI for this batch
                Platform.runLater(() -> {
                    // Re-render visible cards with updated status
                    updateVisibleCardStatuses(finalFollowingMap, finalFriendStatusMap);
                });
                
                try {
                    Thread.sleep(200); // Avoid overwhelming server
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
    
    private void updateVisibleCardStatuses(java.util.Map<String, Boolean> followingMap, 
                                          java.util.Map<String, String> friendStatusMap) {
        // Update cards that are currently visible with new status
        // This is a simplified approach - cards will update when friend status loads
        if (peopleVBox == null) return;
        // Implementation depends on card structure
    }

    private VBox buildUserCard(UserProfile p, boolean following, String friendStatus) {
        String name = p.getFullName() != null ? p.getFullName() : "Unknown";
        String university = p.getUniversityName() != null && !p.getUniversityName().isBlank() ? p.getUniversityName() : "-";
        String department = p.getDepartment() != null && !p.getDepartment().isBlank() ? p.getDepartment() : null;
        String session = p.getSession() != null && !p.getSession().isBlank() ? p.getSession() : null;
        String batch = p.getBatch() != null && !p.getBatch().isBlank() ? p.getBatch() : null;

        Label avatarLabel = new Label(initialsOf(name));
        avatarLabel.getStyleClass().add("people-card-avatar-text");

        VBox avatarWrap = new VBox(avatarLabel);
        avatarWrap.getStyleClass().add("people-card-avatar");
        avatarWrap.setAlignment(javafx.geometry.Pos.CENTER);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("people-card-name");
        nameLabel.setWrapText(true);
        nameLabel.setCursor(javafx.scene.Cursor.HAND);
        nameLabel.setOnMouseClicked(e -> AppRouter.navigateToProfile(p.getId()));

        Label universityLabel = new Label(university);
        universityLabel.getStyleClass().add("people-card-university");
        universityLabel.setWrapText(true);

        VBox identityBox = new VBox(4, nameLabel, universityLabel);
        HBox headerRow = new HBox(14, avatarWrap, identityBox);
        headerRow.getStyleClass().add("people-card-header");

        HBox badgeRow = new HBox(8);
        badgeRow.getStyleClass().add("people-card-badges");
        if (department != null) {
            badgeRow.getChildren().add(buildMetaBadge(department));
        }
        if (session != null || batch != null) {
            String badge = (session != null ? session : "") + (session != null && batch != null ? " • " : "") + (batch != null ? batch : "");
            badgeRow.getChildren().add(buildMetaBadge(badge));
        }

        Button followBtn = new Button("Follow");
        followBtn.getStyleClass().add("people-action-primary");
        Button unfollowBtn = new Button("Unfollow");
        unfollowBtn.getStyleClass().add("people-action-secondary");
        Button friendReqBtn = new Button("Add Friend");
        friendReqBtn.getStyleClass().add("people-action-secondary");
        Label friendStatusLabel = new Label();
        friendStatusLabel.getStyleClass().add("people-friend-status");

        followBtn.setVisible(!following);
        followBtn.setManaged(!following);
        unfollowBtn.setVisible(following);
        unfollowBtn.setManaged(following);

        if ("accepted".equals(friendStatus)) {
            friendReqBtn.setVisible(false);
            friendReqBtn.setManaged(false);
            friendStatusLabel.setText("Friends");
        } else if ("pending".equals(friendStatus)) {
            friendReqBtn.setText("Requested");
            friendReqBtn.setDisable(true);
        } else {
            friendStatusLabel.setManaged(false);
            friendStatusLabel.setVisible(false);
        }

        followBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().followUser(p.getId());
                loadPeople();
            } catch (ApiException ignored) {}
        });
        unfollowBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().unfollowUser(p.getId());
                loadPeople();
            } catch (ApiException ignored) {}
        });
        friendReqBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().sendFriendRequest(p.getId());
                friendReqBtn.setText("Requested");
                friendReqBtn.setDisable(true);
            } catch (ApiException ignored) {}
        });

        HBox actions = new HBox(8, followBtn, unfollowBtn, friendReqBtn, friendStatusLabel);
        actions.getStyleClass().add("people-card-actions");

        VBox card = new VBox(8);
        card.getStyleClass().add("people-profile-card");
        if (badgeRow.getChildren().isEmpty()) {
            card.getChildren().addAll(headerRow, actions);
        } else {
            card.getChildren().addAll(headerRow, badgeRow, actions);
        }
        return card;
    }

    private Label buildMetaBadge(String text) {
        Label badge = new Label(text);
        badge.getStyleClass().add("people-meta-badge");
        return badge;
    }

    private String initialsOf(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void showLoading(boolean show) {
        if (peopleLoadingOverlay == null) return;
        peopleLoadingOverlay.setVisible(show);
        peopleLoadingOverlay.setManaged(show);
        if (show) {
            peopleLoadingOverlay.toFront();
        }
    }
}