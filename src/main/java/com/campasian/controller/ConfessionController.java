package com.campasian.controller;

import com.campasian.model.Confession;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ConfessionController implements Initializable {

    @FXML private Button postBtn;
    @FXML private VBox itemsVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadConfessions();
    }

    @FXML
    protected void onPostClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/confession-modal.fxml"));
            Parent root = loader.load();
            ConfessionModalController ctrl = loader.getController();
            Stage s = new Stage();
            ctrl.setStage(s);
            s.setTitle("Post Confession");
            s.setScene(new javafx.scene.Scene(root));
            s.initModality(Modality.APPLICATION_MODAL);
            s.initOwner(SceneManager.getPrimaryStage());
            ctrl.setOnSuccess(this::loadConfessions);
            s.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConfessions() {
        if (itemsVBox == null) return;
        itemsVBox.getChildren().clear();
        new Thread(() -> {
            try {
                List<Confession> list = ApiService.getInstance().getConfessions();
                Platform.runLater(() -> {
                    if (itemsVBox == null) return;
                    itemsVBox.getChildren().clear();
                    for (Confession confession : list) {
                        itemsVBox.getChildren().add(buildCard(confession));
                    }
                    if (list.isEmpty()) {
                        Label empty = new Label("No confessions yet.");
                        empty.getStyleClass().add("profile-label");
                        itemsVBox.getChildren().add(empty);
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (itemsVBox != null) {
                        Label err = new Label("Unable to load.");
                        err.getStyleClass().add("profile-label");
                        itemsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildCard(Confession confession) {
        Label anon = new Label("Anonymous Student");
        anon.getStyleClass().add("confession-anonymous");

        Label content = new Label(confession.getContent() != null ? confession.getContent() : "");
        content.getStyleClass().add("confession-content");
        content.setWrapText(true);

        Label time = new Label(formatTime(confession.getCreatedAt()));
        time.getStyleClass().add("post-meta");

        VBox card = new VBox(8);
        card.getStyleClass().add("confession-card");
        card.getChildren().addAll(anon, content, time);
        return card;
    }

    private static String formatTime(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            return OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
        } catch (Exception e) {
            return iso;
        }
    }
}
