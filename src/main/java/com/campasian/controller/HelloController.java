package com.campasian.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the hello/welcome view.
 */
public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to Campasian!");
    }
}
