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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the People discovery sub-view. Debounced search by name and university.
 */
public class PeopleController implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox peopleVBox;
    @FXML private StackPane peopleLoadingOverlay;
    @FXML private Button allDeptBtn;
    @FXML private Button cseDeptBtn;
    @FXML private Button eeeDeptBtn;
    @FXML private Button bbaDeptBtn;

    private String currentDepartment;
    private static final String CHIP_ACTIVE = "people-chip-active";
    private final PauseTransition debouncer = new PauseTransition(Duration.millis(300));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDepartment = null;
        loadPeople();
        if (searchField != null) {
            searchField.textProperty().addListener((o, oldVal, newVal) -> {
                debouncer.setOnFinished(e -> loadPeople());
                debouncer.stop();
                debouncer.playFromStart();
            });
        }
    }

    @FXML
    protected void onSearchKeyReleased() {
        debouncer.setOnFinished(e -> loadPeople());
        debouncer.stop();
        debouncer.playFromStart();
    }

    @FXML protected void onFilterAll() { setDepartment(null); }
    @FXML protected void onFilterCSE() { setDepartment("CSE"); }
    @FXML protected void onFilterEEE() { setDepartment("EEE"); }
    @FXML protected void onFilterBBA() { setDepartment("BBA"); }

    private void setDepartment(String dept) {
        currentDepartment = dept;
        if (allDeptBtn != null) allDeptBtn.getStyleClass().remove(CHIP_ACTIVE);
        if (cseDeptBtn != null) cseDeptBtn.getStyleClass().remove(CHIP_ACTIVE);
        if (eeeDeptBtn != null) eeeDeptBtn.getStyleClass().remove(CHIP_ACTIVE);
        if (bbaDeptBtn != null) bbaDeptBtn.getStyleClass().remove(CHIP_ACTIVE);
        if (dept == null && allDeptBtn != null) allDeptBtn.getStyleClass().add(CHIP_ACTIVE);
        else if ("CSE".equals(dept) && cseDeptBtn != null) cseDeptBtn.getStyleClass().add(CHIP_ACTIVE);
        else if ("EEE".equals(dept) && eeeDeptBtn != null) eeeDeptBtn.getStyleClass().add(CHIP_ACTIVE);
        else if ("BBA".equals(dept) && bbaDeptBtn != null) bbaDeptBtn.getStyleClass().add(CHIP_ACTIVE);
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
                    profiles = ApiService.getInstance().getAllProfiles();
                }
                String currentUserId = ApiService.getInstance().getCurrentUserId();

                java.util.Map<String, Boolean> followingMap = new java.util.HashMap<>();
                java.util.Map<String, String> friendStatusMap = new java.util.HashMap<>();
                for (UserProfile p : profiles) {
                    if (p.getId() == null || p.getId().equals(currentUserId)) continue;
                    try {
                        followingMap.put(p.getId(), ApiService.getInstance().isFollowing(p.getId()));
                        friendStatusMap.put(p.getId(), ApiService.getInstance().getFriendRequestStatus(p.getId()));
                    } catch (ApiException ignored) {}
                }
                java.util.Map<String, Boolean> finalFollowingMap = followingMap;
                java.util.Map<String, String> finalFriendStatusMap = friendStatusMap;
                final List<UserProfile> finalProfiles = profiles;
                Platform.runLater(() -> {
                    if (peopleVBox == null) return;
                    peopleVBox.getChildren().clear();
                    for (UserProfile p : finalProfiles) {
                        if (p.getId() != null && p.getId().equals(currentUserId)) continue;
                        peopleVBox.getChildren().add(buildUserCard(
                            p,
                            finalFollowingMap.getOrDefault(p.getId(), false),
                            finalFriendStatusMap.getOrDefault(p.getId(), "none")
                        ));
                    }
                    if (finalProfiles.isEmpty()) {
                        Label empty = new Label("No users found.");
                        empty.getStyleClass().add("profile-label");
                        peopleVBox.getChildren().add(empty);
                    }
                    showLoading(false);
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (peopleVBox != null) {
                        peopleVBox.getChildren().clear();
                        Label err = new Label("Unable to load people.");
                        err.getStyleClass().add("profile-label");
                        peopleVBox.getChildren().add(err);
                    }
                    showLoading(false);
                });
            }
        }).start();
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
