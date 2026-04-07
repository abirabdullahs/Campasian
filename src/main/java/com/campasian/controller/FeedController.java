package com.campasian.controller;

import com.campasian.model.Comment;
import com.campasian.model.Post;
import com.campasian.view.AppRouter;
import com.campasian.service.ApiService;
import com.campasian.service.ApiException;
import com.campasian.view.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

        new Thread(() -> {
            try {
                List<Post> posts = ApiService.getInstance().getFeed(feedFollowingOnly);
                
                // Sort posts by date (newest first) then shuffle within groups for variety
                posts.sort((p1, p2) -> {
                    try {
                        OffsetDateTime d1 = OffsetDateTime.parse(p1.getCreatedAt());
                        OffsetDateTime d2 = OffsetDateTime.parse(p2.getCreatedAt());
                        return d2.compareTo(d1); // Newest first
                    } catch (Exception e) {
                        return 0;
                    }
                });
                
                // Shuffle posts in groups to add variety while keeping new posts visible
                List<Post> shuffledPosts = shufflePostsWithPriority(posts);
                
                Platform.runLater(() -> {
                    if (feedVBox == null) return;
                    feedVBox.getChildren().clear();
                    int postCount = 0;
                    for (Post post : shuffledPosts) {
                        feedVBox.getChildren().add(buildPostCard(post));
                        postCount++;
                        // Add user suggestions every 20-25 posts
                        if (postCount % 25 == 0) {
                            VBox suggestionCard = buildUserSuggestionCard();
                            if (suggestionCard != null) {
                                feedVBox.getChildren().add(suggestionCard);
                            }
                        }
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (feedVBox != null) {
                        feedVBox.getChildren().clear();
                        Label placeholder = new Label("Unable to load feed.");
                        placeholder.getStyleClass().add("profile-label");
                        feedVBox.getChildren().add(placeholder);
                    }
                });
            }
        }).start();
    }

    private List<Post> shufflePostsWithPriority(List<Post> posts) {
        if (posts.size() <= 1) return posts;
        
        List<Post> result = new ArrayList<>();
        
        // Divide posts into groups: newest (first 30%), middle (next 40%), older (last 30%)
        int newSize = Math.max(1, posts.size() / 3);
        int middleSize = Math.max(1, (posts.size() * 2) / 3) - newSize;
        
        List<Post> newest = new ArrayList<>(posts.subList(0, newSize));
        List<Post> middle = new ArrayList<>(posts.subList(newSize, newSize + middleSize));
        List<Post> older = new ArrayList<>(posts.subList(newSize + middleSize, posts.size()));
        
        // Shuffle middle and older, but keep newest mostly in order
        Collections.shuffle(middle);
        Collections.shuffle(older);
        
        // Interleave: 2 newest, 1-2 middle, 1 older, repeat
        int newestIdx = 0, middleIdx = 0, olderIdx = 0;
        
        while (newestIdx < newest.size() || middleIdx < middle.size() || olderIdx < older.size()) {
            // Add 2 newest posts
            if (newestIdx < newest.size()) {
                result.add(newest.get(newestIdx++));
            }
            if (newestIdx < newest.size()) {
                result.add(newest.get(newestIdx++));
            }
            
            // Add 1-2 middle posts
            if (middleIdx < middle.size()) {
                result.add(middle.get(middleIdx++));
            }
            if (middleIdx < middle.size() && Math.random() > 0.5) {
                result.add(middle.get(middleIdx++));
            }
            
            // Add 1 older post
            if (olderIdx < older.size()) {
                result.add(older.get(olderIdx++));
            }
        }
        
        return result;
    }

    private VBox buildPostCard(Post post) {
        String userName = post.getUserName() != null ? post.getUserName() : "Anonymous";
        String university = post.getUniversity() != null && !post.getUniversity().isBlank()
            ? post.getUniversity() : "";
        String timeStr = formatTimeAgo(post.getCreatedAt());

        // Facebook-style header with avatar and user info
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // User avatar
        Label avatarLabel = new Label(userName.substring(0, 1).toUpperCase());
        avatarLabel.getStyleClass().add("post-avatar");
        avatarLabel.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                             "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                             "-fx-alignment: center; -fx-padding: 8; -fx-background-radius: 100%;");
        avatarLabel.setPrefWidth(40);
        avatarLabel.setPrefHeight(40);
        
        // User info column
        VBox userInfo = new VBox(2);
        Label nameLabel = new Label(userName);
        nameLabel.getStyleClass().add("post-author");
        nameLabel.setCursor(javafx.scene.Cursor.HAND);
        nameLabel.setOnMouseClicked(e -> AppRouter.navigateToProfile(post.getUserId()));
        
        Label metaLabel = new Label(university + (university.length() > 0 ? " · " : "") + timeStr);
        metaLabel.getStyleClass().add("post-meta");
        
        userInfo.getChildren().addAll(nameLabel, metaLabel);
        headerBox.getChildren().addAll(avatarLabel, userInfo);
        
        // Post content
        Label content = new Label(post.getContent() != null ? post.getContent() : "");
        content.getStyleClass().add("post-content");
        content.setWrapText(true);
        content.setStyle("-fx-padding: 8 0 0 0; -fx-font-size: 14px; -fx-line-spacing: 4;");

        // Post image
        StackPane postImageFrame = null;
        if (post.getImageUrl() != null && !post.getImageUrl().isBlank()) {
            try {
                ImageView postImageView = new ImageView(new Image(post.getImageUrl(), true));
                postImageView.setFitWidth(520);
                postImageView.setFitHeight(340);
                postImageView.setPreserveRatio(true);
                postImageView.getStyleClass().add("post-image");
                postImageFrame = new StackPane(postImageView);
                postImageFrame.getStyleClass().add("post-image-frame");
                postImageFrame.setStyle("-fx-padding: 12 0 0 0;");
            } catch (Exception ignored) {}
        }

        // Like/Comment stats
        int likeCount = post.getLikeCount();
        int commentCount = post.getCommentCount();
        boolean liked = post.isLikedByMe();
        
        HBox statsBar = new HBox(12);
        statsBar.setStyle("-fx-padding: 12 0; -fx-border-color: #27272a; -fx-border-width: 1 0 0 0;");
        if (likeCount > 0) {
            Label likesStat = new Label("♥ " + likeCount + " likes");
            likesStat.getStyleClass().add("post-meta");
            statsBar.getChildren().add(likesStat);
        }
        if (commentCount > 0) {
            Label commentsStat = new Label("💬 " + commentCount + " comments");
            commentsStat.getStyleClass().add("post-meta");
            statsBar.getChildren().add(commentsStat);
        }
        
        // Action buttons
        Button likeBtn = new Button((liked ? "♥" : "♡") + " Like");
        likeBtn.getStyleClass().add("post-action-btn");
        if (liked) likeBtn.getStyleClass().add("post-action-btn-liked");
        likeBtn.setStyle("-fx-padding: 8 16; -fx-text-fill: " + (liked ? "#ef4444" : "#a1a1aa") + ";");

        Button commentBtn = new Button("💬 Comment");
        commentBtn.getStyleClass().add("post-action-btn");
        commentBtn.setStyle("-fx-padding: 8 16; -fx-text-fill: #a1a1aa;");

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
                likeBtn.setText((post.isLikedByMe() ? "♥" : "♡") + " Like");
                likeBtn.setStyle("-fx-padding: 8 16; -fx-text-fill: " + (post.isLikedByMe() ? "#ef4444" : "#a1a1aa") + ";");
                likeBtn.getStyleClass().remove("post-action-btn-liked");
                if (post.isLikedByMe()) likeBtn.getStyleClass().add("post-action-btn-liked");
            } catch (ApiException ex) { /* ignore */ }
        });

        commentBtn.setOnAction(e -> {
            boolean show = !commentsContainer.isVisible();
            commentsContainer.setVisible(show);
            commentsContainer.setManaged(show);
            if (show) loadCommentsInto(post, commentsContainer, commentField, submitComment, commentBtn);
        });

        submitComment.setOnAction(e -> {
            String c = commentField.getText();
            if (c != null && !c.isBlank()) {
                try {
                    ApiService.getInstance().addComment(post.getId(), c.trim());
                    post.setCommentCount(post.getCommentCount() + 1);
                    commentBtn.setText("💬 Comment");
                    commentField.clear();
                    loadCommentsInto(post, commentsContainer, commentField, submitComment, commentBtn);
                } catch (ApiException ex) { /* ignore */ }
            }
        });

        HBox actions = new HBox(4);
        actions.getStyleClass().add("post-actions");
        actions.setStyle("-fx-padding: 8 0; -fx-border-color: #27272a; -fx-border-width: 1 0 0 0;");
        actions.getChildren().addAll(likeBtn, commentBtn);

        commentsContainer.getChildren().clear();
        commentsContainer.getChildren().add(commentInput);

        // Assemble card
        VBox card = new VBox(0);
        card.getStyleClass().add("post-card");
        card.setUserData(post);
        card.getChildren().add(headerBox);
        if (content.getText() != null && !content.getText().isEmpty()) {
            card.getChildren().add(content);
        }
        if (postImageFrame != null) card.getChildren().add(postImageFrame);
        if (likeCount > 0 || commentCount > 0) card.getChildren().add(statsBar);
        card.getChildren().addAll(actions, commentsContainer);
        return card;
    }

    private void loadCommentsInto(Post post, VBox container, TextField commentField, Button submitBtn, Button commentBtn) {
        container.getChildren().clear();
        HBox inputRow = new HBox(8);
        inputRow.getChildren().addAll(commentField, submitBtn);
        container.getChildren().add(inputRow);
        try {
            List<Comment> comments = ApiService.getInstance().fetchComments(post.getId());
            if (commentBtn != null && post != null) {
                post.setCommentCount(comments.size());
                commentBtn.setText("💬 " + (comments.size() > 0 ? String.valueOf(comments.size()) : ""));
            }
            for (Comment c : comments) {
                String userName = c.getUserName() != null ? c.getUserName() : "Anonymous";
                String content = c.getContent() != null ? c.getContent() : "";
                String timeStr = formatCreatedAt(c.getCreatedAt());

                String avatarText = userName.isBlank() ? "?" : userName.substring(0, 1).toUpperCase();
                Label avatarLabel = new Label(avatarText);
                avatarLabel.getStyleClass().add("comment-avatar");
                avatarLabel.setMinSize(32, 32);
                avatarLabel.setPrefSize(32, 32);
                avatarLabel.setMaxSize(32, 32);
                avatarLabel.setAlignment(javafx.geometry.Pos.CENTER);

                Label userLabel = new Label(userName);
                userLabel.getStyleClass().add("comment-author");

                Label timeLabel = new Label(timeStr);
                timeLabel.getStyleClass().addAll("comment-time", "post-meta");

                VBox userInfo = new VBox(2);
                userInfo.getChildren().addAll(userLabel, timeLabel);

                HBox headerRow = new HBox(8);
                headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                headerRow.getChildren().addAll(avatarLabel, userInfo);

                Label textLabel = new Label(content);
                textLabel.getStyleClass().add("comment-text");
                textLabel.setWrapText(true);

                VBox commentCard = new VBox(6);
                commentCard.getStyleClass().add("comment-card");
                commentCard.getChildren().addAll(headerRow, textLabel);
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

    private VBox buildUserSuggestionCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("post-card");
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1a2e, #0f3460);");
        
        Label titleLabel = new Label("👥 People You Might Know");
        titleLabel.getStyleClass().add("post-author");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700;");
        card.getChildren().add(titleLabel);
        
        new Thread(() -> {
            try {
                List<com.campasian.model.UserProfile> suggestions = ApiService.getInstance().getAllProfiles();
                if (suggestions != null && !suggestions.isEmpty()) {
                    // Take first 3 suggestions
                    java.util.List<com.campasian.model.UserProfile> limited = suggestions.stream()
                        .limit(3)
                        .collect(java.util.stream.Collectors.toList());
                    
                    Platform.runLater(() -> {
                        VBox suggestionsVBox = new VBox(10);
                        for (com.campasian.model.UserProfile profile : limited) {
                            if (profile != null && profile.getFullName() != null) {
                                HBox suggestionRow = new HBox(10);
                                suggestionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                                suggestionRow.setStyle("-fx-background-color: rgba(100, 100, 120, 0.1); -fx-padding: 8; -fx-background-radius: 8;");
                                
                                Label nameLabel = new Label(profile.getFullName());
                                nameLabel.getStyleClass().add("post-author");
                                nameLabel.setStyle("-fx-font-size: 12px;");
                                nameLabel.setCursor(javafx.scene.Cursor.HAND);
                                nameLabel.setOnMouseClicked(e -> AppRouter.navigateToProfile(profile.getId()));
                                
                                Region spacer = new Region();
                                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                                
                                Button followBtn = new Button("Follow");
                                followBtn.getStyleClass().add("btn-primary");
                                followBtn.setStyle("-fx-padding: 4 12; -fx-font-size: 11px;");
                                followBtn.setOnAction(e -> {
                                    try {
                                        ApiService.getInstance().followUser(profile.getId());
                                        followBtn.setText("Following");
                                        followBtn.setDisable(true);
                                    } catch (com.campasian.service.ApiException ex) {
                                        // ignore
                                    }
                                });
                                
                                suggestionRow.getChildren().addAll(nameLabel, spacer, followBtn);
                                suggestionsVBox.getChildren().add(suggestionRow);
                            }
                        }
                        card.getChildren().add(suggestionsVBox);
                    });
                }
            } catch (com.campasian.service.ApiException ignored) {
                // If loading suggestions fails, just skip them
            }
        }).start();
        
        return card;
    }
}