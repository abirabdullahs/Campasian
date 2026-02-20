package com.campasian.controller;

import com.campasian.model.LostFoundItem;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
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

/**
 * Lost & Found feed. Posts show "Lost" (red) or "Found" (gray). Location required.
 */
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

    private void setFilter(String t) {
        currentFilter = t;
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
        } catch (Exception e) { e.printStackTrace(); }
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
                    for (LostFoundItem i : items) {
                        itemsVBox.getChildren().add(buildItemCard(i));
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

    private VBox buildItemCard(LostFoundItem i) {
        boolean isLost = "lost".equalsIgnoreCase(i.getType());
        Label typeLabel = new Label(isLost ? "LOST" : "FOUND");
        typeLabel.setStyle(isLost ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-text-fill: #71717A; -fx-font-weight: bold;");
        Label title = new Label(i.getTitle() != null ? i.getTitle() : "Untitled");
        title.getStyleClass().add("profile-value");
        title.setWrapText(true);
        title.setCursor(javafx.scene.Cursor.HAND);
        title.setOnMouseClicked(e -> AppRouter.navigateToProfile(i.getUserId()));
        Label location = new Label("📍 " + (i.getLocation() != null ? i.getLocation() : "—"));
        location.getStyleClass().add("profile-label");
        location.setStyle("-fx-text-fill: #09090B;");
        Label meta = new Label((i.getUserName() != null ? i.getUserName() : "Anonymous") + " · " + formatTime(i.getCreatedAt()));
        meta.getStyleClass().add("post-meta");
        Label desc = new Label(i.getDescription() != null ? i.getDescription() : "");
        desc.getStyleClass().add("profile-label");
        desc.setWrapText(true);
        VBox card = new VBox(6);
        card.getStyleClass().addAll("content-card", "post-card");
        card.getChildren().addAll(typeLabel, title, location, meta, desc);
        return card;
    }

    private static String formatTime(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            return OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
        } catch (Exception e) { return iso; }
    }
}
