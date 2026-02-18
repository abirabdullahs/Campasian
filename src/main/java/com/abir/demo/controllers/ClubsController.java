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
 * Clubs Controller - Manage clubs and groups
 */
public class ClubsController {

    @FXML
    private TextField clubNameField;

    @FXML
    private TextArea clubDescriptionArea;

    @FXML
    private TextField clubCategoryField;

    @FXML
    private VBox clubsVBox;

    @FXML
    private Button createClubButton;

    @FXML
    public void initialize() {
        loadClubs();

        createClubButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #38bdf8, #a855f7);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10px 30px;" +
            "-fx-background-radius: 20;"
        );

        createClubButton.setOnAction(event -> handleCreateClub());
    }

    private void loadClubs() {
        clubsVBox.getChildren().clear();

        // Sample clubs
        String[][] clubs = {
            {"Coding Club", "Learn and discuss programming", "Technology"},
            {"Debate Society", "Sharpen your argumentation skills", "Academic"},
            {"Photography Club", "Capture moments and share art", "Arts"},
            {"Anime Club", "Discuss and enjoy anime", "Entertainment"},
            {"Business Club", "Entrepreneurship and career development", "Career"}
        };

        for (String[] club : clubs) {
            addClubCard(club[0], club[1], club[2]);
        }
    }

    private void addClubCard(String name, String description, String category) {
        VBox clubCard = new VBox(10);
        clubCard.setPadding(new Insets(15));
        clubCard.setStyle(
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 10;" +
            "-fx-background-color: rgba(255,255,255,0.05);"
        );

        // Club Header
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        Label categoryLabel = new Label("📁 " + category);
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8);");
        descLabel.setWrapText(true);

        Label memberCountLabel = new Label("👥 " + (int)(Math.random() * 100 + 10) + " members");
        memberCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");

        // Action Buttons
        HBox buttonBox = new HBox(10);
        Button joinButton = new Button("➕ Join");
        Button viewButton = new Button("View Club");

        joinButton.setStyle("-fx-padding: 8px 15px; -fx-font-size: 11px;");
        viewButton.setStyle("-fx-padding: 8px 15px; -fx-font-size: 11px;");

        joinButton.setOnAction(e -> showAlert("Success", "You joined " + name + "!"));
        viewButton.setOnAction(e -> showAlert("Info", name + "\n" + description + "\nCategory: " + category));

        buttonBox.getChildren().addAll(joinButton, viewButton);

        clubCard.getChildren().addAll(nameLabel, categoryLabel, descLabel, memberCountLabel, buttonBox);
        clubsVBox.getChildren().add(clubCard);
    }

    @FXML
    private void handleCreateClub() {
        String name = clubNameField.getText();
        String description = clubDescriptionArea.getText();
        String category = clubCategoryField.getText();

        if (name.isEmpty() || description.isEmpty() || category.isEmpty()) {
            showAlert("Error", "Please fill in all fields!");
            return;
        }

        boolean success = FirebaseManager.createClub(name, description, category);

        if (success) {
            showAlert("Success", "Club created successfully!");
            clubNameField.clear();
            clubDescriptionArea.clear();
            clubCategoryField.clear();
            loadClubs();
        } else {
            showAlert("Error", "Failed to create club. Try again.");
        }
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
    private void goToMessages() throws IOException {
        SceneManager.switchScene("messages.fxml");
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
