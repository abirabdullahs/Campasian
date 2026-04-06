package com.campasian.controller;

import com.campasian.model.LostFoundItem;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.view.AppRouter;
import com.campasian.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

public class LostFoundController implements Initializable {

    @FXML private Button allBtn;
    @FXML private Button lostBtn;
    @FXML private Button foundBtn;
    @FXML private Button postBtn;
    @FXML private VBox itemsVBox;

    private String currentFilter;
    private static final String FILTER_ACTIVE = "marketplace-filter-btn-active";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentFilter = null;
        updateFilterButtons();
        loadItems();
    }

    @FXML protected void onFilterAll() { setFilter(null); }
    @FXML protected void onFilterLost() { setFilter("lost"); }
    @FXML protected void onFilterFound() { setFilter("found"); }

    private void setFilter(String type) {
        currentFilter = type;
        updateFilterButtons();
        loadItems();
    }

    private void updateFilterButtons() {
        if (allBtn != null) allBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (lostBtn != null) lostBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (foundBtn != null) foundBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (currentFilter == null && allBtn != null) allBtn.getStyleClass().add(FILTER_ACTIVE);
        else if ("lost".equals(currentFilter) && lostBtn != null) lostBtn.getStyleClass().add(FILTER_ACTIVE);
        else if ("found".equals(currentFilter) && foundBtn != null) foundBtn.getStyleClass().add(FILTER_ACTIVE);
    }

    @FXML
    protected void onPostClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/lost-found-modal.fxml"));
            Parent root = loader.load();
            LostFoundModalController ctrl = loader.getController();
            Stage s = new Stage();
            ctrl.setStage(s);
            s.setTitle("Post Lost or Found");
            s.setScene(new Scene(root));
            s.initModality(Modality.APPLICATION_MODAL);
            s.initOwner(SceneManager.getPrimaryStage());
            ctrl.setOnSuccess(this::loadItems);
            s.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadItems() {
        if (itemsVBox == null) return;
        itemsVBox.getChildren().clear();
        String filter = currentFilter;
        new Thread(() -> {
            try {
                List<LostFoundItem> items = ApiService.getInstance().getLostFoundItems(filter);
                Platform.runLater(() -> {
                    if (itemsVBox == null) return;
                    itemsVBox.getChildren().clear();
                    for (LostFoundItem item : items) {
                        itemsVBox.getChildren().add(buildItemCard(item));
                    }
                    if (items.isEmpty()) {
                        Label empty = new Label("No lost or found posts yet.");
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

    private VBox buildItemCard(LostFoundItem item) {
        boolean isLost = "lost".equalsIgnoreCase(item.getType());

        Label typeLabel = new Label(isLost ? "LOST" : "FOUND");
        typeLabel.getStyleClass().addAll("lost-found-badge", isLost ? "lost-found-badge-lost" : "lost-found-badge-found");

        Label title = new Label(item.getTitle() != null ? item.getTitle() : "Untitled");
        title.getStyleClass().add("lost-found-title");
        title.setWrapText(true);
        title.setCursor(javafx.scene.Cursor.HAND);
        title.setOnMouseClicked(e -> AppRouter.navigateToProfile(item.getUserId()));

        Label location = new Label("Location: " + (item.getLocation() != null ? item.getLocation() : "-"));
        location.getStyleClass().add("lost-found-location");

        Label meta = new Label((item.getUserName() != null ? item.getUserName() : "Anonymous") + " · " + formatTime(item.getCreatedAt()));
        meta.getStyleClass().add("post-meta");

        Label desc = new Label(item.getDescription() != null ? item.getDescription() : "");
        desc.getStyleClass().add("lost-found-description");
        desc.setWrapText(true);

        VBox card = new VBox(6);
        card.getStyleClass().add("lost-found-card");
        card.getChildren().addAll(typeLabel, title, location, meta, desc);

        // Add Message button if not own post
        String currentUserId = ApiService.getInstance().getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(item.getUserId())) {
            Button messageBtn = new Button("Message");
            messageBtn.getStyleClass().add("btn-primary");
            messageBtn.setOnAction(e -> AppRouter.navigateToChat(item.getUserId(), item.getUserName()));
            card.getChildren().add(messageBtn);
        }

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
