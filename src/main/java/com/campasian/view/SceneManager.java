package com.campasian.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Centralized navigation. Loads FXML from resources/fxml/, handles scene transitions.
 * Responsive to stage size (full screen).
 */
public final class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {}

    public static void initialize(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(String fxmlPath) {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneManager not initialized. Call initialize(Stage) in Application.start().");
        }
        try {
            Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            if (root instanceof Region r) {
                r.prefWidthProperty().bind(primaryStage.widthProperty());
                r.prefHeightProperty().bind(primaryStage.heightProperty());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
