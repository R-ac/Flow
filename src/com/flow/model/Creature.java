package com.flow.model;

public class Creature {
    private double x;
    private double y;
    private final String name;
    private final double radius = 35;
    private double dangerLevel = 0; // 0 to 1
    private boolean active = true;
    private double retreatTimer = 0;

    public Creature(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public void update(Boat boat) {
        if (retreatTimer > 0) {
            retreatTimer -= 0.016;
            if (retreatTimer <= 0) {
                active = true;
                dangerLevel = 0;
            }
            return;
        }

        double distToBoat = Math.hypot(boat.getX() - x, boat.getY() - y);

        // Creature rises and threatens boat when boat comes close (within 220 units)
        if (distToBoat < 220) {
            dangerLevel = Math.min(1.0, dangerLevel + 0.015);
            // Slowly lurk towards boat
            double dx = boat.getX() - x;
            double dy = boat.getY() - y;
            if (distToBoat > 40) {
                x += (dx / distToBoat) * 0.8;
                y += (dy / distToBoat) * 0.8;
            }
        } else {
            dangerLevel = Math.max(0.0, dangerLevel - 0.01);
        }
    }

    public boolean scareOff() {
        if (active && dangerLevel > 0.2) {
            active = false;
            retreatTimer = 10.0; // Retreats for 10 seconds
            dangerLevel = 0;
            return true;
        }
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getName() { return name; }
    public double getRadius() { return radius; }
    public double getDangerLevel() { return dangerLevel; }
    public boolean isActive() { return active; }
    public double getRetreatTimer() { return retreatTimer; }
}
