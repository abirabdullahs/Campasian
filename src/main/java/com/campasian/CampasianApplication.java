package com.campasian;

import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the Campasian social media application.
 * Initializes SceneManager and shows the login view.
 */
public class CampasianApplication extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.initialize(stage);
        stage.setTitle("Campasian");
        stage.show();
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
