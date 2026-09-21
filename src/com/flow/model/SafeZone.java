package com.flow.model;

public class SafeZone {
    private double x;
    private double y;
    private double radius = 110;

    public SafeZone(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean isBoatInside(Boat boat) {
        double dist = Math.hypot(boat.getX() - x, boat.getY() - y);
        return dist <= radius;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
}
