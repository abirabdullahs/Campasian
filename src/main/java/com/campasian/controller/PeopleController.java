package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.AppRouter;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the People discovery sub-view. Debounced search by name and university.
 */
public class PeopleController implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox peopleVBox;

    private final PauseTransition debouncer = new PauseTransition(Duration.millis(300));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    private void loadPeople() {
        if (peopleVBox == null) return;
        peopleVBox.getChildren().clear();

        String query = searchField != null ? searchField.getText() : null;
        // Run API call off FX thread; update UI with Platform.runLater
        new Thread(() -> {
            try {
                String trimmed = query != null ? query.trim() : "";
                List<UserProfile> profiles;
                if (!trimmed.isBlank()) {
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
                Platform.runLater(() -> {
                    if (peopleVBox == null) return;
                    peopleVBox.getChildren().clear();
                    for (UserProfile p : profiles) {
                        if (p.getId() != null && p.getId().equals(currentUserId)) continue;
                        peopleVBox.getChildren().add(buildUserCard(p, finalFollowingMap.getOrDefault(p.getId(), false), finalFriendStatusMap.getOrDefault(p.getId(), "none")));
                    }
                    if (profiles.isEmpty()) {
                        Label empty = new Label("No users found.");
                        empty.getStyleClass().add("profile-label");
                        peopleVBox.getChildren().add(empty);
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (peopleVBox != null) {
                        peopleVBox.getChildren().clear();
                        Label err = new Label("Unable to load people.");
                        err.getStyleClass().add("profile-label");
                        peopleVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildUserCard(UserProfile p, boolean following, String friendStatus) {
        String name = p.getFullName() != null ? p.getFullName() : "Unknown";
        String uni = p.getUniversityName() != null && !p.getUniversityName().isBlank()
            ? p.getUniversityName() : "—";

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("profile-value");
        nameLbl.setWrapText(true);
        nameLbl.setCursor(javafx.scene.Cursor.HAND);
        nameLbl.setOnMouseClicked(e -> AppRouter.navigateToProfile(p.getId()));

        Label uniLbl = new Label(uni);
        uniLbl.getStyleClass().add("profile-label");

        Button followBtn = new Button("Follow");
        Button unfollowBtn = new Button("Unfollow");
        Button friendReqBtn = new Button("Send Friend Request");
        Label friendStatusLabel = new Label();

        followBtn.setVisible(!following);
        followBtn.setManaged(!following);
        unfollowBtn.setVisible(following);
        unfollowBtn.setManaged(following);

        if ("accepted".equals(friendStatus)) {
            friendReqBtn.setVisible(false);
            friendReqBtn.setManaged(false);
            friendStatusLabel.setText("Friends");
            friendStatusLabel.getStyleClass().add("profile-label");
        } else if ("pending".equals(friendStatus)) {
            friendReqBtn.setText("Requested");
            friendReqBtn.setDisable(true);
        }

        followBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().followUser(p.getId());
                loadPeople();
            } catch (ApiException ex) { /* ignore */ }
        });
        unfollowBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().unfollowUser(p.getId());
                loadPeople();
            } catch (ApiException ex) { /* ignore */ }
        });
        friendReqBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().sendFriendRequest(p.getId());
                friendReqBtn.setText("Requested");
                friendReqBtn.setDisable(true);
            } catch (ApiException ex) { /* ignore */ }
        });

        HBox actions = new HBox(8);
        actions.getChildren().addAll(followBtn, unfollowBtn, friendReqBtn, friendStatusLabel);

        VBox card = new VBox(8);
        card.getStyleClass().add("user-card");
        card.getChildren().addAll(nameLbl, uniLbl, actions);
        return card;
    }
}
