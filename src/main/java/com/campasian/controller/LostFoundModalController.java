package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Modal for posting lost or found item.
 */
public class LostFoundModalController implements Initializable {

    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private TextArea descField;

    private Stage stage;
    private Runnable onSuccess;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (typeCombo != null) {
            typeCombo.getItems().setAll("Lost", "Found");
            typeCombo.getSelectionModel().selectFirst();
        }
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }

    @FXML
    protected void onPost() {
        String type = typeCombo != null ? typeCombo.getValue() : null;
        String title = titleField != null ? titleField.getText() : null;
        String location = locationField != null ? locationField.getText() : null;
        if (type == null || type.isBlank()) return;
        if (title == null || title.isBlank()) return;
        if (location == null || location.isBlank()) return;
        try {
            ApiService.getInstance().createLostFoundItem(type, title.trim(), descField != null ? descField.getText() : null, location.trim());
            if (onSuccess != null) onSuccess.run();
            if (stage != null) stage.close();
        } catch (ApiException e) { /* ignore */ }
    }

    @FXML
    protected void onCancel() {
        if (stage != null) stage.close();
    }
}
