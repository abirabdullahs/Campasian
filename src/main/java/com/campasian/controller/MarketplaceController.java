package com.campasian.controller;

import com.campasian.model.MarketplaceItem;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.AppRouter;
import com.campasian.view.SceneManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Campus Marketplace. Filter by Books, Electronics, Stationery.
 */
public class MarketplaceController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button sellBtn;
    @FXML private Button allBtn;
    @FXML private Button booksBtn;
    @FXML private Button electronicsBtn;
    @FXML private Button stationeryBtn;
    @FXML private VBox itemsVBox;

    private String currentFilter;
    private final PauseTransition debouncer = new PauseTransition(Duration.millis(300));
    private static final String FILTER_ACTIVE = "marketplace-filter-btn-active";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentFilter = null;
        updateFilterButtons();
        loadItems();
        if (searchField != null) {
            searchField.textProperty().addListener((o, oldVal, newVal) -> {
                debouncer.setOnFinished(e -> loadItems());
                debouncer.stop();
                debouncer.playFromStart();
            });
        }
    }

    @FXML protected void onFilterAll() { setFilter(null); }
    @FXML protected void onFilterBooks() { setFilter("Books"); }
    @FXML protected void onFilterElectronics() { setFilter("Electronics"); }
    @FXML protected void onFilterStationery() { setFilter("Stationery"); }

    private void setFilter(String cat) {
        currentFilter = cat;
        updateFilterButtons();
        loadItems();
    }

    private void updateFilterButtons() {
        clearFilterActive();
        if (currentFilter == null && allBtn != null) allBtn.getStyleClass().add(FILTER_ACTIVE);
        else if ("Books".equals(currentFilter) && booksBtn != null) booksBtn.getStyleClass().add(FILTER_ACTIVE);
        else if ("Electronics".equals(currentFilter) && electronicsBtn != null) electronicsBtn.getStyleClass().add(FILTER_ACTIVE);
        else if ("Stationery".equals(currentFilter) && stationeryBtn != null) stationeryBtn.getStyleClass().add(FILTER_ACTIVE);
    }

    private void clearFilterActive() {
        if (allBtn != null) allBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (booksBtn != null) booksBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (electronicsBtn != null) electronicsBtn.getStyleClass().remove(FILTER_ACTIVE);
        if (stationeryBtn != null) stationeryBtn.getStyleClass().remove(FILTER_ACTIVE);
    }

    @FXML
    protected void onSellItemClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sell-item-modal.fxml"));
            Parent root = loader.load();
            SellItemModalController ctrl = loader.getController();
            Stage s = new Stage();
            ctrl.setStage(s);
            s.setTitle("Sell Item");
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
        String search = searchField != null ? searchField.getText() : null;
        new Thread(() -> {
            try {
                List<MarketplaceItem> items = ApiService.getInstance().getMarketplaceItems(filter);
                if (search != null && !search.trim().isBlank()) {
                    String q = search.trim().toLowerCase();
                    items = items.stream()
                        .filter(i -> (i.getTitle() != null && i.getTitle().toLowerCase().contains(q))
                            || (i.getDescription() != null && i.getDescription().toLowerCase().contains(q)))
                        .toList();
                }

                final List<MarketplaceItem> finalItems = items;
                Platform.runLater(() -> {
                    if (itemsVBox == null) return;
                    itemsVBox.getChildren().clear();
                    for (MarketplaceItem i : finalItems) {
                        itemsVBox.getChildren().add(buildItemCard(i));
                    }
                    if (finalItems.isEmpty()) {
                        Label empty = new Label("No items found.");
                        empty.getStyleClass().add("profile-label");
                        itemsVBox.getChildren().add(empty);
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (itemsVBox != null) {
                        Label err = new Label("Unable to load marketplace.");
                        err.getStyleClass().add("profile-label");
                        itemsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildItemCard(MarketplaceItem i) {
        Label title = new Label(i.getTitle() != null ? i.getTitle() : "Untitled");
        title.getStyleClass().add("profile-value");
        title.setWrapText(true);
        title.setCursor(javafx.scene.Cursor.HAND);
        title.setOnMouseClicked(e -> AppRouter.navigateToProfile(i.getUserId()));
        Label meta = new Label((i.getUserName() != null ? i.getUserName() : "Anonymous") + " · " + (i.getCategory() != null ? i.getCategory() : "") + " · " + formatTime(i.getCreatedAt()));
        meta.getStyleClass().add("post-meta");
        Label price = new Label(i.getPrice() != null ? i.getPrice() : "");
        price.getStyleClass().add("profile-stat-value");
        Label cond = new Label("Condition: " + (i.getCondition() != null ? i.getCondition() : "—"));
        cond.getStyleClass().add("profile-label");
        Label desc = new Label(i.getDescription() != null ? i.getDescription() : "");
        desc.getStyleClass().add("profile-label");
        desc.setWrapText(true);
        VBox card = new VBox(8);
        card.getStyleClass().addAll("marketplace-card", "content-card");
        card.getChildren().addAll(title, meta, price, cond, desc);
        return card;
    }

    private static String formatTime(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            return OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("MMM d"));
        } catch (Exception e) { return iso; }
    }
}
