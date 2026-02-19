package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Controller for the post editor modal.
 */
public class PostEditorModalController {

    @FXML private TextArea contentArea;

    private Stage stage;
    private Runnable onPostSuccess;

    /**
     * Call before showing the stage.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Callback to run after a successful post (e.g. refresh feed).
     */
    public void setOnPostSuccess(Runnable onPostSuccess) {
        this.onPostSuccess = onPostSuccess;
    }

    @FXML
    protected void onPostClick() {
        String content = contentArea != null ? contentArea.getText() : null;
        if (content == null || content.isBlank()) return;

        try {
            ApiService.getInstance().sendPost(content.trim());
            if (onPostSuccess != null) onPostSuccess.run();
            close();
        } catch (ApiException e) {
            // Could show error; for now silent
        }
    }

    @FXML
    protected void onCancelClick() {
        close();
    }

    private void close() {
        if (stage != null) stage.close();
    }
}
