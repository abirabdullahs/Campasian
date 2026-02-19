package com.campasian.controller;

import com.campasian.model.Notification;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the notifications sub-view.
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
        try {
            List<Notification> list = ApiService.getInstance().fetchNotifications();
            for (Notification n : list) {
                notificationsVBox.getChildren().add(buildNotificationCard(n));
            }
            if (list.isEmpty()) {
                Label empty = new Label("No notifications yet.");
                empty.getStyleClass().add("profile-label");
                notificationsVBox.getChildren().add(empty);
            }
        } catch (ApiException e) {
            Label err = new Label("Unable to load notifications.");
            err.getStyleClass().add("profile-label");
            notificationsVBox.getChildren().add(err);
        }
    }

    private VBox buildNotificationCard(Notification n) {
        String text;
        String type = n.getType() != null ? n.getType() : "";
        String actor = n.getActorName() != null ? n.getActorName() : "Someone";
        switch (type) {
            case "like": text = actor + " liked your post."; break;
            case "comment": text = actor + " commented on your post."; break;
            case "follow": text = actor + " started following you."; break;
            default: text = actor + " interacted with you.";
        }
        Label lbl = new Label(text);
        lbl.getStyleClass().add("notification-text");
        lbl.setWrapText(true);
        Label time = new Label(formatTimeAgo(n.getCreatedAt()));
        time.getStyleClass().add("post-meta");
        VBox card = new VBox(4);
        card.getStyleClass().add("notification-card");
        card.getChildren().addAll(lbl, time);
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
