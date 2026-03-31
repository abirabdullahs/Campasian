package com.campasian.controller;

import com.campasian.model.CampusEvent;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    for (CampusEvent event : list) {
                        eventsVBox.getChildren().add(buildCard(event));
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

    private VBox buildCard(CampusEvent event) {
        Label title = new Label(event.getTitle() != null ? event.getTitle() : "Untitled");
        title.getStyleClass().add("events-title");
        title.setWrapText(true);

        Label dateChip = new Label(event.getEventDate() != null ? event.getEventDate() : "TBA");
        dateChip.getStyleClass().add("events-chip");
        Label venueChip = new Label(event.getVenue() != null ? event.getVenue() : "Venue TBA");
        venueChip.getStyleClass().add("events-chip");
        FlowPane meta = new FlowPane(8, 8);
        meta.getChildren().addAll(dateChip, venueChip);

        Label desc = new Label(event.getDescription() != null ? event.getDescription() : "");
        desc.getStyleClass().add("events-description");
        desc.setWrapText(true);

        Button interestedBtn = new Button(event.isUserInterested() ? "Interested" : "Join Interest");
        interestedBtn.getStyleClass().add("btn-interested");
        if (event.isUserInterested()) interestedBtn.getStyleClass().add("btn-interested-active");
        interestedBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().toggleEventInterest(event.getId());
                loadEvents();
            } catch (ApiException ignored) {
            }
        });

        Label countLbl = new Label(event.getInterestedCount() + " interested");
        countLbl.getStyleClass().add("events-count");

        VBox card = new VBox(8);
        card.getStyleClass().addAll("content-card", "events-card");
        card.getChildren().addAll(title, meta, desc, interestedBtn, countLbl);
        return card;
    }
}
