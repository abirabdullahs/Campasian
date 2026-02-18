package com.abir.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import com.abir.demo.utils.FirebaseManager;
import com.abir.demo.utils.SceneManager;

import java.io.IOException;

/**
 * Messages Controller - Direct messaging system
 */
public class MessagesController {

    @FXML
    private ListView<String> conversationsList;

    @FXML
    private VBox chatArea;

    @FXML
    private TextArea messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Label selectedUserLabel;

    private String currentUserId = "user123";
    private String selectedUserId = null;

    @FXML
    public void initialize() {
        loadConversations();
        setupMessageHandlers();
    }

    private void loadConversations() {
        conversationsList.getItems().addAll(
            "Alice Johnson",
            "Bob Smith",
            "Carol White",
            "David Brown",
            "Emma Davis"
        );

        conversationsList.setOnMouseClicked(event -> {
            String selected = conversationsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectConversation(selected);
            }
        });
    }

    private void selectConversation(String userName) {
        selectedUserLabel.setText("Chatting with " + userName);
        selectedUserId = "user_" + userName.toLowerCase().replace(" ", "_");
        chatArea.getChildren().clear();
        loadMessages(userName);
    }

    private void loadMessages(String userName) {
        // Load sample messages
        addMessageBubble("Hey! How are you?", true);
        addMessageBubble("I'm doing great! Just finished a project.", false);
        addMessageBubble("That's awesome! Want to grab coffee?", true);
        addMessageBubble("Sure! When are you free?", false);
    }

    private void addMessageBubble(String message, boolean isFromCurrentUser) {
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(5));

        VBox bubble = new VBox();
        bubble.setPadding(new Insets(10));
        bubble.setStyle(
            isFromCurrentUser ?
            "-fx-background-color: linear-gradient(to right, #38bdf8, #a855f7); -fx-background-radius: 10;" :
            "-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10;"
        );

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        messageLabel.setWrapText(true);

        bubble.getChildren().add(messageLabel);

        if (isFromCurrentUser) {
            messageBox.setStyle("-fx-alignment: CENTER_RIGHT;");
            messageBox.getChildren().add(bubble);
        } else {
            messageBox.setStyle("-fx-alignment: CENTER_LEFT;");
            messageBox.getChildren().add(bubble);
        }

        chatArea.getChildren().add(messageBox);
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText();

        if (message.isEmpty() || selectedUserId == null) {
            showAlert("Error", "Please select a user and write a message!");
            return;
        }

        // Add to UI
        addMessageBubble(message, true);

        // Save to Firebase
        boolean success = FirebaseManager.sendMessage(currentUserId, selectedUserId, message);

        if (success) {
            messageInput.clear();
        } else {
            showAlert("Error", "Failed to send message. Try again.");
        }
    }

    private void setupMessageHandlers() {
        sendButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #38bdf8, #a855f7);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10px 30px;" +
            "-fx-background-radius: 20;"
        );
        sendButton.setOnAction(event -> sendMessage());
    }

    @FXML
    private void goToDashboard() throws IOException {
        SceneManager.switchScene("dashboard.fxml");
    }

    @FXML
    private void goToProfile() throws IOException {
        SceneManager.switchScene("profile.fxml");
    }

    @FXML
    private void goToBrowseUsers() throws IOException {
        SceneManager.switchScene("browseusers.fxml");
    }

    @FXML
    private void goToEvents() throws IOException {
        SceneManager.switchScene("events.fxml");
    }

    @FXML
    private void goToClubs() throws IOException {
        SceneManager.switchScene("clubs.fxml");
    }

    @FXML
    private void logout() throws IOException {
        SceneManager.switchScene("Login.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
