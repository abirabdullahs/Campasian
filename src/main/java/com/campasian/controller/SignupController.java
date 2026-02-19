package com.campasian.controller;

import com.campasian.service.AuthService;
import com.campasian.service.ApiException;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the signup view. Handles validation, EIN auto-fill,
 * and delegates persistence to AuthService.
 */
public class SignupController implements Initializable {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField einNumberField;

    @FXML
    private TextField universityField;

    @FXML
    private TextField departmentField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private AuthService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = AuthService.getInstance();
        einNumberField.textProperty().addListener((obs, oldVal, newVal) -> onEinChanged(newVal));
    }

    private void onEinChanged(String ein) {
        if (ein != null && !ein.isBlank()) {
            universityField.setText(authService.resolveUniversityByEin(ein));
        } else {
            universityField.clear();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setManaged(true);
        }
    }

    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setManaged(false);
        }
    }

    @FXML
    protected void onSignupClick() {
        clearError();

        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String einNumber = einNumberField.getText();
        String department = departmentField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName == null || fullName.isBlank()) {
            showError("Please enter your full name.");
            return;
        }
        if (email == null || email.isBlank()) {
            showError("Please enter your email.");
            return;
        }
        if (einNumber == null || einNumber.isBlank()) {
            showError("Please enter your EIN number.");
            return;
        }
        if (department == null || department.isBlank()) {
            showError("Please enter your department.");
            return;
        }
        if (password == null || password.isBlank()) {
            showError("Please enter a password.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Password and Confirm Password do not match.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        try {
            authService.signup(fullName, email, einNumber, department, password);
            SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
        } catch (ApiException e) {
            if (e.isUserAlreadyRegistered()) {
                showError("This email is already registered.");
                return;
            }
            showError("Registration failed. " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Registration failed. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void onLoginLinkClick() {
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }
}
