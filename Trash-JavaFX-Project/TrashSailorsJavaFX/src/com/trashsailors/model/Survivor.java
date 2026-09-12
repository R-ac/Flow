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

    public void update(boolean isLowTimer, Boat boat, List<Building> buildings) {
        bobbingPhase += 0.05;
        if (rescued) return;

        double distToBoat = Math.hypot(boat.getX() - x, boat.getY() - y);

        // Feature 1: Rooftop / Building edge walk AI when boat arrives near building!
        if (associatedBuilding != null && distToBoat < 450) {
            // Find edge point of the building closest to boat
            double targetX = Math.max(associatedBuilding.getX(), Math.min(associatedBuilding.getX() + associatedBuilding.getWidth(), boat.getX()));
            double targetY = Math.max(associatedBuilding.getY(), Math.min(associatedBuilding.getY() + associatedBuilding.getHeight(), boat.getY()));

            double dx = targetX - x;
            double dy = targetY - y;
            double distToEdge = Math.hypot(dx, dy);

            if (distToEdge > 5) {
                x += (dx / distToEdge) * swimSpeed;
                y += (dy / distToEdge) * swimSpeed;
            }
        }
        // Feature 2: Low-timer swim towards boat AI
        else if (isLowTimer) {
            double dx = boat.getX() - x;
            double dy = boat.getY() - y;

            if (distToBoat > 35 && distToBoat < 550) {
                x += (dx / distToBoat) * swimSpeed;
                y += (dy / distToBoat) * swimSpeed;
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
}
