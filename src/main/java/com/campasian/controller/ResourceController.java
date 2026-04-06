package com.campasian.controller;

import com.campasian.CampasianApplication;
import com.campasian.model.CourseResource;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.view.SceneManager;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ResourceController implements Initializable {

    private static final String ACTIVE = "resource-filter-btn-active";

    @FXML private Button allDeptBtn, cseDeptBtn, eeeDeptBtn, bbaDeptBtn;
    @FXML private Button allSemBtn, sem1Btn, sem2Btn, sem3Btn;
    @FXML private Button addBtn;
    @FXML private VBox itemsVBox;

    private String filterDept;
    private String filterSem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filterDept = null;
        filterSem = null;
        updateDeptButtons();
        updateSemButtons();
        loadResources();
    }

    @FXML protected void onFilterAllDept() { filterDept = null; updateDeptButtons(); loadResources(); }
    @FXML protected void onFilterCSE() { filterDept = "CSE"; updateDeptButtons(); loadResources(); }
    @FXML protected void onFilterEEE() { filterDept = "EEE"; updateDeptButtons(); loadResources(); }
    @FXML protected void onFilterBBA() { filterDept = "BBA"; updateDeptButtons(); loadResources(); }
    @FXML protected void onFilterAllSem() { filterSem = null; updateSemButtons(); loadResources(); }
    @FXML protected void onFilterSem1() { filterSem = "1"; updateSemButtons(); loadResources(); }
    @FXML protected void onFilterSem2() { filterSem = "2"; updateSemButtons(); loadResources(); }
    @FXML protected void onFilterSem3() { filterSem = "3"; updateSemButtons(); loadResources(); }

    private void updateDeptButtons() {
        for (Button b : new Button[]{ allDeptBtn, cseDeptBtn, eeeDeptBtn, bbaDeptBtn }) {
            if (b != null) b.getStyleClass().remove(ACTIVE);
        }
        if (filterDept == null && allDeptBtn != null) allDeptBtn.getStyleClass().add(ACTIVE);
        else if ("CSE".equals(filterDept) && cseDeptBtn != null) cseDeptBtn.getStyleClass().add(ACTIVE);
        else if ("EEE".equals(filterDept) && eeeDeptBtn != null) eeeDeptBtn.getStyleClass().add(ACTIVE);
        else if ("BBA".equals(filterDept) && bbaDeptBtn != null) bbaDeptBtn.getStyleClass().add(ACTIVE);
    }

    private void updateSemButtons() {
        for (Button b : new Button[]{ allSemBtn, sem1Btn, sem2Btn, sem3Btn }) {
            if (b != null) b.getStyleClass().remove(ACTIVE);
        }
        if (filterSem == null && allSemBtn != null) allSemBtn.getStyleClass().add(ACTIVE);
        else if ("1".equals(filterSem) && sem1Btn != null) sem1Btn.getStyleClass().add(ACTIVE);
        else if ("2".equals(filterSem) && sem2Btn != null) sem2Btn.getStyleClass().add(ACTIVE);
        else if ("3".equals(filterSem) && sem3Btn != null) sem3Btn.getStyleClass().add(ACTIVE);
    }

    @FXML
    protected void onAddClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resource-add-modal.fxml"));
            Parent root = loader.load();
            ResourceAddModalController ctrl = loader.getController();
            Stage s = new Stage();
            ctrl.setStage(s);
            s.setTitle("Add Resource");
            s.setScene(new javafx.scene.Scene(root));
            s.initModality(Modality.APPLICATION_MODAL);
            s.initOwner(SceneManager.getPrimaryStage());
            ctrl.setOnSuccess(this::loadResources);
            s.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadResources() {
        if (itemsVBox == null) return;
        itemsVBox.getChildren().clear();
        String dept = filterDept;
        String sem = filterSem;
        new Thread(() -> {
            try {
                List<CourseResource> list = ApiService.getInstance().getCourseResources(dept, sem);
                Platform.runLater(() -> {
                    if (itemsVBox == null) return;
                    itemsVBox.getChildren().clear();
                    for (CourseResource resource : list) {
                        itemsVBox.getChildren().add(buildCard(resource));
                    }
                    if (list.isEmpty()) {
                        itemsVBox.getChildren().add(buildEmptyState(
                                "No resources found",
                                "Try another filter or add the first material for this department and semester."
                        ));
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (itemsVBox != null) {
                        itemsVBox.getChildren().add(buildEmptyState(
                                "Unable to load resources",
                                "The library could not be reached right now. Reload the page and try again."
                        ));
                    }
                });
            }
        }).start();
    }

    private VBox buildCard(CourseResource resource) {
        Label title = new Label(resource.getTitle() != null ? resource.getTitle() : "Untitled");
        title.getStyleClass().add("resource-card-title");
        title.setWrapText(true);

        Label dept = new Label(resource.getDepartment() != null ? resource.getDepartment() : "General");
        dept.getStyleClass().addAll("resource-chip", "resource-chip-dept");

        Label sem = new Label("Sem " + (resource.getSemester() != null ? resource.getSemester() : "-"));
        sem.getStyleClass().addAll("resource-chip", "resource-chip-sem");

        Label type = new Label(detectResourceType(resource));
        type.getStyleClass().addAll("resource-chip", "resource-chip-type");

        Label uploader = new Label("Uploaded by " + (resource.getUserName() != null ? resource.getUserName() : "Unknown"));
        uploader.getStyleClass().add("resource-meta");

        Label timestamp = new Label(resource.getCreatedAt() != null && !resource.getCreatedAt().isBlank()
                ? resource.getCreatedAt()
                : "Recently shared");
        timestamp.getStyleClass().add("resource-secondary-meta");

        Label description = new Label(buildDescription(resource));
        description.getStyleClass().add("resource-card-description");
        description.setWrapText(true);

        FlowPane metaRow = new FlowPane(8, 8);
        metaRow.getStyleClass().add("resource-badge-row");
        metaRow.getChildren().addAll(dept, sem, type);

        Button openBtn = new Button("Download/Open");
        openBtn.getStyleClass().add("btn-download");
        String link = resource.getDriveLink();
        if (link != null && !link.isBlank()) {
            openBtn.setOnAction(e -> {
                HostServices hs = CampasianApplication.getHostServicesStatic();
                if (hs != null) hs.showDocument(link);
            });
        } else {
            openBtn.setDisable(true);
        }

        VBox metaBlock = new VBox(4, uploader, timestamp);
        metaBlock.getStyleClass().add("resource-meta-block");

        HBox footer = new HBox(16, metaBlock, openBtn);
        footer.getStyleClass().add("resource-card-footer");
        HBox.setHgrow(metaBlock, Priority.ALWAYS);

        VBox top = new VBox(10, title, metaRow, description);
        top.getStyleClass().add("resource-card-top");

        VBox card = new VBox(14, top, footer);
        card.getStyleClass().add("resource-card");
        return card;
    }

    private VBox buildEmptyState(String titleText, String bodyText) {
        Label title = new Label(titleText);
        title.getStyleClass().add("resource-empty-title");

        Label body = new Label(bodyText);
        body.getStyleClass().add("resource-empty-copy");
        body.setWrapText(true);

        VBox emptyState = new VBox(6, title, body);
        emptyState.getStyleClass().add("resource-empty-state");
        return emptyState;
    }

    private String detectResourceType(CourseResource resource) {
        String haystack = ((resource.getTitle() != null ? resource.getTitle() : "") + " "
                + (resource.getDriveLink() != null ? resource.getDriveLink() : "")).toLowerCase();
        if (haystack.contains("slide") || haystack.contains("ppt")) return "Slides";
        if (haystack.contains("question") || haystack.contains("quiz") || haystack.contains("exam")) return "Questions";
        if (haystack.contains("note")) return "Notes";
        return "Resource";
    }

    private String buildDescription(CourseResource resource) {
        String type = detectResourceType(resource);
        String dept = resource.getDepartment() != null ? resource.getDepartment() : "General";
        String sem = resource.getSemester() != null ? resource.getSemester() : "-";
        if (resource.getDriveLink() != null && !resource.getDriveLink().isBlank()) {
            return type + " for " + dept + " semester " + sem + " with an attached Drive link.";
        }
        return type + " for " + dept + " semester " + sem + ". No external link attached yet.";
    }
}
