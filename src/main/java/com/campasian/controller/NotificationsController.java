package com.campasian.controller;

import com.campasian.model.FriendRequest;
import com.campasian.model.Notification;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.AppRouter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the notifications sub-view. Shows friend requests and activity notifications.
 */
public class NotificationsController implements Initializable {

    @FXML private VBox notificationsVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadNotifications();
    }

    private void loadNotifications() {
        if (notificationsVBox == null) return;
        notificationsVBox.getChildren().clear();

        new Thread(() -> {
            try {
                List<FriendRequest> friendRequests = ApiService.getInstance().getIncomingFriendRequests();
                List<Notification> notifications = ApiService.getInstance().fetchNotifications();

                Platform.runLater(() -> {
                    if (notificationsVBox == null) return;
                    notificationsVBox.getChildren().clear();

                    if (!friendRequests.isEmpty()) {
                        Label friendReqHeader = new Label("Friend Requests");
                        friendReqHeader.getStyleClass().add("page-title");
                        friendReqHeader.setStyle("-fx-font-size: 18px; -fx-padding: 0 0 8 0;");
                        notificationsVBox.getChildren().add(friendReqHeader);
                        for (FriendRequest fr : friendRequests) {
                            notificationsVBox.getChildren().add(buildFriendRequestCard(fr));
                        }
                        Label notifHeader = new Label("Activity");
                        notifHeader.getStyleClass().add("page-title");
                        notifHeader.setStyle("-fx-font-size: 18px; -fx-padding: 16 0 8 0;");
                        notificationsVBox.getChildren().add(notifHeader);
                    }

                    for (Notification n : notifications) {
                        if ("friend_request".equals(n.getType())) continue;
                        notificationsVBox.getChildren().add(buildNotificationCard(n));
                    }

                    if (friendRequests.isEmpty() && notifications.isEmpty()) {
                        Label empty = new Label("No notifications yet.");
                        empty.getStyleClass().add("profile-label");
                        notificationsVBox.getChildren().add(empty);
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (notificationsVBox != null) {
                        notificationsVBox.getChildren().clear();
                        Label err = new Label("Unable to load notifications.");
                        err.getStyleClass().add("profile-label");
                        notificationsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildFriendRequestCard(FriendRequest fr) {
        String name = fr.getFromName() != null ? fr.getFromName() : "Someone";
        Label lbl = new Label(name + " sent you a friend request");
        lbl.getStyleClass().add("notification-text");
        lbl.setWrapText(true);
        lbl.setCursor(javafx.scene.Cursor.HAND);
        lbl.setOnMouseClicked(e -> AppRouter.navigateToProfile(fr.getFromId()));
        Label time = new Label(formatTimeAgo(fr.getCreatedAt()));
        time.getStyleClass().add("post-meta");

        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().add("btn-primary");
        Button rejectBtn = new Button("Reject");
        rejectBtn.getStyleClass().add("btn-secondary");
        HBox actions = new HBox(8);
        actions.getChildren().addAll(acceptBtn, rejectBtn);

        acceptBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().acceptFriendRequest(fr.getId());
                loadNotifications();
            } catch (ApiException ex) { /* ignore */ }
        });
        rejectBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().rejectFriendRequest(fr.getId());
                loadNotifications();
            } catch (ApiException ex) { /* ignore */ }
        });

        VBox card = new VBox(8);
        card.getStyleClass().add("notification-card");
        card.getChildren().addAll(lbl, time, actions);
        return card;
    }

    private VBox buildNotificationCard(Notification n) {
        String text;
        String type = n.getType() != null ? n.getType() : "";
        String actor = n.getActorName() != null ? n.getActorName() : "Someone";
        switch (type) {
            case "like": text = actor + " liked your post."; break;
            case "comment": text = actor + " commented on your post."; break;
            case "follow": text = actor + " started following you."; break;
            case "friend_request": text = actor + " sent you a friend request."; break;
            case "friend_accepted": text = actor + " accepted your friend request."; break;
            default: text = actor + " interacted with you.";
        }
        Label lbl = new Label(text);
        lbl.getStyleClass().add("notification-text");
        lbl.setWrapText(true);
        lbl.setCursor(javafx.scene.Cursor.HAND);
        if (n.getPostId() != null) lbl.setOnMouseClicked(e -> AppRouter.navigateToProfile(n.getActorId()));
        else if (n.getActorId() != null) lbl.setOnMouseClicked(e -> AppRouter.navigateToProfile(n.getActorId()));
        Label time = new Label(formatTimeAgo(n.getCreatedAt()));
        time.getStyleClass().add("post-meta");
        Button markReadBtn = new Button("Mark as Read");
        markReadBtn.getStyleClass().add("btn-secondary");
        markReadBtn.setVisible(!n.isRead());
        markReadBtn.setManaged(!n.isRead());
        markReadBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().markNotificationAsRead(n.getId());
                loadNotifications();
            } catch (ApiException ex) { /* ignore */ }
        });
        VBox card = new VBox(4);
        card.getStyleClass().add("notification-card");
        if (!n.isRead()) card.setStyle("-fx-border-color: #09090B; -fx-border-width: 1;");
        card.getChildren().add(lbl);
        card.getChildren().add(time);
        if (!n.isRead()) card.getChildren().add(markReadBtn);
        return card;
    }

    private static String formatTimeAgo(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            OffsetDateTime then = OffsetDateTime.parse(iso);
            Duration d = Duration.between(then.toInstant(), java.time.Instant.now());
            long s = d.getSeconds();
            if (s < 60) return "just now";
            if (s < 3600) return (s / 60) + " min ago";
            if (s < 86400) return (s / 3600) + " hours ago";
            if (s < 604800) return (s / 86400) + " days ago";
            return then.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
        } catch (Exception e) {
            return iso.length() > 16 ? iso.substring(0, 16) : iso;
        }
    }
}
