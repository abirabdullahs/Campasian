package com.campasian.controller;

import com.campasian.model.Faculty;
import com.campasian.model.FacultyFeedback;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FacultyController implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox facultyVBox;

    private final PauseTransition debouncer = new PauseTransition(Duration.millis(300));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFaculty();
        if (searchField != null) {
            searchField.textProperty().addListener((o, oldVal, newVal) -> {
                debouncer.setOnFinished(e -> loadFaculty());
                debouncer.stop();
                debouncer.playFromStart();
            });
        }
    }

    @FXML
    protected void onSearchKeyReleased() {
        debouncer.setOnFinished(e -> loadFaculty());
        debouncer.stop();
        debouncer.playFromStart();
    }

    private void loadFaculty() {
        if (facultyVBox == null) return;
        facultyVBox.getChildren().clear();
        String query = searchField != null ? searchField.getText() : null;
        String q = query != null ? query.trim() : "";
        new Thread(() -> {
            try {
                List<Faculty> list = ApiService.getInstance().getFaculty(q.isEmpty() ? null : q);
                Platform.runLater(() -> {
                    if (facultyVBox == null) return;
                    facultyVBox.getChildren().clear();
                    for (Faculty faculty : list) {
                        facultyVBox.getChildren().add(buildCard(faculty));
                    }
                    if (list.isEmpty()) {
                        Label empty = new Label("No faculty found. Add faculty via Supabase or run seed script.");
                        empty.getStyleClass().add("profile-label");
                        empty.setWrapText(true);
                        facultyVBox.getChildren().add(empty);
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (facultyVBox != null) {
                        Label err = new Label("Unable to load faculty.");
                        err.getStyleClass().add("profile-label");
                        facultyVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildCard(Faculty faculty) {
        Label name = new Label(faculty.getName() != null ? faculty.getName() : "-");
        name.getStyleClass().add("faculty-name");
        name.setWrapText(true);

        Label dept = new Label("Department: " + (faculty.getDepartment() != null ? faculty.getDepartment() : "-"));
        dept.getStyleClass().add("faculty-meta");

        Label email = new Label("Email: " + (faculty.getEmail() != null && !faculty.getEmail().isBlank() ? faculty.getEmail() : "-"));
        email.getStyleClass().add("faculty-meta");

        VBox feedbackSection = new VBox(8);
        Label feedbackTitle = new Label("Feedback / Tips");
        feedbackTitle.getStyleClass().add("faculty-feedback-title");

        VBox existingFeedbacks = new VBox(6);
        TextArea feedbackInput = new TextArea();
        feedbackInput.setPromptText("Share tips about teaching style, helpful for other students...");
        feedbackInput.getStyleClass().add("modal-text-area");
        feedbackInput.setPrefRowCount(2);
        feedbackInput.setWrapText(true);

        javafx.scene.control.Button submitBtn = new javafx.scene.control.Button("Submit Feedback");
        submitBtn.getStyleClass().add("faculty-submit-btn");
        submitBtn.setOnAction(e -> {
            String text = feedbackInput.getText();
            if (text != null && !text.isBlank()) {
                try {
                    ApiService.getInstance().createFacultyFeedback(faculty.getId(), null, text.trim());
                    feedbackInput.clear();
                    loadFaculty();
                } catch (ApiException ignored) {
                }
            }
        });

        feedbackSection.getChildren().addAll(feedbackTitle, existingFeedbacks, feedbackInput, submitBtn);
        String facultyId = faculty.getId();
        new Thread(() -> {
            try {
                List<FacultyFeedback> feedbacks = ApiService.getInstance().getFacultyFeedback(facultyId);
                Platform.runLater(() -> {
                    for (FacultyFeedback feedback : feedbacks) {
                        Label feedbackLabel = new Label(feedback.getFeedback() != null ? feedback.getFeedback() : "");
                        feedbackLabel.getStyleClass().add("faculty-feedback-card");
                        feedbackLabel.setWrapText(true);
                        existingFeedbacks.getChildren().add(feedbackLabel);
                    }
                });
            } catch (ApiException ignored) {
            }
        }).start();

        VBox card = new VBox(10);
        card.getStyleClass().add("faculty-card");
        card.getChildren().addAll(name, dept, email, feedbackSection);
        return card;
    }
}
