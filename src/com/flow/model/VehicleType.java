package com.flow.model;

public enum VehicleType {
    BOAT("Flood Rescue Boat", 9.5, 0.60, 5.5, 0.996, 160, 65),
    ROVER("6x6 Land Rover Buggy", 6.2, 0.38, 4.2, 0.980, 115, 66),
    HELICOPTER("Emergency Rescue Helicopter", 13.0, 0.85, 6.5, 0.992, 130, 74);

    private final String displayName;
    private final double maxSpeed;
    private final double acceleration;
    private final double turnSpeed;
    private final double friction;
    private final double width;
    private final double height;

    VehicleType(String displayName, double maxSpeed, double acceleration, double turnSpeed, double friction, double width, double height) {
        this.displayName = displayName;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.turnSpeed = turnSpeed;
        this.friction = friction;
        this.width = width;
        this.height = height;
    }

    public String getDisplayName() { return displayName; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getAcceleration() { return acceleration; }
    public double getTurnSpeed() { return turnSpeed; }
    public double getFriction() { return friction; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
