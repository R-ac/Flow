package com.flow.model;

public class Building {
    private double x;
    private double y;
    private double width;
    private double height;
    private String type; // "ACADEMIC", "HALL", "MOSQUE", "WORKSHOP", "PARK_TREES", "FIELD"
    private String colorHex;
    private String nameLabel;

    public Building(double x, double y, double width, double height, String type, String colorHex, String nameLabel) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.colorHex = colorHex;
        this.nameLabel = nameLabel;
    }

    public boolean intersects(double bx, double by, double bWidth, double bHeight) {
        double boatLeft = bx - bWidth / 2;
        double boatRight = bx + bWidth / 2;
        double boatTop = by - bHeight / 2;
        double boatBottom = by + bHeight / 2;

        return boatRight > x && boatLeft < (x + width) && boatBottom > y && boatTop < (y + height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getType() { return type; }
    public String getColorHex() { return colorHex; }
    public String getNameLabel() { return nameLabel; }
}
