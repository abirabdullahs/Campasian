package com.abir.demo.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Utility class to manage scene transitions with animations
 */
public class SceneManager {
    private static Stage primaryStage;
    private static final String FXML_PATH = "/fxml/";
    private static Scene currentScene;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Load and switch to a new scene with fade animation
     */
    public static void switchScene(String fxmlFileName) throws IOException {
        switchScene(fxmlFileName, null);
    }

    /**
     * Load and switch to a new scene with fade animation and custom data
     */
    public static void switchScene(String fxmlFileName, Object controllerData) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage not set. Call setPrimaryStage first.");
        }

        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(FXML_PATH + fxmlFileName));
        Parent root = loader.load();

        // Pass data to controller if needed
        if (controllerData != null && loader.getController() instanceof DataController) {
            ((DataController) loader.getController()).setData(controllerData);
        }

        Scene newScene = new Scene(root);
        
        // Add CSS if available
        try {
            String cssFile = "/css/" + fxmlFileName.replace(".fxml", ".css");
            newScene.getStylesheets().add(SceneManager.class.getResource(cssFile).toExternalForm());
        } catch (Exception e) {
            // CSS file not found, that's okay
        }

        // Fade transition
        fadeSceneTransition(newScene);
    }

    /**
     * Switch scene with custom controller instance
     */
    public static <T> T switchSceneWithController(String fxmlFileName) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage not set. Call setPrimaryStage first.");
        }

        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(FXML_PATH + fxmlFileName));
        Parent root = loader.load();
        Scene newScene = new Scene(root);

        fadeSceneTransition(newScene);
        return loader.getController();
    }

    /**
     * Apply fade transition when switching scenes
     */
    private static void fadeSceneTransition(Scene newScene) {
        if (currentScene != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(300), primaryStage.getScene().getRoot());
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(event -> {
                primaryStage.setScene(newScene);
                applyFadeIn(newScene);
            });
            fade.play();
        } else {
            primaryStage.setScene(newScene);
            applyFadeIn(newScene);
        }
        currentScene = newScene;
    }

    /**
     * Apply fade-in animation to new scene
     */
    private static void applyFadeIn(Scene scene) {
        scene.getRoot().setOpacity(0.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), scene.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Get current stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Get current scene
     */
    public static Scene getCurrentScene() {
        return currentScene;
    }

    /**
     * Interface for controllers that need to receive data
     */
    public interface DataController {
        void setData(Object data);
    }
}
