package com.campasian.controller;

import com.campasian.model.CampusEvent;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Campus Events & Club Management. Interested toggle increments count.
 */
public class EventsController implements Initializable {

    @FXML private Button addBtn;
    @FXML private VBox eventsVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadEvents();
    }

    @FXML
    protected void onAddClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event-add-modal.fxml"));
            Parent root = loader.load();
            EventAddModalController ctrl = loader.getController();
            Stage s = new Stage();
            ctrl.setStage(s);
            s.setTitle("Create Event");
            s.setScene(new javafx.scene.Scene(root));
            s.initModality(Modality.APPLICATION_MODAL);
            s.initOwner(SceneManager.getPrimaryStage());
            ctrl.setOnSuccess(this::loadEvents);
            s.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadEvents() {
        if (eventsVBox == null) return;
        eventsVBox.getChildren().clear();
        new Thread(() -> {
            try {
                List<CampusEvent> list = ApiService.getInstance().getCampusEvents();
                Platform.runLater(() -> {
                    if (eventsVBox == null) return;
                    eventsVBox.getChildren().clear();
                    for (CampusEvent ev : list) {
                        eventsVBox.getChildren().add(buildCard(ev));
                    }
                    if (list.isEmpty()) {
                        Label empty = new Label("No events yet.");
                        empty.getStyleClass().add("profile-label");
                        eventsVBox.getChildren().add(empty);
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (eventsVBox != null) {
                        Label err = new Label("Unable to load events.");
                        err.getStyleClass().add("profile-label");
                        eventsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildCard(CampusEvent ev) {
        Label title = new Label(ev.getTitle() != null ? ev.getTitle() : "Untitled");
        title.getStyleClass().add("profile-value");
        title.setWrapText(true);
        Label meta = new Label((ev.getEventDate() != null ? ev.getEventDate() : "") + " · " + (ev.getVenue() != null ? ev.getVenue() : ""));
        meta.getStyleClass().add("post-meta");
        Label desc = new Label(ev.getDescription() != null ? ev.getDescription() : "");
        desc.getStyleClass().add("profile-label");
        desc.setWrapText(true);
        Button interestedBtn = new Button(ev.isUserInterested() ? "✓ Interested" : "Interested");
        interestedBtn.getStyleClass().add("btn-interested");
        if (ev.isUserInterested()) interestedBtn.getStyleClass().add("btn-interested-active");
        interestedBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().toggleEventInterest(ev.getId());
                loadEvents();
            } catch (ApiException ex) { /* ignore */ }
        });
        Label countLbl = new Label(ev.getInterestedCount() + " interested");
        countLbl.getStyleClass().add("profile-label");
        VBox card = new VBox(8);
        card.getStyleClass().addAll("content-card", "events-card");
        card.getChildren().addAll(title, meta, desc, interestedBtn, countLbl);
        return card;
    }
}
