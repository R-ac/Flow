package com.trashsailors.model;

public class RopeAnimation {
    private double startX, startY;
    private double targetX, targetY;
    private double progress = 0.0;
    private boolean active = false;
    private Survivor targetSurvivor;

    public void start(double startX, double startY, Survivor target) {
        this.startX = startX;
        this.startY = startY;
        this.targetSurvivor = target;
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.progress = 0.0;
        this.active = true;
    }

    public void update(double dt) {
        if (!active) return;
        progress += dt * 3.0; // 0.33 sec throw speed
        if (progress >= 1.0) {
            progress = 1.0;
            active = false;
        }
    }

    public boolean isActive() { return active; }
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }
    public double getProgress() { return progress; }
    public Survivor getTargetSurvivor() { return targetSurvivor; }
}
