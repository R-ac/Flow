package com.trashsailors.controller;

import com.trashsailors.model.*;
import com.trashsailors.view.SoundManager;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private final double mapWidth = 6000;
    private final double mapHeight = 4200;

    private Boat boat;
    private Crew crew;
    private SafeZone safeZone;

    private List<Building> buildings = new ArrayList<>();
    private List<Survivor> survivors = new ArrayList<>();
    private List<SupplyBox> supplyBoxes = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<>();
    private List<Creature> creatures = new ArrayList<>();
    private List<UsefulObject> usefulObjects = new ArrayList<>();
    private List<FloatingText> floatingTexts = new ArrayList<>();

    private RopeAnimation activeRopeAnimation = new RopeAnimation();
    private Survivor aimedSurvivor = null;
    private SupplyBox aimedSupplyBox = null;

    // Workshop Material Stockpile & Vehicle Unlock Progression
    private int workshopNails = 0;
    private int workshopMetal = 0;
    private boolean roverUnlocked = false;      // Requires 5 Nails + 5 Metal
    private boolean helicopterUnlocked = false; // Requires 10 Nails + 7 Metal
    private boolean workshopMenuOpen = false;

    private String difficultyLevel = "MEDIUM"; // "EASY" (600s), "MEDIUM" (300s), "HARD" (120s)
    private double timeRemaining = 300.0;
    private double lowTimerThreshold = 60.0;
    private boolean lowTimerActive = false;

    private double cameraZoom = 1.0;
    private int totalSurvivorsInLevel = 0;
    private int totalDelivered = 0;
    private int score = 0;

    private boolean gameOver = false;
    private boolean gameWon = false;
    private String statusMessage = "Navigate flooded IUT avenues! Zoom: Q/E or Scroll";

    // ========== INFINITE RANDOM SURVIVOR GENERATION ==========
    private final Random random = new Random();
    private double survivorSpawnTimer = 0.0;          // counts up to 30 seconds
    private static final double SPAWN_INTERVAL = 30.0; // every 30 seconds
    private int waveNumber = 0;                       // how many waves have spawned

    // ========== SURVIVOR SCREAM / HELP MESSAGES ==========
    private double screamTimer = 0.0;
    private static final double SCREAM_INTERVAL = 2.5; // every ~2.5 seconds a random survivor screams
    private static final String[] SCREAM_MESSAGES = {
        "Oh god!",
        "I'm drowning!",
        "Help!",
        "I'm going to die!",
        "Please save us!",
        "We're not gonna make it!",
        "Please help us!",
        "I can't swim!",
        "Please help me!",
        "Help us!"
    };

    public GameEngine() {
        reset();
    }

    public void setDifficulty(String level) {
        this.difficultyLevel = level;
        reset();
    }

    public void reset() {
        if ("EASY".equalsIgnoreCase(difficultyLevel)) {
            timeRemaining = 600.0; // 10 Minutes
            lowTimerThreshold = 120.0;
        } else if ("HARD".equalsIgnoreCase(difficultyLevel)) {
            timeRemaining = 120.0; // 2 Minutes
            lowTimerThreshold = 35.0;
        } else {
            timeRemaining = 300.0; // 5 Minutes (Medium)
            lowTimerThreshold = 60.0;
        }

        // Build Massive IUT Campus Map with Spacious 600+ Unit Avenue Gaps
        setupIUTCampusMapMassive();

        // Guaranteed Non-Overlapping Boat Spawn Position in open avenue (far from buildings & trees)
        double spawnX = 650;
        double spawnY = 2200;
        boat = new Boat(spawnX, spawnY);
        crew = new Crew();

        // Verify zero building or tree overlap at spawn
        ensureNoBoatOverlap(spawnX, spawnY);

        // Safe Zone Helipad beside Cafeteria / Library North Dock
        safeZone = new SafeZone(3550, 1290);

        survivors.clear();
        obstacles.clear();
        creatures.clear();
        usefulObjects.clear();
        floatingTexts.clear();
        aimedSurvivor = null;

        cameraZoom = 1.0;
        lowTimerActive = false;
        totalDelivered = 0;
        score = 0;
        gameOver = false;
        gameWon = false;

        // Reset infinite spawn system
        survivorSpawnTimer = 0.0;
        waveNumber = 0;
        totalSurvivorsInLevel = 0;
        screamTimer = 0.0;

        // Spawn a rich initial wave across the avenues and forest
        spawnRandomSurvivorWave(12, 18); // 12–18 people at the beginning across the vast avenues

        // Spawn Road Obstacles in Wide Avenues
        obstacles.add(new Obstacle(600, 700, 55, "SUBMERGED_CAR"));
        obstacles.add(new Obstacle(2900, 550, 60, "ROAD_BARRIER"));
        obstacles.add(new Obstacle(3800, 1200, 58, "FALLEN_TREE"));
        obstacles.add(new Obstacle(2100, 1800, 55, "FLOODED_DUMPSTER"));
        obstacles.add(new Obstacle(4200, 2800, 60, "SUBMERGED_CAR"));

        // Spawn Sea Creatures in Campus Ponds & Alleys
        creatures.add(new Creature(1600, 3500, "Central Pond Monster"));
        creatures.add(new Creature(2100, 2700, "Auditorium Alley Croc"));

        // Spawn Useful Supply Chests
        usefulObjects.add(new UsefulObject(1400, 1900, "TURBO_FUEL", 150));
        usefulObjects.add(new UsefulObject(2950, 1850, "SUPPLY_CHEST", 300));
        usefulObjects.add(new UsefulObject(4600, 600, "RECYCLABLE_TRASH", 100));
        usefulObjects.add(new UsefulObject(3800, 3500, "TURBO_FUEL", 150));

        // Spawn Initial Supply Crates (Nails and Sheet Metal for Crafting in open avenues)
        supplyBoxes.clear();
        supplyBoxes.add(new SupplyBox(2100, 2900, SupplyBox.BoxType.NAILS));
        supplyBoxes.add(new SupplyBox(3500, 3100, SupplyBox.BoxType.NAILS));
        supplyBoxes.add(new SupplyBox(2700, 1800, SupplyBox.BoxType.NAILS));
        supplyBoxes.add(new SupplyBox(650, 1600, SupplyBox.BoxType.METAL));
        supplyBoxes.add(new SupplyBox(3300, 1200, SupplyBox.BoxType.METAL));
        supplyBoxes.add(new SupplyBox(4300, 2200, SupplyBox.BoxType.METAL));

        workshopNails = 0;
        workshopMetal = 0;
        roverUnlocked = false;
        helicopterUnlocked = false;
        workshopMenuOpen = false;

        statusMessage = "Level: " + difficultyLevel + " | Collect Nails & Metal to craft Rover & Heli in Workshop [E]!";
    }

    private void ensureNoBoatOverlap(double x, double y) {
        for (Building b : buildings) {
            if (!"FIELD".equals(b.getType()) && !"WORKSHOP_DEPOT".equals(b.getType()) && b.intersects(x, y, boat.getWidth() + 60, boat.getHeight() + 60)) {
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
        buildings.clear(); // important when reset is called multiple times
        String iutRed = "#dc2626";

        // 1. Top-West Zone (Workshops & Academic Building 3)
        buildings.add(new Building(150, 200, 220, 600, "ACADEMIC", "#991b1b", "Medical Centre & Laundry"));
        buildings.add(new Building(950, 200, 950, 200, "ACADEMIC", iutRed, "Academic Building-3"));
        buildings.add(new Building(950, 500, 950, 220, "ACADEMIC", iutRed, "North Academic Wing"));
        buildings.add(new Building(950, 850, 950, 220, "ACADEMIC", iutRed, "Middle Academic Wing"));
        buildings.add(new Building(950, 1200, 950, 220, "ACADEMIC", iutRed, "South Academic Wing"));

        // 2. West-Central Zone (OIC Tree Park & Residential Buildings)
        buildings.add(new Building(950, 1700, 950, 1250, "PARK_TREES", "#15803d", "OIC Tree Park"));
        buildings.add(new Building(400, 2000, 380, 280, "ACADEMIC", "#7f1d1d", "Residential Building 1"));
        buildings.add(new Building(400, 2500, 380, 280, "ACADEMIC", "#7f1d1d", "Residential Building 2"));
        buildings.add(new Building(300, 3200, 750, 220, "HALL", iutRed, "Female Hall of Residence"));
        buildings.add(new Building(400, 3550, 650, 220, "ACADEMIC", iutRed, "Female Common Facilities"));

        // 3. Top-Center & Top-Right Zone (Academic 1 & 2 + Halls of Residence)
        buildings.add(new Building(2250, 240, 700, 240, "ACADEMIC", iutRed, "Academic Building-2"));
        buildings.add(new Building(2250, 850, 700, 220, "ACADEMIC", iutRed, "Academic Building-1"));

        buildings.add(new Building(3200, 240, 480, 180, "HALL", iutRed, "North Hall of Residence"));
        buildings.add(new Building(3800, 240, 480, 180, "HALL", iutRed, "North Hall of Residence"));
        buildings.add(new Building(4400, 240, 480, 180, "HALL", iutRed, "North Hall of Residence"));

        buildings.add(new Building(3200, 850, 480, 180, "HALL", iutRed, "South Hall of Residence"));
        buildings.add(new Building(3800, 850, 480, 180, "HALL", iutRed, "South Hall of Residence"));
        buildings.add(new Building(4400, 850, 480, 180, "HALL", iutRed, "South Hall of Residence"));

        buildings.add(new Building(5050, 200, 250, 1000, "HALL", iutRed, "North Cafeteria & Common"));

        // 4. Central Zone (Administrative, Cafeteria/Library, Auditorium & IUT Mosque)
        buildings.add(new Building(2250, 1450, 600, 300, "ACADEMIC", iutRed, "Administrative Building"));
        buildings.add(new Building(3050, 1450, 600, 480, "ACADEMIC", iutRed, "Cafeteria / Library"));
        buildings.add(new Building(1900, 1950, 650, 500, "ACADEMIC", iutRed, "Auditorium"));
        buildings.add(new Building(2700, 2150, 450, 450, "MOSQUE", iutRed, "IUT Mosque"));

        // 5. East & South Zone (Students Centre & Gymnasium, Sports Field)
        buildings.add(new Building(3900, 1450, 1000, 350, "ACADEMIC", iutRed, "Students Centre & Gymnasium"));
        buildings.add(new Building(3900, 1950, 1100, 1500, "FIELD", "#166534", "IUT Sports Field"));

        // 6. Middle-Lower Open Flood Avenue: Dedicated Emergency Vehicle Workshop & Slipway Depot (Zero tree/building overlap)
        buildings.add(new Building(2400, 3100, 900, 460, "WORKSHOP_DEPOT", "#0284c7", "Emergency Vehicle Workshop & Depot"));
    }

    // =========================================================
    //  INFINITE RANDOM SURVIVOR WAVE GENERATOR
    // =========================================================
    /**
     * Spawns a random number of survivors (between minCount and maxCount inclusive)
     * at completely random valid locations on the map.
     * Continues until the game timer finishes.
     */
    private void spawnRandomSurvivorWave(int minCount, int maxCount) {
        int count = minCount + random.nextInt(maxCount - minCount + 1); // random number
        waveNumber++;

        String[] namePool = {
            "Alex", "Sam", "Morgan", "Taylor", "Jordan", "Riley", "Casey",
            "Avery", "Quinn", "Blake", "Jamie", "Drew", "Cameron", "Reese",
            "Skyler", "Parker", "Hayden", "Finley", "Rowan", "Sage",
            "Student", "Staff", "Professor", "Lab Assistant", "Visitor"
        };

        Building forestBuilding = null;
        for (Building b : buildings) {
            if ("PARK_TREES".equals(b.getType())) {
                forestBuilding = b;
                break;
            }
        }

        List<Building> solidBuildings = new ArrayList<>();
        for (Building b : buildings) {
            if (!"FIELD".equals(b.getType()) && !"WORKSHOP_DEPOT".equals(b.getType()) && !"PARK_TREES".equals(b.getType())) {
                solidBuildings.add(b);
            }
        }

        int spawned = 0;
        int attempts = 0;
        final int maxAttempts = count * 45; // safety limit

        // 1. Rooftop Trapped Survivors (Elevated on building rooftops, Helicopter airlift only)
        int rooftopTarget = Math.min(count / 4 + 1, 3); // 1-3 rooftop survivors per wave
        if (!solidBuildings.isEmpty() && rooftopTarget > 0) {
            int rSpawned = 0;
            int rAttempts = 0;
            while (rSpawned < rooftopTarget && rAttempts < 50) {
                rAttempts++;
                Building b = solidBuildings.get(random.nextInt(solidBuildings.size()));
                if (b.getWidth() < 100 || b.getHeight() < 100) continue;

                double rx = b.getX() + 45 + random.nextDouble() * (b.getWidth() - 90);
                double ry = b.getY() + 45 + random.nextDouble() * (b.getHeight() - 90);

                boolean tooClose = false;
                for (Survivor s : survivors) {
                    if (!s.isRescued() && Math.hypot(rx - s.getX(), ry - s.getY()) < 65) {
                        tooClose = true;
                        break;
                    }
                }
                if (tooClose) continue;

                String name = "Rooftop Student #" + (totalSurvivorsInLevel + spawned + 1);
                survivors.add(new Survivor(rx, ry, name, b, true));
                spawned++;
                rSpawned++;
            }
        }

        // 2. Moderate Forest Portion (1 to 2 students in the forest for 6x6 Rover)
        int forestTarget = (forestBuilding != null) ? Math.max(1, Math.min(2, 1 + random.nextInt(2))) : 0;
        if (forestBuilding != null && forestTarget > 0) {
            int forestAttempts = 0;
            int forestSpawned = 0;
            while (forestSpawned < forestTarget && forestAttempts < 50) {
                forestAttempts++;
                double fx = forestBuilding.getX() + 80 + random.nextDouble() * (forestBuilding.getWidth() - 160);
                double fy = forestBuilding.getY() + 80 + random.nextDouble() * (forestBuilding.getHeight() - 160);

                // Reject if too close to an already existing unrescued survivor
                boolean tooClose = false;
                for (Survivor s : survivors) {
                    if (!s.isRescued() && Math.hypot(fx - s.getX(), fy - s.getY()) < 75) {
                        tooClose = true;
                        break;
                    }
                }
                if (tooClose) continue;

                String name = "Forest Student #" + (totalSurvivorsInLevel + spawned + 1);
                survivors.add(new Survivor(fx, fy, name, forestBuilding, false));
                spawned++;
                forestSpawned++;
            }
        }

        // 3. Vast Majority Outside on Open Flooded Avenues & Campus Map (STRICTLY OUTSIDE ALL BUILDINGS)
        while (spawned < count && attempts < maxAttempts) {
            attempts++;

            // Random position anywhere on the map
            double x = 80 + random.nextDouble() * (mapWidth - 160);
            double y = 80 + random.nextDouble() * (mapHeight - 160);

            // Reject if on or near ANY solid building, workshop depot, or park trees
            boolean insideOrNearBuilding = false;
            for (Building b : buildings) {
                if ("FIELD".equals(b.getType())) {
                    continue; // Open field is fine
                }
                double safetyBuffer = 55.0; // Strong 55px buffer ensuring completely outside
                if (x >= b.getX() - safetyBuffer && x <= b.getX() + b.getWidth() + safetyBuffer &&
                    y >= b.getY() - safetyBuffer && y <= b.getY() + b.getHeight() + safetyBuffer) {
                    insideOrNearBuilding = true;
                    break;
                }
            }
            if (insideOrNearBuilding) continue;

            // Reject if too close to the safe zone / helipad
            if (Math.hypot(x - safeZone.getX(), y - safeZone.getY()) < safeZone.getRadius() + 90) {
                continue;
            }

            // Reject if too close to the boat at the moment of spawn
            if (boat != null && Math.hypot(x - boat.getX(), y - boat.getY()) < 130) {
                continue;
            }

            // Reject if too close to an already existing unrescued survivor
            boolean tooCloseToOther = false;
            for (Survivor s : survivors) {
                if (!s.isRescued() && Math.hypot(x - s.getX(), y - s.getY()) < 75) {
                    tooCloseToOther = true;
                    break;
                }
            }
            if (tooCloseToOther) continue;

            // Valid open water location → create water survivor
            String name = namePool[random.nextInt(namePool.length)] + " #" + (totalSurvivorsInLevel + spawned + 1);
            Survivor newSurvivor = new Survivor(x, y, name, null, false);
            survivors.add(newSurvivor);
            spawned++;
        }

        totalSurvivorsInLevel += spawned;

        // 4. Generate Supply Boxes (Nails and Metals) - strictly in open water away from all buildings
        int nailBoxesTarget = 1 + random.nextInt(2); // 1-2 nail crates
        int metalBoxesTarget = 1 + random.nextInt(2); // 1-2 metal crates
        for (int i = 0; i < nailBoxesTarget; i++) {
            double sx = 150 + random.nextDouble() * (mapWidth - 300);
            double sy = 150 + random.nextDouble() * (mapHeight - 300);
            boolean nearBuilding = false;
            for (Building b : buildings) {
                if (!"FIELD".equals(b.getType())) {
                    double buf = 55.0;
                    if (sx >= b.getX() - buf && sx <= b.getX() + b.getWidth() + buf &&
                        sy >= b.getY() - buf && sy <= b.getY() + b.getHeight() + buf) {
                        nearBuilding = true;
                        break;
                    }
                }
            }
            if (!nearBuilding) supplyBoxes.add(new SupplyBox(sx, sy, SupplyBox.BoxType.NAILS));
        }

        for (int i = 0; i < metalBoxesTarget; i++) {
            double sx = 150 + random.nextDouble() * (mapWidth - 300);
            double sy = 150 + random.nextDouble() * (mapHeight - 300);
            boolean nearBuilding = false;
            for (Building b : buildings) {
                if (!"FIELD".equals(b.getType())) {
                    double buf = 55.0;
                    if (sx >= b.getX() - buf && sx <= b.getX() + b.getWidth() + buf &&
                        sy >= b.getY() - buf && sy <= b.getY() + b.getHeight() + buf) {
                        nearBuilding = true;
                        break;
                    }
                }
            }
            if (!nearBuilding) supplyBoxes.add(new SupplyBox(sx, sy, SupplyBox.BoxType.METAL));
        }

        // Feedback
        if (spawned > 0) {
            addFloatingText("WAVE " + waveNumber + ": +" + spawned + " SURVIVORS + SUPPLIES!", 
                            boat.getX(), boat.getY() - 90, "#facc15");
            statusMessage = "Wave " + waveNumber + " arrived! +" + spawned + " people. Nails/Metal crates spawned!";
            SoundManager.playCollectSound(); // short alert sound
        }
    }

    public void update(InputHandler input, double deltaSeconds) {
        if (gameOver || gameWon) return;

        boolean inWorkshop = isInsideWorkshop(boat);

        // 1. Auto-deposit collected materials into workshop stockpile upon entering
        if (inWorkshop) {
            int unloadedNails = boat.unloadNails();
            int unloadedMetal = boat.unloadMetal();
            if (unloadedNails > 0 || unloadedMetal > 0) {
                workshopNails += unloadedNails;
                workshopMetal += unloadedMetal;
                score += (unloadedNails * 100) + (unloadedMetal * 100);
                SoundManager.playCollectSound();
                addFloatingText("📦 STORED +" + unloadedNails + " NAILS, +" + unloadedMetal + " METAL!", boat.getX(), boat.getY() - 70, "#facc15");
                statusMessage = "Stored materials in Workshop! Nails: " + workshopNails + " | Metal: " + workshopMetal + " [Press E for Crafting Menu]";
            }
        }

        // 2. Workshop Fabrication Menu Toggle (Press [E] when inside Workshop)
        if (inWorkshop && input.consumeEPressed()) {
            workshopMenuOpen = !workshopMenuOpen;
            if (workshopMenuOpen) {
                SoundManager.playCollectSound();
                statusMessage = "🔧 WORKSHOP FABRICATION DEPOT: Press [1, 2, 3] or click to craft/deploy vehicles! [E/ESC] to exit.";
            }
        }

        // 3. Workshop Menu Modal Interaction
        if (workshopMenuOpen) {
            if (input.consumeEscPressed() || (!inWorkshop)) {
                workshopMenuOpen = false;
            } else if (input.consumeEPressed()) {
                workshopMenuOpen = false;
            }

            if (input.consumeNum1Pressed()) {
                handleDeployOrCraftRover();
            } else if (input.consumeNum2Pressed()) {
                handleDeployOrCraftHelicopter();
            } else if (input.consumeNum3Pressed()) {
                boat.setVehicleType(Boat.VehicleType.BOAT);
                addFloatingText("DEPLOYED RESCUE BOAT!", boat.getX(), boat.getY() - 80, "#38bdf8");
                SoundManager.playCollectSound();
                workshopMenuOpen = false;
            }

            if (input.consumeMouseClicked()) {
                handleWorkshopMenuClick(input.getMouseClickX(), input.getMouseClickY());
            }

            // Pause game world while menu is open
            return;
        }

        // ========== INFINITE SURVIVOR SPAWN TIMER ==========
        survivorSpawnTimer += deltaSeconds;
        if (survivorSpawnTimer >= SPAWN_INTERVAL) {
            survivorSpawnTimer = 0.0;
            spawnRandomSurvivorWave(8, 28);
        }

        // ========== SURVIVOR SCREAM / HELP MESSAGES ==========
        screamTimer += deltaSeconds;
        if (screamTimer >= SCREAM_INTERVAL) {
            screamTimer = 0.0;
            makeRandomSurvivorScream();
        }

        // Camera Zoom handling
        double scrollDelta = input.consumeScrollDeltaY();
        if (scrollDelta > 0 || input.isKeyPressed(KeyCode.Q) || input.isKeyPressed(KeyCode.EQUALS)) {
            cameraZoom = Math.min(1.8, cameraZoom + 0.02);
        } else if (scrollDelta < 0 || input.isKeyPressed(KeyCode.MINUS)) {
            cameraZoom = Math.max(0.4, cameraZoom - 0.02);
        }

        // Update timer
        timeRemaining -= deltaSeconds;
        if (timeRemaining <= lowTimerThreshold && !lowTimerActive) {
            lowTimerActive = true;
            addFloatingText("WARNING! Survivors swimming towards boat!", boat.getX(), boat.getY() - 60, "#FF3366");
        }

        if (timeRemaining <= 0) {
            timeRemaining = 0;
            if (totalDelivered >= totalSurvivorsInLevel) {
                gameWon = true;
                SoundManager.playWinSound();
            } else {
                gameOver = true;
                SoundManager.playGameOverSound();
            }
            return;
        }

        // Vehicle Switching (ONLY permitted when inside the Workshop Depot & Slipway!)
        if (input.consumeSwitchVehiclePressed()) {
            if (isInsideWorkshop(boat)) {
                boat.switchNextAvailable(roverUnlocked, helicopterUnlocked);
                String vName = boat.getVehicleType().getDisplayName();
                addFloatingText("DEPLOYED " + vName.toUpperCase() + "!", boat.getX(), boat.getY() - 80, "#38bdf8");
                statusMessage = "Active Vehicle: " + vName + " [Press E for Fabrication Menu]";
                SoundManager.playCollectSound();
            } else {
                addFloatingText("⚠️ GO INSIDE WORKSHOP TO CHANGE VEHICLE!", boat.getX(), boat.getY() - 80, "#ef4444");
                statusMessage = "🔒 Depot Locked! Navigate inside the Emergency Vehicle Workshop (Middle-Lower Map) to change vehicles!";
                SoundManager.playWarningSound();
            }
        }

        // Pilot Controls: WASD (Frictionless Gliding)
        boolean forward = input.isKeyPressed(KeyCode.W);
        boolean backward = input.isKeyPressed(KeyCode.S);
        boolean turnLeft = input.isKeyPressed(KeyCode.A);
        boolean turnRight = input.isKeyPressed(KeyCode.D);
        boat.update(forward, backward, turnLeft, turnRight, mapWidth, mapHeight);

        // Collision Resolution: Helicopter flies above buildings & obstacles, while Boat & Rover collide smoothly
        boolean isHelicopter = (boat.getVehicleType() == Boat.VehicleType.HELICOPTER);
        double bx = boat.getX();
        double by = boat.getY();
        double bw = boat.getWidth();
        double bh = boat.getHeight();

        if (!isHelicopter) {
            for (Building b : buildings) {
                if ("WORKSHOP_DEPOT".equals(b.getType())) {
                    // Dedicated Enterable Emergency Vehicle Workshop & Slipway Depot
                    double minX = b.getX();
                    double maxX = b.getX() + b.getWidth();
                    double minY = b.getY();
                    double maxY = b.getY() + b.getHeight();
                    double wallThick = 24.0;

                    // Check if vehicle is inside or touching the workshop depot area
                    boolean inXRange = (bx + bw / 2.0 >= minX && bx - bw / 2.0 <= maxX);
                    boolean inYRange = (by + bh / 2.0 >= minY && by - bh / 2.0 <= maxY);

                    if (inXRange && inYRange) {
                        // Check whether vehicle center is inside the garage interior vs outside
                        boolean centerInsideX = (bx >= minX + wallThick && bx <= maxX + 20);
                        boolean centerInsideY = (by >= minY + wallThick && by <= maxY - wallThick);

                        if (centerInsideX && centerInsideY) {
                            // Vehicle is INSIDE the workshop depot!
                            if (bx - bw / 2.0 < minX + wallThick) {
                                boat.setX(minX + wallThick + bw / 2.0 + 0.5);
                                if (boat.getVx() < 0) boat.setVx(0);
                            }
                            if (by - bh / 2.0 < minY + wallThick) {
                                boat.setY(minY + wallThick + bh / 2.0 + 0.5);
                                if (boat.getVy() < 0) boat.setVy(0);
                            }
                            if (by + bh / 2.0 > maxY - wallThick) {
                                boat.setY(maxY - wallThick - bh / 2.0 - 0.5);
                                if (boat.getVy() > 0) boat.setVy(0);
                            }

                            // Check if over the vehicle switch turntable platform
                            double platCx = minX + (b.getWidth() * 0.62) * 0.48;
                            double platCy = minY + (b.getHeight() * 0.5);
                            if (Math.hypot(boat.getX() - platCx, boat.getY() - platCy) < 70) {
                                statusMessage = "⚡ VEHICLE SERVICE PLATFORM: Press [E] to Craft or [V] to Cycle!";
                            } else {
                                statusMessage = "🔧 INSIDE WORKSHOP DEPOT! Press [E] to open Vehicle Fabrication Menu!";
                            }
                        } else {
                            // Vehicle is OUTSIDE colliding with exterior walls
                            if (bx < minX + wallThick && (by + bh / 2.0 > minY && by - bh / 2.0 < maxY)) {
                                boat.setX(minX - bw / 2.0 - 0.5);
                                if (boat.getVx() > 0) boat.setVx(0);
                            }
                            if (by < minY + wallThick && (bx + bw / 2.0 > minX && bx - bw / 2.0 < maxX - 30)) {
                                boat.setY(minY - bh / 2.0 - 0.5);
                                if (boat.getVy() > 0) boat.setVy(0);
                            }
                            if (by > maxY - wallThick && (bx + bw / 2.0 > minX && bx - bw / 2.0 < maxX - 30)) {
                                boat.setY(maxY + bh / 2.0 + 0.5);
                                if (boat.getVy() < 0) boat.setVy(0);
                            }
                        }

                        bx = boat.getX();
                        by = boat.getY();
                    }
                    continue;
                }

                // 6x6 Rover (Land Buggy) can drive freely through BOTH green sports fields (FIELD) AND dense forests (PARK_TREES)!
                if (("FIELD".equals(b.getType()) || "PARK_TREES".equals(b.getType())) && boat.getVehicleType() == Boat.VehicleType.ROVER) {
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

                    if ("FIELD".equals(b.getType()) && boat.getVehicleType() == Boat.VehicleType.BOAT) {
                        statusMessage = "⛔ SHALLOW GREEN FIELD! Switch to 6x6 Land Buggy in Workshop to drive on fields!";
                    } else if ("PARK_TREES".equals(b.getType()) && boat.getVehicleType() == Boat.VehicleType.BOAT) {
                        statusMessage = "⛔ DENSE TREE PARK! Switch to 6x6 Land Buggy in Workshop to enter forest!";
                    }
                }
            }
        } else {
            // Helicopter is airborne! Can fly freely over all buildings, trees, and floodwaters
            for (Building b : buildings) {
                if ("WORKSHOP_DEPOT".equals(b.getType())) {
                    if (isInsideWorkshop(boat)) {
                        statusMessage = "⚡ WORKSHOP HELIPAD: Press [E] for Fabrication Menu or [V] to switch vehicles!";
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

        // Direct pickup if vehicle drives over supply crate
        for (SupplyBox box : supplyBoxes) {
            if (!box.isCollected()) {
                box.update(deltaSeconds);
                double dist = Math.hypot(boat.getX() - box.getX(), boat.getY() - box.getY());
                if (dist < 48) {
                    if (boat.canAddCargo()) {
                        box.setCollected(true);
                        Boat.CargoType cType = (box.getType() == SupplyBox.BoxType.NAILS) ? Boat.CargoType.NAILS : Boat.CargoType.METAL;
                        boat.addCargo(cType);
                        score += 50;
                        SoundManager.playCollectSound();
                        addFloatingText("PICKED UP " + box.getType().getDisplayName().toUpperCase() + "!", boat.getX(), boat.getY() - 30, box.getType().getColor());
                        statusMessage = "Picked up " + box.getType().getDisplayName() + "! Deliver to Workshop to craft vehicles!";
                    }
                }
            }
        }

        // Update Aimed Targets for Rope / Winch Rescue
        updateAimedTargets();

        // Update Active Rope / Winch Animation
        if (activeRopeAnimation.isActive()) {
            activeRopeAnimation.update(deltaSeconds);
            if (!activeRopeAnimation.isActive()) {
                if (activeRopeAnimation.getTargetSurvivor() != null) {
                    Survivor target = activeRopeAnimation.getTargetSurvivor();
                    if (!target.isRescued() && boat.canAddCargo()) {
                        target.setRescued(true);
                        boat.addCargo(Boat.CargoType.SURVIVOR);
                        score += 150;
                        SoundManager.playRescueSound();
                        if (target.isRooftopTrapped()) {
                            addFloatingText("AIRLIFT RESCUED " + target.getName() + "!", crew.getWorldX(boat), crew.getWorldY(boat) - 30, "#e879f9");
                            statusMessage = "🚁 Airlifted rooftop student via rescue winch! Deliver to Evacuation Helipad!";
                        } else {
                            addFloatingText("RESCUED " + target.getName() + "!", crew.getWorldX(boat), crew.getWorldY(boat) - 30, "#33CCFF");
                            statusMessage = "Rescued survivor! Deliver to Evacuation Helipad!";
                        }
                    }
                } else if (activeRopeAnimation.getTargetSupplyBox() != null) {
                    SupplyBox targetBox = activeRopeAnimation.getTargetSupplyBox();
                    if (!targetBox.isCollected() && boat.canAddCargo()) {
                        targetBox.setCollected(true);
                        Boat.CargoType cType = (targetBox.getType() == SupplyBox.BoxType.NAILS) ? Boat.CargoType.NAILS : Boat.CargoType.METAL;
                        boat.addCargo(cType);
                        score += 50;
                        SoundManager.playCollectSound();
                        addFloatingText("RECOVERED " + targetBox.getType().getDisplayName().toUpperCase() + "!", crew.getWorldX(boat), crew.getWorldY(boat) - 30, targetBox.getType().getColor());
                        statusMessage = "Recovered " + targetBox.getType().getDisplayName() + "! Deliver to Workshop to craft vehicles!";
                    }
                }
            }
        }

        // Check Creature updates
        for (Creature creature : creatures) {
            creature.update(boat, buildings);
            if (creature.isActive() && creature.getDangerLevel() > 0.8 && !isHelicopter) {
                timeRemaining = Math.max(0, timeRemaining - deltaSeconds * 1.5);
            }
        }

        // Update Survivors (Rooftop Edge AI & Swimming AI)
        for (Survivor s : survivors) {
            s.update(lowTimerActive, boat, buildings);
        }

        // Smooth Push-Out for Road Obstacles (Helicopter flies over obstacles)
        if (!isHelicopter) {
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
        }

        // Crew Space or 'R' Key Rope Interaction
        if (input.consumeSpacePressed() || input.isKeyPressed(KeyCode.R)) {
            handleCrewInteraction();
        }

        // Safe Zone Delivery (Delivers Survivors only)
        if (safeZone.isBoatInside(boat) && boat.countCargoType(Boat.CargoType.SURVIVOR) > 0) {
            int count = boat.unloadSurvivors();
            totalDelivered += count;
            score += count * 250;
            SoundManager.playDeliverySound();
            addFloatingText("DELIVERED +" + count + " SURVIVOR(S)!", boat.getX(), boat.getY() - 70, "#00FF88");

            if (totalDelivered >= totalSurvivorsInLevel) {
                statusMessage = "All current survivors evacuated! Next wave incoming...";
            }
        }

        // Update floating text popups
        Iterator<FloatingText> ftIter = floatingTexts.iterator();
        while (ftIter.hasNext()) {
            FloatingText ft = ftIter.next();
            ft.update(deltaSeconds);
            if (ft.isDead()) {
                ftIter.remove();
            }
        }
    }

    public void handleDeployOrCraftRover() {
        if (roverUnlocked) {
            boat.setVehicleType(Boat.VehicleType.ROVER);
            addFloatingText("DEPLOYED 6x6 ROVER!", boat.getX(), boat.getY() - 80, "#facc15");
            statusMessage = "Active Vehicle: 6x6 Land Rover Buggy";
            SoundManager.playCollectSound();
            workshopMenuOpen = false;
        } else if (workshopNails >= 5 && workshopMetal >= 5) {
            workshopNails -= 5;
            workshopMetal -= 5;
            roverUnlocked = true;
            boat.setVehicleType(Boat.VehicleType.ROVER);
            addFloatingText("🔨 FABRICATED & DEPLOYED 6x6 ROVER!", boat.getX(), boat.getY() - 80, "#00ff88");
            statusMessage = "✅ 6x6 Rover crafted & deployed! Traversal unlocked in OIC Tree Park!";
            SoundManager.playRescueSound();
            workshopMenuOpen = false;
        } else {
            int needNails = Math.max(0, 5 - workshopNails);
            int needMetal = Math.max(0, 5 - workshopMetal);
            addFloatingText("NEED " + needNails + " NAILS & " + needMetal + " METAL!", boat.getX(), boat.getY() - 80, "#ef4444");
            statusMessage = "🔒 Need " + needNails + " Nails and " + needMetal + " Metal to craft Land Rover!";
            SoundManager.playWarningSound();
        }
    }

    public void handleDeployOrCraftHelicopter() {
        if (helicopterUnlocked) {
            boat.setVehicleType(Boat.VehicleType.HELICOPTER);
            addFloatingText("DEPLOYED RESCUE HELICOPTER!", boat.getX(), boat.getY() - 80, "#38bdf8");
            statusMessage = "Active Vehicle: Emergency Rescue Helicopter";
            SoundManager.playCollectSound();
            workshopMenuOpen = false;
        } else if (workshopNails >= 10 && workshopMetal >= 7) {
            workshopNails -= 10;
            workshopMetal -= 7;
            helicopterUnlocked = true;
            boat.setVehicleType(Boat.VehicleType.HELICOPTER);
            addFloatingText("🔨 FABRICATED & DEPLOYED HELICOPTER!", boat.getX(), boat.getY() - 80, "#00ff88");
            statusMessage = "✅ Helicopter crafted & deployed! Rooftop airlift unlocked!";
            SoundManager.playRescueSound();
            workshopMenuOpen = false;
        } else {
            int needNails = Math.max(0, 10 - workshopNails);
            int needMetal = Math.max(0, 7 - workshopMetal);
            addFloatingText("NEED " + needNails + " NAILS & " + needMetal + " METAL!", boat.getX(), boat.getY() - 80, "#ef4444");
            statusMessage = "🔒 Need " + needNails + " Nails and " + needMetal + " Metal to craft Helicopter!";
            SoundManager.playWarningSound();
        }
    }

    public void handleWorkshopMenuClick(double mx, double my) {
        // We will match the UI card bounds drawn in GameRenderer
        // Screen Center typically: w around 1000..1280, h around 700..800
        // Left Card (Rover)
        if (my >= 160 && my <= 560) {
            if (mx >= 100 && mx <= 520) {
                handleDeployOrCraftRover();
            } else if (mx >= 560 && mx <= 980) {
                handleDeployOrCraftHelicopter();
            }
        }
        // Bottom Boat Bar
        if (my >= 570 && my <= 640 && mx >= 300 && mx <= 800) {
            boat.setVehicleType(Boat.VehicleType.BOAT);
            addFloatingText("DEPLOYED RESCUE BOAT!", boat.getX(), boat.getY() - 80, "#38bdf8");
            SoundManager.playCollectSound();
            workshopMenuOpen = false;
        }
        // Close Button
        if (my >= 60 && my <= 120 && mx >= 880 && mx <= 980) {
            workshopMenuOpen = false;
        }
    }

    public boolean canVehicleRescue(Boat boat, Survivor s) {
        if (s == null || s.isRescued()) return false;

        if (boat.getVehicleType() == Boat.VehicleType.HELICOPTER) {
            // Helicopter can rescue survivors from ANYWHERE (water, green field, forest, building rooftops)
            return true;
        } else if (boat.getVehicleType() == Boat.VehicleType.ROVER) {
            // 6x6 Land Buggy can rescue survivors from WATER, GREEN FIELDS, and DENSE FORESTS (not from solid buildings)
            return s.isInWater(buildings) || s.isInField(buildings) || s.isInForest(buildings);
        } else {
            // Flood Rescue Boat can ONLY rescue survivors from WATER (not from forest, field, or buildings)
            return s.isInWater(buildings);
        }
    }

    private void updateAimedTargets() {
        double crewWorldX = crew.getWorldX(boat);
        double crewWorldY = crew.getWorldY(boat);
        boolean isHeli = (boat.getVehicleType() == Boat.VehicleType.HELICOPTER);
        double closestDist = isHeli ? 550 : 450;
        aimedSurvivor = null;
        aimedSupplyBox = null;

        // 1. Check closest survivor that can be rescued by the current vehicle
        for (Survivor s : survivors) {
            if (!s.isRescued() && canVehicleRescue(boat, s)) {
                double dist = Math.hypot(crewWorldX - s.getX(), crewWorldY - s.getY());
                if (dist < closestDist) {
                    closestDist = dist;
                    aimedSurvivor = s;
                    aimedSupplyBox = null;
                }
            }
        }

        // 2. Check closest supply box
        for (SupplyBox box : supplyBoxes) {
            if (!box.isCollected()) {
                double dist = Math.hypot(crewWorldX - box.getX(), crewWorldY - box.getY());
                if (dist < closestDist) {
                    closestDist = dist;
                    aimedSurvivor = null;
                    aimedSupplyBox = box;
                }
            }
        }
    }

    private void handleCrewInteraction() {
        double crewWorldX = crew.getWorldX(boat);
        double crewWorldY = crew.getWorldY(boat);
        double radius = crew.getInteractionRadius();
        boolean isHeli = (boat.getVehicleType() == Boat.VehicleType.HELICOPTER);

        // 1. Try Aim & Deploy Winch / Throw Rope Rescue to Aimed Survivor
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
                statusMessage = "Cargo slots full! Deliver students to SafeZone or materials to Workshop!";
                return;
            }
        }

        // 2. Try Aim & Hook Supply Box
        if (aimedSupplyBox != null && !aimedSupplyBox.isCollected()) {
            if (boat.canAddCargo()) {
                activeRopeAnimation.start(crewWorldX, crewWorldY, aimedSupplyBox);
                SoundManager.playRopeThrowSound();
                addFloatingText("HOOKING SUPPLY CRATE!", crewWorldX, crewWorldY - 30, "#facc15");
                statusMessage = "Hooking " + aimedSupplyBox.getType().getDisplayName() + "!";
                return;
            } else {
                addFloatingText("CARGO FULL!", crewWorldX, crewWorldY - 30, "#FF4444");
                statusMessage = "Cargo slots full! Deliver students to SafeZone or materials to Workshop!";
                return;
            }
        }

        // Check if player in boat/rover is near a survivor requiring another vehicle
        if (!isHeli) {
            for (Survivor s : survivors) {
                if (!s.isRescued()) {
                    double dist = Math.hypot(crewWorldX - s.getX(), crewWorldY - s.getY());
                    if (dist < 380) {
                        if (s.isOnSolidBuilding(buildings)) {
                            addFloatingText("🚁 ROOFTOP: HELICOPTER REQUIRED!", crewWorldX, crewWorldY - 40, "#c084fc");
                            statusMessage = "🚁 Stranded on high rooftop! Craft/Switch to Helicopter in Workshop [E] to airlift!";
                            SoundManager.playGameOverSound();
                            return;
                        } else if (s.isInField(buildings) && boat.getVehicleType() == Boat.VehicleType.BOAT) {
                            addFloatingText("🚜 GREEN FIELD: LAND BUGGY REQUIRED!", crewWorldX, crewWorldY - 40, "#22c55e");
                            statusMessage = "🚜 Stranded on green field! Craft/Switch to 6x6 Land Buggy in Workshop [E] to drive on fields!";
                            SoundManager.playGameOverSound();
                            return;
                        } else if (s.isInForest(buildings) && boat.getVehicleType() == Boat.VehicleType.BOAT) {
                            addFloatingText("🚜 DENSE FOREST: LAND BUGGY REQUIRED!", crewWorldX, crewWorldY - 40, "#10b981");
                            statusMessage = "🚜 Stranded in dense tree park! Craft/Switch to 6x6 Land Buggy in Workshop [E] to enter forest!";
                            SoundManager.playGameOverSound();
                            return;
                        }
                    }
                }
            }
        }

        // 3. Clear Road Obstacle
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

        // 4. Scare Creature
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

        // 5. Collect Useful Item
        for (UsefulObject u : usefulObjects) {
            if (!u.isCollected()) {
                double dist = Math.hypot(crewWorldX - u.getX(), crewWorldY - u.getY());
                if (dist <= radius + 30) {
                    u.collect();
                    score += u.getPointValue();
                    boat.applySpeedBoost(6.0);
                    SoundManager.playCollectSound();
                    addFloatingText("+$" + u.getPointValue() + " & TURBO BOOST!", u.getX(), u.getY() - 30, "#FFFF00");
                    statusMessage = "Collected campus supply item! Turbo boost active!";
                    return;
                }
            }
        }
    }

    /**
     * Picks 1–3 random unrescued survivors and makes them scream for help.
     */
    private void makeRandomSurvivorScream() {
        List<Survivor> unrescued = new ArrayList<>();
        for (Survivor s : survivors) {
            if (!s.isRescued()) {
                unrescued.add(s);
            }
        }
        if (unrescued.isEmpty()) return;

        int screamCount = 1 + random.nextInt(Math.min(3, unrescued.size()));
        for (int i = 0; i < screamCount; i++) {
            Survivor s = unrescued.get(random.nextInt(unrescued.size()));
            String msg = SCREAM_MESSAGES[random.nextInt(SCREAM_MESSAGES.length)];
            addFloatingText(msg, s.getX(), s.getY() - 40, "#ff5555");
        }
    }

    private void addFloatingText(String text, double x, double y, String color) {
        floatingTexts.add(new FloatingText(text, x, y, color));
    }

    // Getters
    public double getMapWidth() { return mapWidth; }
    public double getMapHeight() { return mapHeight; }
    public Boat getBoat() { return boat; }
    public Crew getCrew() { return crew; }
    public SafeZone getSafeZone() { return safeZone; }
    public List<Building> getBuildings() { return buildings; }
    public List<Survivor> getSurvivors() { return survivors; }
    public List<SupplyBox> getSupplyBoxes() { return supplyBoxes; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public List<Creature> getCreatures() { return creatures; }
    public List<UsefulObject> getUsefulObjects() { return usefulObjects; }
    public List<FloatingText> getFloatingTexts() { return floatingTexts; }
    public RopeAnimation getActiveRopeAnimation() { return activeRopeAnimation; }
    public Survivor getAimedSurvivor() { return aimedSurvivor; }
    public SupplyBox getAimedSupplyBox() { return aimedSupplyBox; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public double getCameraZoom() { return cameraZoom; }
    public double getTimeRemaining() { return timeRemaining; }
    public boolean isLowTimerActive() { return lowTimerActive; }
    public int getTotalSurvivorsInLevel() { return totalSurvivorsInLevel; }
    public int getTotalDelivered() { return totalDelivered; }
    public int getScore() { return score; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    public String getStatusMessage() { return statusMessage; }

    public int getWorkshopNails() { return workshopNails; }
    public int getWorkshopMetal() { return workshopMetal; }
    public boolean isRoverUnlocked() { return roverUnlocked; }
    public boolean isHelicopterUnlocked() { return helicopterUnlocked; }
    public boolean isWorkshopMenuOpen() { return workshopMenuOpen; }
    public void setWorkshopMenuOpen(boolean open) { this.workshopMenuOpen = open; }

    public double getNextWaveIn() {
        return Math.max(0, SPAWN_INTERVAL - survivorSpawnTimer);
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public boolean isInsideWorkshop(Boat boat) {
        for (Building b : buildings) {
            if ("WORKSHOP_DEPOT".equals(b.getType())) {
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