package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.util.ImageSelectionSupport;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the post editor modal. Supports text and one optional image.
 */
public class PostEditorModalController {

    @FXML private TextArea contentArea;
    @FXML private Label imageLabel;
    @FXML private VBox imagePreviewBox;
    @FXML private ImageView imagePreview;
    @FXML private Button removeImageBtn;

    private Stage stage;
    private Runnable onPostSuccess;
    private byte[] imageBytes;
    private String imageExtension = "png";
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
        ImageSelectionSupport.SelectedImage selected = ImageSelectionSupport.chooseImage(stage, "Select Post Image");
        if (selected != null) {
            imageBytes = selected.getBytes();
            imageExtension = selected.getExtension();
            imageContentType = selected.getContentType();
            if (imageLabel != null) imageLabel.setText(selected.getFileName());
            if (imagePreview != null) imagePreview.setImage(selected.getPreview());
            if (imagePreviewBox != null) {
                imagePreviewBox.setVisible(true);
                imagePreviewBox.setManaged(true);
            }
        }
    }

    @FXML
    protected void onRemoveImage() {
        imageBytes = null;
        imageExtension = "png";
        imageContentType = "image/png";
        if (imageLabel != null) imageLabel.setText("");
        if (imagePreview != null) imagePreview.setImage(null);
        if (imagePreviewBox != null) {
            imagePreviewBox.setVisible(false);
            imagePreviewBox.setManaged(false);
        }
    }

    @FXML
    protected void onPostClick() {
        String content = contentArea != null ? contentArea.getText() : null;
        if ((content == null || content.isBlank()) && (imageBytes == null || imageBytes.length == 0)) return;

        try {
            String imageUrl = null;
            if (imageBytes != null && imageBytes.length > 0) {
                String path = ImageSelectionSupport.buildStoragePath(ApiService.getInstance().getCurrentUserId(), imageExtension);
                imageUrl = ApiService.getInstance().uploadToStorage("post-images", path, imageBytes, imageContentType);
            }
            ApiService.getInstance().sendPost(content != null ? content.trim() : "", imageUrl);
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
