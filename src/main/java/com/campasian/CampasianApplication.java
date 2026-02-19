package com.campasian;

import com.campasian.database.DatabaseInitializer;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the Campasian social media application.
 * Runs DB migrations if reachable, initializes SceneManager, and shows the login view.
 * If the database is unreachable (e.g. UnknownHostException), the app still starts.
 */
public class CampasianApplication extends Application {

    private static final Logger LOG = Logger.getLogger(CampasianApplication.class.getName());

    @Override
    public void start(Stage stage) {
        try {
            DatabaseInitializer.migrate();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Database migration skipped (DB unreachable): {0}. Login/Signup will fail until connected.", e.getMessage());
        }
        SceneManager.initialize(stage);
        stage.setTitle("Campasian");
        stage.show();
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
