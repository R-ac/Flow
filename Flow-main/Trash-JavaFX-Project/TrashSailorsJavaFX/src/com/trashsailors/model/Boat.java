package com.trashsailors.model;

import java.util.ArrayList;
import java.util.List;

public class Boat {
    public enum VehicleType {
        ROVER("6x6 Land Rover Buggy", 5.4, 0.32, 3.6, 0.985, 115, 66),
        BOAT("Flood Rescue Boat", 7.8, 0.46, 4.3, 0.997, 175, 64),
        HELICOPTER("Emergency Rescue Helicopter", 9.8, 0.58, 5.0, 0.992, 130, 74);

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

    public enum CargoType {
        SURVIVOR("Rescued Student", "#ea580c"),
        NAILS("Nails Crate", "#f59e0b"),
        METAL("Metal Parts Crate", "#94a3b8");

        private final String label;
        private final String color;

        CargoType(String label, String color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public String getColor() { return color; }
    }

    private VehicleType vehicleType = VehicleType.BOAT;

    private double x;
    private double y;
    private double vx = 0;
    private double vy = 0;
    private double angle = 0; // in degrees (0 = pointing right)

    private final int maxCargoCapacity = 5;
    private final List<CargoType> cargoSlots = new ArrayList<>();

    private double acceleration = 0.44;
    private double turnSpeed = 4.3;
    private double friction = 0.997;
    private double maxSpeed = 7.4;

    private double boostTimer = 0; // Temporary speed boost duration

    public Boat(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        applyVehicleProperties();
    }

    public void switchVehicle() {
        if (vehicleType == VehicleType.BOAT) {
            vehicleType = VehicleType.ROVER;
        } else if (vehicleType == VehicleType.ROVER) {
            vehicleType = VehicleType.HELICOPTER;
        } else {
            vehicleType = VehicleType.BOAT;
        }
        applyVehicleProperties();
    }

    public void switchNextAvailable(boolean roverUnlocked, boolean heliUnlocked) {
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
    }

    public void setVehicleType(VehicleType type) {
        this.vehicleType = type;
        applyVehicleProperties();
    }

    private void applyVehicleProperties() {
        this.maxSpeed = vehicleType.getMaxSpeed();
        this.acceleration = vehicleType.getAcceleration();
        this.turnSpeed = vehicleType.getTurnSpeed();
        this.friction = vehicleType.getFriction();
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

        // Apply light glide friction
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
        double padding = getWidth() / 2;
        if (x < padding) { x = padding; vx *= -0.3; }
        if (x > mapWidth - padding) { x = mapWidth - padding; vx *= -0.3; }
        if (y < padding) { y = padding; vy *= -0.3; }
        if (y > mapHeight - padding) { y = mapHeight - padding; vy *= -0.3; }
    }

    public boolean canAddCargo() {
        return cargoSlots.size() < maxCargoCapacity;
    }

    public boolean addCargo(CargoType type) {
        if (canAddCargo()) {
            cargoSlots.add(type);
            return true;
        }
        return false;
    }

    public boolean addCargo() {
        return addCargo(CargoType.SURVIVOR);
    }

    public int countCargoType(CargoType type) {
        int count = 0;
        for (CargoType t : cargoSlots) {
            if (t == type) count++;
        }
        return count;
    }

    public int unloadSurvivors() {
        int count = 0;
        List<CargoType> remaining = new ArrayList<>();
        for (CargoType t : cargoSlots) {
            if (t == CargoType.SURVIVOR) {
                count++;
            } else {
                remaining.add(t);
            }
        }
        cargoSlots.clear();
        cargoSlots.addAll(remaining);
        return count;
    }

    public int unloadNails() {
        int count = 0;
        List<CargoType> remaining = new ArrayList<>();
        for (CargoType t : cargoSlots) {
            if (t == CargoType.NAILS) {
                count++;
            } else {
                remaining.add(t);
            }
        }
        cargoSlots.clear();
        cargoSlots.addAll(remaining);
        return count;
    }

    public int unloadMetal() {
        int count = 0;
        List<CargoType> remaining = new ArrayList<>();
        for (CargoType t : cargoSlots) {
            if (t == CargoType.METAL) {
                count++;
            } else {
                remaining.add(t);
            }
        }
        cargoSlots.clear();
        cargoSlots.addAll(remaining);
        return count;
    }

    public int unloadCargo() {
        int count = countCargoType(CargoType.SURVIVOR);
        unloadSurvivors();
        return count;
    }

    public void applySpeedBoost(double seconds) {
        this.boostTimer = seconds;
    }

    public VehicleType getVehicleType() { return vehicleType; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }
    public double getAngle() { return angle; }
    public double getWidth() { return vehicleType.getWidth(); }
    public double getHeight() { return vehicleType.getHeight(); }
    public int getCargoCount() { return cargoSlots.size(); }
    public List<CargoType> getCargoSlots() { return cargoSlots; }
    public int getMaxCargoCapacity() { return maxCargoCapacity; }
    public boolean isBoosted() { return boostTimer > 0; }
}

