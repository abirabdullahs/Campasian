package com.campasian.controller;

import com.campasian.model.Post;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiService;
import com.campasian.service.AuthService;
import com.campasian.service.ApiException;
import com.campasian.view.SceneManager;
import com.campasian.view.ViewPaths;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the dashboard/home view. Fetches and displays user profile and feed.
 */
public class HomeController implements Initializable {

    @FXML private Label fullNameLabel;
    @FXML private Label universityLabel;
    @FXML private Label einLabel;
    @FXML private TextArea postContentArea;
    @FXML private VBox feedVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfile();
        loadFeed();
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
    protected void onPostClick() {
        String content = postContentArea != null ? postContentArea.getText() : null;
        if (content == null || content.isBlank()) return;

        try {
            ApiService.getInstance().sendPost(content.trim());
            if (postContentArea != null) postContentArea.clear();
            loadFeed();
        } catch (ApiException e) {
            // Could show error in UI; for now silent
        }
    }

    @FXML
    protected void onHomeClick() {
        loadProfile();
        loadFeed();
    }

    @FXML
    protected void onSettingsClick() {
        // Placeholder for settings
    }

    @FXML
    protected void onLogoutClick() {
        AuthService.getInstance().logout();
        SceneManager.navigateTo(ViewPaths.LOGIN_VIEW);
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
