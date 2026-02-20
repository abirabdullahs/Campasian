package com.campasian.controller;

import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Modal for posting a marketplace item.
 */
public class SellItemModalController implements Initializable {

    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private TextField conditionField;
    @FXML private TextArea descField;

    private Stage stage;
    private Runnable onSuccess;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (categoryCombo != null) {
            categoryCombo.getItems().setAll("Books", "Electronics", "Stationery");
            categoryCombo.getSelectionModel().selectFirst();
        }
    }

    public void setStage(Stage stage) { this.stage = stage; }
    public Stage getStage() { return stage; }
    public void setOnSuccess(Runnable r) { this.onSuccess = r; }

    @FXML
    protected void onPost() {
        String title = titleField != null ? titleField.getText() : null;
        String price = priceField != null ? priceField.getText() : null;
        String condition = conditionField != null ? conditionField.getText() : null;
        String category = categoryCombo != null ? categoryCombo.getValue() : null;
        if (title == null || title.isBlank()) return;
        if (price == null || price.isBlank()) return;
        if (condition == null || condition.isBlank()) return;
        if (category == null || category.isBlank()) return;
        try {
            ApiService.getInstance().createMarketplaceItem(
                title.trim(),
                descField != null ? descField.getText() : null,
                price.trim(),
                condition.trim(),
                category
            );
            if (onSuccess != null) onSuccess.run();
            if (stage != null) stage.close();
        } catch (ApiException e) { /* ignore */ }
    }

    @FXML
    protected void onCancel() {
        if (stage != null) stage.close();
    }
}
