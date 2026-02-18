package com.abir.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import com.abir.demo.utils.FirebaseManager;
import com.abir.demo.utils.SceneManager;

import java.io.IOException;

/**
 * Dashboard Controller - Main feed with posts
 */
public class DashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TextArea postContentArea;
    
    @FXML
    private VBox feedVBox;
    
    @FXML
    private ScrollPane feedScrollPane;
    
    @FXML
    private Button postButton;

    private String currentUserId = "user123"; // In real app, get from auth

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Campasian Feed!");
        loadFeedPosts();
        
        // Button styling
        postButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #38bdf8, #a855f7);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10px 30px;" +
            "-fx-background-radius: 20;"
        );

        postButton.setOnAction(event -> handlePostSubmit());
    }

    @FXML
    private void loadFeedPosts() {
        feedVBox.getChildren().clear();
        
        // Add sample posts with animations
        String[] samplePosts = {
            "Just finished my final exams! #university",
            "Anyone interested in joining the coding club?",
            "The new cafeteria food is Amazing! 🎉",
            "Looking for study partners for DSA"
        };
        
        for (int i = 0; i < samplePosts.length; i++) {
            addPostToFeed(samplePosts[i], "User " + (i + 1), i * 100);
        }
    }

    private void addPostToFeed(String content, String author, int delay) {
        VBox postCard = createPostCard(content, author);
        
        FadeTransition fade = new FadeTransition(Duration.millis(500), postCard);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setDelay(Duration.millis(delay));
        
        feedVBox.getChildren().add(postCard);
        fade.play();
    }

    private VBox createPostCard(String content, String author) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 10;" +
            "-fx-background-color: rgba(255,255,255,0.05);"
        );

        Label authorLabel = new Label(author);
        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        Text contentText = new Text(content);
        contentText.setStyle("-fx-text-fill: white;");
        
        TextFlow textFlow = new TextFlow(contentText);
        textFlow.setPrefWidth(400);
        textFlow.setStyle("-fx-text-alignment: left;");
        
        HBox actionsBox = new HBox(10);
        Button likeBtn = new Button("👍 Like");
        Button commentBtn = new Button("💬 Comment");
        Button shareBtn = new Button("📤 Share");
        
        likeBtn.setStyle("-fx-padding: 5px 10px; -fx-font-size: 11px;");
        commentBtn.setStyle("-fx-padding: 5px 10px; -fx-font-size: 11px;");
        shareBtn.setStyle("-fx-padding: 5px 10px; -fx-font-size: 11px;");
        
        actionsBox.getChildren().addAll(likeBtn, commentBtn, shareBtn);

        card.getChildren().addAll(authorLabel, textFlow, actionsBox);
        return card;
    }

    @FXML
    private void handlePostSubmit() {
        String content = postContentArea.getText();
        if (content.isEmpty()) {
            showAlert("Error", "Please write something to post!");
            return;
        }

        // Save to Firebase
        boolean success = FirebaseManager.createPost(currentUserId, content, "");
        
        if (success) {
            showAlert("Success", "Post published successfully!");
            postContentArea.clear();
            loadFeedPosts();
        } else {
            showAlert("Error", "Failed to publish post. Try again.");
        }
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
