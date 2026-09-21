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
        update(boat, null);
    }

    public void update(Boat boat, java.util.List<Building> buildings) {
        if (retreatTimer > 0) {
            retreatTimer -= 0.016;
            if (retreatTimer <= 0) {
                active = true;
                dangerLevel = 0;
            }
            return;
        }

        // Workshop Depot Sanctuary: If player enters Workshop Depot, monsters lose interest and de-aggro
        boolean boatInWorkshop = false;
        if (buildings != null) {
            for (Building b : buildings) {
                if ("WORKSHOP_DEPOT".equals(b.getType())) {
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
