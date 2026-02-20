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
import java.util.stream.Collectors;

/**
 * Controller for the People discovery sub-view. Debounced search by name and university.
 */
public class PeopleController implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox peopleVBox;
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

    @FXML protected void onFilterAll()   { setDepartment(null); }
    @FXML protected void onFilterCSE()   { setDepartment("CSE"); }
    @FXML protected void onFilterEEE()   { setDepartment("EEE"); }
    @FXML protected void onFilterBBA()   { setDepartment("BBA"); }

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
        String session = p.getSession() != null && !p.getSession().isBlank() ? p.getSession() : null;
        String batch = p.getBatch() != null && !p.getBatch().isBlank() ? p.getBatch() : null;

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("profile-value");
        nameLbl.setWrapText(true);
        nameLbl.setCursor(javafx.scene.Cursor.HAND);
        nameLbl.setOnMouseClicked(e -> AppRouter.navigateToProfile(p.getId()));

        HBox badgeRow = new HBox(8);
        if (session != null || batch != null) {
            String badge = (session != null ? session : "") + (session != null && batch != null ? " · " : "") + (batch != null ? batch : "");
            Label sessLbl = new Label(badge);
            sessLbl.getStyleClass().add("people-chip");
            sessLbl.setStyle("-fx-background-color: #E4E4E7; -fx-text-fill: #09090B; -fx-padding: 4 8; -fx-background-radius: 6; -fx-font-size: 11px;");
            badgeRow.getChildren().add(sessLbl);
        }

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
        if (badgeRow.getChildren().isEmpty()) {
            card.getChildren().addAll(nameLbl, uniLbl, actions);
        } else {
            card.getChildren().addAll(nameLbl, badgeRow, uniLbl, actions);
        }
        return card;
    }
}
