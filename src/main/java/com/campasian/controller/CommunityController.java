package com.campasian.controller;

import com.campasian.model.CommunityMessage;
import com.campasian.model.CommunityRoom;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.service.AuthService;
import com.campasian.service.CommunityService;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for university community rooms.
 * Uses ObservableList so the selected room and message timeline stay in sync with the UI.
 */
public class CommunityController implements Initializable {

    @FXML private ListView<CommunityRoom> communityListView;
    @FXML private ListView<CommunityMessage> messageListView;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label verificationLabel;
    @FXML private Label memberCountLabel;
    @FXML private Label communityHintLabel;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    private final CommunityService communityService = CommunityService.getInstance();
    private final ObservableList<CommunityRoom> availableRooms = FXCollections.observableArrayList();
    private final ObservableList<CommunityMessage> visibleMessages = FXCollections.observableArrayList();

    private UserProfile currentUserProfile;
    private String currentUserId;
    private CommunityRoom selectedRoom;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (communityListView != null) {
            communityListView.setItems(availableRooms);
            communityListView.setCellFactory(list -> new CommunityRoomCell());
            communityListView.getSelectionModel().selectedItemProperty().addListener((obs, oldRoom, newRoom) -> {
                if (newRoom != null) {
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

        loadCommunityData();
    }

    private void loadCommunityData() {
        new Thread(() -> {
            try {
                currentUserId = ApiService.getInstance().getCurrentUserId();
                currentUserProfile = AuthService.getInstance().getCurrentUserProfile();
                List<UserProfile> allProfiles = ApiService.getInstance().getAllProfiles();
                if (currentUserProfile == null) {
                    allProfiles = Collections.emptyList();
                }
                UserProfile profile = currentUserProfile;
                List<UserProfile> finalAllProfiles = allProfiles;
                Platform.runLater(() -> populateRooms(profile, finalAllProfiles));
            } catch (ApiException e) {
                Platform.runLater(this::showLoadError);
            }
        }, "community-load").start();
    }

    private void populateRooms(UserProfile profile, List<UserProfile> allProfiles) {
        if (profile == null) {
            showLoadError();
            return;
        }

        communityService.ensureAutoJoin(
            profile.getId(),
            null,
            profile.getUniversityName(),
            profile.getDepartment(),
            profile.getEinNumber(),
            profile.getFullName()
        );

        availableRooms.setAll(communityService.buildCommunities(profile, allProfiles));
        if (!availableRooms.isEmpty()) {
            communityListView.getSelectionModel().selectFirst();
        } else {
            titleLabel.setText("Community");
            subtitleLabel.setText("No verified student communities are available yet.");
            verificationLabel.setText("Verification unavailable");
            memberCountLabel.setText("0 students");
            communityHintLabel.setText("Students are grouped by university name and ID/email rules.");
        }
    }

    private void onRoomSelected(CommunityRoom room) {
        selectedRoom = room;
        titleLabel.setText(room.getName());
        subtitleLabel.setText(room.getDescription());
        verificationLabel.setText(room.isVerified() ? "Verified university room" : "Verification pending");
        memberCountLabel.setText(room.getMemberCount() + (room.getMemberCount() == 1 ? " student" : " students"));
        communityHintLabel.setText(room.isAutoJoined()
            ? "Auto-joined from signup using university identity rules."
            : "Joined from your verified university and department profile.");

        visibleMessages.setAll(communityService.getMessages(room.getId()));
        scrollMessagesToBottom();

        if (sendButton != null) {
            sendButton.setDisable(false);
        }
    }

    @FXML
    protected void onSendClick() {
        if (selectedRoom == null || currentUserProfile == null || messageField == null) return;
        String content = messageField.getText();
        if (content == null || content.isBlank()) return;

        CommunityMessage sent = communityService.sendMessage(
            selectedRoom.getId(),
            currentUserId,
            currentUserProfile.getFullName(),
            content.trim()
        );
        visibleMessages.add(sent);
        messageField.clear();
        scrollMessagesToBottom();
    }

    private void showLoadError() {
        availableRooms.clear();
        visibleMessages.clear();
        titleLabel.setText("Community");
        subtitleLabel.setText("Unable to load university communities right now.");
        verificationLabel.setText("Verification unavailable");
        memberCountLabel.setText("0 students");
        communityHintLabel.setText("This screen expects a signed-in student profile and can later be backed by Supabase realtime.");
        if (sendButton != null) {
            sendButton.setDisable(true);
        }
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

            Label badge = new Label(room.isAutoJoined() ? "Auto" : room.getScopeLabel());
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

            Label sender = new Label(message.getSenderName());
            sender.getStyleClass().add(fromCurrentUser ? "community-message-sender-me" : "community-message-sender");

            Label content = new Label(message.getContent());
            content.setWrapText(true);
            content.setMaxWidth(420);
            content.getStyleClass().add(fromCurrentUser ? "community-bubble-me" : "community-bubble-other");

            Label time = new Label(formatTime(message.getCreatedAt()));
            time.getStyleClass().add("community-message-time");

            VBox bubble = new VBox(4, sender, content, time);
            bubble.setAlignment(fromCurrentUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(12);
            row.setPadding(new Insets(4, 0, 4, 0));
            row.setAlignment(fromCurrentUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            if (fromCurrentUser) {
                row.getChildren().addAll(spacer, bubble);
            } else {
                row.getChildren().addAll(bubble, spacer);
            }

            setText(null);
            setGraphic(row);
        }
    }
}
