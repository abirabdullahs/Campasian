package com.campasian.controller;

import com.campasian.model.Message;
import com.campasian.model.CallRecord;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.service.BrowserCallBridgeService;
import com.campasian.service.SupabaseRealtimeService;
import com.campasian.util.ImageSelectionSupport;
import com.campasian.view.NavigationContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
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
    @FXML private Button audioCallBtn;
    @FXML private VBox chatComposer;
    @FXML private Button attachImageBtn;
    @FXML private Button sendMessageBtn;
    @FXML private VBox messagePreviewBox;
    @FXML private ImageView messagePreviewImage;
    @FXML private Label messagePreviewLabel;
    @FXML private VBox callOverlay;
    @FXML private Label callOverlayTitle;
    @FXML private Label callOverlaySubtitle;
    @FXML private Button callAcceptBtn;
    @FXML private Button callRejectBtn;
    @FXML private Button callCancelBtn;
    @FXML private ToggleButton muteCallBtn;
    @FXML private Button endCallBtn;

    private byte[] pendingImageBytes;
    private String pendingImageExtension = "png";
    private String pendingImageContentType = "image/png";

    private String selectedPartnerId;
    private String selectedPartnerName;
    private CallRecord activeCall;
    private String activeCallRole;
    private Timeline callTimerTimeline;
    private long callDurationSeconds;
    private final SupabaseRealtimeService realtimeService = new SupabaseRealtimeService();
    private final ScheduledExecutorService pollExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "chat-poll");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideCallOverlay();
        updateComposerState(false);
        showEmptyConversationState();
        loadChatPartners();
        pollExecutor.scheduleAtFixedRate(this::pollMessages, 2, 2, TimeUnit.SECONDS);
        String partnerId = NavigationContext.getChatPartnerUserId();
        String partnerName = NavigationContext.getChatPartnerName();
        if (partnerId != null && !partnerId.isBlank()) {
            NavigationContext.clearChatPartner();
            selectPartner(partnerId, partnerName != null ? partnerName : "Chat");
        }
        realtimeService.subscribeToCalls(
            ApiService.getInstance().getCurrentUserId(),
            ApiService.getInstance().getAccessToken(),
            this::handleCallChange
        );
    }

    private void loadChatPartners() {
        if (friendsList == null) return;
        friendsList.getChildren().clear();
        new Thread(() -> {
            try {
                List<UserProfile> partners = ApiService.getInstance().getChatPartners();
                Platform.runLater(() -> {
                    if (friendsList == null) return;
                    friendsList.getChildren().clear();
                    for (UserProfile p : partners) {
                        Label lbl = new Label(p.getFullName() != null ? p.getFullName() : "Unknown");
                        lbl.getStyleClass().add("chat-friend-item");
                        lbl.setWrapText(true);
                        lbl.setCursor(javafx.scene.Cursor.HAND);
                        lbl.setMaxWidth(Double.MAX_VALUE);
                        lbl.setAlignment(Pos.CENTER_LEFT);
                        lbl.setUserData(p.getId());
                        lbl.setOnMouseClicked(e -> selectPartner(p.getId(), p.getFullName()));
                        friendsList.getChildren().add(lbl);
                    }
                    if (partners.isEmpty()) {
                        Label empty = new Label("No conversations yet. Start chatting with someone.");
                        empty.setWrapText(true);
                        friendsList.getChildren().add(empty);
                    }
                    refreshFriendSelectionStyles();
                });
            } catch (ApiException ignored) {}
        }).start();
    }

    private void selectPartner(String partnerId, String name) {
        selectedPartnerId = partnerId;
        selectedPartnerName = name;
        Platform.runLater(() -> {
            updateComposerState(selectedPartnerId != null && !selectedPartnerId.isBlank());
            if (chatPartnerLabel != null) chatPartnerLabel.setText(name != null ? name : "Chat");
            refreshFriendSelectionStyles();
            loadMessages();
        });
    }

    private void refreshFriendSelectionStyles() {
        if (friendsList == null) return;
        for (Node node : friendsList.getChildren()) {
            if (!(node instanceof Label lbl)) continue;
            Object userId = lbl.getUserData();
            if (userId == null) continue;
            if (selectedPartnerId != null && selectedPartnerId.equals(userId.toString())) {
                if (!lbl.getStyleClass().contains("active")) {
                    lbl.getStyleClass().add("active");
                }
            } else {
                lbl.getStyleClass().remove("active");
            }
        }
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
                List<CallRecord> calls = ApiService.getInstance().getCallsBetweenUsers(
                    ApiService.getInstance().getCurrentUserId(), 
                    selectedPartnerId
                );
                
                Platform.runLater(() -> {
                    if (messagesVBox == null) return;
                    messagesVBox.getChildren().clear();
                    String currentId = ApiService.getInstance().getCurrentUserId();
                    
                    // Merge and sort messages and calls by timestamp
                    java.util.List<Object> items = new java.util.ArrayList<>();
                    items.addAll(msgs);
                    items.addAll(calls);
                    
                    items.sort((a, b) -> {
                        String timeA = a instanceof Message ? ((Message) a).getCreatedAt() : ((CallRecord) a).getCreatedAt();
                        String timeB = b instanceof Message ? ((Message) b).getCreatedAt() : ((CallRecord) b).getCreatedAt();
                        if (timeA == null || timeB == null) return 0;
                        return timeA.compareTo(timeB);
                    });
                    
                    for (Object item : items) {
                        if (item instanceof Message) {
                            Message m = (Message) item;
                            boolean fromMe = currentId != null && currentId.equals(m.getSenderId());
                            HBox bubble = buildMessageBubble(m, fromMe);
                            messagesVBox.getChildren().add(bubble);
                        } else if (item instanceof CallRecord) {
                            CallRecord call = (CallRecord) item;
                            VBox callCard = buildCallCard(call, currentId);
                            messagesVBox.getChildren().add(callCard);
                        }
                    }
                    
                    if (msgs.isEmpty() && calls.isEmpty()) {
                        messagesVBox.getChildren().add(buildNoMessagesCard());
                    }
                    scrollToBottom();
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    showChatError("Failed to load messages: " + (e.getMessage() != null ? e.getMessage() : "Network error"));
                });
            }
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

    private VBox buildCallCard(CallRecord call, String currentUserId) {
        VBox card = new VBox(6);
        card.getStyleClass().add("chat-call-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 16, 12, 16));
        
        // Call icon and type
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER);
        
        String callType = call.getCallerId() != null && call.getCallerId().equals(currentUserId) ? 
            "📞 Outgoing Call" : "📞 Incoming Call";
        Label typeLabel = new Label(callType);
        typeLabel.getStyleClass().add("chat-call-type");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0084ff;");
        
        // Status badge
        Label statusLabel = new Label("Completed");
        statusLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #65676b;");
        
        headerBox.getChildren().addAll(typeLabel, statusLabel);
        card.getChildren().add(headerBox);
        
        // Timestamp
        Label timeLabel = new Label(formatTime(call.getCreatedAt()));
        timeLabel.getStyleClass().add("chat-call-time");
        timeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #65676b;");
        card.getChildren().add(timeLabel);
        
        return card;
    }

    private void showEmptyConversationState() {
        if (messagesVBox == null) return;
        messagesVBox.getChildren().clear();
        updateComposerState(false);

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
        TextInputDialog dialog = new TextInputDialog(message.getContent() != null ? message.getContent() : "");
        dialog.setTitle("Edit message");
        dialog.setHeaderText("Update your message");
        dialog.setContentText("Message");
        dialog.showAndWait().ifPresent(updated -> {
            if (updated == null || updated.isBlank()) {
                showChatError("Message cannot be empty.");
                return;
            }
            if (updated.equals(message.getContent())) {
                return; // No changes
            }
            new Thread(() -> {
                try {
                    ApiService.getInstance().updateMessage(message.getId(), updated.trim());
                    Thread.sleep(500); // Wait for database to sync
                    Platform.runLater(() -> {
                        loadMessages(); // Reload from database to get updated message
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Message Updated");
                        alert.setContentText("Your message has been updated.");
                        alert.showAndWait();
                    });
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } catch (ApiException e) {
                    Platform.runLater(() -> showChatError("Message update failed: " + (e.getMessage() != null ? e.getMessage() : "Check RLS policies")));
                }
            }).start();
        });
    }

    private void onDeleteMessage(Message message) {
        if (message == null || message.getId() == null || message.getId().isBlank()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Message");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This message will be permanently deleted.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                new Thread(() -> {
                    try {
                        ApiService.getInstance().deleteMessage(message.getId());
                        Thread.sleep(500); // Wait for database to sync
                        Platform.runLater(() -> {
                            loadMessages();
                        });
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    } catch (ApiException e) {
                        Platform.runLater(() -> showChatError("Delete failed: " + (e.getMessage() != null ? e.getMessage() : "Check RLS policies")));
                    }
                }).start();
            }
        });
    }

    @FXML
    protected void onStartAudioCall() {
        if (selectedPartnerId == null || selectedPartnerId.isBlank()) return;
        try {
            activeCall = ApiService.getInstance().createPendingCall(selectedPartnerId);
            activeCallRole = "caller";
            showOutgoingCallState();
        } catch (ApiException e) {
            showChatError("Unable to start the audio call.");
        }
    }

    @FXML
    protected void onAcceptCall() {
        if (activeCall == null) return;
        try {
            ApiService.getInstance().updateCallStatus(activeCall.getId(), "accepted", "receiver_id");
            activeCall.setStatus("accepted");
            startActiveCallUi();
            launchBrowserCall();
        } catch (ApiException e) {
            showChatError("Unable to accept the call.");
        }
    }

    @FXML
    protected void onRejectCall() {
        if (activeCall == null) return;
        try {
            String userColumn = "receiver".equals(activeCallRole) ? "receiver_id" : "caller_id";
            ApiService.getInstance().updateCallStatus(activeCall.getId(), "rejected", userColumn);
        } catch (ApiException ignored) {
        }
        teardownCallUi();
    }

    @FXML
    protected void onCancelCall() {
        if (activeCall == null) return;
        try {
            ApiService.getInstance().updateCallStatus(activeCall.getId(), "ended", "caller_id");
        } catch (ApiException ignored) {
        }
        teardownCallUi();
    }

    @FXML
    protected void onMuteToggle() {
        if (activeCall == null || muteCallBtn == null) return;
        BrowserCallBridgeService.getInstance().updateMute(activeCall.getId(), muteCallBtn.isSelected());
        updateActiveCallText(muteCallBtn.isSelected() ? "Microphone muted" : "Audio call in progress");
    }

    @FXML
    protected void onEndCall() {
        if (activeCall == null) return;
        try {
            ApiService.getInstance().updateCallStatus(activeCall.getId(), "ended");
        } catch (ApiException ignored) {
        }
        BrowserCallBridgeService.getInstance().requestEnd(activeCall.getId());
        teardownCallUi();
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
                try {
                    String path = ImageSelectionSupport.buildStoragePath(ApiService.getInstance().getCurrentUserId(), pendingImageExtension);
                    imageUrl = ApiService.getInstance().uploadToStorage("chat-images", path, pendingImageBytes, pendingImageContentType);
                } catch (ApiException e) {
                    showChatError("Image upload failed: " + (e.getMessage() != null ? e.getMessage() : "Storage error"));
                    return;
                }
            }
            try {
                ApiService.getInstance().sendMessage(selectedPartnerId, hasText ? text.trim() : "", imageUrl);
            } catch (ApiException e) {
                showChatError("Message send failed: " + (e.getMessage() != null ? e.getMessage() : "Network error"));
                return;
            }
            messageField.clear();
            clearPendingImage();
            loadMessages();
        } catch (Exception e) {
            showChatError("Unexpected error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @FXML
    protected void onAttachImage() {
        if (selectedPartnerId == null || selectedPartnerId.isBlank()) return;
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

    private void updateComposerState(boolean enabled) {
        if (chatComposer != null) {
            chatComposer.setDisable(!enabled);
            chatComposer.setOpacity(enabled ? 1.0 : 0.52);
        }
        if (messageField != null) {
            messageField.setDisable(!enabled);
            messageField.setPromptText(enabled ? "Type a message and press Enter..." : "Select a friend to start chatting");
        }
        if (attachImageBtn != null) attachImageBtn.setDisable(!enabled);
        if (sendMessageBtn != null) sendMessageBtn.setDisable(!enabled);
        if (!enabled) clearPendingImage();
    }

    private void showChatError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Chat action failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleCallChange(com.google.gson.JsonObject payload) {
        Platform.runLater(() -> {
            com.google.gson.JsonObject data = payload != null && payload.has("data") && payload.get("data").isJsonObject()
                ? payload.getAsJsonObject("data")
                : payload;
            com.google.gson.JsonObject record = data != null && data.has("record") && data.get("record").isJsonObject()
                ? data.getAsJsonObject("record")
                : data != null && data.has("new") && data.get("new").isJsonObject()
                    ? data.getAsJsonObject("new")
                    : data != null && data.has("payload") && data.get("payload").isJsonObject()
                        ? data.getAsJsonObject("payload")
                        : null;
            if (record == null) return;

            CallRecord call = new CallRecord();
            call.setId(asString(record, "id"));
            call.setCallerId(asString(record, "caller_id"));
            call.setReceiverId(asString(record, "receiver_id"));
            call.setStatus(asString(record, "status"));
            call.setChannelName(asString(record, "channel_name"));
            call.setCreatedAt(asString(record, "created_at"));

            String currentUserId = ApiService.getInstance().getCurrentUserId();
            if (currentUserId == null || currentUserId.isBlank()) return;
            if (!currentUserId.equals(call.getCallerId()) && !currentUserId.equals(call.getReceiverId())) return;

            if ("pending".equals(call.getStatus()) && currentUserId.equals(call.getReceiverId())) {
                activeCall = call;
                activeCallRole = "receiver";
                showIncomingCallState();
                return;
            }
            if (activeCall == null || !call.getId().equals(activeCall.getId())) return;

            activeCall = call;
            if ("accepted".equals(call.getStatus())) {
                if ("active".equals(activeCallRole)) return;
                startActiveCallUi();
                launchBrowserCall();
            } else if ("rejected".equals(call.getStatus()) || "ended".equals(call.getStatus())) {
                BrowserCallBridgeService.getInstance().requestEnd(call.getId());
                teardownCallUi();
            }
        });
    }

    private void showOutgoingCallState() {
        if (callOverlay == null) return;
        callOverlay.setVisible(true);
        callOverlay.setManaged(true);
        if (callOverlayTitle != null) callOverlayTitle.setText(selectedPartnerName != null ? selectedPartnerName : "Calling friend");
        if (callOverlaySubtitle != null) callOverlaySubtitle.setText("Calling...");
        setCallButtons(false, false, true, false, false);
    }

    private void showIncomingCallState() {
        if (callOverlay == null || activeCall == null) return;
        callOverlay.setVisible(true);
        callOverlay.setManaged(true);
        if (callOverlayTitle != null) {
            String title = selectedPartnerId != null && selectedPartnerId.equals(activeCall.getCallerId()) && selectedPartnerName != null
                ? selectedPartnerName
                : "Incoming audio call";
            callOverlayTitle.setText(title);
        }
        if (callOverlaySubtitle != null) callOverlaySubtitle.setText("Incoming call");
        setCallButtons(true, true, false, false, false);
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    private void startActiveCallUi() {
        if (activeCall == null) return;
        activeCallRole = "active";
        callDurationSeconds = 0;
        if (callOverlay == null) return;
        callOverlay.setVisible(true);
        callOverlay.setManaged(true);
        if (callOverlayTitle != null) callOverlayTitle.setText(selectedPartnerName != null ? selectedPartnerName : "Audio call");
        updateActiveCallText("Audio call in progress");
        setCallButtons(false, false, false, true, true);
        if (muteCallBtn != null) muteCallBtn.setSelected(false);
        if (callTimerTimeline != null) callTimerTimeline.stop();
        callTimerTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            callDurationSeconds++;
            updateActiveCallText("Connected • " + formatCallDuration(callDurationSeconds));
        }));
        callTimerTimeline.setCycleCount(Timeline.INDEFINITE);
        callTimerTimeline.play();
        BrowserCallBridgeService.getInstance().registerSession(activeCall.getId(), this::handleBrowserCallEvent);
    }

    private void setCallButtons(boolean accept, boolean reject, boolean cancel, boolean mute, boolean end) {
        if (callAcceptBtn != null) { callAcceptBtn.setVisible(accept); callAcceptBtn.setManaged(accept); }
        if (callRejectBtn != null) { callRejectBtn.setVisible(reject); callRejectBtn.setManaged(reject); }
        if (callCancelBtn != null) { callCancelBtn.setVisible(cancel); callCancelBtn.setManaged(cancel); }
        if (muteCallBtn != null) { muteCallBtn.setVisible(mute); muteCallBtn.setManaged(mute); }
        if (endCallBtn != null) { endCallBtn.setVisible(end); endCallBtn.setManaged(end); }
    }

    private void updateActiveCallText(String text) {
        if (callOverlaySubtitle != null) callOverlaySubtitle.setText(text);
    }

    private void launchBrowserCall() {
        if (activeCall == null) return;
        String currentUserId = ApiService.getInstance().getCurrentUserId();
        if (currentUserId == null || currentUserId.isBlank()) return;
        try {
            BrowserCallBridgeService bridge = BrowserCallBridgeService.getInstance();
            bridge.registerSession(activeCall.getId(), this::handleBrowserCallEvent);
            bridge.launchBrowser(bridge.buildLaunchUri(activeCall.getId(), activeCall.getChannelName(), currentUserId, selectedPartnerName));
        } catch (Exception e) {
            showChatError("Unable to launch the browser call page.");
        }
    }

    private void handleBrowserCallEvent(String type) {
        Platform.runLater(() -> {
            if (type == null) return;
            switch (type) {
                case "user-joined" -> updateActiveCallText("Connected • friend joined");
                case "user-offline" -> updateActiveCallText("Connected • friend left");
                case "error" -> updateActiveCallText("Browser call page reported an error");
                case "ended" -> {
                    if (activeCall != null) {
                        try {
                            ApiService.getInstance().updateCallStatus(activeCall.getId(), "ended");
                        } catch (ApiException ignored) {
                        }
                    }
                    teardownCallUi();
                }
                default -> { }
            }
        });
    }

    private void teardownCallUi() {
        if (callTimerTimeline != null) {
            callTimerTimeline.stop();
            callTimerTimeline = null;
        }
        if (activeCall != null) {
            BrowserCallBridgeService.getInstance().requestEnd(activeCall.getId());
            BrowserCallBridgeService.getInstance().clearSession(activeCall.getId());
        }
        hideCallOverlay();
        activeCall = null;
        activeCallRole = null;
        callDurationSeconds = 0;
    }

    private void hideCallOverlay() {
        if (callOverlay != null) {
            callOverlay.setVisible(false);
            callOverlay.setManaged(false);
        }
        setCallButtons(false, false, false, false, false);
    }

    private static String asString(com.google.gson.JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key).isJsonNull()) return null;
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String formatCallDuration(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
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
