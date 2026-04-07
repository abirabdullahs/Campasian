package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.view.AppRouter;
import com.campasian.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BloodSearchController implements Initializable {

    private static final String[] BLOOD_GROUPS = { "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-" };

    @FXML private HBox bloodGroupPanel;
    @FXML private Label groupSummaryLabel;
    @FXML private Button groupRequestButton;
    @FXML private VBox donorsVBox;
    
    private String selectedBloodGroup;
    private List<UserProfile> currentDonors;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createBloodGroupButtons();
        updateGroupActionIdle();
    }

    private void createBloodGroupButtons() {
        if (bloodGroupPanel == null) return;
        bloodGroupPanel.getChildren().clear();
        
        for (String group : BLOOD_GROUPS) {
            Button btn = new Button(group);
            btn.getStyleClass().addAll("blood-group-btn");
            btn.setPrefWidth(80);
            btn.setPrefHeight(40);
            btn.setOnAction(e -> selectBloodGroup(group, btn));
            bloodGroupPanel.getChildren().add(btn);
        }
    }

    private void selectBloodGroup(String group, Button btn) {
        selectedBloodGroup = group;
        
        // Update button styles
        for (int i = 0; i < bloodGroupPanel.getChildren().size(); i++) {
            Button b = (Button) bloodGroupPanel.getChildren().get(i);
            b.getStyleClass().remove("blood-group-btn-active");
            if (b == btn) {
                b.getStyleClass().add("blood-group-btn-active");
            }
        }
        
        updateGroupActionLoading(group);
        loadDonorsForGroup(group);
    }

    private void loadDonorsForGroup(String group) {
        if (donorsVBox == null) return;
        donorsVBox.getChildren().clear();
        
        new Thread(() -> {
            try {
                currentDonors = ApiService.getInstance().getProfilesByBloodGroup(group);
                Platform.runLater(() -> {
                    if (donorsVBox == null) return;
                    donorsVBox.getChildren().clear();
                    
                    if (currentDonors.isEmpty()) {
                        Label empty = new Label("No donors found for " + group + ".");
                        empty.getStyleClass().add("profile-label");
                        donorsVBox.getChildren().add(empty);
                        updateGroupActionLoaded(group, currentDonors);
                    } else {
                        updateGroupActionLoaded(group, currentDonors);
                        for (UserProfile profile : currentDonors) {
                            donorsVBox.getChildren().add(buildDonorCard(profile, group));
                        }
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (donorsVBox != null) {
                        donorsVBox.getChildren().clear();
                        Label err = new Label("Unable to load donors.");
                        err.getStyleClass().add("profile-label");
                        donorsVBox.getChildren().add(err);
                    }
                    updateGroupActionError();
                });
            }
        }).start();
    }

    private VBox buildDonorCard(UserProfile profile, String bloodGroup) {
        String name = profile.getFullName() != null ? profile.getFullName() : "Unknown";
        String uni = profile.getUniversityName() != null ? profile.getUniversityName() : "-";
        String session = profile.getSession() != null ? profile.getSession() : null;
        String batch = profile.getBatch() != null ? profile.getBatch() : null;

        // Name
        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("blood-donor-name");
        nameLbl.setWrapText(true);
        nameLbl.setCursor(javafx.scene.Cursor.HAND);
        nameLbl.setOnMouseClicked(e -> AppRouter.navigateToProfile(profile.getId()));

        // Meta info
        HBox meta = new HBox(8);
        Label bloodLbl = new Label(bloodGroup);
        bloodLbl.getStyleClass().add("blood-badge");
        meta.getChildren().add(bloodLbl);
        
        if (session != null || batch != null) {
            String badge = (session != null ? session : "") + 
                          (session != null && batch != null ? " · " : "") + 
                          (batch != null ? batch : "");
            Label sessLbl = new Label(badge);
            sessLbl.getStyleClass().add("blood-meta-badge");
            meta.getChildren().add(sessLbl);
        }

        // University
        Label uniLbl = new Label(uni);
        uniLbl.getStyleClass().add("profile-label");

        // Card
        VBox card = new VBox(8);
        card.getStyleClass().add("blood-donor-card");
        card.getChildren().addAll(nameLbl, meta, uniLbl);

        return card;
    }

    @FXML
    protected void onGroupRequest() {
        if (selectedBloodGroup == null || selectedBloodGroup.isBlank()) return;
        if (currentDonors == null || currentDonors.isEmpty()) return;
        showContactModal(selectedBloodGroup);
    }

    private void showContactModal(String bloodGroup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/blood-request-modal.fxml"));
            Parent root = loader.load();
            BloodRequestModalController ctrl = loader.getController();
            
            Stage modalStage = new Stage();
            modalStage.setTitle("Send Blood Request");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(SceneManager.getPrimaryStage());
            modalStage.setResizable(false);
            
            ctrl.setStage(modalStage);
            ctrl.setBloodGroup(bloodGroup);
            ctrl.setCurrentDonors(currentDonors);
            
            modalStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateGroupActionIdle() {
        if (groupSummaryLabel != null) {
            groupSummaryLabel.setText("Select a blood group to view donors.");
        }
        if (groupRequestButton != null) {
            groupRequestButton.setText("Send Request");
            groupRequestButton.setDisable(true);
        }
    }

    private void updateGroupActionLoading(String group) {
        if (groupSummaryLabel != null) {
            groupSummaryLabel.setText("Loading donors for " + group + "...");
        }
        if (groupRequestButton != null) {
            groupRequestButton.setText("Send Request (" + group + ")");
            groupRequestButton.setDisable(true);
        }
    }

    private void updateGroupActionLoaded(String group, List<UserProfile> donors) {
        int total = donors != null ? donors.size() : 0;
        String currentUserId = ApiService.getInstance().getCurrentUserId();

        long eligible = 0;
        if (donors != null) {
            for (UserProfile donor : donors) {
                if (donor == null) continue;
                String id = donor.getId();
                if (id == null || id.isBlank()) continue;
                if (currentUserId != null && currentUserId.equals(id)) continue;
                eligible++;
            }
        }

        if (groupSummaryLabel != null) {
            if (total <= 0) {
                groupSummaryLabel.setText("No donors found for " + group + ".");
            } else if (eligible == total) {
                groupSummaryLabel.setText("Found " + total + " donors for " + group + ".");
            } else {
                groupSummaryLabel.setText("Found " + total + " donors for " + group + " (" + eligible + " available).");
            }
        }

        if (groupRequestButton != null) {
            groupRequestButton.setText("Send Request (" + group + ")");
            groupRequestButton.setDisable(eligible <= 0);
        }
    }

    private void updateGroupActionError() {
        if (groupSummaryLabel != null) {
            groupSummaryLabel.setText("Unable to load donors.");
        }
        if (groupRequestButton != null) {
            groupRequestButton.setText("Send Request");
            groupRequestButton.setDisable(true);
        }
    }
}
