package com.abir.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
        Scene homeScene = new Scene(loader.load());
        homeScene.getStylesheets().add(getClass().getResource("/css/Home.css").toExternalForm());

        stage.setScene(homeScene);
        stage.setTitle("University Social App");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
