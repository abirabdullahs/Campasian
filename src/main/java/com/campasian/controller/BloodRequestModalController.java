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
                int attempted = 0;
                int sent = 0;
                int failed = 0;
                ApiException lastApiError = null;
                if (donors != null) {
                    for (UserProfile donor : donors) {
                        if (donor == null) continue;
                        String donorId = donor.getId();
                        if (donorId == null || donorId.isBlank()) continue;
                        if (currentUserId != null && donorId.equals(currentUserId)) continue;

                        attempted++;
                            try {
                                String message = senderName + " needs " + bloodGroup + " blood. Contact: " + contact;
                                ApiService.getInstance().sendBloodRequestNotification(donorId, message);
                                sent++;
                            } catch (ApiException ex) {
                                failed++;
                                lastApiError = ex;
                            }
                    }
                }

                final int finalAttempted = attempted;
                final int finalSent = sent;
                final int finalFailed = failed;
                final ApiException finalLastApiError = lastApiError;
                
                Platform.runLater(() -> {
                    if (finalSent > 0) {
                        String msg = "Blood request sent to " + finalSent + " donor(s) (attempted: " + finalAttempted + ").";
                        if (finalFailed > 0) msg += " Failed: " + finalFailed + ".";
                        showSuccess(msg);
                        if (stage != null) {
                            stage.close();
                        }
                    } else {
                        String details = finalLastApiError != null ? finalLastApiError.getMessage() : null;
                        showError("Failed to send blood request" + (details != null && !details.isBlank() ? (": " + details) : "."));
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
