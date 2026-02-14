package com.abir.demo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    private ListView<String> postListView;
    @FXML
    private Button loginButton;

    public void initialize() {
        if (postListView == null) {
            return;
        }
        postListView.getItems().addAll(
                "Welcome to UniConnect!",
                "Your first post!"
        );
    }


    public void handleLogin() {
        try {
            // Load the next FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Home - Campasian");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
