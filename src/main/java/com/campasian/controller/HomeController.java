package com.campasian.controller;

import com.campasian.model.Post;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.AuthService;
import com.campasian.service.ApiException;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the dashboard/home view. Manages sidebar navigation, sub-views, and modal post editor.
 */
public class HomeController implements Initializable {

    @FXML private Label fullNameLabel;
    @FXML private Label universityLabel;
    @FXML private Label einLabel;
    @FXML private VBox feedVBox;
    @FXML private StackPane contentStack;
    @FXML private Button feedBtn;
    @FXML private Button communityBtn;
    @FXML private Button profileBtn;
    @FXML private Button settingsBtn;
    @FXML private Node feedView;
    @FXML private Node communityView;
    @FXML private Node profileView;
    @FXML private Node settingsView;

    private static final String SIDEBAR_ACTIVE = "sidebar-btn-active";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfile();
        loadFeed();
        showView(feedView);
        updateSidebarActive(feedBtn);
    }

    private void loadProfile() {
        try {
            UserProfile profile = AuthService.getInstance().getCurrentUserProfile();
            if (profile != null) {
                fullNameLabel.setText(profile.getFullName() != null ? profile.getFullName() : "—");
                universityLabel.setText(profile.getUniversityName() != null ? profile.getUniversityName() : "—");
                einLabel.setText(profile.getEinNumber() != null ? profile.getEinNumber() : "—");
            } else {
                fullNameLabel.setText("—");
                universityLabel.setText("—");
                einLabel.setText("—");
            }
        } catch (ApiException e) {
            if (fullNameLabel != null) fullNameLabel.setText("—");
            if (universityLabel != null) universityLabel.setText("—");
            if (einLabel != null) einLabel.setText("—");
        }
    }

    @FXML
    protected void onCreatePostClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/post-editor-modal.fxml"));
            Parent root = loader.load();
            PostEditorModalController ctrl = loader.getController();

            Stage modalStage = new Stage();
            modalStage.setTitle("Create Post");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(SceneManager.getPrimaryStage());
            modalStage.setResizable(false);

            ctrl.setStage(modalStage);
            ctrl.setOnPostSuccess(this::loadFeed);

            modalStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onFeedClick() {
        showView(feedView);
        updateSidebarActive(feedBtn);
        loadFeed();
    }

    @FXML
    protected void onCommunityClick() {
        showView(communityView);
        updateSidebarActive(communityBtn);
    }

    @FXML
    protected void onProfileClick() {
        showView(profileView);
        updateSidebarActive(profileBtn);
        loadProfile();
    }

    @FXML
    protected void onSettingsClick() {
        showView(settingsView);
        updateSidebarActive(settingsBtn);
    }

    @FXML
    protected void onLogoutClick() {
        AuthService.getInstance().logout();
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
    }

    private void showView(Node view) {
        if (contentStack == null || view == null) return;
        for (Node child : contentStack.getChildren()) {
            child.setVisible(child == view);
            child.setManaged(child == view);
        }
        view.toFront();
    }

    private void updateSidebarActive(Button active) {
        clearSidebarActive();
        if (active != null) active.getStyleClass().add(SIDEBAR_ACTIVE);
    }

    private void clearSidebarActive() {
        if (feedBtn != null) feedBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
        if (communityBtn != null) communityBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
        if (profileBtn != null) profileBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
        if (settingsBtn != null) settingsBtn.getStyleClass().remove(SIDEBAR_ACTIVE);
    }

    private void loadFeed() {
        if (feedVBox == null) return;
        feedVBox.getChildren().clear();

        try {
            List<Post> posts = ApiService.getInstance().getAllPosts();
            for (Post post : posts) {
                feedVBox.getChildren().add(buildPostCard(post));
            }
        } catch (ApiException e) {
            Label placeholder = new Label("Unable to load feed.");
            placeholder.getStyleClass().add("profile-label");
            feedVBox.getChildren().add(placeholder);
        }
    }

    private VBox buildPostCard(Post post) {
        String userName = post.getUserName() != null ? post.getUserName() : "Anonymous";
        String university = post.getUniversity() != null && !post.getUniversity().isBlank()
            ? " · " + post.getUniversity() : "";
        String timeStr = formatCreatedAt(post.getCreatedAt());

        Label meta = new Label(userName + university + " · " + timeStr);
        meta.getStyleClass().add("post-meta");

        Label content = new Label(post.getContent() != null ? post.getContent() : "");
        content.getStyleClass().add("post-content");
        content.setWrapText(true);

        VBox card = new VBox(8);
        card.getStyleClass().add("post-card");
        card.getChildren().addAll(meta, content);
        return card;
    }

    private static String formatCreatedAt(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            OffsetDateTime dt = OffsetDateTime.parse(iso);
            return dt.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
        } catch (Exception e) {
            return iso.length() > 16 ? iso.substring(0, 16) : iso;
        }
    }
}
