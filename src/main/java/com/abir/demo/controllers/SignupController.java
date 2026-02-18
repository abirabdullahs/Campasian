package com.abir.demo.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.abir.demo.utils.SceneManager;

import java.io.IOException;

public class SignupController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private CheckBox termsCheck;

    @FXML
    private void handleSignup(ActionEvent event) {
        if (!termsCheck.isSelected()) {
            showAlert("Please accept the terms to continue.");
            return;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Passwords do not match.");
            return;
        }
        
        try {
            // After successful signup, go to dashboard
            SceneManager.switchScene("dashboard.fxml");
        } catch (IOException e) {
            showAlert("Failed to load dashboard!");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            SceneManager.switchScene("Login.fxml");
        } catch (IOException e) {
            showAlert("Failed to load login page!");
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
