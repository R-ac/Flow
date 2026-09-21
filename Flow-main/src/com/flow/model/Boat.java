package com.flow.model;

public class Boat {
    private double x;
    private double y;
    private double vx = 0;
    private double vy = 0;
    private double angle = 0; // in degrees (0 = pointing right)
    
    private double width = 110;
    private double height = 55;
    
    private final int maxCargoCapacity = 10;
    private int cargoCount = 0;
    
    private double acceleration = 0.35;
    private double turnSpeed = 4.0;
    private double friction = 0.996;
    private double maxSpeed = 6.0;
    
    private double boostTimer = 0; // Temporary speed boost duration

    // Multi-vehicle system
    private VehicleType vehicleType = VehicleType.BOAT;
    private boolean roverUnlocked = true;
    private boolean heliUnlocked = true;

    public Boat(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        applyVehicleProperties();
    }

    private void applyVehicleProperties() {
        if (vehicleType == null) vehicleType = VehicleType.BOAT;
        this.acceleration = vehicleType.getAcceleration();
        this.turnSpeed = vehicleType.getTurnSpeed();
        this.friction = vehicleType.getFriction();
        this.maxSpeed = vehicleType.getMaxSpeed();
        this.width = vehicleType.getWidth();
        this.height = vehicleType.getHeight();
    }

    public String switchVehicle() {
        if (vehicleType == VehicleType.BOAT) {
            if (roverUnlocked) {
                vehicleType = VehicleType.ROVER;
            } else if (heliUnlocked) {
                vehicleType = VehicleType.HELICOPTER;
            }
        } else if (vehicleType == VehicleType.ROVER) {
            if (heliUnlocked) {
                vehicleType = VehicleType.HELICOPTER;
            } else {
                vehicleType = VehicleType.BOAT;
            }
        } else if (vehicleType == VehicleType.HELICOPTER) {
            vehicleType = VehicleType.BOAT;
        }

        applyVehicleProperties();
        return "Vehicle changed: " + vehicleType.getDisplayName();
    }

    public void update(boolean forward, boolean backward, boolean turnLeft, boolean turnRight, double mapWidth, double mapHeight) {
        // Handle rotation based on active turnSpeed
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

        // Acceleration along vehicle heading angle
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

        // Apply friction
        if (!forward && !backward) {
            vx *= (friction * 0.99);
            vy *= (friction * 0.99);
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

        // Keep vehicle inside map bounds
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

    public void clearCargo() {
        cargoCount = 0;
    }

    public void applySpeedBoost(double seconds) {
        this.boostTimer = seconds;
    }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType type) {
        this.vehicleType = type;
        applyVehicleProperties();
    }
    public boolean isHelicopter() { return vehicleType == VehicleType.HELICOPTER; }

    public boolean isRoverUnlocked() { return roverUnlocked; }
    public void setRoverUnlocked(boolean unlocked) { this.roverUnlocked = unlocked; }

    public boolean isHeliUnlocked() { return heliUnlocked; }
    public void setHeliUnlocked(boolean unlocked) { this.heliUnlocked = unlocked; }

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
