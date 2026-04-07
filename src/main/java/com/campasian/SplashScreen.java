package com.campasian;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class SplashScreen {

    private static final Color BG        = Color.web("#0e0e0f");
    private static final Color ACCENT    = Color.web("#7ab88a");
    private static final Color ACCENT_DIM= Color.web("#3a6645");
    private static final Color TEXT_PRI  = Color.web("#f0ede8");
    private static final Color TEXT_MUT  = Color.web("#6b6b72");
    private static final Color BORDER    = Color.web("#2a2a2e");

    private static final double W = 520;
    private static final double H = 340;

    private final Stage stage;
    private Timeline loaderTimeline;
    private Timeline glowTimeline;

    public SplashScreen() {
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.setScene(buildScene());
        stage.setAlwaysOnTop(true);
    }

    public void show(Runnable onFinished) {
        stage.show();
        centerOnScreen();
        runEntranceAnimation(onFinished);
    }

    private void centerOnScreen() {
        stage.setX((javafx.stage.Screen.getPrimary().getBounds().getWidth()  - W) / 2);
        stage.setY((javafx.stage.Screen.getPrimary().getBounds().getHeight() - H) / 2);
    }

    private Scene buildScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");

        /* ── card ── */
        StackPane card = new StackPane();
        card.setPrefSize(W, H);
        card.setStyle(
            "-fx-background-color: #0e0e0f;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #2a2a2e;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;"
        );

        /* ── soft glow behind logo ── */
        Canvas glowCanvas = new Canvas(W, H);
        drawGlow(glowCanvas.getGraphicsContext2D(), 0.08);
        glowCanvas.setEffect(new GaussianBlur(32));

        /* ── logo group ── */
        Group logoGroup = buildLogo();

        /* ── text block ── */
        VBox textBlock = buildTextBlock();

        /* ── dots loader ── */
        HBox dots = buildDots();

        /* ── layout ── */
        VBox center = new VBox(18, logoGroup, textBlock);
        center.setAlignment(Pos.CENTER);

        StackPane.setAlignment(dots, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dots, new javafx.geometry.Insets(0, 0, 36, 0));

        card.getChildren().addAll(glowCanvas, center, dots);
        root.getChildren().add(card);

        /* ── drop-shadow on card ── */
        card.setEffect(new javafx.scene.effect.DropShadow(60, 0, 20, Color.color(0, 0, 0, 0.7)));

        Scene scene = new Scene(root, W + 80, H + 80);
        scene.setFill(Color.TRANSPARENT);
        return scene;
    }

    /* ── glow backdrop ── */
    private void drawGlow(GraphicsContext gc, double alpha) {
        RadialGradient rg = new RadialGradient(
            0, 0, W / 2, H / 2, 140,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.48, 0.72, 0.54, alpha)),
            new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(rg);
        gc.fillRect(0, 0, W, H);
    }

    /* ── SVG-style logo drawn with JavaFX shapes ── */
    private Group buildLogo() {
        double cx = 0, cy = 0, r = 28;

        /* outer ring */
        Circle ring = new Circle(cx, cy, r);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(BORDER);
        ring.setStrokeWidth(1);

        /* peak / mountain path */
        Polyline peak = new Polyline(
            -14.0, 10.0,
              0.0,-10.0,
             14.0, 10.0
        );
        peak.setFill(null);
        peak.setStroke(ACCENT);
        peak.setStrokeWidth(1.5);
        peak.setStrokeLineCap(StrokeLineCap.ROUND);
        peak.setStrokeLineJoin(StrokeLineJoin.ROUND);

        /* crossbar */
        Line bar = new Line(-10, 3, 10, 3);
        bar.setStroke(ACCENT);
        bar.setStrokeWidth(1.5);
        bar.setStrokeLineCap(StrokeLineCap.ROUND);
        bar.setOpacity(0.5);

        /* apex dot */
        Circle apex = new Circle(0, -10, 2.5, ACCENT);

        Group logo = new Group(ring, peak, bar, apex);
        return logo;
    }

    /* ── wordmark + tagline ── */
    private VBox buildTextBlock() {
        Text wordmark = new Text("campasian");
        wordmark.setFont(Font.font("Georgia", FontWeight.NORMAL, FontPosture.ITALIC, 34));
        wordmark.setFill(TEXT_PRI);
        wordmark.setOpacity(0);

        Text tagline = new Text("YOUR CAMPUS, CONNECTED");
        tagline.setFont(Font.font("System", FontWeight.LIGHT, 10));
        tagline.setFill(TEXT_MUT);
        tagline.setOpacity(0);

        VBox block = new VBox(6, wordmark, tagline);
        block.setAlignment(Pos.CENTER);
        block.setUserData(new Text[]{wordmark, tagline});
        return block;
    }

    /* ── animated dots ── */
    private HBox buildDots() {
        Circle[] dots = new Circle[3];
        for (int i = 0; i < 3; i++) {
            dots[i] = new Circle(3.5);
            dots[i].setFill(i == 0 ? ACCENT : BORDER);
            dots[i].setOpacity(i == 0 ? 1.0 : 0.3);
        }

        loaderTimeline = new Timeline();
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            KeyFrame kf = new KeyFrame(Duration.millis(400 * i + 400), e -> {
                for (Circle d : dots) { d.setFill(BORDER); d.setOpacity(0.25); }
                dots[idx].setFill(ACCENT);
                dots[idx].setOpacity(1.0);
            });
            loaderTimeline.getKeyFrames().add(kf);
        }
        loaderTimeline.setCycleCount(Animation.INDEFINITE);
        loaderTimeline.play();

        HBox row = new HBox(7, dots);
        row.setAlignment(Pos.CENTER);
        row.setOpacity(0);
        return row;
    }

    /* ── entrance animation ── */
    private void runEntranceAnimation(Runnable onFinished) {
        StackPane card = (StackPane) ((StackPane) stage.getScene().getRoot()).getChildren().get(0);

        /* card fade in */
        FadeTransition cardFade = new FadeTransition(Duration.millis(500), card);
        cardFade.setFromValue(0); cardFade.setToValue(1);

        /* wordmark + tagline fade-up */
        VBox textBlock = (VBox) ((VBox) card.getChildren().get(1)).getChildren().get(1);
        Text[] texts = (Text[]) textBlock.getUserData();

        FadeTransition wFade = fadeTo(texts[0], 400, 200, 1);
        FadeTransition tFade = fadeTo(texts[1], 400, 400, 1);

        /* dots fade in */
        HBox dots = (HBox) card.getChildren().get(2);
        FadeTransition dotsFade = new FadeTransition(Duration.millis(400), dots);
        dotsFade.setFromValue(0); dotsFade.setToValue(1);
        dotsFade.setDelay(Duration.millis(600));

        SequentialTransition seq = new SequentialTransition(
            cardFade,
            new ParallelTransition(wFade, tFade, dotsFade)
        );
        seq.play();

        /* hold then exit */
        PauseTransition hold = new PauseTransition(Duration.seconds(2.8));
        hold.setOnFinished(e -> exitAnimation(onFinished));
        hold.setDelay(Duration.millis(900));
        hold.play();
    }

    private void exitAnimation(Runnable onFinished) {
        StackPane card = (StackPane) ((StackPane) stage.getScene().getRoot()).getChildren().get(0);
        FadeTransition exit = new FadeTransition(Duration.millis(500), card);
        exit.setFromValue(1); exit.setToValue(0);
        exit.setOnFinished(e -> {
            loaderTimeline.stop();
            stage.close();
            if (onFinished != null) onFinished.run();
        });
        exit.play();
    }

    private FadeTransition fadeTo(javafx.scene.Node node, double ms, double delayMs, double to) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(0); ft.setToValue(to);
        ft.setDelay(Duration.millis(delayMs));
        return ft;
    }
}