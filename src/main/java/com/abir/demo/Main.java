package com.abir.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.abir.demo.utils.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Set primary stage for SceneManager
        SceneManager.setPrimaryStage(stage);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
        Scene homeScene = new Scene(loader.load());
        homeScene.getStylesheets().add(getClass().getResource("/css/Home.css").toExternalForm());

        stage.setScene(homeScene);
        stage.setTitle("Campasian - University Social Network");
        stage.setWidth(1000);
        stage.setHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
