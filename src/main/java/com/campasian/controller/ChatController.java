package com.campasian.controller;

import com.campasian.model.Message;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.util.ImageSelectionSupport;
import com.campasian.view.NavigationContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
    @FXML private VBox messagePreviewBox;
    @FXML private ImageView messagePreviewImage;
    @FXML private Label messagePreviewLabel;

    private byte[] pendingImageBytes;
    private String pendingImageExtension = "png";
    private String pendingImageContentType = "image/png";

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
        if (messagesVBox == null) return;
        if (selectedPartnerId == null) {
            Platform.runLater(this::showEmptyConversationState);
            return;
        }
        new Thread(() -> {
            try {
                List<Message> msgs = ApiService.getInstance().getMessages(selectedPartnerId);
                Platform.runLater(() -> {
                    if (messagesVBox == null) return;
                    messagesVBox.getChildren().clear();
                    String currentId = ApiService.getInstance().getCurrentUserId();
                    for (Message m : msgs) {
                        boolean fromMe = currentId != null && currentId.equals(m.getSenderId());
                        HBox bubble = buildMessageBubble(m, fromMe);
                        messagesVBox.getChildren().add(bubble);
                    }
                    if (msgs.isEmpty()) {
                        messagesVBox.getChildren().add(buildNoMessagesCard());
                    }
                    scrollToBottom();
                });
            } catch (ApiException ignored) {}
        }).start();
    }

    private void pollMessages() {
        if (selectedPartnerId != null) loadMessages();
    }

