package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class StudyPartnerModalController {

    @FXML private TextField subjectField;
    @FXML private TextArea descField;
    private Stage stage;
    private Runnable onSuccess;

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }

    @FXML
    protected void onPost() {
        String subject = subjectField != null ? subjectField.getText() : null;
        if (subject == null || subject.isBlank()) return;
        try {
            ApiService.getInstance().createStudyPartnerPost(subject.trim(), descField != null ? descField.getText() : null);
            if (onSuccess != null) onSuccess.run();
            if (stage != null) stage.close();
        } catch (ApiException e) { /* ignore */ }
    }

    @FXML
    protected void onCancel() { if (stage != null) stage.close(); }
}
