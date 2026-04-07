package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.CommunityService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateRoomModalController implements Initializable {

    @FXML private BorderPane modalRoot;
    @FXML private TextField communityNameField;
    @FXML private TextArea communityDescriptionField;
    @FXML private Button createCommunityButton;
    @FXML private Button cancelButton;

    private CommunityService communityService = CommunityService.getInstance();
    private Runnable onRoomCreated;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cancelButton != null) {
            cancelButton.setOnAction(event -> closeModal());
        }
    }

    @FXML
    protected void onCreateRoomConfirm() {
        String roomName = communityNameField.getText().trim();
        String description = communityDescriptionField.getText().trim();

        if (roomName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a room name.");
            return;
        }

        if (description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a description.");
            return;
        }

        // Create the room asynchronously
        new Thread(() -> {
            try {
                // Get current user ID and profile from AuthService
                String currentUserId = com.campasian.service.ApiService.getInstance().getCurrentUserId();
                com.campasian.model.UserProfile currentProfile = com.campasian.service.AuthService.getInstance().getCurrentUserProfile();
                
                if (currentUserId == null || currentProfile == null) {
                    javafx.application.Platform.runLater(() -> 
                        showAlert(Alert.AlertType.ERROR, "Error", "User profile not found")
                    );
                    return;
                }
                
                communityService.createCustomRoom(
                    currentUserId,
                    currentProfile.getUniversityName(),
                    roomName,
                    description,
                    1  // Initial member count
                );
                javafx.application.Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Room created successfully!");
                    if (onRoomCreated != null) {
                        onRoomCreated.run();
                    }
                    closeModal();
                });
            } catch (ApiException e) {
                javafx.application.Platform.runLater(() -> 
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to create room: " + e.getMessage())
                );
            }
        }, "create-room").start();
    }

    @FXML
    protected void onCancel() {
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) modalRoot.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setOnRoomCreated(Runnable callback) {
        this.onRoomCreated = callback;
    }
}
