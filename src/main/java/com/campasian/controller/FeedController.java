package com.campasian.controller;

import com.campasian.model.Comment;
import com.campasian.model.Post;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.SceneManager;
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

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the feed sub-view.
 */
public class FeedController implements Initializable {

    @FXML private VBox feedVBox;
    @FXML private Button feedGlobalBtn;
    @FXML private Button feedFollowingBtn;

    private static final String FEED_FILTER_ACTIVE = "feed-filter-active";
    private boolean feedFollowingOnly = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFeed();
    }

    @FXML
    protected void onFeedGlobalClick() {
        feedFollowingOnly = false;
        if (feedGlobalBtn != null) feedGlobalBtn.getStyleClass().add(FEED_FILTER_ACTIVE);
        if (feedFollowingBtn != null) feedFollowingBtn.getStyleClass().remove(FEED_FILTER_ACTIVE);
        loadFeed();
    }

    @FXML
    protected void onFeedFollowingClick() {
        feedFollowingOnly = true;
        if (feedFollowingBtn != null) feedFollowingBtn.getStyleClass().add(FEED_FILTER_ACTIVE);
        if (feedGlobalBtn != null) feedGlobalBtn.getStyleClass().remove(FEED_FILTER_ACTIVE);
        loadFeed();
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

    public void loadFeed() {
        if (feedVBox == null) return;
        feedVBox.getChildren().clear();

        try {
            List<Post> posts = ApiService.getInstance().getFeed(feedFollowingOnly);
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
        String timeStr = formatTimeAgo(post.getCreatedAt());

        Label meta = new Label(userName + university + " · " + timeStr);
        meta.getStyleClass().add("post-meta");

        Label content = new Label(post.getContent() != null ? post.getContent() : "");
        content.getStyleClass().add("post-content");
        content.setWrapText(true);

        int likeCount = post.getLikeCount();
        boolean liked = post.isLikedByMe();
        Button likeBtn = new Button((liked ? "♥ " : "♡ ") + (likeCount > 0 ? String.valueOf(likeCount) : ""));
        likeBtn.getStyleClass().add("post-action-btn");
        if (liked) likeBtn.getStyleClass().add("post-action-btn-liked");

        Button commentBtn = new Button("Comment");
        commentBtn.getStyleClass().add("post-action-btn");

        VBox commentsContainer = new VBox(8);
        commentsContainer.getStyleClass().add("comments-container");
        commentsContainer.setVisible(false);
        commentsContainer.setManaged(false);

        TextField commentField = new TextField();
        commentField.getStyleClass().add("comment-field");
        commentField.setPromptText("Write a comment...");
        Button submitComment = new Button("Post");
        submitComment.getStyleClass().add("btn-primary");
        HBox commentInput = new HBox(8);
        commentInput.getChildren().addAll(commentField, submitComment);

        likeBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().toggleLike(post.getId());
                post.setLikedByMe(!post.isLikedByMe());
                post.setLikeCount(post.getLikeCount() + (post.isLikedByMe() ? 1 : -1));
                likeBtn.setText((post.isLikedByMe() ? "♥ " : "♡ ") + (post.getLikeCount() > 0 ? String.valueOf(post.getLikeCount()) : ""));
                likeBtn.getStyleClass().remove("post-action-btn-liked");
                if (post.isLikedByMe()) likeBtn.getStyleClass().add("post-action-btn-liked");
            } catch (ApiException ex) { /* ignore */ }
        });

        commentBtn.setOnAction(e -> {
            boolean show = !commentsContainer.isVisible();
            commentsContainer.setVisible(show);
            commentsContainer.setManaged(show);
            if (show) loadCommentsInto(post.getId(), commentsContainer, commentField, submitComment);
        });

        submitComment.setOnAction(e -> {
            String c = commentField.getText();
            if (c != null && !c.isBlank()) {
                try {
                    ApiService.getInstance().addComment(post.getId(), c.trim());
                    commentField.clear();
                    loadCommentsInto(post.getId(), commentsContainer, commentField, submitComment);
                } catch (ApiException ex) { /* ignore */ }
            }
        });

        HBox actions = new HBox(16);
        actions.getStyleClass().add("post-actions");
        actions.getChildren().addAll(likeBtn, commentBtn);

        commentsContainer.getChildren().clear();
        commentsContainer.getChildren().add(commentInput);

        VBox card = new VBox(8);
        card.getStyleClass().add("post-card");
        card.setUserData(post);
        card.getChildren().addAll(meta, content, actions, commentsContainer);
        return card;
    }

    private void loadCommentsInto(Long postId, VBox container, TextField commentField, Button submitBtn) {
        container.getChildren().clear();
        HBox inputRow = new HBox(8);
        inputRow.getChildren().addAll(commentField, submitBtn);
        container.getChildren().add(inputRow);
        try {
            List<Comment> comments = ApiService.getInstance().fetchComments(postId);
            for (Comment c : comments) {
                String line = (c.getUserName() != null ? c.getUserName() : "Anonymous") + ": "
                    + (c.getContent() != null ? c.getContent() : "");
                Label lbl = new Label(line);
                lbl.getStyleClass().add("comment-text");
                lbl.setWrapText(true);
                Label time = new Label(formatCreatedAt(c.getCreatedAt()));
                time.getStyleClass().add("post-meta");
                VBox commentCard = new VBox(2);
                commentCard.getStyleClass().add("comment-card");
                commentCard.getChildren().addAll(lbl, time);
                container.getChildren().add(commentCard);
            }
        } catch (ApiException e) {
            Label err = new Label("Unable to load comments.");
            err.getStyleClass().add("profile-label");
            container.getChildren().add(err);
        }
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

    private static String formatTimeAgo(String iso) {
        if (iso == null || iso.isBlank()) return "";
        try {
            OffsetDateTime then = OffsetDateTime.parse(iso);
            Duration d = Duration.between(then.toInstant(), java.time.Instant.now());
            long s = d.getSeconds();
            if (s < 60) return "just now";
            if (s < 3600) return (s / 60) + " min ago";
            if (s < 86400) return (s / 3600) + " hours ago";
            if (s < 604800) return (s / 86400) + " days ago";
            return then.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
        } catch (Exception e) {
            return formatCreatedAt(iso);
        }
    }
}
