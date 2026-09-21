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
        update(boat, null);
    }

    public void update(Boat boat, java.util.List<Building> buildings) {
        if (retreatTimer > 0) {
            retreatTimer -= 0.016;
            dangerLevel = Math.max(0, dangerLevel - 0.02);
            if (retreatTimer <= 0) {
                active = true;
            }
            return;
        }

        // Workshop Depot Sanctuary: If player enters Workshop Depot, monsters lose interest and de-aggro
        boolean boatInWorkshop = false;
        if (buildings != null) {
            for (Building b : buildings) {
                if ("WORKSHOP".equalsIgnoreCase(b.getType()) || "WORKSHOP_DEPOT".equalsIgnoreCase(b.getType())) {
                    if (boat.getX() >= b.getX() - 30 && boat.getX() <= b.getX() + b.getWidth() + 30 &&
                        boat.getY() >= b.getY() - 30 && boat.getY() <= b.getY() + b.getHeight() + 30) {
                        boatInWorkshop = true;
                        break;
                    }
                }
            }
        }

        if (boatInWorkshop) {
            dangerLevel = Math.max(0.0, dangerLevel - 0.03);
            return;
        }

        // Check distance to boat
        double dist = Math.hypot(boat.getX() - x, boat.getY() - y);
        if (dist < 180 && active) {
            // Increase danger level when near boat
            dangerLevel = Math.min(1.0, dangerLevel + 0.005);
            double dx = boat.getX() - x;
            double dy = boat.getY() - y;
            if (dist > 40) {
                x += (dx / dist) * 0.8;
                y += (dy / dist) * 0.8;
            }
        } else {
            dangerLevel = Math.max(0, dangerLevel - 0.002);
        }

        // STRICT BOUNDARY: Monsters CANNOT enter the Workshop Depot or ANY solid building!
        if (buildings != null) {
            for (Building b : buildings) {
                if ("FIELD".equals(b.getType()) || "PARK_TREES".equals(b.getType())) {
                    continue; // Ponds in field/trees allowed
                }

                double margin = radius + 20.0;
                double bx = b.getX() - margin;
                double by = b.getY() - margin;
                double bw = b.getWidth() + (margin * 2);
                double bh = b.getHeight() + (margin * 2);

                if (x >= bx && x <= bx + bw && y >= by && y <= by + bh) {
                    double leftDist = Math.abs(x - bx);
                    double rightDist = Math.abs((bx + bw) - x);
                    double topDist = Math.abs(y - by);
                    double bottomDist = Math.abs((by + bh) - y);

                    double minDist = Math.min(Math.min(leftDist, rightDist), Math.min(topDist, bottomDist));

                    if (minDist == leftDist) {
                        x = bx - 2;
                    } else if (minDist == rightDist) {
                        x = bx + bw + 2;
                    } else if (minDist == topDist) {
                        y = by - 2;
                    } else {
                        y = by + bh + 2;
                    }
                }
            }
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
