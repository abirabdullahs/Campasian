package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Placeholder controller for the settings sub-view.
 */
public class SettingsController implements Initializable {

    @FXML private Label accountNameLabel;
    @FXML private Label accountUniLabel;
    @FXML private CheckBox notificationsToggle;
    @FXML private CheckBox darkModeToggle;
    @FXML private CheckBox compactModeToggle;
    @FXML private CheckBox showOnlineStatusToggle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserProfile profile = null;
        try {
            String currentUserId = ApiService.getInstance().getCurrentUserId();
            if (currentUserId != null && !currentUserId.isBlank()) {
                profile = ApiService.getInstance().getProfile(currentUserId);
            }
        } catch (Exception ignored) {
        }

        if (accountNameLabel != null) {
            accountNameLabel.setText(profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()
                ? profile.getFullName()
                : "Campus account");
        }
        if (accountUniLabel != null) {
            accountUniLabel.setText(profile != null && profile.getUniversityName() != null && !profile.getUniversityName().isBlank()
                ? profile.getUniversityName()
                : "Preferences for your current account");
        }

        if (notificationsToggle != null) notificationsToggle.setSelected(true);
        if (darkModeToggle != null) darkModeToggle.setSelected(true);
        if (compactModeToggle != null) compactModeToggle.setSelected(false);
        if (showOnlineStatusToggle != null) showOnlineStatusToggle.setSelected(true);
    }
}
