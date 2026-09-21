package com.flow.model;

public class Obstacle {
    private double x;
    private double y;
    private double radius;
    private String type; // "SUBMERGED_CAR", "ROAD_BARRIER", "FALLEN_TREE"
    private boolean cleared = false;
    private int health = 3; // Interactions needed to clear

    public Obstacle(double x, double y, double radius, String type) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.type = type;
    }

    public boolean interact() {
        if (!cleared) {
            health--;
            if (health <= 0) {
                cleared = true;
                return true;
            }
        }
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public String getType() { return type; }
    public boolean isCleared() { return cleared; }
    public int getHealth() { return health; }
}