//    private HBox buildMessageBubble(Message m, boolean fromMe) {
//        // 1. Message Text Content
//        Label content = new Label(m.getContent() != null ? m.getContent() : "");
//        content.setWrapText(true);
//        content.setMaxWidth(350); // Ekta nirdishto poriman boro hobe (Messenger style)
//
//        // 2. Timestamp
//        Label time = new Label(formatTime(m.getCreatedAt()));
//        time.getStyleClass().add("chat-partner-status"); // CSS theke muted text style nichhe
//        time.setStyle("-fx-font-size: 9px;");
//
//        // 3. Message Bubble (Vertical container for text + time)
//        VBox bubble = new VBox(2);
//        bubble.getChildren().addAll(content, time);
//
//        // 4. Main Container (HBox) to handle Left/Right alignment
//        HBox container = new HBox();
//        container.setFillHeight(true);
//        container.setMaxWidth(Double.MAX_VALUE);
//
//        if (fromMe) {
//            bubble.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
//            container.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
//        } else {
//            bubble.setAlignment(javafx.geometry.Pos.TOP_LEFT);
//            container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
//        }
//
//        container.getChildren().add(bubble);
//        return container;
//    }


    private HBox buildMessageBubble(Message m, boolean fromMe) {
        VBox bubble = new VBox(8);
        bubble.getStyleClass().add(fromMe ? "sent-bubble" : "received-bubble");
        bubble.setMaxWidth(360);

        String text = m.getContent() != null ? m.getContent().trim() : "";
        if (!text.isEmpty()) {
            Label content = new Label(text);
            content.setWrapText(true);
            content.setMaxWidth(320);
            content.getStyleClass().add("chat-message-text");
            bubble.getChildren().add(content);
        }

        if (m.getImageUrl() != null && !m.getImageUrl().isBlank()) {
            try {
                ImageView imageView = new ImageView(new Image(m.getImageUrl(), true));
                imageView.setFitWidth(280);
                imageView.setFitHeight(220);
                imageView.setPreserveRatio(true);
                imageView.getStyleClass().add("chat-message-image");

                StackPane imageFrame = new StackPane(imageView);
                imageFrame.getStyleClass().add("chat-image-frame");
                bubble.getChildren().add(imageFrame);
            } catch (Exception ignored) {
            }
        }

        Label time = new Label(formatTime(m.getCreatedAt()));
        time.getStyleClass().add("chat-partner-status");
        time.getStyleClass().add("chat-message-time");
        if (fromMe) {
            HBox footer = new HBox(8);
            footer.setAlignment(Pos.CENTER_RIGHT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            if (text != null && !text.isBlank()) {
                Button editBtn = new Button("Edit");
                editBtn.getStyleClass().add("chat-message-action");
                editBtn.setOnAction(e -> onEditMessage(m));
                footer.getChildren().add(editBtn);
            }

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("chat-message-action");
            deleteBtn.getStyleClass().add("chat-message-delete");
            deleteBtn.setOnAction(e -> onDeleteMessage(m));
            footer.getChildren().add(deleteBtn);
            footer.getChildren().add(time);
            bubble.getChildren().add(footer);
        } else {
            bubble.getChildren().add(time);
        }
        bubble.setAlignment(fromMe ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

        HBox container = new HBox();
        container.getChildren().add(bubble);
        container.setAlignment(fromMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return container;
    }

    private VBox buildNoMessagesCard() {
        VBox card = new VBox(8);
        card.getStyleClass().add("chat-empty-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("No messages yet");
        title.getStyleClass().add("chat-empty-title");

        Label body = new Label("Send the first message to get this conversation started.");
        body.getStyleClass().add("chat-empty-subtitle");
        body.setWrapText(true);

        card.getChildren().addAll(title, body);
        return card;
    }

    private void showEmptyConversationState() {
        if (messagesVBox == null) return;
        messagesVBox.getChildren().clear();

        VBox hero = new VBox(12);
        hero.getStyleClass().add("chat-empty-hero");
        hero.setAlignment(Pos.CENTER);
        hero.setFillWidth(true);

        Label badge = new Label("CHAT");
        badge.getStyleClass().add("chat-empty-badge");

        Label title = new Label("Start a conversation");
        title.getStyleClass().add("chat-empty-title");

        Label body = new Label("Pick a friend from the left to open your messages, share an image, or continue an existing chat.");
        body.getStyleClass().add("chat-empty-subtitle");
        body.setWrapText(true);
        body.setMaxWidth(360);

        hero.getChildren().addAll(badge, title, body);
        messagesVBox.getChildren().add(hero);
    }

    private void onEditMessage(Message message) {
        if (message == null || message.getId() == null || message.getId().isBlank()) return;
        TextInputDialog dialog = new TextInputDialog(message.getContent());
        dialog.setTitle("Edit message");
        dialog.setHeaderText("Update your message");
        dialog.setContentText("Message");
        dialog.showAndWait().ifPresent(updated -> {
            if (updated == null || updated.isBlank() || updated.equals(message.getContent())) return;
            try {
                ApiService.getInstance().updateMessage(message.getId(), updated);
                loadMessages();
            } catch (ApiException ignored) {
            }
        });
    }

    private void onDeleteMessage(Message message) {
        if (message == null || message.getId() == null || message.getId().isBlank()) return;
        try {
            ApiService.getInstance().deleteMessage(message.getId());
            loadMessages();
        } catch (ApiException ignored) {
        }
    }



    @FXML
    protected void onSendMessage() {
        if (selectedPartnerId == null || messageField == null) return;
        String text = messageField.getText();
        boolean hasText = text != null && !text.isBlank();
        boolean hasImage = pendingImageBytes != null && pendingImageBytes.length > 0;
        if (!hasText && !hasImage) return;
        try {
            String imageUrl = null;
            if (hasImage) {
                String path = ImageSelectionSupport.buildStoragePath(ApiService.getInstance().getCurrentUserId(), pendingImageExtension);
                imageUrl = ApiService.getInstance().uploadToStorage("chat-images", path, pendingImageBytes, pendingImageContentType);
            }
            ApiService.getInstance().sendMessage(selectedPartnerId, hasText ? text.trim() : "", imageUrl);
            messageField.clear();
            clearPendingImage();
            loadMessages();
        } catch (ApiException ignored) {}
    }

    @FXML
    protected void onAttachImage() {
        Stage stage = messagesVBox != null && messagesVBox.getScene() != null
            ? (Stage) messagesVBox.getScene().getWindow()
            : null;
        ImageSelectionSupport.SelectedImage selected = ImageSelectionSupport.chooseImage(stage, "Select Chat Image");
        if (selected == null) return;

        pendingImageBytes = selected.getBytes();
        pendingImageExtension = selected.getExtension();
        pendingImageContentType = selected.getContentType();
        if (messagePreviewImage != null) messagePreviewImage.setImage(selected.getPreview());
        if (messagePreviewLabel != null) messagePreviewLabel.setText(selected.getFileName());
        if (messagePreviewBox != null) {
            messagePreviewBox.setManaged(true);
            messagePreviewBox.setVisible(true);
        }
    }

    @FXML
    protected void onRemoveImage() {
        clearPendingImage();
    }

    private void clearPendingImage() {
        pendingImageBytes = null;
        pendingImageExtension = "png";
        pendingImageContentType = "image/png";
        if (messagePreviewImage != null) messagePreviewImage.setImage(null);
        if (messagePreviewLabel != null) messagePreviewLabel.setText("");
        if (messagePreviewBox != null) {
            messagePreviewBox.setManaged(false);
            messagePreviewBox.setVisible(false);
        }
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
