package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ResourceAddModalController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextField linkField;
    @FXML private ComboBox<String> deptCombo;
    @FXML private ComboBox<String> semCombo;

    private Stage stage;
    private Runnable onSuccess;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (deptCombo != null) {
            deptCombo.getItems().setAll("CSE", "EEE", "BBA");
            deptCombo.getSelectionModel().selectFirst();
        }
        if (semCombo != null) {
            semCombo.getItems().setAll("1", "2", "3");
            semCombo.getSelectionModel().selectFirst();
        }
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }

    @FXML
    protected void onAdd() {
        String title = titleField != null ? titleField.getText() : null;
        if (title == null || title.isBlank()) return;
        String dept = deptCombo != null ? deptCombo.getValue() : null;
        String sem = semCombo != null ? semCombo.getValue() : null;
        if (dept == null || sem == null) return;
        try {
            ApiService.getInstance().createCourseResource(title, linkField != null ? linkField.getText() : null, dept, sem);
            if (onSuccess != null) onSuccess.run();
            if (stage != null) stage.close();
        } catch (ApiException e) { /* ignore */ }
    }

    @FXML
    protected void onCancel() { if (stage != null) stage.close(); }
}
