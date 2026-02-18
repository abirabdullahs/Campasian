package com.abir.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.scene.shape.Circle;
import com.abir.demo.utils.SceneManager;

import java.io.IOException;

/**
 * Browse Users Controller - Find and add friends
 */
public class BrowseUsersController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private VBox usersVBox;

    @FXML
    private ScrollPane usersScrollPane;

    @FXML
    public void initialize() {
        loadUsers();
        setupFilters();
        
        searchField.setStyle("-fx-control-inner-background: rgba(255,255,255,0.1); -fx-text-fill: white;");
        searchField.setOnKeyReleased(event -> searchUsers());
    }

    private void setupFilters() {
        filterCombo.getItems().addAll("All", "Same University", "Same Major", "Active Now");
        filterCombo.setValue("All");
        filterCombo.setOnAction(event -> loadUsers());
    }

    private void loadUsers() {
        usersVBox.getChildren().clear();

        // Sample users data
        String[][] users = {
            {"Alice Johnson", "State University", "Computer Science"},
            {"Bob Smith", "State University", "Business"},
            {"Carol White", "Tech Institute", "Computer Science"},
            {"David Brown", "State University", "Engineering"},
            {"Emma Davis", "State University", "Computer Science"},
            {"Frank Miller", "Tech Institute", "Data Science"}
        };

        for (String[] user : users) {
            addUserCard(user[0], user[1], user[2]);
        }
    }

    private void addUserCard(String name, String university, String major) {
        HBox userCard = new HBox(15);
        userCard.setPadding(new Insets(15));
        userCard.setStyle(
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 10;" +
            "-fx-background-color: rgba(255,255,255,0.05);"
        );

        // Profile Picture
        Circle profilePic = new Circle(40);
        profilePic.setStyle("-fx-fill: linear-gradient(135deg, #38bdf8, #a855f7);");

        // User Info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        Label univLabel = new Label(university);
        univLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");
        
        Label majorLabel = new Label("Major: " + major);
        majorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");

        infoBox.getChildren().addAll(nameLabel, univLabel, majorLabel);

        // Buttons
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("➕ Add");
        Button viewButton = new Button("👁️ View");
        
        addButton.setStyle("-fx-padding: 8px 15px; -fx-font-size: 11px;");
        viewButton.setStyle("-fx-padding: 8px 15px; -fx-font-size: 11px;");
        
        addButton.setOnAction(e -> showAlert("Success", name + " added to friends!"));
        viewButton.setOnAction(e -> showAlert("Info", "Viewing " + name + "'s profile"));

        buttonBox.getChildren().addAll(addButton, viewButton);

        userCard.getChildren().addAll(profilePic, infoBox, new Region(), buttonBox);
        HBox.setHgrow(new Region(), javafx.scene.layout.Priority.ALWAYS);
        usersVBox.getChildren().add(userCard);
    }

    @FXML
    private void searchUsers() {
        String query = searchField.getText().toLowerCase();
        // Filter logic would go here
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
