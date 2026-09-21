package com.flow.view;

import javafx.scene.image.Image;
import java.io.File;
import java.io.FileInputStream;

public class AssetManager {
    public static Image boatImg;
    public static Image roverImg;
    public static Image helicopterImg;
    public static Image crewImg;
    public static Image pilotImg;
    public static Image debrisImg;
    public static Image monsterImg;
    public static Image survivorImg;
    public static Image timerImg;

    // Building Roof Tiles
    public static Image buildingAcademicImg;
    public static Image buildingResidentialImg;
    public static Image buildingHallsImg;
    public static Image buildingMosqueImg;
    public static Image buildingAdminImg;
    public static Image buildingLibraryImg;
    public static Image buildingFemaleHallImg;
    public static Image buildingMedicalImg;

    // Cafeteria Food & Pot Icons
    public static Image foodCurryImg;
    public static Image foodFishImg;
    public static Image foodTeaImg;
    public static Image cookingPotImg;

    // Medical Center & Survivor Status Assets
    public static Image medKitImg;
    public static Image medIvImg;
    public static Image medBandageImg;
    public static Image badgeHungryImg;
    public static Image badgeInjuredImg;
    public static Image badgeHealthyImg;

    // --- 2D Top-Down Sprite Pack Additions ---
    // Boats
    public static Image boatMainImg;
    public static Image boatMotorImg;
    public static Image boatDamagedImg;
    public static Image boatSurvivorsImg;

    // Captain
    public static Image captainIdleImg;
    public static Image captainSteeringImg;
    public static Image captainRescueImg;

    // Crew
    public static Image crewMedicImg;
    public static Image crewEngineerImg;
    public static Image crewDiverImg;
    public static Image crewRadioImg;

    // Survivors
    public static Image survivorWavingImg;
    public static Image survivorInjuredImg;
    public static Image survivorRooftopImg;
    public static Image survivorSwimmingImg;

    // Monsters
    public static Image monsterAquaticImg;
    public static Image monsterSwampImg;
    public static Image monsterCrocImg;
    public static Image monsterFishImg;
    public static Image monsterTentacleImg;
    public static Image monsterBossImg;

    // Debris
    public static Image debrisCrateImg;
    public static Image debrisBarrelImg;
    public static Image debrisTireImg;
    public static Image debrisCarImg;
    public static Image debrisTreeImg;
    public static Image debrisPlankImg;
    public static Image debrisTrashImg;

    public static void loadAssets() {
        boatImg = loadImage("Boat.png");
        roverImg = loadImage("rover.png");
        helicopterImg = loadImage("helicopter.png");
        crewImg = loadImage("crew.png");
        pilotImg = loadImage("pilot.png");
        debrisImg = loadImage("debris.png");
        monsterImg = loadImage("monster.png");
        survivorImg = loadImage("survivor.png");
        timerImg = loadImage("timer.png");

        // Load Building Tiles
        buildingAcademicImg = loadImage("building_academic.png");
        buildingResidentialImg = loadImage("building_residential.png");
        buildingHallsImg = loadImage("building_halls.png");
        buildingMosqueImg = loadImage("building_mosque.png");
        buildingAdminImg = loadImage("building_admin.png");
        buildingLibraryImg = loadImage("building_library.png");
        buildingFemaleHallImg = loadImage("building_female_hall.png");
        buildingMedicalImg = loadImage("building_medical.png");

        // Load Cafeteria Food Icons
        foodCurryImg = loadImage("food_curry.png");
        foodFishImg = loadImage("food_fish.png");
        foodTeaImg = loadImage("food_tea.png");
        cookingPotImg = loadImage("cooking_pot.png");

        // Load Medical & Status Icons
        medKitImg = loadImage("med_kit.png");
        medIvImg = loadImage("med_iv.png");
        medBandageImg = loadImage("med_bandage.png");
        badgeHungryImg = loadImage("badge_hungry.png");
        badgeInjuredImg = loadImage("badge_injured.png");
        badgeHealthyImg = loadImage("badge_healthy.png");

        // Load Sprite Pack Assets
        boatMainImg = loadImage("boat_main.png");
        boatMotorImg = loadImage("boat_motor.png");
        boatDamagedImg = loadImage("boat_damaged.png");
        boatSurvivorsImg = loadImage("boat_survivors.png");

        captainIdleImg = loadImage("captain_idle.png");
        captainSteeringImg = loadImage("captain_steering.png");
        captainRescueImg = loadImage("captain_rescue.png");

        crewMedicImg = loadImage("crew_medic.png");
        crewEngineerImg = loadImage("crew_engineer.png");
        crewDiverImg = loadImage("crew_diver.png");
        crewRadioImg = loadImage("crew_radio.png");

        survivorWavingImg = loadImage("survivor_waving.png");
        survivorInjuredImg = loadImage("survivor_injured.png");
        survivorRooftopImg = loadImage("survivor_rooftop.png");
        survivorSwimmingImg = loadImage("survivor_swimming.png");

        monsterAquaticImg = loadImage("monster_aquatic.png");
        monsterSwampImg = loadImage("monster_swamp.png");
        monsterCrocImg = loadImage("monster_croc.png");
        monsterFishImg = loadImage("monster_fish.png");
        monsterTentacleImg = loadImage("monster_tentacle.png");
        monsterBossImg = loadImage("monster_boss.png");

        debrisCrateImg = loadImage("debris_crate.png");
        debrisBarrelImg = loadImage("debris_barrel.png");
        debrisTireImg = loadImage("debris_tire.png");
        debrisCarImg = loadImage("debris_car.png");
        debrisTreeImg = loadImage("debris_tree.png");
        debrisPlankImg = loadImage("debris_plank.png");
        debrisTrashImg = loadImage("debris_trash.png");

        System.out.println("[AssetManager] All Character, Vehicle, Building, Food, Medical & Sprite Pack Assets Loaded Successfully!");
    }

    private static Image loadImage(String filename) {
        try {
            var is = AssetManager.class.getResourceAsStream("/Resources/Images/" + filename);
            if (is != null) return new Image(is);

            is = AssetManager.class.getResourceAsStream("/com/flow/view/Resources/Images/" + filename);
            if (is != null) return new Image(is);

            File f1 = new File("src/Resources/Images/" + filename);
            if (f1.exists()) return new Image(new FileInputStream(f1));

            File f2 = new File("bin/Resources/Images/" + filename);
            if (f2.exists()) return new Image(new FileInputStream(f2));

            File f3 = new File("Resources/Images/" + filename);
            if (f3.exists()) return new Image(new FileInputStream(f3));
        } catch (Exception e) {
            System.err.println("[AssetManager] Failed loading " + filename + ": " + e.getMessage());
        }
        return null;
    }
}
