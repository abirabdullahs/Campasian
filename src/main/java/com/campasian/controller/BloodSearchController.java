package com.campasian.controller;

import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
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

/**
 * Blood Donation Hub. Search donors by blood group.
 */
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
                    for (UserProfile p : donors) {
                        donorsVBox.getChildren().add(buildDonorCard(p));
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

    private VBox buildDonorCard(UserProfile p) {
        String name = p.getFullName() != null ? p.getFullName() : "Unknown";
        String uni = p.getUniversityName() != null && !p.getUniversityName().isBlank() ? p.getUniversityName() : "—";
        String blood = p.getBloodGroup() != null && !p.getBloodGroup().isBlank() ? p.getBloodGroup() : "—";
        String session = p.getSession() != null && !p.getSession().isBlank() ? p.getSession() : null;
        String batch = p.getBatch() != null && !p.getBatch().isBlank() ? p.getBatch() : null;

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("profile-value");
        nameLbl.setWrapText(true);
        nameLbl.setCursor(javafx.scene.Cursor.HAND);
        nameLbl.setOnMouseClicked(e -> AppRouter.navigateToProfile(p.getId()));

        HBox meta = new HBox(8);
        Label bloodLbl = new Label(blood);
        bloodLbl.getStyleClass().add("people-chip");
        bloodLbl.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-padding: 4 8; -fx-background-radius: 6;");
        meta.getChildren().add(bloodLbl);
        if (session != null || batch != null) {
            String badge = (session != null ? session : "") + (session != null && batch != null ? " · " : "") + (batch != null ? batch : "");
            Label sessLbl = new Label(badge);
            sessLbl.getStyleClass().add("profile-label");
            meta.getChildren().add(sessLbl);
        }

        Label uniLbl = new Label(uni);
        uniLbl.getStyleClass().add("profile-label");

        VBox card = new VBox(8);
        card.getStyleClass().addAll("content-card", "user-card");
        card.getChildren().addAll(nameLbl, meta, uniLbl);
        return card;
    }
}
