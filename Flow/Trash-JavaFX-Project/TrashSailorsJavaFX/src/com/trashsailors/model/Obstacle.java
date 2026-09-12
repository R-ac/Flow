package com.trashsailors.model;

public class Obstacle {
    private double x;
    private double y;
    private double radius;
    private String type; // "DEBRIS", "ROCK", "DRIFTWOOD"
    private boolean cleared = false;
    private double moveProgress = 0.0; // 0 to 1 for clearing animation

    public Obstacle(double x, double y, double radius, String type) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.type = type;
    }

    public boolean interact() {
        if (!cleared) {
            cleared = true;
            return true;
        }
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public String getType() { return type; }
    public boolean isCleared() { return cleared; }
}
