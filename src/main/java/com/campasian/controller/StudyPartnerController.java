package com.campasian.controller;

import com.campasian.model.StudyPartnerPost;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.AppRouter;
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

/**
 * Study Partner Finder. Message button redirects to chat.
 */
public class StudyPartnerController implements Initializable {

    @FXML private Button postBtn;
    @FXML private VBox itemsVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPosts();
    }

    @FXML
    protected void onPostClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/study-partner-modal.fxml"));
            Parent root = loader.load();
            StudyPartnerModalController ctrl = loader.getController();
            Stage s = new Stage();
            ctrl.setStage(s);
            s.setTitle("Looking for Partner");
            s.setScene(new javafx.scene.Scene(root));
            s.initModality(Modality.APPLICATION_MODAL);
            s.initOwner(SceneManager.getPrimaryStage());
            ctrl.setOnSuccess(this::loadPosts);
            s.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPosts() {
        if (itemsVBox == null) return;
        itemsVBox.getChildren().clear();
        new Thread(() -> {
            try {
                List<StudyPartnerPost> list = ApiService.getInstance().getStudyPartnerPosts();
                Platform.runLater(() -> {
                    if (itemsVBox == null) return;
                    itemsVBox.getChildren().clear();
                    for (StudyPartnerPost p : list) {
                        itemsVBox.getChildren().add(buildCard(p));
                    }
                    if (list.isEmpty()) {
                        Label empty = new Label("No posts yet.");
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

    private VBox buildCard(StudyPartnerPost p) {
        Label subj = new Label("Looking for a partner for " + (p.getSubject() != null ? p.getSubject() : "—"));
        subj.getStyleClass().add("profile-value");
        subj.setWrapText(true);
        Label meta = new Label((p.getUserName() != null ? p.getUserName() : "Anonymous") + " · " + formatTime(p.getCreatedAt()));
        meta.getStyleClass().add("post-meta");
        Label desc = new Label(p.getDescription() != null ? p.getDescription() : "");
        desc.getStyleClass().add("profile-label");
        desc.setWrapText(true);
        Button msgBtn = new Button("Message");
        msgBtn.getStyleClass().add("btn-primary");
        msgBtn.setOnAction(e -> AppRouter.navigateToChat(p.getUserId(), p.getUserName()));
        VBox card = new VBox(8);
        card.getStyleClass().addAll("content-card", "study-partner-card");
        card.getChildren().addAll(subj, meta, desc, msgBtn);
        return card;
    }

    private static String formatTime(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            return OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
        } catch (Exception e) { return iso; }
    }
}
