package com.trashsailors.controller;

import com.trashsailors.view.GameRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private StackPane rootPane;
    @FXML private Canvas gameCanvas;
    @FXML private VBox menuOverlay;
    @FXML private VBox gameOverOverlay;
    @FXML private VBox victoryOverlay;

    private GameEngine engine;
    private GameRenderer renderer;
    private InputHandler inputHandler;
    private AnimationTimer gameLoop;

    private boolean isPaused = false;
    private long lastTime = 0;

    public void initialize() {
        engine = new GameEngine();
        renderer = new GameRenderer(gameCanvas);
        inputHandler = new InputHandler();

        // 60 FPS Game Loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double deltaSeconds = (now - lastTime) / 1e9;
                lastTime = now;

                if (!isPaused && !menuOverlay.isVisible()) {
                    engine.update(inputHandler, deltaSeconds);

                    if (engine.isGameWon()) {
                        showVictoryScreen();
                    } else if (engine.isGameOver()) {
                        showGameOverScreen();
                    }
                }
                renderer.render(engine);
            }
        };

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupSceneListeners(newScene);
            }
        });

        showMainMenu();
        gameLoop.start();
    }

    private void setupSceneListeners(Scene scene) {
        inputHandler.attach(scene);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> gameCanvas.setWidth(newVal.doubleValue()));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> gameCanvas.setHeight(newVal.doubleValue()));

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.P || event.getCode() == KeyCode.ESCAPE) {
                if (!menuOverlay.isVisible() && !gameOverOverlay.isVisible() && !victoryOverlay.isVisible()) {
                    isPaused = !isPaused;
                }
            }
        });
    }

    @FXML
    public void onSelectEasy() {
        engine.setDifficulty("EASY"); // 10 Minutes
        startGameInternal();
    }

    @FXML
    public void onSelectMedium() {
        engine.setDifficulty("MEDIUM"); // 5 Minutes
        startGameInternal();
    }

    @FXML
    public void onSelectHard() {
        engine.setDifficulty("HARD"); // 2 Minutes
        startGameInternal();
    }

    @FXML
    public void onStartGame() {
        startGameInternal();
    }

    private void startGameInternal() {
        engine.reset();
        inputHandler.clear();
        menuOverlay.setVisible(false);
        gameOverOverlay.setVisible(false);
        victoryOverlay.setVisible(false);
        isPaused = false;
        lastTime = 0;

        gameCanvas.requestFocus();
        rootPane.requestFocus();
    }

    @FXML
    public void onQuitGame() {
        Platform.exit();
        System.exit(0);
    }

    private void showMainMenu() {
        menuOverlay.setVisible(true);
        gameOverOverlay.setVisible(false);
        victoryOverlay.setVisible(false);
        isPaused = true;
    }

    private void showGameOverScreen() {
        gameOverOverlay.setVisible(true);
        isPaused = true;
    }

    private void showVictoryScreen() {
        victoryOverlay.setVisible(true);
        isPaused = true;
    }
}
