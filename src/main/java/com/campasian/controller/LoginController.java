package com.campasian.controller;

import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the login view. Handles login validation and delegates
 * navigation to SceneManager (no Stage logic here).
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    protected void onLoginClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            if (errorLabel != null) {
                errorLabel.setText("Please enter username and password.");
                errorLabel.setManaged(true);
            }
            return;
        }

        // Successful login — delegate navigation to SceneManager
        SceneManager.navigateTo(ViewPaths.HOME_VIEW);
    }
}
