package com.campasian.controller;

import com.campasian.CampasianApplication;
import javafx.application.HostServices;
import com.campasian.model.CourseResource;
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
 * Course Resource Library. Filter by Department and Semester.
 */
public class ResourceController implements Initializable {

    private static final String ACTIVE = "resource-filter-btn-active";
    private static final String[] DEPTS = { "CSE", "EEE", "BBA" };
    private static final String[] SEMS = { "1", "2", "3" };

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
    @FXML protected void onFilterCSE()   { filterDept = "CSE"; updateDeptButtons(); loadResources(); }
    @FXML protected void onFilterEEE()   { filterDept = "EEE"; updateDeptButtons(); loadResources(); }
    @FXML protected void onFilterBBA()   { filterDept = "BBA"; updateDeptButtons(); loadResources(); }
    @FXML protected void onFilterAllSem() { filterSem = null; updateSemButtons(); loadResources(); }
    @FXML protected void onFilterSem1()  { filterSem = "1"; updateSemButtons(); loadResources(); }
    @FXML protected void onFilterSem2()  { filterSem = "2"; updateSemButtons(); loadResources(); }
    @FXML protected void onFilterSem3()  { filterSem = "3"; updateSemButtons(); loadResources(); }

    private void updateDeptButtons() {
        for (Button b : new Button[]{ allDeptBtn, cseDeptBtn, eeeDeptBtn, bbaDeptBtn })
            if (b != null) b.getStyleClass().remove(ACTIVE);
        if (filterDept == null && allDeptBtn != null) allDeptBtn.getStyleClass().add(ACTIVE);
        else if ("CSE".equals(filterDept) && cseDeptBtn != null) cseDeptBtn.getStyleClass().add(ACTIVE);
        else if ("EEE".equals(filterDept) && eeeDeptBtn != null) eeeDeptBtn.getStyleClass().add(ACTIVE);
        else if ("BBA".equals(filterDept) && bbaDeptBtn != null) bbaDeptBtn.getStyleClass().add(ACTIVE);
    }

    private void updateSemButtons() {
        for (Button b : new Button[]{ allSemBtn, sem1Btn, sem2Btn, sem3Btn })
            if (b != null) b.getStyleClass().remove(ACTIVE);
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
        } catch (Exception e) { e.printStackTrace(); }
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
                    for (CourseResource r : list) {
                        itemsVBox.getChildren().add(buildCard(r));
                    }
                    if (list.isEmpty()) {
                        Label empty = new Label("No resources yet.");
                        empty.getStyleClass().add("profile-label");
                        itemsVBox.getChildren().add(empty);
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (itemsVBox != null) {
                        Label err = new Label("Unable to load resources.");
                        err.getStyleClass().add("profile-label");
                        itemsVBox.getChildren().add(err);
                    }
                });
            }
        }).start();
    }

    private VBox buildCard(CourseResource r) {
        Label title = new Label(r.getTitle() != null ? r.getTitle() : "Untitled");
        title.getStyleClass().add("profile-value");
        title.setWrapText(true);
        Label meta = new Label((r.getDepartment() != null ? r.getDepartment() : "") + " · Sem " + (r.getSemester() != null ? r.getSemester() : "") + " · " + (r.getUserName() != null ? r.getUserName() : ""));
        meta.getStyleClass().add("post-meta");
        javafx.scene.control.Button openBtn = new javafx.scene.control.Button("Download/Open");
        openBtn.getStyleClass().addAll("btn-primary", "btn-download");
        String link = r.getDriveLink();
        if (link != null && !link.isBlank()) {
            openBtn.setOnAction(e -> {
                HostServices hs = CampasianApplication.getHostServicesStatic();
                if (hs != null) hs.showDocument(link);
            });
        } else {
            openBtn.setDisable(true);
        }
        VBox card = new VBox(8);
        card.getStyleClass().addAll("content-card", "resource-card");
        card.getChildren().addAll(title, meta, openBtn);
        return card;
    }
}
