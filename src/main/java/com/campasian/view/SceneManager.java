package com.campasian.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Centralized navigation utility. Handles loading FXML from resources/fxml/
 * and switching scenes. Holds the primary Stage reference so controllers
 * never need stage logic—they simply call navigateTo(path).
 */
public final class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {}

    /**
     * Initializes the manager with the primary stage. Must be called once
     * from {@link javafx.application.Application#start(Stage)}.
     */
    public static void initialize(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Loads an FXML file from resources/fxml/ and switches the current scene.
     * The path should be relative to resources (e.g. "/fxml/login.fxml").
     *
     * @param fxmlPath path to FXML resource (e.g. ViewPaths.LOGIN_VIEW)
     */
    public static void navigateTo(String fxmlPath) {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneManager not initialized. Call initialize(Stage) in Application.start().");
        }
        try {
            Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    /**
     * Returns the primary stage. Use sparingly; prefer navigateTo() for UI changes.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
