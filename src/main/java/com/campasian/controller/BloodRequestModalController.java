package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class BloodRequestModalController {
    
    @FXML private TextField contactField;
    @FXML private Label descLabel;
    
    private Stage stage;
    private String bloodGroup;
    private List<UserProfile> donors;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
        if (descLabel != null) {
            descLabel.setText("Send your contact to all " + bloodGroup + " donors");
        }
    }
    
    public void setCurrentDonors(List<UserProfile> donors) {
        this.donors = donors;
    }
    
    @FXML
    protected void onCancel() {
        if (stage != null) stage.close();
    }
    
    @FXML
    protected void onSend() {
        String contact = contactField != null ? contactField.getText() : null;
        
        if (contact == null || contact.isBlank()) {
            showError("Please enter your contact number");
            return;
        }
        
        if (!contact.matches("^[+]?[0-9]{10,15}$")) {
            showError("Please enter a valid phone number");
            return;
        }
        
        // Send notification to all donors
        new Thread(() -> {
            try {
                String currentUserId = ApiService.getInstance().getCurrentUserId();
                String currentUserName = null;
                try {
                    UserProfile profile = ApiService.getInstance().getProfile(currentUserId);
                    if (profile != null) {
                        currentUserName = profile.getFullName();
                    }
                } catch (ApiException ignored) {}
                
                final String senderName = currentUserName != null ? currentUserName : "Someone";
                
                // Send notification to all donors in this blood group
                if (donors != null) {
                    for (UserProfile donor : donors) {
                        if (donor.getId() != null && !donor.getId().equals(currentUserId)) {
                            try {
                                String message = senderName + " needs " + bloodGroup + " blood. Contact: " + contact;
                                ApiService.getInstance().sendBloodRequestNotification(donor.getId(), message, contact, bloodGroup);
                            } catch (ApiException ignored) {}
                        }
                    }
                }
                
                Platform.runLater(() -> {
                    showSuccess("Blood request sent to " + (donors != null ? donors.size() : 0) + " donors!");
                    if (stage != null) {
                        stage.close();
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to send request: " + e.getMessage()));
            }
        }).start();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}