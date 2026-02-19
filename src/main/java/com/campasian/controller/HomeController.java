package com.campasian.controller;

import com.campasian.service.AuthService;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the home layout. Manages sidebar and dynamic content loading.
 */
public class HomeController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button feedBtn;
    @FXML private Button communityBtn;
    @FXML private Button profileBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button settingsBtn;

    private static final String SIDEBAR_ACTIVE = "sidebar-btn-active";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadView(ViewPaths.FEED_VIEW);
        updateSidebarActive(feedBtn);
    }

    /**
     * Loads a sub-view FXML into the content area. Data is fetched from ApiService
     * in each sub-controller's initialize(), ensuring fresh data when switching tabs.
     */
    public void loadView(String fxmlPath) {
        if (contentArea == null || fxmlPath == null || fxmlPath.isBlank()) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }

    @FXML
    protected void onFeedClick() {
        loadView(ViewPaths.FEED_VIEW);
        updateSidebarActive(feedBtn);
    }

    @FXML
    protected void onCommunityClick() {
        loadView(ViewPaths.COMMUNITY_VIEW);
        updateSidebarActive(communityBtn);
    }

    @FXML
    protected void onProfileClick() {
        loadView(ViewPaths.PROFILE_VIEW);
        updateSidebarActive(profileBtn);
    }

    @FXML
    protected void onNotificationsClick() {
        loadView(ViewPaths.NOTIFICATIONS_VIEW);
        updateSidebarActive(notificationsBtn);
    }

    @FXML
    protected void onSettingsClick() {
        loadView(ViewPaths.SETTINGS_VIEW);
        updateSidebarActive(settingsBtn);
    }

    @FXML
    protected void onLogoutClick() {
        AuthService.getInstance().logout();
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }

    private void updateSidebarActive(Button active) {
        clearSidebarActive();
        if (active != null) active.getStyleClass().add(SIDEBAR_ACTIVE);
    }

    private void clearSidebarActive() {
        if (feedBtn != null) feedBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
        if (communityBtn != null) communityBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
        if (profileBtn != null) profileBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
        if (notificationsBtn != null) notificationsBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
        if (settingsBtn != null) settingsBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
    }
}
