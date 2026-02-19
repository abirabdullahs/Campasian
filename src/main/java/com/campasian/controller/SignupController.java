package com.campasian.controller;

import com.campasian.service.AuthService;
import com.campasian.service.ApiException;
import com.campasian.service.UniversityService;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the signup view. University dropdown with type search,
 * real-time password validation, Supabase REST signup.
 */
public class SignupController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> universityCombo;
    @FXML private TextField numberField;
    @FXML private TextField departmentField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private AuthService authService;
    private String lastFilterQuery = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = AuthService.getInstance();
        var all = FXCollections.observableArrayList(UniversityService.loadUniversities());
        var filtered = new FilteredList<>(all, s -> true);
        universityCombo.setItems(filtered);
        universityCombo.setEditable(true);
        universityCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal != null ? newVal.trim().toLowerCase() : "";
            if (q.equals(lastFilterQuery)) return;
            lastFilterQuery = q;
            Platform.runLater(() -> {
                try {
                    filtered.setPredicate(s -> q.isEmpty() || (s != null && s.toLowerCase().contains(q)));
                } finally {
                    lastFilterQuery = q;
                }
            });
        });

        // Real-time password match validation
        ChangeListener<String> passwordListener = (obs, oldVal, newVal) -> updatePasswordMatchStyle();
        passwordField.textProperty().addListener(passwordListener);
        confirmPasswordField.textProperty().addListener(passwordListener);
    }

    private void updatePasswordMatchStyle() {
        String p = passwordField.getText();
        String c = confirmPasswordField.getText();
        boolean match = p != null && c != null && p.equals(c);
        boolean hasBoth = (p != null && !p.isBlank()) && (c != null && !c.isBlank());
        if (hasBoth && !match) {
            confirmPasswordField.getStyleClass().add("error");
        } else {
            confirmPasswordField.getStyleClass().removeAll("error");
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
        confirmPasswordField.getStyleClass().removeAll("error");

        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String university = universityCombo.getEditor().getText();
        if (university == null) university = universityCombo.getValue();
        String number = numberField.getText();
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
        if (university == null || university.isBlank()) {
            showError("Please select or enter your university.");
            return;
        }
        if (number == null || number.isBlank()) {
            showError("Please enter your student/ID number.");
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
            confirmPasswordField.getStyleClass().add("error");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        try {
            authService.signup(fullName, email, university, number, department, password);
            SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
        } catch (ApiException e) {
            if (e.isUserAlreadyRegistered()) {
                showError("This email is already registered.");
                return;
            }
            showError("Registration failed. " + e.getMessage());
        } catch (Exception e) {
            showError("Registration failed. Please try again.");
        }
    }

    @FXML
    protected void onLoginLinkClick() {
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }
}
