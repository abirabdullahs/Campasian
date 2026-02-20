package com.campasian.controller;

import com.campasian.model.User;
import com.campasian.service.ApiService;
import com.campasian.service.AuthService;
import com.campasian.service.ApiException;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
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
    private CheckBox rememberMeCheck;

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
                if (rememberMeCheck != null && rememberMeCheck.isSelected()) {
                    ApiService.getInstance().persistSession();
                }
                SceneManager.navigateTo(ViewPaths.HOME_VIEW);
            } else {
                showError("Invalid email or password.");
            }
        } catch (ApiException e) {
            if (e.isEmailNotConfirmed()) {
                showError("Email not confirmed. Please confirm your email via the link sent to you, " +
                    "or ask an admin to confirm your account in the Supabase Dashboard. " +
                    "Alternatively, delete the user and sign up again.");
            } else if (e.isInvalidCredentials()) {
                showError("Invalid email or password.");
            } else {
                showError("Login failed: " + e.getMessage());
            }
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
