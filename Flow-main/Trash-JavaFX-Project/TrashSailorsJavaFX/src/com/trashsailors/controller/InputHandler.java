package com.trashsailors.controller;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import java.util.HashSet;
import java.util.Set;

public class InputHandler {
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private boolean spacePressedThisFrame = false;
    private boolean switchVehiclePressedThisFrame = false;
    private boolean ePressedThisFrame = false;
    private boolean escPressedThisFrame = false;
    private boolean num1PressedThisFrame = false;
    private boolean num2PressedThisFrame = false;
    private boolean num3PressedThisFrame = false;
    private double scrollDeltaY = 0;

    private boolean mouseClickedThisFrame = false;
    private double mouseClickX = 0;
    private double mouseClickY = 0;

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
            if (code == KeyCode.V || code == KeyCode.TAB || code == KeyCode.F) {
                switchVehiclePressedThisFrame = true;
            }
            if (code == KeyCode.E) {
                ePressedThisFrame = true;
            }
            if (code == KeyCode.ESCAPE) {
                escPressedThisFrame = true;
            }
            if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
                num1PressedThisFrame = true;
            }
            if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
                num2PressedThisFrame = true;
            }
            if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
                num3PressedThisFrame = true;
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

        // Mouse click filter
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            mouseClickedThisFrame = true;
            mouseClickX = event.getX();
            mouseClickY = event.getY();
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
               code == KeyCode.SPACE || code == KeyCode.Q || code == KeyCode.E || code == KeyCode.ESCAPE ||
               code == KeyCode.V || code == KeyCode.TAB || code == KeyCode.F ||
               code == KeyCode.DIGIT1 || code == KeyCode.DIGIT2 || code == KeyCode.DIGIT3 ||
               code == KeyCode.NUMPAD1 || code == KeyCode.NUMPAD2 || code == KeyCode.NUMPAD3 ||
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

    public boolean consumeSwitchVehiclePressed() {
        if (switchVehiclePressedThisFrame) {
            switchVehiclePressedThisFrame = false;
            return true;
        }
        return false;
    }

    public boolean consumeEPressed() {
        if (ePressedThisFrame) {
            ePressedThisFrame = false;
            return true;
        }
        return false;
    }

    public boolean consumeEscPressed() {
        if (escPressedThisFrame) {
            escPressedThisFrame = false;
            return true;
        }
        return false;
    }

    public boolean consumeNum1Pressed() {
        if (num1PressedThisFrame) {
            num1PressedThisFrame = false;
            return true;
        }
        return false;
    }

    public boolean consumeNum2Pressed() {
        if (num2PressedThisFrame) {
            num2PressedThisFrame = false;
            return true;
        }
        return false;
    }

    public boolean consumeNum3Pressed() {
        if (num3PressedThisFrame) {
            num3PressedThisFrame = false;
            return true;
        }
        return false;
    }

    public boolean consumeMouseClicked() {
        if (mouseClickedThisFrame) {
            mouseClickedThisFrame = false;
            return true;
        }
        return false;
    }

    public double getMouseClickX() { return mouseClickX; }
    public double getMouseClickY() { return mouseClickY; }

    public double consumeScrollDeltaY() {
        double delta = scrollDeltaY;
        scrollDeltaY = 0;
        return delta;
    }

    public void clear() {
        activeKeys.clear();
        spacePressedThisFrame = false;
        switchVehiclePressedThisFrame = false;
        ePressedThisFrame = false;
        escPressedThisFrame = false;
        num1PressedThisFrame = false;
        num2PressedThisFrame = false;
        num3PressedThisFrame = false;
        mouseClickedThisFrame = false;
        scrollDeltaY = 0;
    }
}
