package com.flow.controller;

import com.flow.model.*;
import com.flow.view.SoundManager;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameEngine {
    private final double mapWidth = 6000;
    private final double mapHeight = 4200;

    private Boat boat;
    private Crew crew;
    private SafeZone safeZone;

    private List<Building> buildings = new ArrayList<>();
    private List<Survivor> survivors = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<>();
    private List<Creature> creatures = new ArrayList<>();
    private List<UsefulObject> usefulObjects = new ArrayList<>();
    private List<FloatingText> floatingTexts = new ArrayList<>();

    private List<Survivor> boatCargoSurvivors = new ArrayList<>();
    private List<Survivor> safePlatformSurvivors = new ArrayList<>();

    private RopeAnimation activeRopeAnimation = new RopeAnimation();
    private Survivor aimedSurvivor = null;
    private CafeteriaState cafeteriaState = new CafeteriaState();
    private MedicalCenterState medicalCenterState = new MedicalCenterState();

    // Tracking Metrics & Stats (Continuous Gameplay)
    private int totalInjuredSpawned = 0;
    private int injuredTreatedCount = 0;
    private int totalHungrySpawned = 0;
    private int hungryFedCount = 0;
    private int normalRescuedCount = 0;
    private int totalPerishedCount = 0;

    private double survivorRespawnTimer = 0.0;
    private int survivorNamingCounter = 1;

    private double cameraZoom = 1.0;
    private int totalSurvivorsInLevel = 0;
    private int totalDelivered = 0;
    private int score = 0;

    // Difficulty (not time-based)
    private int evacuationGoal = 8;
    private String difficultyLabel = "EASY";
    private String currentDifficulty = "EASY";
    private double spawnIntervalSeconds = 15.0;
    private int maxUnrescuedBeforeSpawn = 12;
    private int maxPerishedBeforeFail = 5;
    private double hungerDepletionMultiplier = 0.8;
    private int creatureCount = 1;

    // Rescue Combo Streak
    private int rescueCombo = 0;
    private double rescueComboTimer = 0.0;
    private static final double COMBO_WINDOW_SECONDS = 8.0;

    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean menuOpen = true;
    private String statusMessage = "Continuous Mission: Rescue, Feed, and Treat survivors across campus!";

    public GameEngine() {
        reset();
    }

    public boolean isMenuOpen() { return menuOpen; }
    public void setMenuOpen(boolean open) { this.menuOpen = open; }

    public void setDifficulty(String level) {
        currentDifficulty = level.toUpperCase();
        applyDifficultySettings();
        reset();
    }

    private void applyDifficultySettings() {
        switch (currentDifficulty) {
            case "COZY":
                difficultyLabel = "COZY (RELAXED)";
                evacuationGoal = 15;
                spawnIntervalSeconds = 25.0;
                maxUnrescuedBeforeSpawn = 20;
                maxPerishedBeforeFail = 9999; // Never fail from deaths!
                hungerDepletionMultiplier = 0.0; // Zero hunger decay
                creatureCount = 0;
                break;
            case "HARD":
                difficultyLabel = "HARD";
                evacuationGoal = 18;
                spawnIntervalSeconds = 8.0;
                maxUnrescuedBeforeSpawn = 10;
                maxPerishedBeforeFail = 10;
                hungerDepletionMultiplier = 0.8;
                creatureCount = 3;
                break;
            case "MEDIUM":
                difficultyLabel = "MEDIUM";
                evacuationGoal = 12;
                spawnIntervalSeconds = 15.0;
                maxUnrescuedBeforeSpawn = 14;
                maxPerishedBeforeFail = 15;
                hungerDepletionMultiplier = 0.4;
                creatureCount = 2;
                break;
            case "EASY":
            default:
                difficultyLabel = "EASY";
                evacuationGoal = 8;
                spawnIntervalSeconds = 20.0;
                maxUnrescuedBeforeSpawn = 18;
                maxPerishedBeforeFail = 25; // 25 mistakes allowed!
                hungerDepletionMultiplier = 0.2; // Very slow decay
                creatureCount = 1;
                break;
        }
    }

    public void reset() {
        // The start screen and difficulty selection can both call reset().
        // Rebuild the campus from a clean list so copies do not accumulate.
        buildings.clear();
        setupIUTCampusMapMassive();

        double spawnX = 650;
        double spawnY = 2200;
        boat = new Boat(spawnX, spawnY);
        crew = new Crew();

        ensureNoBoatOverlap(spawnX, spawnY);
        safeZone = new SafeZone(3550, 1290); // Positioned beside Cafeteria / Library North Dock

        survivors.clear();
        obstacles.clear();
        creatures.clear();
        usefulObjects.clear();
        floatingTexts.clear();
        boatCargoSurvivors.clear();
        safePlatformSurvivors.clear();
        aimedSurvivor = null;
        cafeteriaState = new CafeteriaState();
        medicalCenterState = new MedicalCenterState();

        totalInjuredSpawned = 0;
        injuredTreatedCount = 0;
        totalHungrySpawned = 0;
        hungryFedCount = 0;
        normalRescuedCount = 0;
        totalPerishedCount = 0;
        survivorRespawnTimer = 0.0;
        survivorNamingCounter = 1;

        cameraZoom = 1.0;
        totalDelivered = 0;
        score = 0;
        rescueCombo = 0;
        rescueComboTimer = 0.0;
        gameOver = false;
        gameWon = false;
        applyDifficultySettings();

        // Spawn Initial 16 Campus Survivors across various locations
        Building ab3 = findBuildingByName("Academic Building-3");
        Building ab2 = findBuildingByName("Academic Building-2");
        Building ab1 = findBuildingByName("Academic Building-1");
        Building southHall = findBuildingByName("South Hall of Residence");
        Building northHall = findBuildingByName("North Hall of Residence");
        Building treePark = findBuildingByName("OIC Tree Park");
        Building gym = findBuildingByName("Students Centre & Gymnasium");
        Building femaleHall = findBuildingByName("Female Hall of Residence");
        Building library = findBuildingByName("Cafeteria / Library");
        Building admin = findBuildingByName("Administrative Building");
        Building auditorium = findBuildingByName("Auditorium");
        Building mosque = findBuildingByName("IUT Mosque");
        Building res1 = findBuildingByName("Residential Building 1");
        Building res2 = findBuildingByName("Residential Building 2");

        // 1. Rooftop Trapped Survivors (Strictly on building rooftops, never enter water)
        spawnSurvivor(1200, 300, "Alex (AB-3)", ab3, SurvivorType.HUNGRY, true);
        spawnSurvivor(2600, 960, "Sam (AB-1)", ab1, SurvivorType.INJURED, true);
        spawnSurvivor(2600, 360, "Chris (AB-2)", ab2, SurvivorType.INJURED, true);
        spawnSurvivor(3440, 330, "Pat (North Hall)", northHall, SurvivorType.HUNGRY, true);
        spawnSurvivor(2550, 1600, "Jesse (Admin)", admin, SurvivorType.NORMAL, true);
        spawnSurvivor(2200, 2200, "Avery (Auditorium)", auditorium, SurvivorType.INJURED, true);
        spawnSurvivor(2920, 2370, "Robin (Mosque)", mosque, SurvivorType.HUNGRY, true);
        spawnSurvivor(4400, 1620, "Jordan (Gymnasium)", gym, SurvivorType.NORMAL, true);
        spawnSurvivor(670, 3310, "Riley (Female Hall)", femaleHall, SurvivorType.HUNGRY, true);
        spawnSurvivor(590, 2140, "Drew (Res-1)", res1, SurvivorType.NORMAL, true);
        spawnSurvivor(590, 2640, "Logan (Res-2)", res2, SurvivorType.INJURED, true);

        // 2. Drowning Water Survivors (Strictly in deep floodwater avenues with swim buoys, never in buildings)
        spawnSurvivor(1420, 2350, "Taylor (Tree Park Water)", null, SurvivorType.INJURED, false);
        spawnSurvivor(4000, 680, "Morgan (East Water Avenue)", null, SurvivorType.HUNGRY, false);
        spawnSurvivor(3400, 2050, "Casey (Central Water Avenue)", null, SurvivorType.NORMAL, false);
        spawnSurvivor(750, 750, "Quinn (West Water Canal)", null, SurvivorType.HUNGRY, false);
        spawnSurvivor(4450, 2500, "Reese (Sports Field Water)", null, SurvivorType.NORMAL, false);

        totalSurvivorsInLevel = survivors.size();

        obstacles.add(new Obstacle(1400, 700, 55, "SUBMERGED_CAR"));
        obstacles.add(new Obstacle(2900, 550, 60, "ROAD_BARRIER"));
        obstacles.add(new Obstacle(3800, 1200, 58, "FALLEN_TREE"));
        obstacles.add(new Obstacle(2100, 1800, 55, "FLOODED_DUMPSTER"));
        obstacles.add(new Obstacle(4200, 2800, 60, "SUBMERGED_CAR"));

        spawnCreaturesForDifficulty();

        usefulObjects.add(new UsefulObject(1400, 1900, "TURBO_FUEL", 150));
        usefulObjects.add(new UsefulObject(3300, 1600, "SUPPLY_CHEST", 300));
        usefulObjects.add(new UsefulObject(4600, 600, "RECYCLABLE_TRASH", 100));
        usefulObjects.add(new UsefulObject(3800, 3500, "TURBO_FUEL", 150));

        statusMessage = difficultyLabel + " Mission: Evacuate " + evacuationGoal + " survivors! [C] Cafeteria, [M] Medical";
    }

    private void spawnCreaturesForDifficulty() {
        creatures.clear();
        creatures.add(new Creature(3200, 3100, "Central Pond Monster"));
        if (creatureCount >= 2) {
            creatures.add(new Creature(2100, 2700, "Auditorium Alley Croc"));
        }
        if (creatureCount >= 3) {
            creatures.add(new Creature(1400, 1200, "Workshop Swamp Beast"));
        }
    }

    private SurvivorType rollSurvivorTypeForDifficulty() {
        double rnd = Math.random();
        if ("HARD".equals(currentDifficulty)) {
            return rnd < 0.50 ? SurvivorType.HUNGRY : rnd < 0.85 ? SurvivorType.INJURED : SurvivorType.NORMAL;
        } else if ("EASY".equals(currentDifficulty)) {
            return rnd < 0.25 ? SurvivorType.HUNGRY : rnd < 0.45 ? SurvivorType.INJURED : SurvivorType.NORMAL;
        }
        return rnd < 0.40 ? SurvivorType.HUNGRY : rnd < 0.75 ? SurvivorType.INJURED : SurvivorType.NORMAL;
    }

    private void spawnSurvivor(double x, double y, String name, Building building, SurvivorType type, boolean rooftopTrapped) {
        Survivor s = new Survivor(x, y, name, building, type, rooftopTrapped);
        survivors.add(s);
        if (type == SurvivorType.HUNGRY) totalHungrySpawned++;
        else if (type == SurvivorType.INJURED) totalInjuredSpawned++;
    }

    private void updateSurvivorSpawning(double deltaSeconds) {
        survivorRespawnTimer += deltaSeconds;
        long unrescuedCount = survivors.stream().filter(s -> !s.isRescued() && !s.isDead()).count();

        if (unrescuedCount < maxUnrescuedBeforeSpawn || survivorRespawnTimer >= spawnIntervalSeconds) {
            survivorRespawnTimer = 0.0;
            if (buildings.isEmpty()) return;

            SurvivorType type = rollSurvivorTypeForDifficulty();
            boolean spawnOnRooftop = Math.random() < 0.50;

            if (spawnOnRooftop) {
                // Pick a random solid building
                List<Building> solidBldgs = new ArrayList<>();
                for (Building b : buildings) {
                    if (!"FIELD".equals(b.getType()) && !"PARK_TREES".equals(b.getType()) && !"WORKSHOP_DEPOT".equals(b.getType())) {
                        solidBldgs.add(b);
                    }
                }
                if (!solidBldgs.isEmpty()) {
                    Building b = solidBldgs.get((int)(Math.random() * solidBldgs.size()));
                    double rx = b.getX() + 35 + Math.random() * Math.max(10, b.getWidth() - 70);
                    double ry = b.getY() + 35 + Math.random() * Math.max(10, b.getHeight() - 70);
                    String name = "Rooftop Survivor #" + (++survivorNamingCounter) + " (" + b.getNameLabel() + ")";
                    spawnSurvivor(rx, ry, name, b, type, true);
                    totalSurvivorsInLevel++;
                    addFloatingText("🚨 NEW ROOFTOP SURVIVOR AT " + b.getNameLabel().toUpperCase() + "!", rx, ry - 30, "#FFCC00");
                }
            } else {
                // Spawn Drowning Survivor in Floodwater Avenues (outside all buildings)
                double wx = 0, wy = 0;
                boolean validWater = false;
                for (int attempt = 0; attempt < 50; attempt++) {
                    wx = 400 + Math.random() * (mapWidth - 800);
                    wy = 400 + Math.random() * (mapHeight - 800);

                    boolean insideBldg = false;
                    for (Building b : buildings) {
                        if (!"FIELD".equals(b.getType()) && !"PARK_TREES".equals(b.getType())) {
                            if (wx >= b.getX() - 35 && wx <= b.getX() + b.getWidth() + 35 &&
                                wy >= b.getY() - 35 && wy <= b.getY() + b.getHeight() + 35) {
                                insideBldg = true;
                                break;
                            }
                        }
                    }
                    if (!insideBldg) {
                        validWater = true;
                        break;
                    }
                }
                if (validWater) {
                    String name = "Drowning Survivor #" + (++survivorNamingCounter);
                    spawnSurvivor(wx, wy, name, null, type, false);
                    totalSurvivorsInLevel++;
                    addFloatingText("🌊 NEW DROWNING SURVIVOR IN WATER AVENUE!", wx, wy - 30, "#38BDF8");
                }
            }
        }
    }

    private void ensureNoBoatOverlap(double x, double y) {
        for (Building b : buildings) {
            if (!"FIELD".equals(b.getType()) && b.intersects(x, y, boat.getWidth() + 60, boat.getHeight() + 60)) {
                boat.setX(x + 400);
                boat.setY(y + 400);
                break;
            }
        }
    }

    private Building findBuildingByName(String name) {
        for (Building b : buildings) {
            if (name.equalsIgnoreCase(b.getNameLabel())) return b;
        }
        return null;
    }

    private void setupIUTCampusMapMassive() {
        String iutRed = "#dc2626";

        buildings.add(new Building(150, 200, 220, 600, "ACADEMIC", "#991b1b", "Medical Centre & Laundry"));
        buildings.add(new Building(950, 200, 950, 200, "ACADEMIC", iutRed, "Academic Building-3"));
        buildings.add(new Building(950, 500, 950, 220, "ACADEMIC", iutRed, "North Academic Wing"));
        buildings.add(new Building(950, 850, 950, 220, "ACADEMIC", iutRed, "Middle Academic Wing"));
        buildings.add(new Building(950, 1200, 950, 220, "ACADEMIC", iutRed, "South Academic Wing"));

        buildings.add(new Building(950, 1700, 950, 1250, "PARK_TREES", "#15803d", "OIC Tree Park"));
        buildings.add(new Building(400, 2000, 380, 280, "ACADEMIC", "#7f1d1d", "Residential Building 1"));
        buildings.add(new Building(400, 2500, 380, 280, "ACADEMIC", "#7f1d1d", "Residential Building 2"));
        buildings.add(new Building(300, 3200, 750, 220, "HALL", iutRed, "Female Hall of Residence"));
        buildings.add(new Building(400, 3550, 650, 220, "ACADEMIC", iutRed, "Female Common Facilities"));

        buildings.add(new Building(2250, 240, 700, 240, "ACADEMIC", iutRed, "Academic Building-2"));
        buildings.add(new Building(2250, 850, 700, 220, "ACADEMIC", iutRed, "Academic Building-1"));

        buildings.add(new Building(3200, 240, 480, 180, "HALL", iutRed, "North Hall of Residence"));
        buildings.add(new Building(3800, 240, 480, 180, "HALL", iutRed, "North Hall of Residence"));
        buildings.add(new Building(4400, 240, 480, 180, "HALL", iutRed, "North Hall of Residence"));

        buildings.add(new Building(3200, 850, 480, 180, "HALL", iutRed, "South Hall of Residence"));
        buildings.add(new Building(3800, 850, 480, 180, "HALL", iutRed, "South Hall of Residence"));
        buildings.add(new Building(4400, 850, 480, 180, "HALL", iutRed, "South Hall of Residence"));

        buildings.add(new Building(5050, 200, 250, 1000, "HALL", iutRed, "North Cafeteria & Common"));

        buildings.add(new Building(2250, 1450, 600, 300, "ACADEMIC", iutRed, "Administrative Building"));
        buildings.add(new Building(3050, 1450, 600, 480, "ACADEMIC", iutRed, "Cafeteria / Library"));
        buildings.add(new Building(1900, 1950, 650, 500, "ACADEMIC", iutRed, "Auditorium"));
        buildings.add(new Building(2700, 2150, 450, 450, "MOSQUE", iutRed, "IUT Mosque"));

        buildings.add(new Building(3900, 1450, 1000, 350, "ACADEMIC", iutRed, "Students Centre & Gymnasium"));
        buildings.add(new Building(3900, 1950, 1100, 1500, "FIELD", "#166534", "IUT Sports Field"));

        // Standalone Emergency Vehicle Workshop & Slipway Depot in the Lower Middle of the map
        buildings.add(new Building(2400, 3100, 900, 460, "WORKSHOP_DEPOT", "#0284c7", "Emergency Vehicle Workshop & Depot"));
    }

    public void update(InputHandler input, double deltaSeconds) {
        if (gameOver || gameWon) return;

        if (rescueComboTimer > 0) {
            rescueComboTimer -= deltaSeconds;
            if (rescueComboTimer <= 0) rescueCombo = 0;
        }

        if (totalDelivered >= evacuationGoal) {
            gameWon = true;
            statusMessage = "All evacuation targets met! IUT campus secured!";
            SoundManager.playWinSound();
            return;
        }

        if (totalPerishedCount >= maxPerishedBeforeFail) {
            gameOver = true;
            statusMessage = "Too many survivors perished! Evacuation mission failed.";
            SoundManager.playGameOverSound();
            return;
        }

        // Continuous Survivor Spawning
        updateSurvivorSpawning(deltaSeconds);

        // Update Medical Center Transition
        if (medicalCenterState.isTransitioning()) {
            medicalCenterState.updateTransition(deltaSeconds);
            return;
        }

        // Inside Medical Center Healthcare View Controls
        if (medicalCenterState.isInsideMedicalCenter()) {
            updateMedicalCenterTreatment(input, deltaSeconds);
            return;
        }

        // Update Cafeteria Transition
        if (cafeteriaState.isTransitioning()) {
            cafeteriaState.updateTransition(deltaSeconds);
            return;
        }

        // Inside Cafeteria Cooking View Controls
        if (cafeteriaState.isInsideCafeteria()) {
            updateCafeteriaCooking(input, deltaSeconds);
            return;
        }

        // Check proximity to Medical Center
        Building medCenter = findBuildingByName("Medical Centre & Laundry");
        boolean nearMedicalCenter = false;
        if (medCenter != null) {
            double dist = Math.hypot(boat.getX() - (medCenter.getX() + medCenter.getWidth() / 2),
                                     boat.getY() - (medCenter.getY() + medCenter.getHeight() / 2));
            if (dist < 420) {
                nearMedicalCenter = true;
                statusMessage = "🏥 MEDICAL CENTER NEARBY! Press [M] or [SPACE] to enter & Treat Injured!";
            }
        }

        if (nearMedicalCenter && (input.consumeKeyPressed(KeyCode.M) || input.consumeSpacePressed())) {
            medicalCenterState.startTransition(true); // Enter Medical Center
            SoundManager.playCollectSound();
            return;
        }

        // Check proximity to Cafeteria
        Building cafeteria = findBuildingByName("Cafeteria / Library");
        boolean nearCafeteria = false;
        if (cafeteria != null) {
            double dist = Math.hypot(boat.getX() - (cafeteria.getX() + cafeteria.getWidth() / 2),
                                     boat.getY() - (cafeteria.getY() + cafeteria.getHeight() / 2));
            if (dist < 420) {
                nearCafeteria = true;
                statusMessage = "🍽️ CAFETERIA NEARBY! Press [C] or [SPACE] to enter Kitchen & Cook Food!";
            }
        }

        if (nearCafeteria && (input.consumeKeyPressed(KeyCode.C) || input.consumeSpacePressed())) {
            cafeteriaState.startTransition(true); // Enter Cafeteria
            SoundManager.playCollectSound();
            return;
        }

        // Check proximity to Emergency Vehicle Workshop & Depot (Lower-Middle map station)
        Building workshop = null;
        for (Building b : buildings) {
            if ("WORKSHOP_DEPOT".equals(b.getType())) {
                workshop = b;
                break;
            }
        }
        if (workshop != null) {
            double dist = Math.hypot(boat.getX() - (workshop.getX() + workshop.getWidth() / 2),
                                     boat.getY() - (workshop.getY() + workshop.getHeight() / 2));
            if (dist < 550) {
                statusMessage = "🏭 VEHICLE WORKSHOP & DEPOT! Drive onto turntable & Press [V] to toggle RESCUE BOAT / 6×6 ROVER!";
            }
        }

        // Vehicle Switching Controls [V] (ONLY permitted when inside the Emergency Vehicle Workshop & Depot!)
        if (input.consumeKeyPressed(KeyCode.V)) {
            if (isInsideWorkshop(boat)) {
                String msg = boat.switchVehicle();
                SoundManager.playCollectSound();
                addFloatingText("🛸 " + msg.toUpperCase() + "!", boat.getX(), boat.getY() - 60, "#38BDF8");
                statusMessage = msg + " active!";
            } else {
                addFloatingText("⚠️ GO INSIDE WORKSHOP TO CHANGE VEHICLE!", boat.getX(), boat.getY() - 60, "#EF4444");
                statusMessage = "🔒 Depot Locked! Navigate inside the Emergency Vehicle Workshop (Middle-Lower Map) to change vehicles!";
                SoundManager.playGameOverSound();
            }
        }

        // Camera Zoom handling
        double scrollDelta = input.consumeScrollDeltaY();
        if (scrollDelta > 0 || input.isKeyPressed(KeyCode.Q) || input.isKeyPressed(KeyCode.EQUALS)) {
            cameraZoom = Math.min(1.8, cameraZoom + 0.02);
        } else if (scrollDelta < 0 || input.isKeyPressed(KeyCode.E) || input.isKeyPressed(KeyCode.MINUS)) {
            cameraZoom = Math.max(0.4, cameraZoom - 0.02);
        }

        // Pilot Controls: WASD
        boolean forward = input.isKeyPressed(KeyCode.W);
        boolean backward = input.isKeyPressed(KeyCode.S);
        boolean turnLeft = input.isKeyPressed(KeyCode.A);
        boolean turnRight = input.isKeyPressed(KeyCode.D);
        boat.update(forward, backward, turnLeft, turnRight, mapWidth, mapHeight);

        // Smooth Hard-Wall Sliding Collision Resolution (Helicopter flies over buildings & terrain!)
        if (!boat.isHelicopter()) {
            double bw = boat.getWidth();
            double bh = boat.getHeight();
            double bx = boat.getX();
            double by = boat.getY();

            for (Building b : buildings) {
                if ("WORKSHOP_DEPOT".equals(b.getType())) {
                    continue; // Workshop depot has its own internal dock
                }

                // 6x6 Rover (Land Buggy) can drive freely through BOTH green sports fields (FIELD) AND dense forests (PARK_TREES)!
                if (("FIELD".equals(b.getType()) || "PARK_TREES".equals(b.getType())) && boat.getVehicleType() == VehicleType.ROVER) {
                    continue;
                }

                if (b.intersects(bx, by, bw, bh)) {
                    double overlapLeft = (bx + bw / 2.0) - b.getX();
                    double overlapRight = (b.getX() + b.getWidth()) - (bx - bw / 2.0);
                    double overlapTop = (by + bh / 2.0) - b.getY();
                    double overlapBottom = (b.getY() + b.getHeight()) - (by - bh / 2.0);

                    double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

                    if (minOverlap == overlapLeft) {
                        boat.setX(b.getX() - bw / 2.0 - 0.5);
                        if (boat.getVx() > 0) boat.setVx(0);
                    } else if (minOverlap == overlapRight) {
                        boat.setX(b.getX() + b.getWidth() + bw / 2.0 + 0.5);
                        if (boat.getVx() < 0) boat.setVx(0);
                    } else if (minOverlap == overlapTop) {
                        boat.setY(b.getY() - bh / 2.0 - 0.5);
                        if (boat.getVy() > 0) boat.setVy(0);
                    } else if (minOverlap == overlapBottom) {
                        boat.setY(b.getY() + b.getHeight() + bh / 2.0 + 0.5);
                        if (boat.getVy() < 0) boat.setVy(0);
                    }

                    bx = boat.getX();
                    by = boat.getY();

                    if ("FIELD".equals(b.getType()) && boat.getVehicleType() == VehicleType.BOAT) {
                        statusMessage = "⛔ SHALLOW GREEN FIELD! Switch to 6x6 Land Buggy in Workshop to drive on fields!";
                    } else if ("PARK_TREES".equals(b.getType()) && boat.getVehicleType() == VehicleType.BOAT) {
                        statusMessage = "⛔ DENSE TREE PARK! Switch to 6x6 Land Buggy in Workshop to enter forest!";
                    }
                }
            }
        }

        // Crew Controls: Arrow Keys
        boolean cUp = input.isKeyPressed(KeyCode.UP);
        boolean cDown = input.isKeyPressed(KeyCode.DOWN);
        boolean cLeft = input.isKeyPressed(KeyCode.LEFT);
        boolean cRight = input.isKeyPressed(KeyCode.RIGHT);
        crew.update(cUp, cDown, cLeft, cRight);

        updateAimedSurvivor();

        if (activeRopeAnimation.isActive()) {
            activeRopeAnimation.update(deltaSeconds);
            if (!activeRopeAnimation.isActive() && activeRopeAnimation.getTargetSurvivor() != null) {
                Survivor target = activeRopeAnimation.getTargetSurvivor();
                if (!target.isRescued() && boat.canAddCargo()) {
                    target.setRescued(true);
                    boat.addCargo();
                    boatCargoSurvivors.add(target);
                    registerRescueCombo();
                    int comboBonus = rescueCombo > 1 ? (rescueCombo - 1) * 50 : 0;
                    score += 120 + comboBonus;
                    SoundManager.playRescueSound();
                    String comboMsg = rescueCombo > 1 ? " 🔥 x" + rescueCombo + " COMBO!" : "";
                    addFloatingText("ROPE RESCUED " + target.getName() + " (" + target.getType() + ")!" + comboMsg, crew.getWorldX(boat), crew.getWorldY(boat) - 30, "#33CCFF");
                    statusMessage = "Rescued survivor! Type: " + target.getType() + ". Deliver to Safe Platform / Helipad!";
                }
            }
        }

        for (Creature creature : creatures) {
            creature.update(boat, buildings);
        }

        // Survivor updates & health depletion
        Iterator<Survivor> sIter = survivors.iterator();
        while (sIter.hasNext()) {
            Survivor s = sIter.next();
            s.update(boat, buildings, deltaSeconds, hungerDepletionMultiplier);
            if (s.isDead()) {
                addFloatingText("💔 SURVIVOR " + s.getName().toUpperCase() + " PERISHED OF HUNGER!", s.getX(), s.getY() - 50, "#FF1111");
                score = Math.max(0, score - 200);
                totalPerishedCount++;
                sIter.remove();
                safePlatformSurvivors.remove(s);
                boatCargoSurvivors.remove(s);
                SoundManager.playGameOverSound();
                if (totalPerishedCount >= maxPerishedBeforeFail) {
                    gameOver = true;
                    statusMessage = "Too many survivors perished! Evacuation mission failed.";
                }
            }
        }

        for (Obstacle o : obstacles) {
            if (!o.isCleared()) {
                double dx = boat.getX() - o.getX();
                double dy = boat.getY() - o.getY();
                double dist = Math.hypot(dx, dy);
                double minDist = (boat.getWidth() / 2.0) + o.getRadius();
                if (dist < minDist && dist > 0.001) {
                    double pushOut = minDist - dist + 0.5;
                    boat.setX(boat.getX() + (dx / dist) * pushOut);
                    boat.setY(boat.getY() + (dy / dist) * pushOut);
                }
            }
        }

        if (input.consumeSpacePressed() || input.consumeKeyPressed(KeyCode.R)) {
            handleCrewInteraction();
        }

        // Safe Platform Staging & Delivery Logic!
        if (safeZone.isBoatInside(boat) && boat.getCargoCount() > 0) {
            boolean isHeli = boat.isHelicopter();
            boat.unloadCargo();
            for (Survivor s : boatCargoSurvivors) {
                if (s.getType() == SurvivorType.NORMAL) {
                    totalDelivered++;
                    normalRescuedCount++;
                    int pts = isHeli ? 375 : 250;
                    score += pts;
                    String text = isHeli ? "🚁 AERIAL WINCH EVACUATION! (+375 PTS)" : "DELIVERED NORMAL SURVIVOR! (+250 PTS)";
                    addFloatingText(text, boat.getX(), boat.getY() - 70, isHeli ? "#38BDF8" : "#00FF88");
                } else {
                    // Stage HUNGRY or INJURED survivors onto the Safe Platform (beside Cafeteria)!
                    s.setOnSafePlatform(true);
                    s.setX(3190 + 35 + (safePlatformSurvivors.size() % 4) * 50);
                    s.setY(1220 + 35 + (safePlatformSurvivors.size() / 4) * 55);
                    safePlatformSurvivors.add(s);
                    addFloatingText("STAGED ON SAFE PLATFORM! " + s.getType() + " Needs Assistance!", boat.getX(), boat.getY() - 80, "#FACC15");
                }
            }
            boatCargoSurvivors.clear();
            SoundManager.playDeliverySound();
        }

        // Helicopter Aerial Food Airdrop [H]
        if (boat.isHelicopter() && input.consumeKeyPressed(KeyCode.H)) {
            if (cafeteriaState.getCookedMealsCount() > 0) {
                Survivor hungryTarget = null;
                for (Survivor s : safePlatformSurvivors) {
                    if (s.getType() == SurvivorType.HUNGRY) { hungryTarget = s; break; }
                }
                if (hungryTarget == null) {
                    for (Survivor s : survivors) {
                        if (!s.isDead() && s.getType() == SurvivorType.HUNGRY) {
                            double dist = Math.hypot(boat.getX() - s.getX(), boat.getY() - s.getY());
                            if (dist < 400) { hungryTarget = s; break; }
                        }
                    }
                }
                if (hungryTarget != null) {
                    cafeteriaState.consumeMeal();
                    hungryTarget.feed(100);
                    totalDelivered++;
                    hungryFedCount++;
                    safePlatformSurvivors.remove(hungryTarget);
                    score += 400;
                    SoundManager.playDeliverySound();
                    addFloatingText("🪂 AERIAL FOOD AIRDROP! +400 PTS!", boat.getX(), boat.getY() - 90, "#00FFCC");
                } else {
                    addFloatingText("NO HUNGRY SURVIVOR NEARBY FOR AIRDROP!", boat.getX(), boat.getY() - 70, "#FF8800");
                }
            } else {
                addFloatingText("NO MEALS IN STOCK! COOK AT CAFETERIA [C]!", boat.getX(), boat.getY() - 70, "#FF4444");
            }
        }

        // Feeding & Treating Survivors on Safe Platform (Press 'F', 'T', or 'Space' near Safe Platform)
        double distToSafePlatform = Math.hypot(boat.getX() - 3315, boat.getY() - 1290);
        if (distToSafePlatform < 380) {
            if (input.consumeKeyPressed(KeyCode.F) || input.consumeSpacePressed()) {
                Survivor hungryTarget = null;
                for (Survivor s : safePlatformSurvivors) {
                    if (s.getType() == SurvivorType.HUNGRY || s.hasFever()) {
                        hungryTarget = s;
                        break;
                    }
                }
                if (hungryTarget != null) {
                    SurvivorCraving desired = hungryTarget.getCraving();
                    if (cafeteriaState.consumeCravingMeal(desired)) {
                        boolean perfect = hungryTarget.feedSpecific(desired);
                        totalDelivered++;
                        hungryFedCount++;
                        if (hungryTarget.getType() == SurvivorType.NORMAL) {
                            safePlatformSurvivors.remove(hungryTarget);
                        }
                        score += 350;
                        SoundManager.playDeliverySound();
                        addFloatingText("🌟 PERFECT MEAL MATCH (" + desired.getIcon() + ")! +350 PTS!", boat.getX(), boat.getY() - 90, "#00FFCC");
                    } else if (cafeteriaState.getCookedMealsCount() > 0) {
                        cafeteriaState.consumeMeal();
                        hungryTarget.feed(70);
                        score += 150;
                        SoundManager.playDeliverySound();
                        addFloatingText("🍛 PARTIAL MEAL (+150 PTS) - CRAVES " + desired.getDisplayName().toUpperCase() + "!", boat.getX(), boat.getY() - 90, "#FACC15");
                    } else {
                        addFloatingText("NO MEALS IN STOCK! COOK AT CAFETERIA [C]!", boat.getX(), boat.getY() - 70, "#FF4444");
                    }
                }
            }

            if (input.consumeKeyPressed(KeyCode.T)) {
                Survivor injuredTarget = null;
                for (Survivor s : safePlatformSurvivors) {
                    if (s.getType() == SurvivorType.INJURED || s.hasFever()) {
                        injuredTarget = s;
                        break;
                    }
                }
                if (injuredTarget != null) {
                    InjuryType needed = injuredTarget.getInjuryType();
                    if (medicalCenterState.consumeInjuryKit(needed)) {
                        boolean perfect = injuredTarget.treatSpecific(needed);
                        totalDelivered++;
                        injuredTreatedCount++;
                        if (injuredTarget.getType() == SurvivorType.NORMAL) {
                            safePlatformSurvivors.remove(injuredTarget);
                        }
                        score += 400;
                        SoundManager.playDeliverySound();
                        addFloatingText("🩺 PERFECT TRIAGE MATCH (" + needed.getIcon() + ")! +400 PTS!", boat.getX(), boat.getY() - 90, "#38BDF8");
                    } else if (medicalCenterState.getTreatedSurvivorsCount() > 0) {
                        medicalCenterState.consumeAnyKit();
                        injuredTarget.treat();
                        score += 150;
                        SoundManager.playDeliverySound();
                        addFloatingText("🩹 PARTIAL HEALING (+150 PTS) - REQUIRES " + needed.getDisplayName().toUpperCase() + "!", boat.getX(), boat.getY() - 90, "#FACC15");
                    } else {
                        addFloatingText("NO MEDICAL KITS IN STOCK! PREPARE AT MEDICAL CENTER [M]!", boat.getX(), boat.getY() - 70, "#FF4444");
                    }
                }
            }
        }

        Iterator<FloatingText> ftIter = floatingTexts.iterator();
        while (ftIter.hasNext()) {
            FloatingText ft = ftIter.next();
            ft.update(deltaSeconds);
            if (ft.isDead()) {
                ftIter.remove();
            }
        }
    }

    private void updateCafeteriaCooking(InputHandler input, double deltaSeconds) {
        if (input.consumeKeyPressed(KeyCode.ESCAPE) || input.consumeKeyPressed(KeyCode.C)) {
            cafeteriaState.startTransition(false); // Leave Cafeteria
            SoundManager.playCollectSound();
            return;
        }

        if (!cafeteriaState.isCooking()) {
            if (input.consumeKeyPressed(KeyCode.DIGIT1) || input.consumeKeyPressed(KeyCode.NUMPAD1)) {
                cafeteriaState.startCooking("Warm Rice & Curry", 0.4);
                SoundManager.playCollectSound();
            } else if (input.consumeKeyPressed(KeyCode.DIGIT2) || input.consumeKeyPressed(KeyCode.NUMPAD2)) {
                cafeteriaState.startCooking("Grilled Fish Stew", 0.6);
                SoundManager.playCollectSound();
            } else if (input.consumeKeyPressed(KeyCode.DIGIT3) || input.consumeKeyPressed(KeyCode.NUMPAD3)) {
                cafeteriaState.startCooking("Hot Tea & Snacks", 0.2);
                SoundManager.playCollectSound();
            }
        } else {
            double duration = "Warm Rice & Curry".equals(cafeteriaState.getCurrentRecipe()) ? 0.4 :
                             "Grilled Fish Stew".equals(cafeteriaState.getCurrentRecipe()) ? 0.6 : 0.2;

            if (cafeteriaState.updateCooking(deltaSeconds, duration)) {
                score += 150;
                SoundManager.playDeliverySound();
                addFloatingText("COOKED 3x " + cafeteriaState.getCurrentRecipe().toUpperCase() + "!", 550, 300, "#00FF88");
            }
        }
    }

    private void updateMedicalCenterTreatment(InputHandler input, double deltaSeconds) {
        if (input.consumeKeyPressed(KeyCode.ESCAPE) || input.consumeKeyPressed(KeyCode.M)) {
            medicalCenterState.startTransition(false); // Leave Medical Center
            SoundManager.playCollectSound();
            return;
        }

        if (!medicalCenterState.isTreating()) {
            if (input.consumeKeyPressed(KeyCode.DIGIT1) || input.consumeKeyPressed(KeyCode.NUMPAD1)) {
                medicalCenterState.startTreatment("Bandages & Antiseptic", 0.3);
                SoundManager.playCollectSound();
            } else if (input.consumeKeyPressed(KeyCode.DIGIT2) || input.consumeKeyPressed(KeyCode.NUMPAD2)) {
                medicalCenterState.startTreatment("IV Drip & Saline Solution", 0.5);
                SoundManager.playCollectSound();
            } else if (input.consumeKeyPressed(KeyCode.DIGIT3) || input.consumeKeyPressed(KeyCode.NUMPAD3)) {
                medicalCenterState.startTreatment("Emergency First Aid Kit", 0.6);
                SoundManager.playCollectSound();
            }
        } else {
            double duration = "Bandages & Antiseptic".equals(medicalCenterState.getCurrentTreatment()) ? 0.3 :
                             "IV Drip & Saline Solution".equals(medicalCenterState.getCurrentTreatment()) ? 0.5 : 0.6;

            if (medicalCenterState.updateTreatment(deltaSeconds, duration)) {
                Survivor injuredTarget = null;
                for (Survivor s : safePlatformSurvivors) {
                    if (s.getType() == SurvivorType.INJURED) {
                        injuredTarget = s;
                        break;
                    }
                }
                if (injuredTarget == null) {
                    for (Survivor s : boatCargoSurvivors) {
                        if (s.getType() == SurvivorType.INJURED) {
                            injuredTarget = s;
                            break;
                        }
                    }
                }
                if (injuredTarget == null) {
                    for (Survivor s : survivors) {
                        if (s.getType() == SurvivorType.INJURED && s.isRescued()) {
                            injuredTarget = s;
                            break;
                        }
                    }
                }

                score += 350;
                if (injuredTarget != null) {
                    injuredTarget.treat();
                    totalDelivered++;
                    injuredTreatedCount++;
                    safePlatformSurvivors.remove(injuredTarget);
                    boatCargoSurvivors.remove(injuredTarget);
                    addFloatingText("HEALED " + injuredTarget.getName().toUpperCase() + "! +350 PTS!", 550, 300, "#00FF88");
                } else {
                    addFloatingText("MEDICAL TREATMENT PREPARED! +150 PTS", 550, 300, "#33CCFF");
                }
                SoundManager.playDeliverySound();
            }
        }
    }

    public boolean canVehicleRescue(Boat boat, Survivor s) {
        if (s == null || s.isRescued() || s.isDead()) return false;

        if (boat.isHelicopter()) {
            // Helicopter can rescue survivors from ANYWHERE (water, green field, forest, building rooftops)
            return true;
        } else if (boat.getVehicleType() == VehicleType.ROVER) {
            // 6x6 Land Buggy can rescue survivors from WATER, GREEN FIELDS, and DENSE FORESTS (not from solid buildings)
            return s.isInWater(buildings) || s.isInField(buildings) || s.isInForest(buildings);
        } else {
            // Flood Rescue Boat can ONLY rescue survivors from WATER (not from forest, field, or buildings)
            return s.isInWater(buildings);
        }
    }

    private void updateAimedSurvivor() {
        double crewWorldX = crew.getWorldX(boat);
        double crewWorldY = crew.getWorldY(boat);
        boolean isHeli = boat.isHelicopter();
        double closestDist = isHeli ? 750 : 600;
        aimedSurvivor = null;

        for (Survivor s : survivors) {
            if (!s.isRescued() && !s.isDead() && canVehicleRescue(boat, s)) {
                double dist = Math.hypot(crewWorldX - s.getX(), crewWorldY - s.getY());
                if (dist < closestDist) {
                    closestDist = dist;
                    aimedSurvivor = s;
                }
            }
        }
    }

    private void handleCrewInteraction() {
        double crewWorldX = crew.getWorldX(boat);
        double crewWorldY = crew.getWorldY(boat);
        double radius = crew.getInteractionRadius();
        boolean isHeli = boat.isHelicopter();

        if (aimedSurvivor != null && !aimedSurvivor.isRescued() && canVehicleRescue(boat, aimedSurvivor)) {
            if (boat.canAddCargo()) {
                activeRopeAnimation.start(crewWorldX, crewWorldY, aimedSurvivor);
                SoundManager.playRopeThrowSound();
                if (isHeli) {
                    addFloatingText("WINCH HOIST LOWERED!", crewWorldX, crewWorldY - 30, "#38bdf8");
                    statusMessage = "Rescue winch cable lowered to airlift survivor!";
                } else {
                    addFloatingText("ROPE THROWN!", crewWorldX, crewWorldY - 30, "#facc15");
                    statusMessage = "Rope lasso thrown to rescue survivor!";
                }
                return;
            } else {
                addFloatingText("CARGO FULL!", crewWorldX, crewWorldY - 30, "#FF4444");
                statusMessage = "Cargo slots full! Deliver to Safe Platform / Helipad!";
                return;
            }
        }

        // Feedback when player tries to rescue near a survivor that requires a different vehicle
        if (!isHeli) {
            for (Survivor s : survivors) {
                if (!s.isRescued() && !s.isDead()) {
                    double dist = Math.hypot(crewWorldX - s.getX(), crewWorldY - s.getY());
                    if (dist < 420) {
                        if (s.isOnSolidBuilding(buildings)) {
                            addFloatingText("🚁 ROOFTOP: HELICOPTER REQUIRED!", crewWorldX, crewWorldY - 40, "#c084fc");
                            statusMessage = "🚁 Stranded on high rooftop! Switch to Helicopter in Workshop [V] to airlift!";
                            SoundManager.playGameOverSound();
                            return;
                        } else if (s.isInField(buildings) && boat.getVehicleType() == VehicleType.BOAT) {
                            addFloatingText("🚜 GREEN FIELD: LAND BUGGY REQUIRED!", crewWorldX, crewWorldY - 40, "#22c55e");
                            statusMessage = "🚜 Stranded on green field! Switch to 6x6 Land Buggy in Workshop [V] to drive on fields!";
                            SoundManager.playGameOverSound();
                            return;
                        } else if (s.isInForest(buildings) && boat.getVehicleType() == VehicleType.BOAT) {
                            addFloatingText("🚜 DENSE FOREST: LAND BUGGY REQUIRED!", crewWorldX, crewWorldY - 40, "#10b981");
                            statusMessage = "🚜 Stranded in dense tree park! Switch to 6x6 Land Buggy in Workshop [V] to enter forest!";
                            SoundManager.playGameOverSound();
                            return;
                        }
                    }
                }
            }
        }

        for (Obstacle o : obstacles) {
            if (!o.isCleared()) {
                double dist = Math.hypot(crewWorldX - o.getX(), crewWorldY - o.getY());
                if (dist <= radius + o.getRadius()) {
                    o.interact();
                    score += 50;
                    SoundManager.playObstacleClearedSound();
                    addFloatingText("AVENUE CLEARED!", o.getX(), o.getY() - 30, "#FFCC00");
                    statusMessage = "Cleared obstacle from flooded campus avenue!";
                    return;
                }
            }
        }

        for (Creature c : creatures) {
            if (c.isActive()) {
                double dist = Math.hypot(crewWorldX - c.getX(), crewWorldY - c.getY());
                if (dist <= radius + c.getRadius() + 30) {
                    if (c.scareOff()) {
                        score += 150;
                        SoundManager.playCreatureScaredSound();
                        addFloatingText("SCARED OFF " + c.getName().toUpperCase() + "!", c.getX(), c.getY() - 30, "#FF00FF");
                        statusMessage = "Fended off campus pond monster!";
                        return;
                    }
                }
            }
        }

        for (UsefulObject u : usefulObjects) {
            if (!u.isCollected()) {
                double dist = Math.hypot(crewWorldX - u.getX(), crewWorldY - u.getY());
                if (dist <= radius + 30) {
                    u.collect();
                    score += u.getPointValue();
                    boat.applySpeedBoost(6.0);
                    cafeteriaState.addSupplies(2);
                    medicalCenterState.addSupplies(2);
                    SoundManager.playCollectSound();
                    addFloatingText("📦 RESTOCKED KITCHEN & MEDICAL KITS (+2 ALL)!", u.getX(), u.getY() - 30, "#FFFF00");
                    statusMessage = "Collected campus supply chest! Restocked all meals & medical kits!";
                    return;
                }
            }
        }
    }

    private void registerRescueCombo() {
        if (rescueComboTimer > 0) {
            rescueCombo++;
        } else {
            rescueCombo = 1;
        }
        rescueComboTimer = COMBO_WINDOW_SECONDS;
    }

    private void addFloatingText(String text, double x, double y, String color) {
        floatingTexts.add(new FloatingText(text, x, y, color));
    }

    // Helper counts for HUD
    public int getRemainingHungryCount() {
        int count = 0;
        for (Survivor s : survivors) {
            if (!s.isDead() && s.getType() == SurvivorType.HUNGRY) count++;
        }
        return count;
    }

    public int getRemainingInjuredCount() {
        int count = 0;
        for (Survivor s : survivors) {
            if (!s.isDead() && s.getType() == SurvivorType.INJURED) count++;
        }
        return count;
    }

    public Survivor getClosestUnrescuedSurvivor() {
        Survivor closest = null;
        double minDst = Double.MAX_VALUE;
        for (Survivor s : survivors) {
            if (!s.isRescued() && !s.isDead()) {
                double dist = Math.hypot(boat.getX() - s.getX(), boat.getY() - s.getY());
                if (dist < minDst) {
                    minDst = dist;
                    closest = s;
                }
            }
        }
        return closest;
    }

    // Getters
    public double getMapWidth() { return mapWidth; }
    public double getMapHeight() { return mapHeight; }
    public Boat getBoat() { return boat; }
    public Crew getCrew() { return crew; }
    public SafeZone getSafeZone() { return safeZone; }
    public List<Building> getBuildings() { return buildings; }
    public List<Survivor> getSurvivors() { return survivors; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public List<Creature> getCreatures() { return creatures; }
    public List<UsefulObject> getUsefulObjects() { return usefulObjects; }
    public List<FloatingText> getFloatingTexts() { return floatingTexts; }
    public List<Survivor> getSafePlatformSurvivors() { return safePlatformSurvivors; }
    public List<Survivor> getBoatCargoSurvivors() { return boatCargoSurvivors; }
    public RopeAnimation getActiveRopeAnimation() { return activeRopeAnimation; }
    public Survivor getAimedSurvivor() { return aimedSurvivor; }
    public CafeteriaState getCafeteriaState() { return cafeteriaState; }
    public MedicalCenterState getMedicalCenterState() { return medicalCenterState; }

    public int getTotalInjuredSpawned() { return totalInjuredSpawned; }
    public int getInjuredTreatedCount() { return injuredTreatedCount; }
    public int getTotalHungrySpawned() { return totalHungrySpawned; }
    public int getHungryFedCount() { return hungryFedCount; }
    public int getNormalRescuedCount() { return normalRescuedCount; }
    public int getTotalPerishedCount() { return totalPerishedCount; }

    public double getCameraZoom() { return cameraZoom; }
    public int getTotalSurvivorsInLevel() { return totalSurvivorsInLevel; }
    public int getTotalDelivered() { return totalDelivered; }
    public int getScore() { return score; }
    public int getEvacuationGoal() { return evacuationGoal; }
    public String getDifficultyLabel() { return difficultyLabel; }
    public int getMaxPerishedBeforeFail() { return maxPerishedBeforeFail; }
    public int getRescueCombo() { return rescueCombo; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public String getStatusMessage() { return statusMessage; }
    public boolean isInsideWorkshop(Boat boat) {
        for (Building b : buildings) {
            if ("WORKSHOP_DEPOT".equals(b.getType()) || "WORKSHOP".equals(b.getType())) {
                double minX = b.getX();
                double maxX = b.getX() + b.getWidth();
                double minY = b.getY();
                double maxY = b.getY() + b.getHeight();
                double wallThick = 20.0;

                return (boat.getX() >= minX + wallThick && boat.getX() <= maxX + 30 &&
                        boat.getY() >= minY + wallThick && boat.getY() <= maxY - wallThick);
            }
        }
        return false;
    }

    public static class FloatingText {
        private String text;
        private double x;
        private double y;
        private String color;
        private double lifetime = 1.6;

        public FloatingText(String text, double x, double y, String color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void update(double dt) {
            lifetime -= dt;
            y -= dt * 25;
        }

        public boolean isDead() { return lifetime <= 0; }
        public String getText() { return text; }
        public double getX() { return x; }
        public double getY() { return y; }
        public String getColor() { return color; }
        public double getOpacity() { return Math.max(0, lifetime / 1.6); }
    }
}
