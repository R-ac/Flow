package com.trashsailors.model;

public class Boat {
    private double x;
    private double y;
    private double vx = 0;
    private double vy = 0;
    private double angle = 0; // in degrees (0 = pointing right)
    
    private final double width = 110;
    private final double height = 55;
    
    private final int maxCargoCapacity = 5;
    private int cargoCount = 0;
    
    private double acceleration = 0.35;
    private double turnSpeed = 4.0;
    private double friction = 0.996; // Removed heavy water drag for smooth gliding
    private double maxSpeed = 6.0;
    
    private double boostTimer = 0; // Temporary speed boost duration

    public Boat(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(boolean forward, boolean backward, boolean turnLeft, boolean turnRight, double mapWidth, double mapHeight) {
        // Handle rotation
        if (turnLeft) {
            angle -= turnSpeed;
        }
        if (turnRight) {
            angle += turnSpeed;
        }

        // Speed boost check
        double effectiveMaxSpeed = maxSpeed;
        double effectiveAccel = acceleration;
        if (boostTimer > 0) {
            boostTimer -= 0.016; // approx 60 FPS update
            effectiveMaxSpeed *= 1.4;
            effectiveAccel *= 1.4;
        }

        // Acceleration along boat heading angle
        double radians = Math.toRadians(angle);
        double forwardX = Math.cos(radians);
        double forwardY = Math.sin(radians);

        if (forward) {
            vx += forwardX * effectiveAccel;
            vy += forwardY * effectiveAccel;
        }
        if (backward) {
            vx -= forwardX * (effectiveAccel * 0.7);
            vy -= forwardY * (effectiveAccel * 0.7);
        }

        // Apply light glide friction (no heavy drag)
        if (!forward && !backward) {
            vx *= 0.985;
            vy *= 0.985;
        } else {
            vx *= friction;
            vy *= friction;
        }

        // Limit max speed
        double currentSpeed = Math.hypot(vx, vy);
        if (currentSpeed > effectiveMaxSpeed) {
            vx = (vx / currentSpeed) * effectiveMaxSpeed;
            vy = (vy / currentSpeed) * effectiveMaxSpeed;
        }

        // Move position
        x += vx;
        y += vy;

        // Keep boat inside map bounds
        double padding = width / 2;
        if (x < padding) { x = padding; vx *= -0.3; }
        if (x > mapWidth - padding) { x = mapWidth - padding; vx *= -0.3; }
        if (y < padding) { y = padding; vy *= -0.3; }
        if (y > mapHeight - padding) { y = mapHeight - padding; vy *= -0.3; }
    }

    public boolean canAddCargo() {
        return cargoCount < maxCargoCapacity;
    }

    public boolean addCargo() {
        if (canAddCargo()) {
            cargoCount++;
            return true;
        }
        return false;
    }

    public int unloadCargo() {
        int count = cargoCount;
        cargoCount = 0;
        return count;
    }

    public void applySpeedBoost(double seconds) {
        this.boostTimer = seconds;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }
    public double getAngle() { return angle; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getCargoCount() { return cargoCount; }
    public int getMaxCargoCapacity() { return maxCargoCapacity; }
    public boolean isBoosted() { return boostTimer > 0; }
}
