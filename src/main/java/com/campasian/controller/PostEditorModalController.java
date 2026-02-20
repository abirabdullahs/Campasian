package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;

/**
 * Controller for the post editor modal. Supports text and one optional image.
 */
public class PostEditorModalController {

    @FXML private TextArea contentArea;
    @FXML private Label imageLabel;

    private Stage stage;
    private Runnable onPostSuccess;
    private byte[] imageBytes;
    private String imageContentType = "image/png";

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
    protected void onAttachImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
        File f = chooser.showOpenDialog(stage);
        if (f != null) {
            try {
                imageBytes = Files.readAllBytes(f.toPath());
                String name = f.getName().toLowerCase();
                imageContentType = name.endsWith(".jpg") || name.endsWith(".jpeg") ? "image/jpeg" :
                    name.endsWith(".gif") ? "image/gif" : name.endsWith(".webp") ? "image/webp" : "image/png";
                if (imageLabel != null) imageLabel.setText(f.getName());
            } catch (Exception ignored) {}
        }
    }

    @FXML
    protected void onPostClick() {
        String content = contentArea != null ? contentArea.getText() : null;
        if (content == null || content.isBlank()) return;

        try {
            String imageUrl = null;
            if (imageBytes != null && imageBytes.length > 0) {
                String path = ApiService.getInstance().getCurrentUserId() + "/" + System.currentTimeMillis() + ".png";
                imageUrl = ApiService.getInstance().uploadToStorage("post-images", path, imageBytes, imageContentType);
            }
            ApiService.getInstance().sendPost(content.trim(), imageUrl);
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
