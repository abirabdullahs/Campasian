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
 * Events Controller - Manage university events
 */
public class EventsController {

    @FXML
    private TextField eventNameField;

    @FXML
    private TextArea eventDescriptionArea;

    @FXML
    private TextField eventDateField;

    @FXML
    private TextField eventLocationField;

    @FXML
    private VBox eventsVBox;

    @FXML
    private Button createEventButton;

    @FXML
    public void initialize() {
        loadEvents();
        
        createEventButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #38bdf8, #a855f7);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10px 30px;" +
            "-fx-background-radius: 20;"
        );
        
        createEventButton.setOnAction(event -> handleCreateEvent());
    }

    private void loadEvents() {
        eventsVBox.getChildren().clear();

        // Sample events
        String[][] events = {
            {"Tech Summit 2026", "Annual technology conference for students", "2026-03-15", "Main Auditorium"},
            {"Programming Workshop", "Learn advanced Java and Spring", "2026-02-25", "CS Lab"},
            {"Career Fair", "Connect with top tech companies", "2026-03-10", "Student Center"},
            {"Debate Competition", "Annual inter-university debate contest", "2026-03-20", "Debate Hall"}
        };

        for (String[] event : events) {
            addEventCard(event[0], event[1], event[2], event[3]);
        }
    }

    private void addEventCard(String name, String description, String date, String location) {
        VBox eventCard = new VBox(10);
        eventCard.setPadding(new Insets(15));
        eventCard.setStyle(
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 10;" +
            "-fx-background-color: rgba(255,255,255,0.05);"
        );

        // Event Header
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        // Event Details
        Label dateLabel = new Label("📅 " + date);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");

        Label locationLabel = new Label("📍 " + location);
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8);");
        descLabel.setWrapText(true);

        // Action Buttons
        HBox buttonBox = new HBox(10);
        Button attendButton = new Button("✔️ Attend");
        Button detailsButton = new Button("Details");

        attendButton.setStyle("-fx-padding: 8px 15px; -fx-font-size: 11px;");
        detailsButton.setStyle("-fx-padding: 8px 15px; -fx-font-size: 11px;");

        attendButton.setOnAction(e -> showAlert("Success", "You're attending " + name + "!"));
        detailsButton.setOnAction(e -> showAlert("Info", name + "\n" + description + "\n" + date + "\n" + location));

        buttonBox.getChildren().addAll(attendButton, detailsButton);

        eventCard.getChildren().addAll(nameLabel, dateLabel, locationLabel, descLabel, buttonBox);
        eventsVBox.getChildren().add(eventCard);
    }

    @FXML
    private void handleCreateEvent() {
        String name = eventNameField.getText();
        String description = eventDescriptionArea.getText();
        String date = eventDateField.getText();
        String location = eventLocationField.getText();

        if (name.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
            showAlert("Error", "Please fill in all fields!");
            return;
        }

        boolean success = FirebaseManager.createEvent(name, description, date, location);

        if (success) {
            showAlert("Success", "Event created successfully!");
            eventNameField.clear();
            eventDescriptionArea.clear();
            eventDateField.clear();
            eventLocationField.clear();
            loadEvents();
        } else {
            showAlert("Error", "Failed to create event. Try again.");
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
