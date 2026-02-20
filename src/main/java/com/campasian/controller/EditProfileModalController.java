package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Edit Profile modal.
 */
public class EditProfileModalController {

    @FXML private TextField fullNameField;
    @FXML private TextField universityField;
    @FXML private TextArea bioField;

    private Stage stage;
    private Runnable onSaved;

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnSaved(Runnable r) { this.onSaved = r; }

    public void setInitialData(String fullName, String university, String bio) {
        if (fullNameField != null) fullNameField.setText(fullName != null ? fullName : "");
        if (universityField != null) universityField.setText(university != null ? university : "");
        if (bioField != null) bioField.setText(bio != null ? bio : "");
    }

    @FXML
    protected void onSaveClick() {
        try {
            ApiService.getInstance().updateProfile(
                fullNameField != null ? fullNameField.getText() : null,
                universityField != null ? universityField.getText() : null,
                bioField != null ? bioField.getText() : null
            );
            if (onSaved != null) onSaved.run();
            if (stage != null) stage.close();
        } catch (ApiException e) { /* ignore */ }
    }

    @FXML
    protected void onCancelClick() {
        if (stage != null) stage.close();
    }
}
