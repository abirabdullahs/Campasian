package com.campasian.controller;

import com.campasian.model.CommunityMessage;
import com.campasian.model.CommunityRoom;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.service.AuthService;
import com.campasian.service.CommunityService;
import com.campasian.util.ImageSelectionSupport;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class CommunityController implements Initializable {

    private static final Duration REFRESH_INTERVAL = Duration.seconds(4);

    @FXML private BorderPane communityRoot;
    @FXML private SplitPane communitySplitPane;
    @FXML private VBox communityRightPanel;
    @FXML private Button panelToggleButton;
    @FXML private ListView<CommunityRoom> communityListView;
    @FXML private ListView<CommunityMessage> messageListView;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label verificationLabel;
    @FXML private Label memberCountLabel;
    @FXML private Label communityHintLabel;
    @FXML private TextField messageField;
    @FXML private Button attachImageBtn;
    @FXML private Button sendButton;
    @FXML private TextField communityNameField;
    @FXML private TextArea communityDescriptionField;
    @FXML private Label communityEditorLabel;
    @FXML private Button createCommunityButton;
    @FXML private Button saveCommunityButton;
    @FXML private Button deleteCommunityButton;

    private byte[] pendingImageBytes;
    private String pendingImageExtension = "png";
    private String pendingImageContentType = "image/png";

    private final CommunityService communityService = CommunityService.getInstance();
    private final ObservableList<CommunityRoom> availableRooms = FXCollections.observableArrayList();
    private final ObservableList<CommunityMessage> visibleMessages = FXCollections.observableArrayList();

    private UserProfile currentUserProfile;
    private String currentUserId;
    private CommunityRoom selectedRoom;
    private boolean rightPanelCollapsed;
    private boolean suppressSelectionHandler;
    private volatile boolean refreshInFlight;
    private Timeline refreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (communitySplitPane != null) {
            communitySplitPane.setDividerPositions(0.24, 0.78);
        }
        if (communityListView != null) {
            communityListView.setItems(availableRooms);
            communityListView.setCellFactory(list -> new CommunityRoomCell());
            communityListView.getSelectionModel().selectedItemProperty().addListener((obs, oldRoom, newRoom) -> {
                if (!suppressSelectionHandler && newRoom != null) {
                    onRoomSelected(newRoom);
                }
            });
        }
        if (messageListView != null) {
            messageListView.setItems(visibleMessages);
            messageListView.setCellFactory(list -> new CommunityMessageCell());
            messageListView.setPlaceholder(new Label("No messages yet. Start the conversation."));
        }
        if (messageField != null) {
            messageField.setOnAction(event -> onSendClick());
        }
        if (sendButton != null) {
            sendButton.setDisable(true);
        }
        if (communityRoot != null) {
            communityRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (oldScene != null && newScene == null) {
                    stopRealtimeRefresh();
                } else if (newScene != null) {
                    startRealtimeRefresh();
                }
            });
        }
        resetCommunityEditor();
        loadCommunityData();
    }

    private void loadCommunityData() {
        new Thread(() -> {
            try {
                currentUserId = ApiService.getInstance().getCurrentUserId();
                currentUserProfile = AuthService.getInstance().getCurrentUserProfile();
                List<UserProfile> allProfiles = currentUserProfile == null
                    ? Collections.emptyList()
                    : ApiService.getInstance().getAllProfiles();
                List<CommunityRoom> rooms = communityService.buildCommunities(currentUserProfile, allProfiles);
                Platform.runLater(() -> applyRooms(currentUserProfile, rooms, null));
            } catch (ApiException e) {
                Platform.runLater(this::showLoadError);
            }
        }, "community-load").start();
    }

    private void onRoomSelected(CommunityRoom room) {
        selectedRoom = room;
        renderRoomDetails(room, true);
        loadMessagesAsync(room, false);
    }

    @FXML
    protected void onSendClick() {
        if (selectedRoom == null || currentUserProfile == null || messageField == null) return;
        String content = messageField.getText();
        boolean hasText = content != null && !content.isBlank();
        boolean hasImage = pendingImageBytes != null && pendingImageBytes.length > 0;
        if (!hasText && !hasImage) return;

        try {
            String imageUrl = null;
            if (hasImage) {
                try {
                    String path = ImageSelectionSupport.buildStoragePath(currentUserId, pendingImageExtension);
                    imageUrl = ApiService.getInstance().uploadToStorage("community-images", path, pendingImageBytes, pendingImageContentType);
                } catch (ApiException e) {
                    // ignore for now
                    return;
                }
            }
            CommunityMessage sent = communityService.sendMessage(
                selectedRoom.getId(),
                currentUserId,
                currentUserProfile.getFullName(),
                hasText ? content.trim() : "",
                imageUrl
            );
            visibleMessages.add(sent);
            messageField.clear();
            pendingImageBytes = null;
            scrollMessagesToBottom();
        } catch (ApiException ignored) {
        }
    }

    @FXML
    protected void onCreateCommunityClick() {
        if (currentUserProfile == null || communityNameField == null || communityDescriptionField == null) return;
        String roomName = communityNameField.getText();
        String description = communityDescriptionField.getText();
        if (roomName == null || roomName.isBlank()) return;
        try {
            CommunityRoom room = communityService.createCustomRoom(
                currentUserId,
                currentUserProfile.getUniversityName(),
                roomName.trim(),
                description != null ? description.trim() : "",
                approximateCommunitySize()
            );
            reloadRooms(room.getId());
            resetCommunityEditor();
        } catch (ApiException ignored) {
        }
    }

    @FXML
    protected void onSaveCommunityClick() {
        if (selectedRoom == null || !communityService.canManage(selectedRoom, currentUserId)) return;
        try {
            CommunityRoom updated = communityService.updateCustomRoom(
                selectedRoom.getId(),
                currentUserId,
                communityNameField != null ? communityNameField.getText() : null,
                communityDescriptionField != null ? communityDescriptionField.getText() : null
            );
            if (updated != null) {
                selectedRoom = updated;
                reloadRooms(updated.getId());
            }
        } catch (ApiException ignored) {
        }
    }

    @FXML
    protected void onDeleteCommunityClick() {
        if (selectedRoom == null) return;
        try {
            if (communityService.deleteCustomRoom(selectedRoom.getId(), currentUserId)) {
                selectedRoom = null;
                resetCommunityEditor();
                visibleMessages.clear();
                reloadRooms(null);
            }
        } catch (ApiException ignored) {
        }
    }

    @FXML
    protected void onTogglePanelClick() {
        rightPanelCollapsed = !rightPanelCollapsed;
        if (communityRightPanel != null) {
            communityRightPanel.setManaged(!rightPanelCollapsed);
            communityRightPanel.setVisible(!rightPanelCollapsed);
        }
        if (communitySplitPane != null) {
            communitySplitPane.setDividerPositions(rightPanelCollapsed ? 0.98 : 0.78);
        }
        if (panelToggleButton != null) {
            panelToggleButton.setText(rightPanelCollapsed ? "Show Panel" : "Hide Panel");
        }
    }

    @FXML
    protected void onAttachImageClick() {
        Stage stage = messageField != null && messageField.getScene() != null
            ? (Stage) messageField.getScene().getWindow()
            : null;
        ImageSelectionSupport.SelectedImage selected = ImageSelectionSupport.chooseImage(stage, "Select Community Image");
        if (selected == null) return;

        pendingImageBytes = selected.getBytes();
        pendingImageExtension = selected.getExtension();
        pendingImageContentType = selected.getContentType();
    }

    private void applyRooms(UserProfile profile, List<CommunityRoom> rooms, String roomIdToSelect) {
        if (profile == null) {
            showLoadError();
            return;
        }

        suppressSelectionHandler = true;
        try {
            // Preserve the selected room ID - CRITICAL to maintain selection
            String selectedId = roomIdToSelect != null ? roomIdToSelect : (selectedRoom != null ? selectedRoom.getId() : null);
            
            // Check if the room list has actually changed (by ID and order)
            List<String> currentIds = availableRooms.stream().map(CommunityRoom::getId).toList();
            List<String> newIds = rooms.stream().map(CommunityRoom::getId).toList();
            boolean listChanged = !currentIds.equals(newIds);
            
            // Only update the list if it has actually changed
            if (listChanged) {
                availableRooms.setAll(rooms);
            }
            
            if (!availableRooms.isEmpty()) {
                // Find the room to display: prioritize selectedId, then current selectedRoom
                CommunityRoom roomToUse = selectedRoom;  // Keep current selection if possible
                
                // Only change selection if we need to (roomIdToSelect was explicitly provided)
                if (roomIdToSelect != null || selectedRoom == null) {
                    // Try to find the room by ID
                    if (selectedId != null) {
                        roomToUse = availableRooms.stream()
                            .filter(room -> room.getId().equals(selectedId))
                            .findFirst()
                            .orElse(null);
                    }
                    
                    // Only default to first room if we couldn't find the selected one
                    if (roomToUse == null && !availableRooms.isEmpty()) {
                        roomToUse = availableRooms.get(0);
                    }
                    
                    // Update selectedRoom only if we have a valid room
                    if (roomToUse != null) {
                        selectedRoom = roomToUse;
                    }
                } else if (selectedRoom != null) {
                    // Verify the current selectedRoom still exists in the list
                    roomToUse = availableRooms.stream()
                        .filter(room -> room.getId().equals(selectedRoom.getId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (roomToUse != null) {
                        selectedRoom = roomToUse;  // Update to the new instance if list changed
                    } else {
                        // Selected room was removed, pick the first one
                        selectedRoom = availableRooms.get(0);
                        roomToUse = selectedRoom;
                    }
                }
                
                // Update ListView selection only if needed
                if (communityListView != null && roomToUse != null) {
                    CommunityRoom currentlySelected = communityListView.getSelectionModel().getSelectedItem();
                    // Compare by ID since object instances may be different
                    boolean needsUpdate = currentlySelected == null || !Objects.equals(currentlySelected.getId(), roomToUse.getId());
                    if (needsUpdate) {
                        communityListView.getSelectionModel().select(roomToUse);
                        communityListView.scrollTo(roomToUse);
                    }
                }
                
                if (roomToUse != null) {
                    renderRoomDetails(roomToUse, false);
                }
            } else {
                selectedRoom = null;
                titleLabel.setText("Community");
                subtitleLabel.setText("No verified student communities are available yet.");
                verificationLabel.setText("Verification unavailable");
                memberCountLabel.setText("0 students");
                communityHintLabel.setText("Students are grouped by university name and ID/email rules.");
            }
            
            if (selectedRoom != null) {
                loadMessagesAsync(selectedRoom, false);
            }
        } finally {
            suppressSelectionHandler = false;
        }
    }

    private void renderRoomDetails(CommunityRoom room, boolean updateEditor) {
        if (room == null) return;
        titleLabel.setText(room.getName());
        subtitleLabel.setText(room.getDescription());
        verificationLabel.setText(room.isCustom() ? "Custom community" : (room.isVerified() ? "Verified university room" : "Verification pending"));
        memberCountLabel.setText(room.getMemberCount() + (room.getMemberCount() == 1 ? " student" : " students"));
        communityHintLabel.setText(room.isAutoJoined()
            ? "Auto-joined from signup using university identity rules."
            : room.isCustom()
                ? "Custom community created by a student. You can edit or delete your own rooms."
                : "Joined from your verified university and department profile.");
        if (sendButton != null) sendButton.setDisable(false);
        if (updateEditor) {
            populateCommunityEditor(room);
        }
    }

    private void loadMessagesAsync(CommunityRoom room, boolean preserveScrollPosition) {
        if (room == null) return;
        String roomId = room.getId();
        new Thread(() -> {
            try {
                List<CommunityMessage> messages = communityService.getMessages(roomId);
                Platform.runLater(() -> {
                    if (selectedRoom == null || !roomId.equals(selectedRoom.getId())) return;
                    boolean changed = messagesChanged(messages);
                    if (changed) {
                        visibleMessages.setAll(messages);
                        if (!preserveScrollPosition || roomId.equals(selectedRoom.getId())) {
                            scrollMessagesToBottom();
                        }
                    }
                });
            } catch (ApiException ignored) {
            }
        }, "community-messages-" + roomId).start();
    }

    private boolean messagesChanged(List<CommunityMessage> messages) {
        if (messages.size() != visibleMessages.size()) return true;
        for (int i = 0; i < messages.size(); i++) {
            CommunityMessage incoming = messages.get(i);
            CommunityMessage current = visibleMessages.get(i);
            if (!Objects.equals(incoming.getCreatedAt(), current.getCreatedAt())
                || !Objects.equals(incoming.getSenderId(), current.getSenderId())
                || !Objects.equals(incoming.getContent(), current.getContent())) {
                return true;
            }
        }
        return false;
    }

    private void populateCommunityEditor(CommunityRoom room) {
        if (communityNameField == null || communityDescriptionField == null) return;
        boolean canManage = communityService.canManage(room, currentUserId);
        if (room != null && room.isCustom()) {
            communityNameField.setText(room.getName());
            communityDescriptionField.setText(room.getDescription());
        } else if (room == null || !room.isCustom()) {
            communityNameField.clear();
            communityDescriptionField.clear();
        }
        communityNameField.setDisable(!canManage && room != null && room.isCustom());
        communityDescriptionField.setDisable(!canManage && room != null && room.isCustom());
        if (communityEditorLabel != null) {
            communityEditorLabel.setText(room != null && room.isCustom()
                ? (canManage ? "Edit your custom community" : "Custom community details")
                : "Create a new custom community");
        }
        if (saveCommunityButton != null) saveCommunityButton.setDisable(room == null || !canManage);
        if (deleteCommunityButton != null) deleteCommunityButton.setDisable(room == null || !canManage);
        if (createCommunityButton != null) createCommunityButton.setDisable(false);
    }

    private void resetCommunityEditor() {
        if (communityNameField != null) communityNameField.clear();
        if (communityDescriptionField != null) communityDescriptionField.clear();
        if (communityEditorLabel != null) communityEditorLabel.setText("Create a new custom community");
        if (saveCommunityButton != null) saveCommunityButton.setDisable(true);
        if (deleteCommunityButton != null) deleteCommunityButton.setDisable(true);
    }

    private int approximateCommunitySize() {
        return Math.max(availableRooms.stream().mapToInt(CommunityRoom::getMemberCount).max().orElse(1), 1);
    }

    private void reloadRooms(String roomIdToSelect) {
        if (currentUserProfile == null) return;
        new Thread(() -> {
            try {
                List<UserProfile> allProfiles = ApiService.getInstance().getAllProfiles();
                List<CommunityRoom> rooms = communityService.buildCommunities(currentUserProfile, allProfiles);
                Platform.runLater(() -> applyRooms(currentUserProfile, rooms, roomIdToSelect));
            } catch (ApiException e) {
                Platform.runLater(this::showLoadError);
            }
        }, "community-refresh").start();
    }

    private void startRealtimeRefresh() {
        if (refreshTimeline != null) return;
        refreshTimeline = new Timeline(new KeyFrame(REFRESH_INTERVAL, event -> refreshCommunityState()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopRealtimeRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
    }

    private void refreshCommunityState() {
        if (refreshInFlight || currentUserProfile == null || selectedRoom == null) return;
        refreshInFlight = true;
        String selectedId = selectedRoom.getId();  // Capture selected room ID immediately
        new Thread(() -> {
            try {
                List<UserProfile> allProfiles = ApiService.getInstance().getAllProfiles();
                List<CommunityRoom> rooms = communityService.buildCommunities(currentUserProfile, allProfiles);
                List<CommunityMessage> latestMessages = selectedId != null
                    ? communityService.getMessages(selectedId)
                    : List.of();
                Platform.runLater(() -> {
                    applyRooms(currentUserProfile, rooms, selectedId);
                    if (selectedRoom != null && selectedId.equals(selectedRoom.getId()) && messagesChanged(latestMessages)) {
                        visibleMessages.setAll(latestMessages);
                        scrollMessagesToBottom();
                    }
                    refreshInFlight = false;
                });
            } catch (ApiException e) {
                Platform.runLater(() -> refreshInFlight = false);
            }
        }, "community-live-refresh").start();
    }

    private void showLoadError() {
        availableRooms.clear();
        visibleMessages.clear();
        titleLabel.setText("Community");
        subtitleLabel.setText("Unable to load university communities right now.");
        verificationLabel.setText("Verification unavailable");
        memberCountLabel.setText("0 students");
        communityHintLabel.setText("This screen expects a signed-in student profile and can later be backed by Supabase realtime.");
        if (sendButton != null) sendButton.setDisable(true);
    }

    private void scrollMessagesToBottom() {
        if (messageListView == null || visibleMessages.isEmpty()) return;
        Platform.runLater(() -> messageListView.scrollTo(visibleMessages.size() - 1));
    }

    private static String formatTime(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            return OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ignored) {
            return iso;
        }
    }

    private final class CommunityRoomCell extends ListCell<CommunityRoom> {
        @Override
        protected void updateItem(CommunityRoom room, boolean empty) {
            super.updateItem(room, empty);
            if (empty || room == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label name = new Label(room.getName());
            name.getStyleClass().add("community-room-name");

            Label description = new Label(room.getDescription());
            description.getStyleClass().add("community-room-description");
            description.setWrapText(true);

            Label badge = new Label(room.isCustom() ? "Custom" : room.isAutoJoined() ? "Auto" : room.getScopeLabel());
            badge.getStyleClass().add("community-room-badge");

            Label count = new Label(room.getMemberCount() + " members");
            count.getStyleClass().add("community-room-meta");

            HBox metaRow = new HBox(8, badge, count);
            VBox content = new VBox(6, name, description, metaRow);
            content.getStyleClass().add("community-room-card");

            setText(null);
            setGraphic(content);
        }
    }

    private final class CommunityMessageCell extends ListCell<CommunityMessage> {
        @Override
        protected void updateItem(CommunityMessage message, boolean empty) {
            super.updateItem(message, empty);
            if (empty || message == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            boolean fromCurrentUser = currentUserId != null && currentUserId.equals(message.getSenderId());

            VBox bubble = new VBox(8);
            bubble.getStyleClass().add(fromCurrentUser ? "sent-bubble" : "received-bubble");
            bubble.setMaxWidth(360);

            String text = message.getContent() != null ? message.getContent().trim() : "";
            if (!text.isEmpty()) {
                Label content = new Label(text);
                content.setWrapText(true);
                content.setMaxWidth(320);
                content.getStyleClass().add("chat-message-text");
                bubble.getChildren().add(content);
            }

            if (message.getImageUrl() != null && !message.getImageUrl().isBlank()) {
                try {
                    ImageView imageView = new ImageView(new Image(message.getImageUrl(), true));
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

            Label time = new Label(formatTime(message.getCreatedAt()));
            time.getStyleClass().add("chat-partner-status");
            time.getStyleClass().add("chat-message-time");
            bubble.getChildren().add(time);

            bubble.setAlignment(fromCurrentUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

            HBox container = new HBox();
            container.getChildren().add(bubble);
            container.setAlignment(fromCurrentUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            setText(null);
            setGraphic(container);
        }
    }
}