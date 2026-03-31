package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.view.AppRouter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BloodSearchController implements Initializable {

    private static final String[] BLOOD_GROUPS = { "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-" };

    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private VBox donorsVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (bloodGroupCombo != null) {
            bloodGroupCombo.getItems().setAll(BLOOD_GROUPS);
            bloodGroupCombo.getSelectionModel().selectFirst();
            onBloodGroupChanged();
        }
    }

    @FXML
    protected void onBloodGroupChanged() {
        if (donorsVBox == null) return;
        String group = bloodGroupCombo != null ? bloodGroupCombo.getValue() : null;
        if (group == null || group.isBlank()) return;
        donorsVBox.getChildren().clear();
        new Thread(() -> {
            try {
                List<UserProfile> donors = ApiService.getInstance().getProfilesByBloodGroup(group);
                Platform.runLater(() -> {
                    if (donorsVBox == null) return;
                    donorsVBox.getChildren().clear();
                    for (UserProfile profile : donors) {
                        donorsVBox.getChildren().add(buildDonorCard(profile));
                    }
                    if (donors.isEmpty()) {
                        Label empty = new Label("No donors found for " + group + ".");
                        empty.getStyleClass().add("profile-label");
                        donorsVBox.getChildren().add(empty);
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
                });
            }
        }).start();
    }

    private VBox buildDonorCard(UserProfile profile) {
        String name = profile.getFullName() != null ? profile.getFullName() : "Unknown";
        String uni = profile.getUniversityName() != null && !profile.getUniversityName().isBlank() ? profile.getUniversityName() : "-";
        String blood = profile.getBloodGroup() != null && !profile.getBloodGroup().isBlank() ? profile.getBloodGroup() : "-";
        String session = profile.getSession() != null && !profile.getSession().isBlank() ? profile.getSession() : null;
        String batch = profile.getBatch() != null && !profile.getBatch().isBlank() ? profile.getBatch() : null;

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("blood-donor-name");
        nameLbl.setWrapText(true);
        nameLbl.setCursor(javafx.scene.Cursor.HAND);
        nameLbl.setOnMouseClicked(e -> AppRouter.navigateToProfile(profile.getId()));

        HBox meta = new HBox(8);
        Label bloodLbl = new Label(blood);
        bloodLbl.getStyleClass().add("blood-badge");
        meta.getChildren().add(bloodLbl);
        if (session != null || batch != null) {
            String badge = (session != null ? session : "") + (session != null && batch != null ? " · " : "") + (batch != null ? batch : "");
            Label sessLbl = new Label(badge);
            sessLbl.getStyleClass().add("blood-meta-badge");
            meta.getChildren().add(sessLbl);
        }

        Label uniLbl = new Label(uni);
        uniLbl.getStyleClass().add("profile-label");

        VBox card = new VBox(8);
        card.getStyleClass().add("blood-donor-card");
        card.getChildren().addAll(nameLbl, meta, uniLbl);
        return card;
    }
}
