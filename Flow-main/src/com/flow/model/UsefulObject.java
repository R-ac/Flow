package com.flow.model;

public class UsefulObject {
    private double x;
    private double y;
    private String type; // "SUPPLY_CHEST", "TURBO_FUEL", "RECYCLABLE_TRASH"
    private int pointValue;
    private boolean collected = false;

    public UsefulObject(double x, double y, String type, int pointValue) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.pointValue = pointValue;
    }

    public void collect() {
        this.collected = true;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getType() { return type; }
    public int getPointValue() { return pointValue; }
    public boolean isCollected() { return collected; }
}
