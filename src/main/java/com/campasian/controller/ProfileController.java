package com.campasian.controller;

import com.campasian.model.Comment;
import com.campasian.model.Post;
import com.campasian.model.UserProfile;
import com.campasian.service.ApiException;
import com.campasian.service.ApiService;
import com.campasian.service.AuthService;
import com.campasian.view.AppRouter;
import com.campasian.view.NavigationContext;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileController implements Initializable {

    @FXML private Label fullNameLabel;
    @FXML private Label fullNameLabel2;
    @FXML private javafx.scene.image.ImageView avatarImageView;
    @FXML private Label universityLabel;
    @FXML private Label universityLabel2;
    @FXML private Label departmentLabel;
    @FXML private Label einLabel;
    @FXML private Label bioLabel;
    @FXML private Label bloodGroupLabel;
    @FXML private Label sessionLabel;
    @FXML private Label batchLabel;
    @FXML private Label followersCountLabel;
    @FXML private Label followingCountLabel;
    @FXML private Label postCountLabel;
    @FXML private Button editProfileBtn;
    @FXML private Button followBtn;
    @FXML private Button unfollowBtn;
    @FXML private VBox postsVBox;

    private String viewingUserId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewingUserId = NavigationContext.getViewingProfileUserId();
        if (viewingUserId == null || viewingUserId.isBlank()) {
            viewingUserId = ApiService.getInstance().getCurrentUserId();
        }
        loadProfile();
        loadPosts();
        updateActionButtons();
    }

    @FXML
    protected void onEditProfileClick() {
        try {
            UserProfile profile = AuthService.getInstance().getCurrentUserProfile();
            if (profile == null) return;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-profile-modal.fxml"));
            Parent root = loader.load();
            EditProfileModalController ctrl = loader.getController();
            ctrl.setInitialData(profile.getFullName(), profile.getUniversityName(), profile.getBio(), profile.getBloodGroup(), profile.getSession(), profile.getBatch());
            ctrl.setInitialAvatarUrl(profile.getAvatarUrl());
            ctrl.setOnSaved(this::loadProfile);

            Stage stage = new Stage();
            ctrl.setStage(stage);
            stage.setTitle("Edit Profile");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(SceneManager.getPrimaryStage());
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onFollowClick() {
        try {
            ApiService.getInstance().followUser(viewingUserId);
            updateActionButtons();
            loadProfile();
        } catch (ApiException ignored) {
        }
    }

    @FXML
    protected void onUnfollowClick() {
        try {
            ApiService.getInstance().unfollowUser(viewingUserId);
            updateActionButtons();
            loadProfile();
        } catch (ApiException ignored) {
        }
    }

    private void updateActionButtons() {
        String currentId = ApiService.getInstance().getCurrentUserId();
        boolean isSelf = viewingUserId != null && viewingUserId.equals(currentId);

        if (editProfileBtn != null) {
            editProfileBtn.setVisible(isSelf);
            editProfileBtn.setManaged(isSelf);
        }
        if (followBtn != null) {
            followBtn.setVisible(!isSelf);
            followBtn.setManaged(!isSelf);
        }
        if (unfollowBtn != null) {
            unfollowBtn.setVisible(!isSelf);
            unfollowBtn.setManaged(!isSelf);
        }

        if (!isSelf && followBtn != null && unfollowBtn != null) {
            try {
                boolean following = ApiService.getInstance().isFollowing(viewingUserId);
                followBtn.setVisible(!following);
                followBtn.setManaged(!following);
                unfollowBtn.setVisible(following);
                unfollowBtn.setManaged(following);
            } catch (ApiException e) {
                followBtn.setVisible(true);
                unfollowBtn.setVisible(false);
            }
        }
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                UserProfile profile = viewingUserId != null
                    ? ApiService.getInstance().getProfile(viewingUserId)
                    : AuthService.getInstance().getCurrentUserProfile();
                String uid = profile != null ? profile.getId() : null;
                AtomicInteger followers = new AtomicInteger(0);
                AtomicInteger following = new AtomicInteger(0);
                if (uid != null) {
                    try {
                        followers.set(ApiService.getInstance().getFollowerCount(uid));
                        following.set(ApiService.getInstance().getFollowingCount(uid));
                    } catch (ApiException ignored) {
                    }
                }
                UserProfile finalProfile = profile;
                Platform.runLater(() -> {
                    if (finalProfile != null) {
                        setLabel(fullNameLabel, finalProfile.getFullName());
                        setLabel(fullNameLabel2, finalProfile.getFullName());
                        if (avatarImageView != null && finalProfile.getAvatarUrl() != null && !finalProfile.getAvatarUrl().isBlank()) {
                            try {
                                avatarImageView.setImage(new javafx.scene.image.Image(finalProfile.getAvatarUrl(), true));
                            } catch (Exception ignored) {
                            }
                        }
                        setLabel(universityLabel, finalProfile.getUniversityName());
                        setLabel(universityLabel2, finalProfile.getUniversityName());
                        setLabel(departmentLabel, finalProfile.getDepartment());
                        setLabel(einLabel, finalProfile.getEinNumber());
                        setLabel(bioLabel, finalProfile.getBio());
                        setLabel(bloodGroupLabel, finalProfile.getBloodGroup());
                        setLabel(sessionLabel, finalProfile.getSession());
                        setLabel(batchLabel, finalProfile.getBatch());
                        if (followersCountLabel != null) followersCountLabel.setText(String.valueOf(followers.get()));
                        if (followingCountLabel != null) followingCountLabel.setText(String.valueOf(following.get()));
                    } else {
                        clearProfileLabels();
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(this::clearProfileLabels);
            }
        }, "profile-load").start();
    }

    private void loadPosts() {
        if (postsVBox == null) return;
        postsVBox.getChildren().clear();
        if (viewingUserId == null || viewingUserId.isBlank()) return;

        new Thread(() -> {
            try {
                List<Post> posts = ApiService.getInstance().getPostsByUserId(viewingUserId);
                String currentId = ApiService.getInstance().getCurrentUserId();
                boolean isSelf = viewingUserId.equals(currentId);
                Platform.runLater(() -> {
                    if (postsVBox == null) return;
                    postsVBox.getChildren().clear();
                    for (Post post : posts) {
                        postsVBox.getChildren().add(buildPostCard(post, isSelf));
                    }
                    if (posts.isEmpty()) {
                        Label empty = new Label("No posts yet.");
                        empty.getStyleClass().add("profile-label");
                        postsVBox.getChildren().add(empty);
                    }
                    if (postCountLabel != null) postCountLabel.setText(String.valueOf(posts.size()));
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    if (postsVBox != null) {
                        Label err = new Label("Unable to load posts.");
                        err.getStyleClass().add("profile-label");
                        postsVBox.getChildren().add(err);
                    }
                    if (postCountLabel != null) postCountLabel.setText("0");
                });
            }
        }, "profile-posts-load").start();
    }

    private VBox buildPostCard(Post post, boolean canEdit) {
        String userName = post.getUserName() != null ? post.getUserName() : "Anonymous";
        String university = post.getUniversity() != null && !post.getUniversity().isBlank() ? " · " + post.getUniversity() : "";
        Label meta = new Label(userName + university + " · " + formatTimeAgo(post.getCreatedAt()));
        meta.getStyleClass().add("post-meta");
        meta.setOnMouseClicked(e -> AppRouter.navigateToProfile(post.getUserId()));

        Label content = new Label(post.getContent() != null ? post.getContent() : "");
        content.getStyleClass().add("post-content");
        content.setWrapText(true);

        StackPane postImageFrame = null;
        if (post.getImageUrl() != null && !post.getImageUrl().isBlank()) {
            try {
                javafx.scene.image.ImageView postImageView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(post.getImageUrl(), true));
                postImageView.setFitWidth(520);
                postImageView.setFitHeight(340);
                postImageView.setPreserveRatio(true);
                postImageView.getStyleClass().add("post-image");
                postImageFrame = new StackPane(postImageView);
                postImageFrame.getStyleClass().add("post-image-frame");
            } catch (Exception ignored) {
            }
        }

        int likeCount = post.getLikeCount();
        int commentCount = post.getCommentCount();
        boolean liked = post.isLikedByMe();
        Button likeBtn = new Button((liked ? "♥ " : "♡ ") + (likeCount > 0 ? String.valueOf(likeCount) : ""));
        likeBtn.getStyleClass().add("post-action-btn");
        if (liked) likeBtn.getStyleClass().add("post-action-btn-liked");
        Button commentBtn = new Button("💬 " + (commentCount > 0 ? String.valueOf(commentCount) : ""));
        commentBtn.getStyleClass().add("post-action-btn");

        likeBtn.setOnAction(e -> {
            try {
                ApiService.getInstance().toggleLike(post.getId());
                post.setLikedByMe(!post.isLikedByMe());
                post.setLikeCount(post.getLikeCount() + (post.isLikedByMe() ? 1 : -1));
                likeBtn.setText((post.isLikedByMe() ? "♥ " : "♡ ") + (post.getLikeCount() > 0 ? String.valueOf(post.getLikeCount()) : ""));
                likeBtn.getStyleClass().remove("post-action-btn-liked");
                if (post.isLikedByMe()) likeBtn.getStyleClass().add("post-action-btn-liked");
            } catch (ApiException ignored) {
            }
        });

        VBox commentsContainer = new VBox(8);
        commentsContainer.getStyleClass().add("comments-container");
        commentsContainer.setVisible(false);
        commentsContainer.setManaged(false);
        TextField commentField = new TextField();
        commentField.getStyleClass().add("comment-field");
        commentField.setPromptText("Write a comment...");
        Button submitComment = new Button("Post");
        submitComment.getStyleClass().add("btn-primary");

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
                    commentBtn.setText("💬 " + (post.getCommentCount() > 0 ? String.valueOf(post.getCommentCount()) : ""));
                    commentField.clear();
                    loadCommentsInto(post, commentsContainer, commentField, submitComment, commentBtn);
                } catch (ApiException ignored) {
                }
            }
        });

        HBox actions = new HBox(16);
        actions.getStyleClass().add("post-actions");
        actions.getChildren().addAll(likeBtn, commentBtn);

        if (canEdit) {
            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("post-action-btn");
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("post-action-btn");
            deleteBtn.setStyle("-fx-text-fill: #ef4444;");
            editBtn.setOnAction(ev -> {
                TextInputDialog d = new TextInputDialog(post.getContent());
                d.setTitle("Edit Post");
                d.setHeaderText("Edit your post");
                d.showAndWait().ifPresent(newContent -> {
                    if (!newContent.equals(post.getContent())) {
                        try {
                            ApiService.getInstance().updatePost(post.getId(), newContent);
                            post.setContent(newContent);
                            content.setText(newContent);
                            loadPosts();
                        } catch (ApiException ignored) {
                        }
                    }
                });
            });
            deleteBtn.setOnAction(ev -> {
                try {
                    ApiService.getInstance().deletePost(post.getId());
                    loadPosts();
                } catch (ApiException ignored) {
                }
            });
            actions.getChildren().addAll(editBtn, deleteBtn);
        }

        commentsContainer.getChildren().clear();
        HBox inputRow = new HBox(8);
        inputRow.getChildren().addAll(commentField, submitComment);
        commentsContainer.getChildren().add(inputRow);

        VBox card = new VBox(8);
        card.getStyleClass().add("post-card");
        card.getChildren().add(meta);
        card.getChildren().add(content);
        if (postImageFrame != null) card.getChildren().add(postImageFrame);
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
                String line = (c.getUserName() != null ? c.getUserName() : "Anonymous") + ": " + (c.getContent() != null ? c.getContent() : "");
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

    private void setLabel(Label label, String value) {
        if (label != null) {
            label.setText(value != null && !value.isBlank() ? value : "—");
        }
    }

    private void clearProfileLabels() {
        setLabel(fullNameLabel, null);
        setLabel(fullNameLabel2, null);
        setLabel(universityLabel, null);
        setLabel(universityLabel2, null);
        setLabel(departmentLabel, null);
        setLabel(einLabel, null);
        setLabel(bioLabel, null);
        setLabel(bloodGroupLabel, null);
        setLabel(sessionLabel, null);
        setLabel(batchLabel, null);
        if (followersCountLabel != null) followersCountLabel.setText("0");
        if (followingCountLabel != null) followingCountLabel.setText("0");
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
