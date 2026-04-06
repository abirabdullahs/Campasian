package com.campasian.controller;

import com.campasian.model.MarketplaceItem;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class MarketplaceController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button sellBtn;
    @FXML private Button allBtn;
    @FXML private Button booksBtn;
    @FXML private Button electronicsBtn;
    @FXML private Button stationeryBtn;
    @FXML private VBox itemsVBox;
    @FXML private Label resultsLabel;
    @FXML private Label spotlightLabel;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
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

                List<MarketplaceItem> finalItems = items;
                Platform.runLater(() -> {
                    if (itemsVBox == null) return;
                    itemsVBox.getChildren().clear();
                    if (resultsLabel != null) {
                        resultsLabel.setText(finalItems.size() + (finalItems.size() == 1 ? " item live" : " items live"));
                    }
                    if (spotlightLabel != null) {
                        spotlightLabel.setText(finalItems.isEmpty()
                            ? "No active listings right now"
                            : "Latest drop: " + (finalItems.get(0).getTitle() != null ? finalItems.get(0).getTitle() : "Fresh listing"));
                    }
                    for (MarketplaceItem item : finalItems) {
                        itemsVBox.getChildren().add(buildItemCard(item));
                    }
                    if (finalItems.isEmpty()) {
                        Label empty = new Label("No items found.");
                        empty.getStyleClass().add("marketplace-empty-text");
                        itemsVBox.getChildren().add(empty);
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (itemsVBox != null) {
                        Label err = new Label("Unable to load marketplace.");
                        err.getStyleClass().add("marketplace-empty-text");
                        itemsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildItemCard(MarketplaceItem item) {
        Label seller = new Label(item.getUserName() != null ? item.getUserName() : "Anonymous");
        seller.getStyleClass().add("marketplace-seller");

        Label category = new Label(item.getCategory() != null ? item.getCategory() : "General");
        category.getStyleClass().add("marketplace-chip");

        Label price = new Label(item.getPrice() != null && !item.getPrice().isBlank() ? item.getPrice() : "Price on request");
        price.getStyleClass().add("marketplace-price");

        VBox sellerBlock = new VBox(4, seller, category);
        HBox topRow = new HBox(10);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(sellerBlock, spacer, price);

        Label title = new Label(item.getTitle() != null ? item.getTitle() : "Untitled");
        title.getStyleClass().add("marketplace-card-title");
        title.setWrapText(true);
        title.setCursor(javafx.scene.Cursor.HAND);
        title.setOnMouseClicked(e -> AppRouter.navigateToProfile(item.getUserId()));

        Label meta = new Label("Listed " + formatTime(item.getCreatedAt()));
        meta.getStyleClass().add("marketplace-meta");

        Label condition = new Label("Condition: " + (item.getCondition() != null ? item.getCondition() : "-"));
        condition.getStyleClass().add("marketplace-condition");

        Label desc = new Label(item.getDescription() != null ? item.getDescription() : "");
        desc.getStyleClass().add("marketplace-description");
        desc.setWrapText(true);

        Button contactBtn = new Button("Open Seller Profile");
        contactBtn.getStyleClass().add("marketplace-contact-btn");
        contactBtn.setOnAction(e -> AppRouter.navigateToProfile(item.getUserId()));

        VBox card = new VBox(8);
        card.getStyleClass().add("marketplace-card");
        card.getChildren().addAll(topRow, title, meta, condition, desc, contactBtn);
        return card;
    }

    private static String formatTime(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            return OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("MMM d"));
        } catch (Exception e) {
            return iso;
        }
    }
}
