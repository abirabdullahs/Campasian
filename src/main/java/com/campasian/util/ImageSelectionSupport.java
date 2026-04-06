package com.campasian.util;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Set;

/**
 * Shared image selection and validation helpers for post and chat uploads.
 */
public final class ImageSelectionSupport {

    public static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private ImageSelectionSupport() {
    }

    public static SelectedImage chooseImage(Stage owner, String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        File file = chooser.showOpenDialog(owner);
        if (file == null) {
            return null;
        }

        String extension = extensionOf(file.getName());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            showValidationError("Unsupported image type. Use PNG, JPG, JPEG, GIF, or WEBP.");
            return null;
        }

        try {
            long size = Files.size(file.toPath());
            if (size <= 0 || size > MAX_IMAGE_BYTES) {
                showValidationError("Image must be 5 MB or smaller.");
                return null;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            try (FileInputStream inputStream = new FileInputStream(file)) {
                Image preview = new Image(inputStream, 420, 280, true, true);
                if (preview.isError()) {
                    showValidationError("The selected file could not be loaded as an image.");
                    return null;
                }
                return new SelectedImage(
                    file.getName(),
                    extension,
                    bytes,
                    contentTypeFor(extension),
                    preview
                );
            }
        } catch (IOException e) {
            showValidationError("Unable to read the selected image.");
            return null;
        }
    }

    public static String buildStoragePath(String userId, String extension) {
        String safeExtension = ALLOWED_EXTENSIONS.contains(extension) ? extension : "png";
        return userId + "/" + System.currentTimeMillis() + "." + safeExtension;
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private static String contentTypeFor(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/png";
        };
    }

    private static void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Image upload error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static final class SelectedImage {
        private final String fileName;
        private final String extension;
        private final byte[] bytes;
        private final String contentType;
        private final Image preview;

        public SelectedImage(String fileName, String extension, byte[] bytes, String contentType, Image preview) {
            this.fileName = fileName;
            this.extension = extension;
            this.bytes = bytes;
            this.contentType = contentType;
            this.preview = preview;
        }

        public String getFileName() {
            return fileName;
        }

        public String getExtension() {
            return extension;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getContentType() {
            return contentType;
        }

        public Image getPreview() {
            return preview;
        }
    }
}
