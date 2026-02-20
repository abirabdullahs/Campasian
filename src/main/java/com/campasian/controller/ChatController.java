package com.campasian.controller;

import com.campasian.model.Message;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.AppRouter;
import com.campasian.view.NavigationContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the Messages / Chat view. One-to-one messaging with accepted friends.
 */
public class ChatController implements Initializable {

    @FXML private VBox friendsList;
    @FXML private Label chatPartnerLabel;
    @FXML private ScrollPane messagesScroll;
    @FXML private VBox messagesVBox;
    @FXML private TextField messageField;

    private String selectedPartnerId;
    private final ScheduledExecutorService pollExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "chat-poll");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFriends();
        pollExecutor.scheduleAtFixedRate(this::pollMessages, 2, 2, TimeUnit.SECONDS);
        String partnerId = NavigationContext.getChatPartnerUserId();
        String partnerName = NavigationContext.getChatPartnerName();
        if (partnerId != null && !partnerId.isBlank()) {
            NavigationContext.clearChatPartner();
            selectPartner(partnerId, partnerName != null ? partnerName : "Chat");
        }
    }

    private void loadFriends() {
        if (friendsList == null) return;
        friendsList.getChildren().clear();
        new Thread(() -> {
            try {
                List<UserProfile> friends = ApiService.getInstance().getFriends();
                Platform.runLater(() -> {
                    if (friendsList == null) return;
                    friendsList.getChildren().clear();
                    for (UserProfile f : friends) {
                        Label lbl = new Label(f.getFullName() != null ? f.getFullName() : "Unknown");
                        lbl.getStyleClass().add("profile-value");
                        lbl.setWrapText(true);
                        lbl.setCursor(javafx.scene.Cursor.HAND);
                        lbl.setUserData(f.getId());
                        lbl.setOnMouseClicked(e -> selectPartner(f.getId(), f.getFullName()));
                        friendsList.getChildren().add(lbl);
                    }
                    if (friends.isEmpty()) {
                        Label empty = new Label("No friends yet. Accept friend requests to chat.");
                        empty.getStyleClass().add("profile-label");
                        empty.setWrapText(true);
                        friendsList.getChildren().add(empty);
                    }
                });
            } catch (ApiException ignored) {}
        }).start();
    }

    private void selectPartner(String partnerId, String name) {
        selectedPartnerId = partnerId;
        Platform.runLater(() -> {
            if (chatPartnerLabel != null) chatPartnerLabel.setText(name != null ? name : "Chat");
            loadMessages();
        });
    }

    private void loadMessages() {
        if (messagesVBox == null || selectedPartnerId == null) return;
        new Thread(() -> {
            try {
                List<Message> msgs = ApiService.getInstance().getMessages(selectedPartnerId);
                Platform.runLater(() -> {
                    if (messagesVBox == null) return;
                    messagesVBox.getChildren().clear();
                    String currentId = ApiService.getInstance().getCurrentUserId();
                    for (Message m : msgs) {
                        boolean fromMe = currentId != null && currentId.equals(m.getSenderId());
                        VBox bubble = buildMessageBubble(m, fromMe);
                        messagesVBox.getChildren().add(bubble);
                    }
                    scrollToBottom();
                });
            } catch (ApiException ignored) {}
        }).start();
    }

    private void pollMessages() {
        if (selectedPartnerId != null) loadMessages();
    }

    private VBox buildMessageBubble(Message m, boolean fromMe) {
        Label content = new Label(m.getContent() != null ? m.getContent() : "");
        content.getStyleClass().add("comment-text");
        content.setWrapText(true);
        content.setMaxWidth(400);
        Label time = new Label(formatTime(m.getCreatedAt()));
        time.getStyleClass().add("post-meta");
        VBox bubble = new VBox(4);
        bubble.getStyleClass().add("comment-card");
        if (fromMe) bubble.setStyle("-fx-alignment: center-right; -fx-background-color: #E4E4E7;");
        bubble.getChildren().addAll(content, time);
        return bubble;
    }

    @FXML
    protected void onSendMessage() {
        if (selectedPartnerId == null || messageField == null) return;
        String text = messageField.getText();
        if (text == null || text.isBlank()) return;
        try {
            ApiService.getInstance().sendMessage(selectedPartnerId, text.trim());
            messageField.clear();
            loadMessages();
        } catch (ApiException ignored) {}
    }

    private void scrollToBottom() {
        if (messagesScroll != null) {
            Platform.runLater(() -> {
                messagesScroll.setVvalue(1.0);
            });
        }
    }

    private static String formatTime(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            OffsetDateTime dt = OffsetDateTime.parse(iso);
            return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return iso.length() > 5 ? iso.substring(11, 16) : iso;
        }
    }
}
