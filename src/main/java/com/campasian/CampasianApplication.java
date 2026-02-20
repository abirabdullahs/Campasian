package com.campasian;

import com.campasian.service.AuthService;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import javafx.stage.Screen;

/**
 * Main entry point for Campasian. Launches in full screen with Shadcn-style UI.
 * Supports persistent login (Remember Me).
 */
public class CampasianApplication extends Application {

    private static HostServices hostServices;

    public static HostServices getHostServicesStatic() { return hostServices; }

    @Override
    public void start(Stage stage) {
        hostServices = getHostServices();
        var bounds = Screen.getPrimary().getVisualBounds();
        SceneManager.initialize(stage);
        stage.setTitle("Campasian");
        stage.setFullScreen(true);
        stage.setMaximized(true);
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.show();

        if (AuthService.getInstance().tryRestoreSession()) {
            SceneManager.navigateTo(ViewPaths.HOME_VIEW);
        } else {
            SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
