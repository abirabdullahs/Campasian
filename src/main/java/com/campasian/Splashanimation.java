package com.campasian;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.util.Duration;

public class Splashanimation extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: #000000;"); // Full black background

        // Title: "Campasian"
        String word = "Campasian";
        HBox titleContainer = new HBox(5); // 5px spacing between letters
        titleContainer.setAlignment(Pos.CENTER);

        for (int i = 0; i < word.length(); i++) {
            Text letter = new Text(String.valueOf(word.charAt(i)));
            letter.setFont(Font.font("Poppins", FontWeight.BOLD, 80)); // Poppins font
            letter.setFill(Color.WHITE);

            // Start the letter as invisible/scaled to 0
            letter.setScaleX(0);
            letter.setScaleY(0);

            titleContainer.getChildren().add(letter);

            // Create the "Pop" animation
            ScaleTransition st = new ScaleTransition(Duration.millis(400), letter);
            st.setToX(1.2); // Slight overshoot for a bouncy feel
            st.setToY(1.2);
            st.setCycleCount(1);

            // Stagger the start time based on the letter's position
            st.setDelay(Duration.millis(i * 150));

            // Add a second transition to settle back to normal scale
            st.setOnFinished(e -> {
                ScaleTransition settle = new ScaleTransition(Duration.millis(150), letter);
                settle.setToX(1.0);
                settle.setToY(1.0);
                settle.play();
            });

            st.play();
        }

        // Punchline: "It is a campus based social media app"
        Text punchline = new Text("Built where you belong.");
        punchline.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));
        punchline.setFill(Color.web("#888888")); // Gray color
        punchline.setOpacity(0); // Start invisible

        // Fade in animation for punchline (starts after title finishes)
        FadeTransition ft = new FadeTransition(Duration.millis(800), punchline);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(word.length() * 150 + 600)); // Start after title animation
        ft.play();

        mainContainer.getChildren().addAll(titleContainer, punchline);

        var bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(mainContainer, bounds.getWidth(), bounds.getHeight());
        primaryStage.setTitle("Campasian");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}