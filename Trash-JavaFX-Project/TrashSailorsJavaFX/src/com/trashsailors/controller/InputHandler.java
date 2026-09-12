package com.trashsailors.controller;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private boolean spacePressedThisFrame = false;
    private double scrollDeltaY = 0;

    public void attach(Scene scene) {
        // Intercept key events at capturing phase
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (!activeKeys.contains(code)) {
                activeKeys.add(code);
            }
            if (code == KeyCode.SPACE) {
                spacePressedThisFrame = true;
            }

            if (isGameKey(code)) {
                event.consume();
            }
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            KeyCode code = event.getCode();
            activeKeys.remove(code);
            if (isGameKey(code)) {
                event.consume();
            }
        });

        // Mouse scroll wheel for Camera Zoom In / Out
        scene.addEventFilter(ScrollEvent.SCROLL, event -> {
            scrollDeltaY += event.getDeltaY();
            event.consume();
        });
    }

    private boolean isGameKey(KeyCode code) {
        return code == KeyCode.W || code == KeyCode.A || code == KeyCode.S || code == KeyCode.D ||
               code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT ||
               code == KeyCode.SPACE || code == KeyCode.Q || code == KeyCode.E ||
               code == KeyCode.EQUALS || code == KeyCode.MINUS;
    }

    public boolean isKeyPressed(KeyCode code) {
        return activeKeys.contains(code);
    }

    public boolean consumeSpacePressed() {
        if (spacePressedThisFrame) {
            spacePressedThisFrame = false;
            return true;
        }
        return false;
    }

    public double consumeScrollDeltaY() {
        double delta = scrollDeltaY;
        scrollDeltaY = 0;
        return delta;
    }

    public void clear() {
        activeKeys.clear();
        spacePressedThisFrame = false;
        scrollDeltaY = 0;
    }
}
