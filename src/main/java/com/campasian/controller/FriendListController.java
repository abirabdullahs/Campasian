package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.AppRouter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FriendListController implements Initializable {

    @FXML private VBox friendsVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFriends();
    }

    private void loadFriends() {
        if (friendsVBox == null) return;
        friendsVBox.getChildren().clear();

        new Thread(() -> {
            try {
                List<UserProfile> friends = ApiService.getInstance().getFriends();
                Platform.runLater(() -> {
                    if (friendsVBox == null) return;
                    friendsVBox.getChildren().clear();
                    
                    if (friends.isEmpty()) {
                        Label empty = new Label("No friends yet. Start connecting!");
                        empty.setWrapText(true);
                        friendsVBox.getChildren().add(empty);
                    } else {
                        for (UserProfile friend : friends) {
                            friendsVBox.getChildren().add(buildFriendCard(friend));
                        }
                    }
                });
            } catch (ApiException ignored) {
                Platform.runLater(() -> {
                    if (friendsVBox != null) {
                        Label err = new Label("Failed to load friends");
                        friendsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private HBox buildFriendCard(UserProfile friend) {
        HBox card = new HBox(16);
        card.setStyle("-fx-padding: 12; -fx-border-color: #e4e4e7; -fx-border-radius: 6; -fx-background-color: #fafafa;");
        card.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        Label name = new Label(friend.getFullName() != null ? friend.getFullName() : "Unknown");
        name.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Label uni = new Label(friend.getUniversityName() != null ? friend.getUniversityName() : "");
        uni.setStyle("-fx-font-size: 12; -fx-text-fill: #71717a;");
        info.getChildren().addAll(name, uni);
        HBox.setHgrow(info, Priority.ALWAYS);

        javafx.scene.control.Button viewBtn = new javafx.scene.control.Button("View");
        viewBtn.setStyle("-fx-font-size: 12; -fx-padding: 6 12;");
        viewBtn.setOnAction(e -> AppRouter.navigateToProfile(friend.getId()));

        javafx.scene.control.Button msgBtn = new javafx.scene.control.Button("Message");
        msgBtn.setStyle("-fx-font-size: 12; -fx-padding: 6 12;");
        msgBtn.setOnAction(e -> AppRouter.navigateToChat(friend.getId(), friend.getFullName()));

        card.getChildren().addAll(info, viewBtn, msgBtn);
        return card;
    }
}
