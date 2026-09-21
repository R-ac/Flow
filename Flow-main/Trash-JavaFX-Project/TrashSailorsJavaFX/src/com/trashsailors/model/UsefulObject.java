package com.trashsailors.model;

public class UsefulObject {
    private double x;
    private double y;
    private String itemType; // "TREASURE_CHEST", "RECYCLABLE_TRASH", "TURBO_FUEL"
    private int pointValue;
    private boolean collected = false;

    public UsefulObject(double x, double y, String itemType, int pointValue) {
        this.x = x;
        this.y = y;
        this.itemType = itemType;
        this.pointValue = pointValue;
    }

    public boolean collect() {
        if (!collected) {
            collected = true;
            return true;
        }
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getItemType() { return itemType; }
    public int getPointValue() { return pointValue; }
    public boolean isCollected() { return collected; }
}
