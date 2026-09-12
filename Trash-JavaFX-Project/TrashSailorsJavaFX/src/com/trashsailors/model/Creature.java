package com.trashsailors.model;

public class Creature {
    private double x;
    private double y;
    private double radius = 40;
    private boolean active = true;
    private double dangerLevel = 0.0; // 0.0 to 1.0
    private double retreatTimer = 0.0;
    private String name;

    public Creature(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public void update(Boat boat) {
        if (retreatTimer > 0) {
            retreatTimer -= 0.016;
            dangerLevel = Math.max(0, dangerLevel - 0.02);
            if (retreatTimer <= 0) {
                active = true;
            }
            return;
        }

        // Check distance to boat
        double dist = Math.hypot(boat.getX() - x, boat.getY() - y);
        if (dist < 180 && active) {
            // Increase danger level when near boat
            dangerLevel = Math.min(1.0, dangerLevel + 0.005);
        } else {
            dangerLevel = Math.max(0, dangerLevel - 0.002);
        }
    }

    public boolean scareOff() {
        if (active && dangerLevel > 0.1) {
            active = false;
            dangerLevel = 0.0;
            retreatTimer = 10.0; // Submerges for 10 seconds
            return true;
        }
        return false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public boolean isActive() { return active; }
    public double getDangerLevel() { return dangerLevel; }
    public String getName() { return name; }
    public double getRetreatTimer() { return retreatTimer; }
}
