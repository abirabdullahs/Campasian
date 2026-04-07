package com.campasian;

import com.campasian.service.AuthService;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.util.Duration;

/**
 * Main entry point for Campasian. Shows splash animation first, then navigates to login/home.
 * Launches in full screen with Shadcn-style UI. Supports persistent login (Remember Me).
 */
public class CampasianApplication extends Application {

    private static HostServices hostServices;
    private Stage mainStage;

    public static HostServices getHostServicesStatic() { return hostServices; }

    @Override
    public void start(Stage stage) {
        hostServices = getHostServices();
        this.mainStage = stage;
        
        // Show splash screen first
        showSplashScreen();
    }

    private void showSplashScreen() {
        Stage splashStage = new Stage();
        splashStage.setTitle("Campasian");
        
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: #000000;");

        // Title: "Campasian"
        String word = "Campasian";
        HBox titleContainer = new HBox(5);
        titleContainer.setAlignment(Pos.CENTER);

        for (int i = 0; i < word.length(); i++) {
            Text letter = new Text(String.valueOf(word.charAt(i)));
            letter.setFont(Font.font("Poppins", FontWeight.BOLD, 80));
            letter.setFill(Color.WHITE);

            letter.setScaleX(0);
            letter.setScaleY(0);

            titleContainer.getChildren().add(letter);

            ScaleTransition st = new ScaleTransition(Duration.millis(400), letter);
            st.setToX(1.2);
            st.setToY(1.2);
            st.setCycleCount(1);
            st.setDelay(Duration.millis(i * 150));

            st.setOnFinished(e -> {
                ScaleTransition settle = new ScaleTransition(Duration.millis(150), letter);
                settle.setToX(1.0);
                settle.setToY(1.0);
                settle.play();
            });

            st.play();
        }

        // Punchline
        Text punchline = new Text("Built where you belong.");
        punchline.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));
        punchline.setFill(Color.web("#888888"));
        punchline.setOpacity(0);

        FadeTransition ft = new FadeTransition(Duration.millis(800), punchline);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(word.length() * 150 + 600));
        ft.play();

        mainContainer.getChildren().addAll(titleContainer, punchline);

        Scene splashScene = new Scene(mainContainer, 800, 400);
        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();
        splashStage.show();

        // After splash animation completes, show main app
        // Animation duration: (9 letters * 150ms) + 400ms + 150ms settle + 600ms delay + 800ms fade = ~2.5 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.millis(3000));
        delay.setOnFinished(e -> {
            splashStage.close();
            showMainApplication();
        });
        delay.play();
    }

    private void showMainApplication() {
        var bounds = Screen.getPrimary().getVisualBounds();
        SceneManager.initialize(mainStage);
        mainStage.setTitle("Campasian");
        mainStage.setFullScreen(true);
        mainStage.setMaximized(true);
        mainStage.setX(bounds.getMinX());
        mainStage.setY(bounds.getMinY());
        mainStage.setWidth(bounds.getWidth());
        mainStage.setHeight(bounds.getHeight());
        mainStage.show();

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
