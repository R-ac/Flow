package com.flow.controller;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final Set<KeyCode> justPressedKeys = new HashSet<>();
    private double scrollDeltaY = 0;

    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode code = event.getCode();
                if (!activeKeys.contains(code)) {
                    justPressedKeys.add(code);
                }
                activeKeys.add(code);
            }
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                activeKeys.remove(event.getCode());
                justPressedKeys.remove(event.getCode());
            }
        });

        scene.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                scrollDeltaY += event.getDeltaY();
            }
        });
    }

    public boolean isKeyPressed(KeyCode code) {
        return activeKeys.contains(code);
    }

    public boolean consumeKeyPressed(KeyCode code) {
        if (justPressedKeys.contains(code)) {
            justPressedKeys.remove(code);
            return true;
        }
        return false;
    }

    public boolean consumeSpacePressed() {
        return consumeKeyPressed(KeyCode.SPACE);
    }

    public double consumeScrollDeltaY() {
        double delta = scrollDeltaY;
        scrollDeltaY = 0;
        return delta;
    }

    public void clear() {
        activeKeys.clear();
        justPressedKeys.clear();
        scrollDeltaY = 0;
    }
}
