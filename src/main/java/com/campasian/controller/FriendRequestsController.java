package com.campasian.controller;

import com.campasian.model.FriendRequest;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.AppRouter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FriendRequestsController implements Initializable {

    @FXML private VBox requestsVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFriendRequests();
    }

    private void loadFriendRequests() {
        if (requestsVBox == null) return;
        requestsVBox.getChildren().clear();

        new Thread(() -> {
            try {
                List<FriendRequest> requests = ApiService.getInstance().getIncomingFriendRequests();
                Platform.runLater(() -> {
                    if (requestsVBox == null) return;
                    requestsVBox.getChildren().clear();
                    
                    if (requests.isEmpty()) {
                        Label empty = new Label("No pending friend requests");
                        empty.setWrapText(true);
                        requestsVBox.getChildren().add(empty);
                    } else {
                        for (FriendRequest req : requests) {
                            requestsVBox.getChildren().add(buildRequestCard(req));
                        }
                    }
                });
            } catch (ApiException ignored) {
                Platform.runLater(() -> {
                    if (requestsVBox != null) {
                        Label err = new Label("Failed to load friend requests");
                        requestsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildRequestCard(FriendRequest req) {
        VBox card = new VBox(12);
        card.getStyleClass().add("friend-requests-card");

        // Get sender profile
        new Thread(() -> {
            try {
                UserProfile sender = ApiService.getInstance().getProfile(req.getFromId());
                Platform.runLater(() -> {
                    if (sender != null) {
                        HBox infoBox = new HBox(12);
                        infoBox.setAlignment(Pos.CENTER_LEFT);

                        VBox info = new VBox(4);
                        Label name = new Label(sender.getFullName() != null ? sender.getFullName() : "Unknown");
                        name.getStyleClass().add("friend-requests-name");
                        Label uni = new Label(sender.getUniversityName() != null ? sender.getUniversityName() : "");
                        uni.getStyleClass().add("friend-requests-info");
                        info.getChildren().addAll(name, uni);
                        HBox.setHgrow(info, Priority.ALWAYS);

                        Button viewBtn = new Button("View Profile");
                        viewBtn.getStyleClass().add("friend-requests-button");
                        viewBtn.setOnAction(e -> AppRouter.navigateToProfile(sender.getId()));

                        infoBox.getChildren().addAll(info, viewBtn);
                        card.getChildren().add(0, infoBox);
                    }
                });
            } catch (ApiException ignored) {}
        }).start();

        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button acceptBtn = new Button("✓ Accept");
        acceptBtn.getStyleClass().add("friend-requests-button");
        acceptBtn.getStyleClass().add("friend-requests-accept");
        acceptBtn.setOnAction(e -> acceptRequest(req));

        Button rejectBtn = new Button("✕ Reject");
        rejectBtn.getStyleClass().add("friend-requests-button");
        rejectBtn.getStyleClass().add("friend-requests-reject");
        rejectBtn.setOnAction(e -> rejectRequest(req));

        actionsBox.getChildren().addAll(acceptBtn, rejectBtn);
        card.getChildren().add(actionsBox);

        return card;
    }

    private void acceptRequest(FriendRequest req) {
        new Thread(() -> {
            try {
                ApiService.getInstance().acceptFriendRequest(req.getId());
                Platform.runLater(this::loadFriendRequests);
            } catch (ApiException ignored) {}
        }).start();
    }

    private void rejectRequest(FriendRequest req) {
        new Thread(() -> {
            try {
                ApiService.getInstance().rejectFriendRequest(req.getId());
                Platform.runLater(this::loadFriendRequests);
            } catch (ApiException ignored) {}
        }).start();
    }
}
