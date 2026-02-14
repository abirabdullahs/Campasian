package com.abir.demo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.animation.*;
import javafx.util.Duration;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private CheckBox rememberCheckbox;
    
    @FXML
    private Button loginButton;
    
    @FXML
    public void initialize() {
        // Add entrance animations
        addEntranceAnimations();
        
        // Setup button action
        loginButton.setOnAction(event -> handleLogin());
    }
    
    private void addEntranceAnimations() {
        // Fade in animation for form elements
        FadeTransition fade = new FadeTransition(Duration.millis(1000));
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(800));
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        // Apply animations to login button
        if (loginButton != null) {
            fade.setNode(loginButton);
            scale.setNode(loginButton);
            
            ParallelTransition parallel = new ParallelTransition(fade, scale);
            parallel.setDelay(Duration.millis(500));
            parallel.play();
        }
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean remember = rememberCheckbox.isSelected();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password!");
            shakeAnimation(loginButton);
            return;
        }
        
        // Add your login logic here
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("Remember: " + remember);
        
        // Success animation
        successAnimation();
        
        // Show success message
        showAlert("Success", "Login successful! Welcome to Campasian!");
    }
    
    private void shakeAnimation(Button button) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), button);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    private void successAnimation() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
