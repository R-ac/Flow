package com.trashsailors.model;

import java.util.List;

public class Survivor {
    private double x;
    private double y;
    private final String name;
    private double bobbingPhase;
    private boolean rescued = false;
    private double swimSpeed = 1.5;
    private Building associatedBuilding;
    private boolean rooftopTrapped = false;

    public Survivor(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.bobbingPhase = Math.random() * Math.PI * 2;
    }

    public Survivor(double x, double y, String name, Building building) {
        this(x, y, name);
        this.associatedBuilding = building;
    }

    public Survivor(double x, double y, String name, Building building, boolean rooftopTrapped) {
        this(x, y, name, building);
        this.rooftopTrapped = rooftopTrapped;
    }

    public void update(boolean isLowTimer, Boat boat, List<Building> buildings) {
        bobbingPhase += 0.05;
        if (rescued) return;

        double distToBoat = Math.hypot(boat.getX() - x, boat.getY() - y);

        // 1. Rooftop Trapped Survivors AI (Confined strictly to rooftop surface)
        if (rooftopTrapped) {
            if (associatedBuilding != null) {
                double minX = associatedBuilding.getX() + 30;
                double maxX = associatedBuilding.getX() + associatedBuilding.getWidth() - 30;
                double minY = associatedBuilding.getY() + 30;
                double maxY = associatedBuilding.getY() + associatedBuilding.getHeight() - 30;

                // When rescue vehicle approaches near the building, run to closest rooftop edge waving for help
                if (distToBoat < 650) {
                    double targetX = Math.max(minX, Math.min(maxX, boat.getX()));
                    double targetY = Math.max(minY, Math.min(maxY, boat.getY()));

                    double dx = targetX - x;
                    double dy = targetY - y;
                    double distToTarget = Math.hypot(dx, dy);

                    if (distToTarget > 6) {
                        double runSpeed = 2.4;
                        x += (dx / distToTarget) * runSpeed;
                        y += (dy / distToTarget) * runSpeed;
                    }
                }

                // Strictly clamp within rooftop perimeter
                x = Math.max(minX, Math.min(maxX, x));
                y = Math.max(minY, Math.min(maxY, y));
            }
            return;
        }

        // 2. Water Survivors AI (Stay in water avenues; low-timer swim towards boat)
        if (isLowTimer) {
            double dx = boat.getX() - x;
            double dy = boat.getY() - y;

            if (distToBoat > 35 && distToBoat < 550) {
                x += (dx / distToBoat) * swimSpeed;
                y += (dy / distToBoat) * swimSpeed;
            }
        }

        // 3. Strict Building Collisions: Water survivors CANNOT enter, touch, or spawn on ANY building or workshop
        if (buildings != null) {
            for (Building b : buildings) {
                if ("FIELD".equals(b.getType())) {
                    continue; // Sports field is open water/field
                }
                if ("PARK_TREES".equals(b.getType())) {
                    continue; // Forest survivors can be in forest
                }

                double margin = 28.0;
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

    public double getBobbingOffset() {
        return Math.sin(bobbingPhase) * 4;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getName() { return name; }
    public boolean isRescued() { return rescued; }
    public void setRescued(boolean rescued) { this.rescued = rescued; }
    public Building getAssociatedBuilding() { return associatedBuilding; }
    public void setAssociatedBuilding(Building building) { this.associatedBuilding = building; }
    public boolean isRooftopTrapped() { return rooftopTrapped; }
    public void setRooftopTrapped(boolean rooftopTrapped) { this.rooftopTrapped = rooftopTrapped; }
}
