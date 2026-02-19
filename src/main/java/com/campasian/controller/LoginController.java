package com.campasian.controller;

import com.campasian.model.User;
import com.campasian.service.AuthService;
import com.campasian.service.ApiException;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the login view. Verifies credentials via Supabase REST APIs
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
            if (user != null) {
                SceneManager.navigateTo(ViewPaths.HOME_VIEW);
            } else {
                showError("Invalid email or password.");
            }
        } catch (ApiException e) {
            // ইমেইল কনফার্মেশন এরর চেক করা হচ্ছে
            if (e.getMessage().toLowerCase().contains("email not confirmed")) {
                showError("Please confirm your email or disable 'Confirm Email' in Supabase.");
            } else {
                showError("Login failed: " + e.getMessage());
            }
            e.printStackTrace();
        } catch (Exception e) {
            showError("An unexpected error occurred. Please try again.");
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
