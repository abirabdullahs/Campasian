package com.campasian.controller;

import com.campasian.model.User;
import com.campasian.service.AuthService;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

/**
 * Controller for the login view. Verifies credentials against the database
 * and delegates navigation to SceneManager.
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            showError("Please enter email and password.");
            return;
        }

        try {
            User user = AuthService.getInstance().login(email, password);
            if (user == null) {
                showError("Invalid email or password.");
                return;
            }
            SceneManager.navigateTo(ViewPaths.HOME_VIEW);
        } catch (SQLException e) {
            showError("Login failed. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSignupLinkClick() {
        SceneManager.navigateTo(ViewPaths.SIGNUP_VIEW);
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setManaged(true);
        }
    }
}
