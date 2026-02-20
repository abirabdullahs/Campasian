package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

/**
 * Controller for the Edit Profile modal. Supports avatar upload.
 */
public class EditProfileModalController {

    @FXML private TextField fullNameField;
    @FXML private TextField universityField;
    @FXML private TextArea bioField;
    @FXML private ImageView avatarPreview;
    @FXML private javafx.scene.control.Label avatarLabel;

    private Stage stage;
    private Runnable onSaved;
    private byte[] avatarBytes;
    private String avatarContentType = "image/png";

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnSaved(Runnable r) { this.onSaved = r; }

    public void setInitialData(String fullName, String university, String bio) {
        if (fullNameField != null) fullNameField.setText(fullName != null ? fullName : "");
        if (universityField != null) universityField.setText(university != null ? university : "");
        if (bioField != null) bioField.setText(bio != null ? bio : "");
    }

    public void setInitialAvatarUrl(String avatarUrl) {
        if (avatarPreview != null && avatarUrl != null && !avatarUrl.isBlank()) {
            try {
                avatarPreview.setImage(new Image(avatarUrl, true));
            } catch (Exception ignored) {}
        }
    }

    @FXML
    protected void onChangePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Picture");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = chooser.showOpenDialog(stage);
        if (f != null) {
            try {
                avatarBytes = Files.readAllBytes(f.toPath());
                String name = f.getName().toLowerCase();
                avatarContentType = name.endsWith(".jpg") || name.endsWith(".jpeg") ? "image/jpeg" :
                    name.endsWith(".gif") ? "image/gif" : "image/png";
                avatarPreview.setImage(new Image(new FileInputStream(f), 64, 64, true, true));
                if (avatarLabel != null) avatarLabel.setText(f.getName());
            } catch (Exception ignored) {}
        }
    }

    @FXML
    protected void onSaveClick() {
        try {
            if (avatarBytes != null && avatarBytes.length > 0) {
                String path = ApiService.getInstance().getCurrentUserId() + "/avatar.png";
                String url = ApiService.getInstance().uploadToStorage("avatars", path, avatarBytes, avatarContentType);
                ApiService.getInstance().updateProfileAvatar(url);
            }
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
