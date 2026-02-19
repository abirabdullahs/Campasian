package com.campasian;

import com.campasian.database.DatabaseInitializer;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the Campasian social media application.
 * Runs DB migrations, initializes SceneManager, and shows the login view.
 */
public class CampasianApplication extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseInitializer.migrate();
        SceneManager.initialize(stage);
        stage.setTitle("Campasian");
        stage.show();
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
