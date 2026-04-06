package com.campasian.controller;

import com.campasian.model.StudyPartnerPost;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.view.AppRouter;
import com.campasian.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    for (StudyPartnerPost post : list) {
                        itemsVBox.getChildren().add(buildCard(post));
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

    private VBox buildCard(StudyPartnerPost post) {
        Label subj = new Label("Looking for a partner for " + (post.getSubject() != null ? post.getSubject() : "-"));
        subj.getStyleClass().add("study-card-title");
        subj.setWrapText(true);

        FlowPane tags = new FlowPane(8, 8);
        tags.getStyleClass().add("study-card-tags");

        Label subjectTag = new Label(post.getSubject() != null ? post.getSubject() : "General");
        subjectTag.getStyleClass().addAll("study-card-tag", "study-card-tag-subject");
        tags.getChildren().add(subjectTag);

        Label meta = new Label((post.getUserName() != null ? post.getUserName() : "Anonymous") + " · " + formatTime(post.getCreatedAt()));
        meta.getStyleClass().add("post-meta");

        Label desc = new Label(post.getDescription() != null ? post.getDescription() : "");
        desc.getStyleClass().add("study-card-description");
        desc.setWrapText(true);

        Button msgBtn = new Button("Message");
        msgBtn.getStyleClass().add("study-card-button");
        msgBtn.setOnAction(e -> AppRouter.navigateToChat(post.getUserId(), post.getUserName()));

        VBox card = new VBox(12);
        card.getStyleClass().add("study-partner-card");
        card.getChildren().addAll(subj, tags, meta, desc, msgBtn);
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
