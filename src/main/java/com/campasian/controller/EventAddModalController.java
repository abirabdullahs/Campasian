package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EventAddModalController {

    @FXML private TextField titleField;
    @FXML private TextField dateField;
    @FXML private TextField venueField;
    @FXML private TextArea descField;
    private Stage stage;
    private Runnable onSuccess;

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }

    @FXML
    protected void onCreate() {
        String title = titleField != null ? titleField.getText() : null;
        String date = dateField != null ? dateField.getText() : null;
        String venue = venueField != null ? venueField.getText() : null;
        if (title == null || title.isBlank()) return;
        if (date == null || date.isBlank()) return;
        if (venue == null || venue.isBlank()) return;
        try {
            ApiService.getInstance().createCampusEvent(title, descField != null ? descField.getText() : null, date, venue);
            if (onSuccess != null) onSuccess.run();
            if (stage != null) stage.close();
        } catch (ApiException e) { /* ignore */ }
    }

    @FXML
    protected void onCancel() { if (stage != null) stage.close(); }
}
