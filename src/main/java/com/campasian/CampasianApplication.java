package com.campasian;

import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Screen;

/**
 * Main entry point for Campasian. Launches in full screen with Shadcn-style UI.
 */
public class CampasianApplication extends Application {

    @Override
    public void start(Stage stage) {
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
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
