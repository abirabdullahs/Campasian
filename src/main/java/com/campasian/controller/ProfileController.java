package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.AuthService;
import com.campasian.service.ApiException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the profile sub-view.
 */
public class ProfileController implements Initializable {

    @FXML private Label fullNameLabel;
    @FXML private Label universityLabel;
    @FXML private Label einLabel;
    @FXML private Label followersCountLabel;
    @FXML private Label followingCountLabel;

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
                String uid = profile.getId();
                if (uid != null) {
                    int followers = ApiService.getInstance().getFollowerCount(uid);
                    int following = ApiService.getInstance().getFollowingCount(uid);
                    if (followersCountLabel != null) followersCountLabel.setText(String.valueOf(followers));
                    if (followingCountLabel != null) followingCountLabel.setText(String.valueOf(following));
                }
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
}
