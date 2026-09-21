package com.flow.model;

import java.util.List;

public class Survivor {
    private double x;
    private double y;
    private final String name;
    private double bobbingPhase;
    private boolean rescued = false;
    private double swimSpeed = 1.5;
    private Building associatedBuilding;

    // Categorization & Health Mechanics
    private SurvivorType type = SurvivorType.NORMAL;
    private double health = 100.0;
    private double maxHealth = 100.0;
    private boolean isDead = false;
    private boolean isOnSafePlatform = false;

    // Complex Triage Mechanics: Cravings, Injury Severity & Fever Complications
    private SurvivorCraving craving = SurvivorCraving.RICE_CURRY;
    private InjuryType injuryType = InjuryType.MINOR_CUT;
    private boolean hasFever = false;
    private double injuryTimer = 0.0;

    public Survivor(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.bobbingPhase = Math.random() * Math.PI * 2;
        this.craving = SurvivorCraving.values()[(int)(Math.random() * SurvivorCraving.values().length)];
        this.injuryType = InjuryType.values()[(int)(Math.random() * InjuryType.values().length)];
    }

    public Survivor(double x, double y, String name, Building building) {
        this(x, y, name);
        this.associatedBuilding = building;
    }

    public Survivor(double x, double y, String name, Building building, SurvivorType type) {
        this(x, y, name, building);
        this.type = type;
        if (type == SurvivorType.HUNGRY) {
            this.maxHealth = 200.0;
            this.health = 200.0; // Starts with full 200 health for generous survival time
        } else if (type == SurvivorType.INJURED) {
            this.maxHealth = 150.0;
            this.health = 150.0; // Injured survivor initial health
        }
    }

    public void update(Boat boat, List<Building> buildings, double dt, double hungerDepletionMultiplier) {
        bobbingPhase += 0.05;
        if (isDead) return;

        // Complication Logic: Untreated Injured survivors develop Secondary Fever after 45s of neglect
        if (type == SurvivorType.INJURED && !hasFever) {
            injuryTimer += dt;
            if (injuryTimer >= 45.0) {
                hasFever = true; // Secondary Fever complication!
            }
        }

        // Health/Hunger depletion over time for HUNGRY survivors & Fever complications
        if (type == SurvivorType.HUNGRY && !rescued && !isOnSafePlatform) {
            health -= dt * 0.4 * hungerDepletionMultiplier;
            if (health <= 0) {
                health = 0;
                isDead = true;
            }
        } else if (type == SurvivorType.HUNGRY && isOnSafePlatform) {
            health -= dt * 0.05 * hungerDepletionMultiplier;
            if (health <= 0) {
                health = 0;
                isDead = true;
            }
        }

        // Secondary Fever complication causes steady health decline
        if (hasFever) {
            health -= dt * 0.5 * hungerDepletionMultiplier;
            if (health <= 0) {
                health = 0;
                isDead = true;
            }
        }

        if (rescued && !isOnSafePlatform) return;

        double distToBoat = Math.hypot(boat.getX() - x, boat.getY() - y);

        // Feature 1: Rooftop / Building edge walk AI when boat arrives near building!
        if (associatedBuilding != null && distToBoat < 450 && !isOnSafePlatform) {
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
        // Critical health: swim toward boat when starving
        else if (isCriticallyLow() && !isOnSafePlatform) {
            double dx = boat.getX() - x;
            double dy = boat.getY() - y;

            if (distToBoat > 35 && distToBoat < 550) {
                x += (dx / distToBoat) * swimSpeed;
                y += (dy / distToBoat) * swimSpeed;
            }
        }
    }

    public boolean isCriticallyLow() {
        return (type == SurvivorType.HUNGRY && health / maxHealth < 0.30) || hasFever;
    }

    public void feed(double amount) {
        health = Math.min(maxHealth, health + amount);
        if (health >= maxHealth && !hasFever) {
            type = SurvivorType.NORMAL; // Cured of hunger!
        }
    }

    public boolean feedSpecific(SurvivorCraving servedMeal) {
        if (servedMeal == craving) {
            health = maxHealth;
            if (!hasFever || type == SurvivorType.HUNGRY) {
                type = SurvivorType.NORMAL; // Cured of craving!
            }
            return true; // Perfect Craving Match!
        } else {
            health = Math.min(maxHealth, health + 60.0);
            return false; // Partial Recovery (Craving mismatch)
        }
    }

    public void treat() {
        health = maxHealth;
        if (!hasFever) {
            type = SurvivorType.NORMAL; // Cured of injury!
        }
    }

    public boolean treatSpecific(InjuryType treatmentGiven) {
        if (treatmentGiven == injuryType) {
            health = maxHealth;
            if (hasFever) {
                hasFever = false; // Fever cleared!
            } else {
                type = SurvivorType.NORMAL; // Fully Cured!
            }
            return true; // Perfect Triage Match!
        } else {
            health = Math.min(maxHealth, health + 50.0);
            return false; // Partial Healing (Injury mismatch)
        }
    }

    public double getBobbingOffset() {
        return Math.sin(bobbingPhase) * 4;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public String getName() { return name; }
    public boolean isRescued() { return rescued; }
    public void setRescued(boolean rescued) { this.rescued = rescued; }
    public Building getAssociatedBuilding() { return associatedBuilding; }
    public void setAssociatedBuilding(Building building) { this.associatedBuilding = building; }

    public SurvivorType getType() { return type; }
    public void setType(SurvivorType type) { this.type = type; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public boolean isDead() { return isDead; }
    public boolean isOnSafePlatform() { return isOnSafePlatform; }
    public void setOnSafePlatform(boolean onPlatform) { this.isOnSafePlatform = onPlatform; }

    public SurvivorCraving getCraving() { return craving; }
    public void setCraving(SurvivorCraving craving) { this.craving = craving; }
    public InjuryType getInjuryType() { return injuryType; }
    public void setInjuryType(InjuryType injuryType) { this.injuryType = injuryType; }
    public boolean hasFever() { return hasFever; }
    public void setHasFever(boolean fever) { this.hasFever = fever; }
}
