package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ConfessionModalController {

    @FXML private TextArea contentField;
    private Stage stage;
    private Runnable onSuccess;

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }

    @FXML
    protected void onPost() {
        String content = contentField != null ? contentField.getText() : null;
        if (content == null || content.isBlank()) return;
        try {
            ApiService.getInstance().createConfession(content.trim());
            if (onSuccess != null) onSuccess.run();
            if (stage != null) stage.close();
        } catch (ApiException e) { /* ignore */ }
    }

    @FXML
    protected void onCancel() { if (stage != null) stage.close(); }
}
