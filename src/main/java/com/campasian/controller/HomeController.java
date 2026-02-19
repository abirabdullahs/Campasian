package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.AuthService;
import com.campasian.service.ApiException;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the dashboard/home view. Fetches and displays user profile.
 */
public class HomeController implements Initializable {

    @FXML private Label fullNameLabel;
    @FXML private Label universityLabel;
    @FXML private Label einLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfile();
    }

    private void loadProfile() {
        try {
            UserProfile profile = AuthService.getInstance().getCurrentUserProfile();
            if (profile != null) {
                fullNameLabel.setText(profile.getFullName() != null ? profile.getFullName() : "—");
                universityLabel.setText(profile.getUniversityName() != null ? profile.getUniversityName() : "—");
                einLabel.setText(profile.getEinNumber() != null ? profile.getEinNumber() : "—");
            } else {
                fullNameLabel.setText("—");
                universityLabel.setText("—");
                einLabel.setText("—");
            }
        } catch (ApiException e) {
            if (fullNameLabel != null) fullNameLabel.setText("—");
            if (universityLabel != null) universityLabel.setText("—");
            if (einLabel != null) einLabel.setText("—");
        }
    }

    @FXML
    protected void onHomeClick() {
        loadProfile();
    }

    @FXML
    protected void onSettingsClick() {
        // Placeholder for settings
    }

    @FXML
    protected void onLogoutClick() {
        AuthService.getInstance().logout();
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }
}
