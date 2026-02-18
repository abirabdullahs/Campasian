package com.abir.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.shape.Circle;
import com.abir.demo.utils.FirebaseManager;
import com.abir.demo.utils.SceneManager;

import java.io.IOException;

/**
 * Profile Controller - User profile management
 */
public class ProfileController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label universityLabel;

    @FXML
    private TextArea bioTextArea;

    @FXML
    private Label followersLabel;

    @FXML
    private Label followingLabel;

    @FXML
    private Button editButton;

    @FXML
    private Button saveButton;

    private String currentUserId = "user123";
    private boolean isEditing = false;

    @FXML
    public void initialize() {
        loadUserProfile();
        setupClickHandlers();
    }

    private void loadUserProfile() {
        // In real app, fetch from Firebase
        userNameLabel.setText("Abir Ahmed");
        emailLabel.setText("abir@university.edu");
        universityLabel.setText("State University");
        bioTextArea.setText("Computer Science student • Tech enthusiast • Love coding");
        followersLabel.setText("Followers: 342");
        followingLabel.setText("Following: 125");

        bioTextArea.setEditable(false);
    }

    @FXML
    private void handleEdit() {
        isEditing = !isEditing;
        bioTextArea.setEditable(isEditing);
        
        if (isEditing) {
            editButton.setText("Cancel");
            saveButton.setVisible(true);
        } else {
            editButton.setText("Edit Profile");
            saveButton.setVisible(false);
        }
    }

    @FXML
    private void handleSave() {
        String newBio = bioTextArea.getText();
        // Update in Firebase
        showAlert("Success", "Profile updated successfully!");
        isEditing = false;
        editButton.setText("Edit Profile");
        saveButton.setVisible(false);
        bioTextArea.setEditable(false);
    }

    @FXML
    private void goToDashboard() throws IOException {
        SceneManager.switchScene("dashboard.fxml");
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
    private void goToMessages() throws IOException {
        SceneManager.switchScene("messages.fxml");
    }

    @FXML
    private void logout() throws IOException {
        SceneManager.switchScene("Login.fxml");
    }

    private void setupClickHandlers() {
        editButton.setOnAction(event -> handleEdit());
        saveButton.setOnAction(event -> handleSave());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
