package com.flow.view;

import com.flow.controller.GameEngine;
import com.flow.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameRenderer {
    /**
     * World-space size of one roof tile.  Keeping this fixed prevents wide
     * buildings from stretching a single image into an indistinct texture.
     */
    private static final double BUILDING_TILE_SIZE = 160;
    private static final double PARK_TILE_SIZE = 90;
    private static final double FIELD_TILE_SIZE = 110;
    private static final double VIEW_CULL_MARGIN = 120;

    private final Canvas canvas;
    private final GraphicsContext gc;
    private double animTimer = 0;

    private final List<Particle> particles = new ArrayList<>();
    private final List<RainDrop> rainDrops = new ArrayList<>();
    private boolean rainInitialized = false;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void render(GameEngine engine) {
        animTimer += 0.035;
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();

        CafeteriaState cafeteria = engine.getCafeteriaState();
        MedicalCenterState medCenter = engine.getMedicalCenterState();

        // 1. Render Interior Views if active
        if (medCenter.isInsideMedicalCenter()) {
            drawMedicalCenterInterior(engine, viewWidth, viewHeight);
        } else if (cafeteria.isInsideCafeteria()) {
            drawCafeteriaInterior(engine, viewWidth, viewHeight);
        } else {
            // Render Main Flooded Campus World
            renderMainCampusWorld(engine, viewWidth, viewHeight);
        }

        // 2. Render Screen Transition Fade Overlays
        if (medCenter.isTransitioning()) {
            gc.setFill(Color.web("#020617", medCenter.getTransitionAlpha()));
            gc.fillRect(0, 0, viewWidth, viewHeight);
        } else if (cafeteria.isTransitioning()) {
            gc.setFill(Color.web("#020617", cafeteria.getTransitionAlpha()));
            gc.fillRect(0, 0, viewWidth, viewHeight);
        }
    }

    private void renderMainCampusWorld(GameEngine engine, double viewWidth, double viewHeight) {
        Boat boat = engine.getBoat();
        Crew crew = engine.getCrew();
        double zoom = engine.getCameraZoom();

        double effectiveW = viewWidth / zoom;
        double effectiveH = viewHeight / zoom;

        double camX = boat.getX() - (effectiveW / 2);
        double camY = boat.getY() - (effectiveH / 2);
        camX = Math.max(0, Math.min(engine.getMapWidth() - effectiveW, camX));
        camY = Math.max(0, Math.min(engine.getMapHeight() - effectiveH, camY));

        updateParticles(engine);

        gc.clearRect(0, 0, viewWidth, viewHeight);

        gc.save();
        gc.scale(zoom, zoom);

        drawFloodedIUTCampusGround(engine, camX, camY, effectiveW, effectiveH);

        gc.save();
        gc.translate(-camX, -camY);

        for (Building building : engine.getBuildings()) {
            if (isWorldRectVisible(building.getX(), building.getY(), building.getWidth(), building.getHeight(),
                    camX, camY, effectiveW, effectiveH)) {
                drawIUTBuilding(building, boat);
            }
        }

        drawSafePlatform(engine);
        drawIUTEvacuationSafeZone(engine.getSafeZone());
        drawParticles();

        for (UsefulObject obj : engine.getUsefulObjects()) {
            if (!obj.isCollected()) {
                drawUsefulObject(obj);
            }
        }

        for (Obstacle obs : engine.getObstacles()) {
            if (!obs.isCleared()) {
                drawObstacle(obs);
            }
        }

        for (Creature creature : engine.getCreatures()) {
            drawCreature(creature);
        }

        for (Survivor survivor : engine.getSurvivors()) {
            if (!survivor.isRescued()) {
                drawSurvivor(survivor, engine.getBuildings());
            }
        }

        drawAimLine(crew, boat, engine.getAimedSurvivor());
        drawActiveRope(engine.getActiveRopeAnimation());
        drawSalvageRaft(boat);
        drawCrew(crew, boat);
        drawFloatingTexts(engine.getFloatingTexts());

        gc.restore(); // Restore world translation
        gc.restore(); // Restore camera zoom scaling

        drawRainOverlay(viewWidth, viewHeight);
        drawHUD(engine, viewWidth, viewHeight);
        drawMinimap(engine, viewWidth, viewHeight);

        // Draw Multi-Layered Atmospheric Background overlay for Menu state ONLY
        if (engine.isMenuOpen()) {
            drawMenuAtmosphericBackground(viewWidth, viewHeight);
        }
    }

    private void drawMenuAtmosphericBackground(double w, double h) {
        // 1. Dark Navy Deep Water Radial Vignette
        RadialGradient grad = new RadialGradient(0, 0, w / 2, h / 2, Math.max(w, h) * 0.7, false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0b1d2e", 0.45)),
                new Stop(0.65, Color.web("#06111f", 0.75)),
                new Stop(1.0, Color.web("#020617", 0.90)));
        gc.setFill(grad);
        gc.fillRect(0, 0, w, h);

        // 2. Submerged IUT Campus Silhouettes (Midground Depth)
        gc.setFill(Color.web("#06111f", 0.5));
        gc.fillRect(40, h - 220, 180, 160); // Academic Building 1
        gc.fillRect(240, h - 180, 220, 120); // Workshop
        gc.fillOval(520, h - 260, 120, 120); // Mosque Dome Silhouette
        gc.fillRect(500, h - 200, 160, 140); // Mosque Body

        // 3. Emergency Strobe Light Flashes in background fog
        double strobeRed = (Math.sin(animTimer * 5.0) + 1.0) / 2.0;
        double strobeBlue = (Math.cos(animTimer * 5.0) + 1.0) / 2.0;
        gc.setFill(Color.web("#d83a3a", strobeRed * 0.20));
        gc.fillOval(550, h - 270, 60, 60);
        gc.setFill(Color.web("#087eaf", strobeBlue * 0.20));
        gc.fillOval(120, h - 230, 60, 60);

        // 4. Subtle Floating Debris & Water Surface Waves
        gc.setStroke(Color.web("#18a9d8", 0.15));
        gc.setLineWidth(1.5);
        for (int i = 0; i < 6; i++) {
            double rx = (i * 190 + animTimer * 15) % (w + 200) - 100;
            double ry = h - 140 + Math.sin(animTimer * 2 + i) * 10;
            gc.strokeArc(rx, ry, 60, 20, 0, 180, ArcType.OPEN);
        }
    }

    /** Skips world objects that cannot contribute pixels to the current frame. */
    private boolean isWorldRectVisible(double x, double y, double width, double height,
                                       double camX, double camY, double viewWidth, double viewHeight) {
        return x + width >= camX - VIEW_CULL_MARGIN &&
                x <= camX + viewWidth + VIEW_CULL_MARGIN &&
                y + height >= camY - VIEW_CULL_MARGIN &&
                y <= camY + viewHeight + VIEW_CULL_MARGIN;
    }

    private void drawSafePlatform(GameEngine engine) {
        double px = 3190;
        double py = 1220;
        double pw = 250;
        double ph = 140;

        // Platform base water shadow
        gc.setFill(Color.web("#020617", 0.5));
        gc.fillRect(px + 8, py + 8, pw, ph);

        // Wooden Deck Platform Floor
        gc.setFill(Color.web("#78350f"));
        gc.fillRoundRect(px, py, pw, ph, 16, 16);
        gc.setStroke(Color.web("#facc15", 0.8));
        gc.setLineWidth(3);
        gc.strokeRoundRect(px, py, pw, ph, 16, 16);

        // Deck Planks
        gc.setStroke(Color.web("#451a03", 0.6));
        gc.setLineWidth(2);
        for (double x = px + 20; x < px + pw; x += 20) {
            gc.strokeLine(x, py + 4, x, py + ph - 4);
        }

        // Yellow-Black Hazard Stripes Border
        gc.setFill(Color.web("#facc15"));
        gc.fillRect(px + 10, py + 4, pw - 20, 10);
        gc.setFill(Color.web("#1e293b"));
        for (double sx = px + 10; sx < px + pw - 20; sx += 20) {
            gc.fillRect(sx, py + 4, 10, 10);
        }

        // Label
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText("📦 SAFE PLATFORM (STAGING & FEEDING)", px + 8, py - 10);

        // Draw Survivors Staged on Safe Platform
        List<Survivor> staged = engine.getSafePlatformSurvivors();
        for (int i = 0; i < staged.size(); i++) {
            Survivor s = staged.get(i);
            double sx = px + 35 + (i % 4) * 50;
            double sy = py + 35 + (i / 4) * 55;
            s.setX(sx);
            s.setY(sy);
            drawSurvivor(s, engine.getBuildings());
        }
    }

    private void drawMedicalCenterInterior(GameEngine engine, double w, double h) {
        MedicalCenterState med = engine.getMedicalCenterState();

        // Medical Tile Floor Base
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, w, h);

        double tileSize = 60;
        for (double x = 0; x < w; x += tileSize) {
            for (double y = 0; y < h; y += tileSize) {
                if (((int)(x/tileSize) + (int)(y/tileSize)) % 2 == 0) {
                    gc.setFill(Color.web("#1e293b"));
                    gc.fillRect(x, y, tileSize, tileSize);
                }
            }
        }

        // Hospital Back Wall
        gc.setFill(Color.web("#0284c7"));
        gc.fillRect(0, 0, w, 110);
        gc.setFill(Color.web("#0369a1"));
        gc.fillRect(0, 100, w, 10);

        // Header Title
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText("🏥 IUT MEDICAL CENTER - EMERGENCY WARD", 40, 50);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#bae6fd"));
        gc.fillText("Press [1], [2], or [3] to administer medical care | Press [M] or [ESC] to Exit", 40, 80);

        // Hospital Beds & Patient Monitors
        double bedY = 220;
        for (int i = 0; i < 3; i++) {
            double bedX = 80 + i * 280;

            // Bed frame & mattress
            gc.setFill(Color.web("#cbd5e1"));
            gc.fillRoundRect(bedX, bedY, 220, 140, 14, 14);
            gc.setFill(Color.web("#38bdf8"));
            gc.fillRect(bedX + 10, bedY + 10, 200, 120);

            // Pillow
            gc.setFill(Color.web("#ffffff"));
            gc.fillRoundRect(bedX + 20, bedY + 20, 50, 40, 8, 8);

            // Red Cross Banner on bed
            gc.setFill(Color.web("#ef4444"));
            gc.fillRect(bedX + 110, bedY + 60, 12, 32);
            gc.fillRect(bedX + 100, bedY + 70, 32, 12);

            // Heart Rate ECG Monitor Display
            gc.setFill(Color.web("#090d16"));
            gc.fillRect(bedX + 140, bedY - 70, 70, 50);
            gc.setStroke(Color.web("#38bdf8"));
            gc.setLineWidth(2);
            gc.strokeRect(bedX + 140, bedY - 70, 70, 50);

            // Animated Pulsing Heartbeat Line
            gc.setStroke(Color.web("#22c55e"));
            gc.setLineWidth(2);
            double ecgOffset = (animTimer * 100 + i * 30) % 60;
            gc.strokeLine(bedX + 145, bedY - 45, bedX + 165, bedY - 45);
            gc.strokeLine(bedX + 165, bedY - 45, bedX + 172, bedY - 60);
            gc.strokeLine(bedX + 172, bedY - 60, bedX + 178, bedY - 30);
            gc.strokeLine(bedX + 178, bedY - 30, bedX + 185, bedY - 45);
            gc.strokeLine(bedX + 185, bedY - 45, bedX + 205, bedY - 45);
        }

        // Treatment Station Panel at Bottom
        gc.setFill(Color.web("#090d16", 0.95));
        gc.fillRoundRect(40, h - 230, w - 80, 200, 20, 20);
        gc.setStroke(Color.web("#38bdf8"));
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(40, h - 230, w - 80, 200, 20, 20);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        gc.setFill(Color.web("#38bdf8"));
        gc.fillText("🩺 MEDICAL TREATMENT STATION", 70, h - 195);

        // Healthcare Options Card 1
        drawTreatmentCard(gc, 70, h - 175, 260, 120, "1", "Bandages & Antiseptic", "Fast wound care (1.5s)", AssetManager.medBandageImg);
        // Healthcare Options Card 2
        drawTreatmentCard(gc, 360, h - 175, 260, 120, "2", "IV Drip & Saline", "Restores fluids (2.5s)", AssetManager.medIvImg);
        // Healthcare Options Card 3
        drawTreatmentCard(gc, 650, h - 175, 260, 120, "3", "Emergency First Aid", "Full trauma care (4.0s)", AssetManager.medKitImg);

        // Active Treatment Animation & Progress Bar
        if (med.isTreating()) {
            gc.setFill(Color.web("#000000", 0.7));
            gc.fillRoundRect(w / 2 - 250, h / 2 - 80, 500, 120, 16, 16);
            gc.setStroke(Color.web("#22c55e"));
            gc.setLineWidth(3);
            gc.strokeRoundRect(w / 2 - 250, h / 2 - 80, 500, 120, 16, 16);

            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            gc.setFill(Color.web("#22c55e"));
            gc.fillText("💉 ADMINISTERING " + med.getCurrentTreatment().toUpperCase() + "...", w / 2 - 210, h / 2 - 40);

            double progress = med.getTreatmentProgress();
            gc.setFill(Color.web("#1e293b"));
            gc.fillRect(w / 2 - 210, h / 2 - 20, 420, 26);

            gc.setFill(Color.web("#22c55e"));
            gc.fillRect(w / 2 - 210, h / 2 - 20, 420 * progress, 26);

            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText((int)(progress * 100) + "%", w / 2 - 15, h / 2);
        }
    }

    private void drawTreatmentCard(GraphicsContext gc, double x, double y, double w, double h, String key, String name, String desc, Image icon) {
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(x, y, w, h, 12, 12);
        gc.setStroke(Color.web("#38bdf8", 0.5));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, w, h, 12, 12);

        gc.setFill(Color.web("#0284c7"));
        gc.fillOval(x + 15, y + 15, 32, 32);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText(key, x + 25, y + 38);

        if (icon != null) {
            gc.drawImage(icon, x + 180, y + 20, 60, 60);
        }

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText(name, x + 60, y + 36);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#94a3b8"));
        gc.fillText(desc, x + 60, y + 58);
    }

    private void drawCafeteriaInterior(GameEngine engine, double w, double h) {
        CafeteriaState caf = engine.getCafeteriaState();

        // Checkered Kitchen Floor Base
        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(0, 0, w, h);

        double tileSize = 60;
        for (double x = 0; x < w; x += tileSize) {
            for (double y = 0; y < h; y += tileSize) {
                if (((int)(x/tileSize) + (int)(y/tileSize)) % 2 == 0) {
                    gc.setFill(Color.web("#334155"));
                    gc.fillRect(x, y, tileSize, tileSize);
                }
            }
        }

        // Kitchen Back Wall & Stainless Steel Counter
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, w, 110);

        // Header Title
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText("🍳 IUT CAFETERIA - KITCHEN INTERIOR", 40, 50);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#94a3b8"));
        gc.fillText("Press [1], [2], or [3] to select recipe | Press [C] or [ESC] to Exit Kitchen", 40, 80);

        // Stoves & Cooking Pots
        double stoveY = 220;
        for (int i = 0; i < 3; i++) {
            double stoveX = 80 + i * 280;

            gc.setFill(Color.web("#475569"));
            gc.fillRoundRect(stoveX, stoveY, 220, 140, 14, 14);

            gc.setFill(Color.web("#0f172a"));
            gc.fillOval(stoveX + 25, stoveY + 20, 80, 80);
            gc.fillOval(stoveX + 115, stoveY + 20, 80, 80);

            if (caf.isCooking()) {
                gc.setFill(Color.web("#ef4444"));
                gc.fillOval(stoveX + 45, stoveY + 40, 40, 40);
                gc.setFill(Color.web("#f59e0b"));
                gc.fillOval(stoveX + 50, stoveY + 45, 30, 30);
            }

            if (AssetManager.cookingPotImg != null) {
                gc.drawImage(AssetManager.cookingPotImg, stoveX + 35, stoveY + 10, 150, 110);
            }
        }

        // Recipe Menu Panel at Bottom
        gc.setFill(Color.web("#090d16", 0.95));
        gc.fillRoundRect(40, h - 230, w - 80, 200, 20, 20);
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(40, h - 230, w - 80, 200, 20, 20);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("📋 SELECT SURVIVOR MEAL RECIPE", 70, h - 195);

        drawRecipeCard(gc, 70, h - 175, 260, 120, "1", "Warm Rice & Curry", "Prepares in 2s (+200 pts)", AssetManager.foodCurryImg);
        drawRecipeCard(gc, 360, h - 175, 260, 120, "2", "Grilled Fish Stew", "Prepares in 3.5s (+350 pts)", AssetManager.foodFishImg);
        drawRecipeCard(gc, 650, h - 175, 260, 120, "3", "Hot Tea & Snacks", "Prepares in 1s (+100 pts)", AssetManager.foodTeaImg);

        // Active Cooking Progress Bar
        if (caf.isCooking()) {
            gc.setFill(Color.web("#000000", 0.7));
            gc.fillRoundRect(w / 2 - 250, h / 2 - 80, 500, 120, 16, 16);
            gc.setStroke(Color.web("#00ff88"));
            gc.setLineWidth(3);
            gc.strokeRoundRect(w / 2 - 250, h / 2 - 80, 500, 120, 16, 16);

            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            gc.setFill(Color.web("#00ff88"));
            gc.fillText("🔥 COOKING " + caf.getCurrentRecipe().toUpperCase() + "...", w / 2 - 210, h / 2 - 40);

            double progress = caf.getCookingProgress();
            gc.setFill(Color.web("#1e293b"));
            gc.fillRect(w / 2 - 210, h / 2 - 20, 420, 26);

            gc.setFill(Color.web("#00ff88"));
            gc.fillRect(w / 2 - 210, h / 2 - 20, 420 * progress, 26);

            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText((int)(progress * 100) + "%", w / 2 - 15, h / 2);
        }
    }

    private void drawRecipeCard(GraphicsContext gc, double x, double y, double w, double h, String key, String name, String desc, Image icon) {
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(x, y, w, h, 12, 12);
        gc.setStroke(Color.web("#facc15", 0.5));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, w, h, 12, 12);

        gc.setFill(Color.web("#eab308"));
        gc.fillOval(x + 15, y + 15, 32, 32);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText(key, x + 25, y + 38);

        if (icon != null) {
            gc.drawImage(icon, x + 180, y + 20, 60, 60);
        }

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText(name, x + 60, y + 36);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#94a3b8"));
        gc.fillText(desc, x + 60, y + 58);
    }

    private void drawSurvivor(Survivor s, List<Building> buildings) {
        double sy = s.getY() + s.getBobbingOffset();
        boolean inWater = s.isInWater(buildings);

        // Contact shadow / water ripple
        gc.setFill(Color.web("#020617", inWater ? 0.25 : 0.40));
        gc.fillOval(s.getX() - 16, sy - 4, 32, 10);

        if (s.isCriticallyLow()) {
            gc.setStroke(Color.web("#facc15", 0.8 + Math.sin(animTimer * 5) * 0.2));
            gc.setLineWidth(2.5);
            gc.strokeOval(s.getX() - 26, sy - 56, 52, 60);
        }

        drawSurvivorSprite(s, s.getX(), sy, inWater);

        // Draw Survivor Status Badge & Overhead Craving/Injury Icon
        if (s.getType() == SurvivorType.HUNGRY) {
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            gc.setFill(Color.web("#facc15"));
            gc.fillText(s.getCraving().getIcon(), s.getX() + 12, sy - 66);
        } else if (s.getType() == SurvivorType.INJURED) {
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            gc.setFill(Color.web("#ef4444"));
            gc.fillText(s.getInjuryType().getIcon(), s.getX() + 12, sy - 66);
        }

        if (s.hasFever()) {
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            gc.setFill(Color.web("#ff0055"));
            gc.fillText("🌡️", s.getX() - 30, sy - 66);
        }

        // Draw Health Bar for HUNGRY or INJURED survivors
        if (s.getType() != SurvivorType.NORMAL) {
            double barW = 44;
            double barH = 6;
            double bx = s.getX() - barW / 2;
            double by = sy - 60;

            gc.setFill(Color.web("#090d16", 0.9));
            gc.fillRect(bx, by, barW, barH);

            double healthRatio = s.getHealth() / s.getMaxHealth();
            Color barColor = s.hasFever() ? Color.web("#ff0055") :
                             s.getType() == SurvivorType.HUNGRY ? Color.web("#f97316") : Color.web("#ef4444");
            gc.setFill(barColor);
            gc.fillRect(bx, by, barW * healthRatio, barH);

            gc.setStroke(Color.web("#ffffff", 0.6));
            gc.setLineWidth(1);
            gc.strokeRect(bx, by, barW, barH);
        }

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#ffffff"));
        double nameY = s.getType() != SurvivorType.NORMAL ? (sy - 72) : (sy - 62);
        gc.fillText(s.getName(), s.getX() - 24, nameY);
    }

    private void drawSurvivorSprite(Survivor s, double x, double y, boolean inWater) {
        if (inWater) {
            // Stranded in Deep Floodwater: Struggling Water Survivor Sprite
            Image waterSprite = AssetManager.survivorSwimmingImg;
            if (waterSprite != null) {
                gc.drawImage(waterSprite, x - 28, y - 48, 56, 56);
                return;
            }
        }

        // Building Rooftop / Safe Platform Survivor (Unchanged standing student)
        SurvivorType type = s.getType();
        Image sprite = type == SurvivorType.INJURED ? AssetManager.survivorInjuredImg :
                       type == SurvivorType.HUNGRY ? AssetManager.survivorWavingImg : AssetManager.survivorRooftopImg;

        if (sprite == null) {
            sprite = AssetManager.survivorImg;
        }

        if (sprite != null) {
            gc.drawImage(sprite, x - 18, y - 54, 36, 56);
            return;
        }

        Color vestColor = type == SurvivorType.HUNGRY ? Color.web("#f97316") :
                type == SurvivorType.INJURED ? Color.web("#ef4444") : Color.web("#22c55e");

        // Procedural Detailed Animated Fallback
        // Legs & Jeans
        gc.setFill(Color.web("#1e3a5f"));
        gc.fillRect(x - 7, y - 22, 5, 20);
        gc.fillRect(x + 2, y - 22, 5, 20);

        // Shoes
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(x - 8, y - 4, 7, 4);
        gc.fillRect(x + 1, y - 4, 7, 4);

        // Torso & Safety Vest
        gc.setFill(vestColor);
        gc.fillRoundRect(x - 11, y - 42, 22, 22, 6, 6);
        gc.setStroke(Color.web("#0f172a", 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x - 11, y - 42, 22, 22, 6, 6);

        // Head & Face
        gc.setFill(Color.web("#f4c7a1"));
        gc.fillOval(x - 9, y - 54, 18, 16);

        // Hair
        gc.setFill(Color.web("#451a03"));
        gc.fillOval(x - 9.5, y - 56, 19, 10);

        // Animated Waving Arms
        gc.setStroke(Color.web("#f4c7a1"));
        gc.setLineWidth(3.0);
        double waveOffset = Math.sin(animTimer * 8) * 5;
        gc.strokeLine(x - 9, y - 36, x - 16, y - 46 + waveOffset);
        gc.strokeLine(x + 9, y - 36, x + 16, y - 46 - waveOffset);
    }

    private void drawFloodedIUTCampusGround(GameEngine engine, double camX, double camY, double viewW, double viewH) {
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, viewW, viewH);

        gc.setFill(Color.web("#0284c7", 0.35));
        gc.fillRect(0, 0, viewW, viewH);

        gc.setStroke(Color.web("#38bdf8", 0.15));
        gc.setLineWidth(1.5);

        double waveSpacing = 160; // Optimized spacing to reduce draw calls by 75%
        double startX = Math.floor(camX / waveSpacing) * waveSpacing;
        double startY = Math.floor(camY / waveSpacing) * waveSpacing;

        for (double x = startX; x < camX + viewW + waveSpacing; x += waveSpacing) {
            for (double y = startY; y < camY + viewH + waveSpacing; y += waveSpacing) {
                double screenX = x - camX;
                double screenY = y - camY;

                double offset = Math.sin((x * 0.01) + (y * 0.01) + (animTimer * 2)) * 8;
                gc.strokeArc(screenX, screenY + offset, 45, 22, 0, 180, ArcType.OPEN);
            }
        }
    }

    private void drawIUTBuilding(Building b, Boat boat) {
        gc.save();

        if ("WORKSHOP_DEPOT".equals(b.getType())) {
            drawWorkshopDepotAndSlipway(b, boat);
        } else if ("FIELD".equals(b.getType())) {
            drawSportsFieldTileMap(b);
        } else if ("PARK_TREES".equals(b.getType())) {
            drawTreeParkTileMap(b);
        } else {
            Image tileImg = getBuildingTileImage(b);

            if (tileImg != null) {
                drawTiledBuildingTexture(b, tileImg);
            } else {
                gc.setFill(Color.web(b.getColorHex()));
                gc.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
                gc.setStroke(Color.web("#7f1d1d"));
                gc.setLineWidth(3);
                gc.strokeRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            }
        }

        if (!"WORKSHOP_DEPOT".equals(b.getType())) {
            drawBuildingLabel(b);
        }
        gc.restore();
    }

    private void drawWorkshopDepotAndSlipway(Building b, Boat boat) {
        double bx = b.getX();
        double by = b.getY();
        double bw = b.getWidth();
        double bh = b.getHeight();

        // 1. Concrete Ground Foundation & Wet Dock Pier Extension
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(bx, by, bw, bh, 16, 16);

        // Dry Workshop Bay (Left 62% of building)
        double dryW = bw * 0.62;
        gc.setFill(Color.web("#334155"));
        gc.fillRect(bx + 8, by + 8, dryW - 12, bh - 16);

        // Industrial Floor Grid Mesh
        gc.setStroke(Color.web("#475569", 0.4));
        gc.setLineWidth(1);
        for (double gx = bx + 24; gx < bx + dryW - 10; gx += 30) {
            gc.strokeLine(gx, by + 10, gx, by + bh - 10);
        }
        for (double gy = by + 24; gy < by + bh - 10; gy += 30) {
            gc.strokeLine(bx + 10, gy, bx + dryW - 10, gy);
        }

        // Wet Dock Slipway (Right 38% of building)
        double slipX = bx + dryW;
        double slipW = bw - dryW - 8;
        gc.setFill(Color.web("#0e7490", 0.7)); // Submerged Slipway Ramp
        gc.fillRect(slipX, by + 8, slipW, bh - 16);

        // Slipway Dock Wooden Pilings & Planks
        gc.setFill(Color.web("#78350f"));
        gc.fillRect(slipX, by + 12, slipW, 16);
        gc.fillRect(slipX, by + bh - 28, slipW, 16);
        gc.setStroke(Color.web("#451a03"));
        gc.setLineWidth(1.5);
        gc.strokeRect(slipX, by + 12, slipW, 16);
        gc.strokeRect(slipX, by + bh - 28, slipW, 16);

        // Mooring Cleats on Dock
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillOval(slipX + 15, by + 17, 8, 6);
        gc.fillOval(slipX + slipW - 25, by + 17, 8, 6);
        gc.fillOval(slipX + 15, by + bh - 23, 8, 6);
        gc.fillOval(slipX + slipW - 25, by + bh - 23, 8, 6);

        // Safety Warning Hazard Stripes (Yellow & Black) at Garage Threshold
        for (double hy = by + 10; hy < by + bh - 15; hy += 16) {
            gc.setFill(Color.web("#facc15"));
            gc.fillRect(slipX - 6, hy, 6, 8);
            gc.setFill(Color.web("#0f172a"));
            gc.fillRect(slipX - 6, hy + 8, 6, 8);
        }

        // 2. Circular Vehicle Selection Turntable Platform
        double platCx = bx + dryW * 0.48;
        double platCy = by + bh * 0.5;
        double platRadius = 54;

        // Pulsing Neon Selector Glow Ring
        RadialGradient platGlow = new RadialGradient(
                0, 0, platCx, platCy, platRadius + 15, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#00ff88", 0.45)),
                new Stop(0.8, Color.web("#0284c7", 0.2)),
                new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(platGlow);
        gc.fillOval(platCx - platRadius - 15, platCy - platRadius - 15, (platRadius + 15) * 2, (platRadius + 15) * 2);

        // Rotating Turntable Base
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(platCx - platRadius, platCy - platRadius, platRadius * 2, platRadius * 2);

        gc.setStroke(Color.web("#00ff88", 0.8 + Math.sin(animTimer * 4) * 0.2));
        gc.setLineWidth(3);
        gc.setLineDashes(10);
        gc.strokeOval(platCx - platRadius + 4, platCy - platRadius + 4, (platRadius - 4) * 2, (platRadius - 4) * 2);
        gc.setLineDashes(null);

        // Hydraulic Platform Center Pad
        gc.setFill(Color.web("#1e293b"));
        gc.fillOval(platCx - 30, platCy - 30, 60, 60);
        gc.setStroke(Color.web("#38bdf8"));
        gc.setLineWidth(2);
        gc.strokeOval(platCx - 30, platCy - 30, 60, 60);

        // Staged Alternate Vehicle Preview Icon on Platform
        boolean isRover = (boat.getVehicleType() == VehicleType.ROVER);
        boolean isHeli = (boat.getVehicleType() == VehicleType.HELICOPTER);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        gc.setFill(Color.web("#facc15"));
        String nextIcon = isRover ? "🚁" : (isHeli ? "🚤" : "🚗");
        gc.fillText(nextIcon, platCx - 12, platCy + 8);

        // 3. Workshop Equipment & Tools
        // Hydraulic Vehicle Lift Ramp
        gc.setFill(Color.web("#ca8a04"));
        gc.fillRect(bx + 20, by + 25, 65, 14);
        gc.fillRect(bx + 20, by + 50, 65, 14);
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(1.5);
        gc.strokeRect(bx + 20, by + 25, 65, 14);
        gc.strokeRect(bx + 20, by + 50, 65, 14);

        // Tool Chest Cabinets & Workbench
        gc.setFill(Color.web("#dc2626"));
        gc.fillRect(bx + 20, by + bh - 55, 45, 35);
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillRect(bx + 22, by + bh - 50, 41, 6);
        gc.fillRect(bx + 22, by + bh - 40, 41, 6);

        // Work Table with Tools
        gc.setFill(Color.web("#92400e"));
        gc.fillRect(bx + 85, by + bh - 50, 50, 28);
        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(1.5);
        gc.strokeLine(bx + 92, by + bh - 40, bx + 105, by + bh - 40);

        // Overhead Engine Hoist Crane
        gc.setStroke(Color.web("#eab308"));
        gc.setLineWidth(4);
        gc.strokeLine(bx + 20, by + 105, bx + 120, by + 105);
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(bx + 65, by + 100, 10, 10);
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(2);
        gc.strokeLine(bx + 70, by + 110, bx + 70, by + 125);

        // Stack of Spare 6x6 Off-Road Tires
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(bx + dryW - 45, by + 20, 20, 20);
        gc.fillOval(bx + dryW - 35, by + 30, 20, 20);
        gc.fillOval(bx + dryW - 40, by + 45, 20, 20);
        gc.setFill(Color.web("#64748b"));
        gc.fillOval(bx + dryW - 39, by + 26, 8, 8);
        gc.fillOval(bx + dryW - 29, by + 36, 8, 8);
        gc.fillOval(bx + dryW - 34, by + 51, 8, 8);

        // Emergency Orange Fuel Drums
        gc.setFill(Color.web("#ea580c"));
        gc.fillOval(bx + dryW - 45, by + bh - 55, 16, 16);
        gc.fillOval(bx + dryW - 28, by + bh - 50, 16, 16);
        gc.fillOval(bx + dryW - 40, by + bh - 36, 16, 16);
        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(bx + dryW - 40, by + bh - 50, 6, 6);
        gc.fillOval(bx + dryW - 23, by + bh - 45, 6, 6);
        gc.fillOval(bx + dryW - 35, by + bh - 31, 6, 6);

        // Outer Structural Walls (North, West, South) - East side is WIDE OPEN SLIPWAY ENTRANCE
        gc.setStroke(Color.web("#0284c7"));
        gc.setLineWidth(5);
        gc.strokeLine(bx + 4, by + 4, bx + bw - 4, by + 4);
        gc.strokeLine(bx + 4, by + 4, bx + 4, by + bh - 4);
        gc.strokeLine(bx + 4, by + bh - 4, bx + bw - 4, by + bh - 4);

        // Open East Dock Entrance Guide Pylons
        gc.setFill(Color.web("#facc15"));
        gc.fillOval(bx + bw - 14, by + 2, 12, 12);
        gc.fillOval(bx + bw - 14, by + bh - 14, 12, 12);

        // Flashing Blue & Amber Workshop Corner Beacons
        boolean strobe = ((int)(animTimer * 8) % 2 == 0);
        gc.setFill(strobe ? Color.web("#38bdf8") : Color.web("#0284c7"));
        gc.fillOval(bx + 6, by + 6, 12, 12);
        gc.setFill(!strobe ? Color.web("#facc15") : Color.web("#ea580c"));
        gc.fillOval(bx + 6, by + bh - 18, 12, 12);

        // Workshop Title Badge
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        String title = "🏭 EMERGENCY VEHICLE DEPOT [ENTER & PRESS V TO TOGGLE ROVER/BOAT]";
        double textW = title.length() * 7.5;
        gc.setFill(Color.web("#0f172a", 0.92));
        gc.fillRoundRect(bx + bw / 2 - textW / 2 - 10, by - 16, textW + 20, 24, 8, 8);
        gc.setStroke(Color.web("#38bdf8"));
        gc.setLineWidth(1.8);
        gc.strokeRoundRect(bx + bw / 2 - textW / 2 - 10, by - 16, textW + 20, 24, 8, 8);

        gc.setFill(Color.web("#38bdf8"));
        gc.fillText(title, bx + bw / 2 - textW / 2, by + 1);
    }

    /** A clipped, repeatable forest-floor tile layer for the OIC Tree Park. */
    private void drawTreeParkTileMap(Building park) {
        gc.setFill(Color.web("#052e16", 0.55));
        gc.fillRect(park.getX() + 6, park.getY() + 8, park.getWidth(), park.getHeight());

        gc.save();
        gc.beginPath();
        gc.rect(park.getX(), park.getY(), park.getWidth(), park.getHeight());
        gc.closePath();
        gc.clip();

        int column = 0;
        for (double x = park.getX(); x < park.getX() + park.getWidth(); x += PARK_TILE_SIZE, column++) {
            int row = 0;
            for (double y = park.getY(); y < park.getY() + park.getHeight(); y += PARK_TILE_SIZE, row++) {
                int variant = Math.floorMod(column * 19 + row * 37, 6);
                gc.setFill(Color.web(variant == 0 ? "#365314" : variant == 1 ? "#3f6212" : "#2f5b1a"));
                gc.fillRect(x, y, PARK_TILE_SIZE, PARK_TILE_SIZE);

                // Flooded footpath tiles break up the forest and show a navigable route.
                if (variant == 0 || (column % 5 == 2 && row % 3 != 0)) {
                    gc.setFill(Color.web("#155e75", 0.56));
                    gc.fillRoundRect(x + 8, y + 34, PARK_TILE_SIZE - 16, 22, 8, 8);
                    gc.setStroke(Color.web("#7dd3fc", 0.28));
                    gc.setLineWidth(1);
                    gc.strokeLine(x + 15, y + 45, x + PARK_TILE_SIZE - 15, y + 45);
                }

                if (variant != 0 || row % 2 == 0) {
                    double canopyX = x + 19 + (variant % 2) * 10;
                    double canopyY = y + 13 + ((column + row) % 3) * 7;
                    gc.setFill(Color.web("#14532d", 0.65));
                    gc.fillOval(canopyX + 5, canopyY + 9, 52, 48);
                    gc.setFill(Color.web("#22c55e"));
                    gc.fillOval(canopyX, canopyY, 52, 48);
                    gc.setFill(Color.web("#4ade80", 0.5));
                    gc.fillOval(canopyX + 11, canopyY + 7, 25, 18);
                }
            }
        }
        gc.restore();

        gc.setStroke(Color.web("#86efac", 0.75));
        gc.setLineWidth(3);
        gc.strokeRoundRect(park.getX(), park.getY(), park.getWidth(), park.getHeight(), 12, 12);
    }

    /** A striped grass tile layer with football pitch markings for the central field. */
    private void drawSportsFieldTileMap(Building field) {
        gc.setFill(Color.web("#052e16", 0.45));
        gc.fillRect(field.getX() + 7, field.getY() + 8, field.getWidth(), field.getHeight());

        gc.save();
        gc.beginPath();
        gc.rect(field.getX(), field.getY(), field.getWidth(), field.getHeight());
        gc.closePath();
        gc.clip();

        int row = 0;
        for (double y = field.getY(); y < field.getY() + field.getHeight(); y += FIELD_TILE_SIZE, row++) {
            Color grass = row % 2 == 0 ? Color.web("#15803d") : Color.web("#166534");
            gc.setFill(grass);
            gc.fillRect(field.getX(), y, field.getWidth(), FIELD_TILE_SIZE);
            gc.setStroke(Color.web("#86efac", 0.12));
            gc.setLineWidth(1);
            for (double x = field.getX(); x < field.getX() + field.getWidth(); x += FIELD_TILE_SIZE) {
                gc.strokeRect(x, y, FIELD_TILE_SIZE, FIELD_TILE_SIZE);
            }
        }

        double x = field.getX();
        double y = field.getY();
        double w = field.getWidth();
        double h = field.getHeight();
        gc.setStroke(Color.web("#f8fafc", 0.85));
        gc.setLineWidth(4);
        gc.strokeRect(x + 42, y + 42, w - 84, h - 84);
        gc.strokeLine(x + 42, y + h / 2, x + w - 42, y + h / 2);
        gc.strokeOval(x + w / 2 - 92, y + h / 2 - 92, 184, 184);
        gc.setFill(Color.web("#f8fafc", 0.85));
        gc.fillOval(x + w / 2 - 5, y + h / 2 - 5, 10, 10);
        gc.strokeRect(x + w / 2 - 210, y + 42, 420, 150);
        gc.strokeRect(x + w / 2 - 210, y + h - 192, 420, 150);
        gc.strokeRect(x + w / 2 - 100, y + 42, 200, 55);
        gc.strokeRect(x + w / 2 - 100, y + h - 97, 200, 55);
        gc.restore();

        gc.setStroke(Color.web("#86efac", 0.82));
        gc.setLineWidth(3);
        gc.strokeRoundRect(field.getX(), field.getY(), field.getWidth(), field.getHeight(), 12, 12);
    }

    /** Draws a repeated, clipped roof texture across one building footprint. */
    private void drawTiledBuildingTexture(Building building, Image tileImage) {
        gc.setFill(Color.web("#020617", 0.45));
        gc.fillRoundRect(building.getX() + 7, building.getY() + 7,
                building.getWidth(), building.getHeight(), 10, 10);

        gc.setFill(Color.web(building.getColorHex()));
        gc.fillRoundRect(building.getX(), building.getY(), building.getWidth(), building.getHeight(), 10, 10);

        double bw = building.getWidth();
        double bh = building.getHeight();
        gc.save();
        gc.beginPath();
        gc.rect(building.getX(), building.getY(), bw, bh);
        gc.closePath();
        gc.clip();

        double stepX = Math.max(180, bw / 3.0);
        double stepY = Math.max(180, bh / 3.0);
        for (double x = building.getX(); x < building.getX() + bw; x += stepX) {
            for (double y = building.getY(); y < building.getY() + bh; y += stepY) {
                gc.drawImage(tileImage, x, y, stepX, stepY);
            }
        }
        gc.restore();

        gc.setStroke(Color.web("#f8fafc", 0.75));
        gc.setLineWidth(2);
        gc.strokeRoundRect(building.getX(), building.getY(), building.getWidth(), building.getHeight(), 10, 10);
    }

    /** Chooses the reusable roof tile for every campus building category. */
    private Image getBuildingTileImage(Building building) {
        String name = building.getNameLabel();

        if ("MOSQUE".equals(building.getType())) return AssetManager.buildingMosqueImg;
        if ("Medical Centre & Laundry".equalsIgnoreCase(name)) return AssetManager.buildingMedicalImg;
        if (name.startsWith("Residential Building")) return AssetManager.buildingResidentialImg;
        if (name.startsWith("Female Hall") || name.startsWith("Female Common")) return AssetManager.buildingFemaleHallImg;
        if ("Administrative Building".equalsIgnoreCase(name)) return AssetManager.buildingAdminImg;
        if ("Cafeteria / Library".equalsIgnoreCase(name)) return AssetManager.buildingLibraryImg;
        if ("HALL".equals(building.getType())) return AssetManager.buildingHallsImg;
        if ("WORKSHOP".equals(building.getType()) || "WORKSHOP_DEPOT".equals(building.getType()) || (name != null && name.toLowerCase().contains("workshop"))) {
            return AssetManager.buildingWorkshopImg != null ? AssetManager.buildingWorkshopImg : AssetManager.buildingAcademicImg;
        }

        // Academic blocks share the campus roof-tile set.
        return AssetManager.buildingAcademicImg;
    }

    private void drawBuildingLabel(Building b) {
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        double centerX = b.getX() + (b.getWidth() / 2.0);
        double centerY = b.getY() + (b.getHeight() / 2.0);

        double textWidth = b.getNameLabel().length() * 8.5;
        double textHeight = 24;

        gc.setFill(Color.web("#ffffff", 0.95));
        gc.fillRoundRect(centerX - (textWidth / 2) - 10, centerY - (textHeight / 2), textWidth + 20, textHeight, 10, 10);
        gc.setStroke(Color.web("#dc2626"));
        gc.setLineWidth(1.8);
        gc.strokeRoundRect(centerX - (textWidth / 2) - 10, centerY - (textHeight / 2), textWidth + 20, textHeight, 10, 10);

        gc.setFill(Color.web("#dc2626"));
        gc.fillText(b.getNameLabel(), centerX - (textWidth / 2), centerY + 4);
    }

    private void drawIUTEvacuationSafeZone(SafeZone sz) {
        RadialGradient stationGlow = new RadialGradient(
                0, 0, sz.getX(), sz.getY(), sz.getRadius(), false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#00ff88", 0.55)),
                new Stop(0.6, Color.web("#059669", 0.25)),
                new Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(stationGlow);
        gc.fillOval(sz.getX() - sz.getRadius(), sz.getY() - sz.getRadius(), sz.getRadius() * 2, sz.getRadius() * 2);

        gc.setStroke(Color.web("#00ff88", 0.8 + Math.sin(animTimer * 4) * 0.2));
        gc.setLineWidth(4);
        gc.setLineDashes(12);
        gc.strokeOval(sz.getX() - sz.getRadius(), sz.getY() - sz.getRadius(), sz.getRadius() * 2, sz.getRadius() * 2);
        gc.setLineDashes(null);

        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(sz.getX() - 50, sz.getY() - 50, 100, 100);
        gc.setStroke(Color.web("#00ff88"));
        gc.setLineWidth(3.5);
        gc.strokeOval(sz.getX() - 45, sz.getY() - 45, 90, 90);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 46));
        gc.setFill(Color.web("#00ff88"));
        gc.fillText("H", sz.getX() - 16, sz.getY() + 16);

        double beamAngle = animTimer * 70;
        gc.setFill(Color.web("#fef08a", 0.35));
        gc.fillArc(sz.getX() - 200, sz.getY() - 200, 400, 400, beamAngle, 45, ArcType.ROUND);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        gc.setFill(Color.web("#00ff88"));
        gc.fillText("🚁 IUT EVACUATION HELIPAD", sz.getX() - 110, sz.getY() + 85);
    }

    private void drawAimLine(Crew crew, Boat boat, Survivor target) {
        if (target == null || target.isRescued()) return;

        double crewX = crew.getWorldX(boat);
        double crewY = crew.getWorldY(boat);

        gc.setStroke(Color.web("#facc15", 0.8 + Math.sin(animTimer * 6) * 0.2));
        gc.setLineWidth(3);
        gc.setLineDashes(8);
        gc.strokeLine(crewX, crewY, target.getX(), target.getY());
        gc.setLineDashes(null);

        gc.setStroke(Color.web("#facc15", 0.9));
        gc.setLineWidth(2.5);
        gc.strokeOval(target.getX() - 26, target.getY() - 26, 52, 52);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("🎯 THROW ROPE [SPACE/R]", target.getX() - 65, target.getY() - 32);
    }

    private void drawActiveRope(RopeAnimation rope) {
        if (!rope.isActive()) return;

        double currentX = rope.getStartX() + (rope.getTargetX() - rope.getStartX()) * rope.getProgress();
        double currentY = rope.getStartY() + (rope.getTargetY() - rope.getStartY()) * rope.getProgress();

        gc.setStroke(Color.web("#ca8a04"));
        gc.setLineWidth(4);
        gc.strokeLine(rope.getStartX(), rope.getStartY(), currentX, currentY);

        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(3);
        gc.strokeOval(currentX - 12, currentY - 12, 24, 24);
    }

    private void drawSalvageRaft(Boat boat) {
        gc.save();
        gc.translate(boat.getX(), boat.getY());
        gc.rotate(boat.getAngle());

        double bw = boat.getWidth();
        double bh = boat.getHeight();

        if (boat.isBoosted()) {
            gc.setStroke(Color.web("#facc15", 0.95));
            gc.setLineWidth(6);
            gc.strokeRoundRect(-bw / 2 - 10, -bh / 2 - 10, bw + 20, bh + 20, 20, 20);
        }

        if (boat.isHelicopter()) {
            drawHelicopterVisuals(boat, bw, bh);
        } else {
            Image vehicleImg = boat.getVehicleType() == VehicleType.ROVER ? AssetManager.roverImg : AssetManager.boatImg;

            if (vehicleImg != null) {
                gc.drawImage(vehicleImg, -bw / 2, -bh / 2, bw, bh);
            } else {
                drawRescueBoatSprite(bw, bh);
            }
        }

        drawCaptainSprite(-bw / 4, 0);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("P1", -bw / 4 + 2, -18);

        double startSlotX = 5;
        double slotSpacing = 16;
        for (int i = 0; i < boat.getMaxCargoCapacity(); i++) {
            double slotX = startSlotX + (i * slotSpacing);
            double slotY = -14;

            gc.setStroke(Color.web("#ffffff", 0.5));
            gc.setLineWidth(1.5);
            gc.strokeRect(slotX, slotY, 13, 28);

            if (i < boat.getCargoCount()) {
                gc.setFill(Color.web("#f4c7a1"));
                gc.fillOval(slotX + 3, slotY + 3, 7, 7);
                gc.setFill(Color.web("#22c55e"));
                gc.fillRoundRect(slotX + 2.5, slotY + 10, 8, 13, 3, 3);
            }
        }

        gc.restore();
    }

    private void drawHelicopterVisuals(Boat boat, double bw, double bh) {
        // 1. Altitude Ground Shadow (offset down-right for 3D flight perspective)
        gc.setFill(Color.web("#020617", 0.35));
        gc.fillOval(-bw / 2 + 10, -bh / 2 + 25, bw + 10, bh - 5);

        // 2. Helicopter Body Image (Clean Fuselage without baked wings/circles)
        Image heliImg = AssetManager.helicopterImg;
        if (heliImg != null) {
            gc.drawImage(heliImg, -bw / 2, -bh / 2, bw, bh);
        } else {
            gc.setFill(Color.web("#365314"));
            gc.fillRoundRect(-bw / 2, -bh / 2, bw, bh, 15, 15);
        }

        // 3. Dynamic Animated Spinning 4-Blade Main Rotor (Procedural Real-Time Rotation)
        double hubX = -bw * 0.05;
        double hubY = 0;
        double bladeRadius = bw * 0.55;
        double rotorAngleDeg = (animTimer * 150 * 60) % 360;

        for (int i = 0; i < 4; i++) {
            double angleRad = Math.toRadians(rotorAngleDeg + (i * 90));
            double cos = Math.cos(angleRad);
            double sin = Math.sin(angleRad);

            // Blade Root Horn
            gc.setStroke(Color.web("#475569"));
            gc.setLineWidth(3.5);
            gc.strokeLine(hubX, hubY, hubX + cos * 12, hubY + sin * 12);

            // Carbon-Fiber Rotor Blade Body
            gc.setStroke(Color.web("#0f172a"));
            gc.setLineWidth(3.0);
            gc.strokeLine(hubX + cos * 12, hubY + sin * 12, hubX + cos * (bladeRadius - 10), hubY + sin * (bladeRadius - 10));

            // High-Visibility Safety Yellow Blade Tip
            gc.setStroke(Color.web("#facc15"));
            gc.setLineWidth(3.5);
            gc.strokeLine(hubX + cos * (bladeRadius - 10), hubY + sin * (bladeRadius - 10), hubX + cos * bladeRadius, hubY + sin * bladeRadius);
        }

        // Central Rotor Mast Hub
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(hubX - 7, hubY - 7, 14, 14);
        gc.setFill(Color.web("#64748b"));
        gc.fillOval(hubX - 4, hubY - 4, 8, 8);
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillOval(hubX - 2, hubY - 2, 4, 4);

        // 4. Dynamic Animated Spinning 2-Blade Tail Rotor
        double trX = -bw / 2 - 2;
        double trY = -12;
        double trRadius = 16;
        double trAngle = Math.toRadians(animTimer * 240 * 60);
        double trCos = Math.cos(trAngle);
        double trSin = Math.sin(trAngle);

        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(2.5);
        gc.strokeLine(trX - trCos * trRadius, trY - trSin * trRadius, trX + trCos * trRadius, trY + trSin * trRadius);

        // Safety Yellow Tips on Tail Rotor
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(3);
        gc.strokeLine(trX + trCos * (trRadius - 4), trY + trSin * (trRadius - 4), trX + trCos * trRadius, trY + trSin * trRadius);
        gc.strokeLine(trX - trCos * (trRadius - 4), trY - trSin * (trRadius - 4), trX - trCos * trRadius, trY - trSin * trRadius);

        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(trX - 3, trY - 3, 6, 6);

        // 5. Aerial Winch Cable Down to Ground
        gc.setStroke(Color.web("#facc15", 0.8));
        gc.setLineWidth(2.5);
        gc.strokeLine(0, 0, 15, 30);
        gc.setFill(Color.web("#f97316"));
        gc.fillOval(10, 26, 10, 10);
    }

    /**
     * A crisp, top-down rescue craft.  It is drawn at world resolution so it
     * remains sharp at every camera zoom and does not depend on an external
     * image file.
     */
    private void drawRescueBoatSprite(double bw, double bh) {
        // Water shadow makes the boat feel anchored in the flooded world.
        gc.setFill(Color.web("#020617", 0.45));
        gc.fillOval(-bw / 2 - 7, -bh / 2 + 12, bw + 20, bh - 3);

        // Deep navy outer hull, pointed toward the boat heading (right).
        double[] hullX = {-bw / 2, -bw / 2 + 10, bw / 2 - 11, bw / 2 + 8, bw / 2 - 11, -bw / 2 + 10};
        double[] hullY = {-bh / 2 + 9, -bh / 2, -bh / 2 + 5, 0, bh / 2 - 5, bh / 2};
        gc.setFill(Color.web("#0b1e3a"));
        gc.fillPolygon(hullX, hullY, hullX.length);
        gc.setStroke(Color.web("#e2e8f0", 0.9));
        gc.setLineWidth(2.5);
        gc.strokePolygon(hullX, hullY, hullX.length);

        // Bright orange rescue collar around the deck.
        double[] collarX = {-bw / 2 + 8, -bw / 2 + 16, bw / 2 - 15, bw / 2 - 1, bw / 2 - 15, -bw / 2 + 16};
        double[] collarY = {-bh / 2 + 12, -bh / 2 + 7, -bh / 2 + 10, 0, bh / 2 - 10, bh / 2 - 7};
        gc.setFill(Color.web("#f97316"));
        gc.fillPolygon(collarX, collarY, collarX.length);

        // Light deck panel
        gc.setFill(Color.web("#eaf4ff"));
        gc.fillRoundRect(-bw / 2 + 18, -bh / 2 + 12, bw - 42, bh - 24, 12, 12);

        // Tinted wheelhouse and glass windshield.
        gc.setFill(Color.web("#164e7a"));
        gc.fillRoundRect(-30, -18, 34, 36, 8, 8);
        gc.setFill(Color.web("#7dd3fc"));
        gc.fillRoundRect(-25, -14, 20, 12, 4, 4);
        gc.setFill(Color.web("#38bdf8", 0.75));
        gc.fillRoundRect(-25, 3, 20, 10, 4, 4);

        // Rescue beacon and compact bow rail.
        gc.setFill(Color.web("#facc15"));
        gc.fillOval(-18, -25, 10, 7);
        gc.setStroke(Color.web("#f8fafc"));
        gc.setLineWidth(2);
        gc.strokeLine(bw / 2 - 21, -15, bw / 2 - 8, -8);
        gc.strokeLine(bw / 2 - 21, 15, bw / 2 - 8, 8);
        gc.strokeLine(bw / 2 - 21, -15, bw / 2 - 21, 15);

        // Stern utility hatch plate
        gc.setFill(Color.web("#475569"));
        gc.fillRoundRect(-bw / 2 + 25, -7, 18, 14, 3, 3);
        gc.setStroke(Color.web("#64748b"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(-bw / 2 + 25, -7, 18, 14, 3, 3);
    }

    private void drawCaptainSprite(double x, double y) {
        gc.setFill(Color.web("#0f172a", 0.55));
        gc.fillOval(x - 13, y + 10, 26, 10);
        gc.setFill(Color.web("#fbbf24"));
        gc.fillRoundRect(x - 10, y - 4, 20, 22, 7, 7);
        gc.setFill(Color.web("#f4c7a1"));
        gc.fillOval(x - 8, y - 17, 16, 17);
        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(x - 10, y - 20, 20, 7, 4, 4);
        gc.setFill(Color.web("#38bdf8"));
        gc.fillRect(x - 12, y + 4, 24, 4);
    }

    private void drawCrew(Crew crew, Boat boat) {
        double worldX = crew.getWorldX(boat);
        double worldY = crew.getWorldY(boat);

        gc.setStroke(Color.web("#ff0055", 0.5));
        gc.setLineWidth(2);
        gc.setLineDashes(5);
        gc.strokeOval(worldX - crew.getInteractionRadius(), worldY - crew.getInteractionRadius(), crew.getInteractionRadius() * 2, crew.getInteractionRadius() * 2);
        gc.setLineDashes(null);

        drawCrewSprite(worldX, worldY);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#ff0055"));
        gc.fillText("P2 (CREW)", worldX - 26, worldY - 24);
    }

    private void drawCrewSprite(double x, double y) {
        gc.setFill(Color.web("#020617", 0.38));
        gc.fillOval(x - 17, y + 11, 34, 12);
        gc.setFill(Color.web("#e11d48"));
        gc.fillRoundRect(x - 12, y - 5, 24, 25, 8, 8);
        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(x - 10, y + 4, 20, 4);
        gc.setFill(Color.web("#f4c7a1"));
        gc.fillOval(x - 9, y - 18, 18, 18);
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(x - 11, y - 21, 22, 8, 5, 5);
        gc.setStroke(Color.web("#f8fafc", 0.8));
        gc.setLineWidth(2);
        gc.strokeLine(x + 10, y, x + 19, y + 8);
    }

    private void drawObstacle(Obstacle obs) {
        gc.save();
        drawDebrisSprite(obs);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillText("🚧 " + obs.getType().replace("_", " "), obs.getX() - 40, obs.getY() + obs.getRadius() + 16);
        gc.restore();
    }

    private void drawDebrisSprite(Obstacle obstacle) {
        Image sprite = obstacle.getType().contains("TREE") ? AssetManager.debrisTreeImg :
                       obstacle.getType().contains("CAR") ? AssetManager.debrisCarImg :
                       obstacle.getType().contains("BARRIER") ? AssetManager.debrisBarrelImg : AssetManager.debrisCrateImg;

        if (sprite != null) {
            double r = obstacle.getRadius();
            gc.drawImage(sprite, obstacle.getX() - r, obstacle.getY() - r, r * 2, r * 2);
            return;
        }

        double x = obstacle.getX();
        double y = obstacle.getY();
        double r = obstacle.getRadius();
        gc.setFill(Color.web("#020617", 0.38));
        gc.fillOval(x - r, y + r * 0.35, r * 2, r * 0.75);

        if (obstacle.getType().contains("TREE")) {
            gc.setStroke(Color.web("#78350f"));
            gc.setLineWidth(13);
            gc.strokeLine(x - r * .7, y + r * .45, x + r * .75, y - r * .5);
            gc.setFill(Color.web("#166534"));
            gc.fillOval(x - r * .3, y - r * .7, r, r * .85);
            gc.fillOval(x + r * .15, y - r * .35, r, r * .8);
        } else if (obstacle.getType().contains("CAR")) {
            gc.setFill(Color.web("#475569"));
            gc.fillRoundRect(x - r * .85, y - r * .45, r * 1.7, r * .95, 11, 11);
            gc.setFill(Color.web("#93c5fd"));
            gc.fillRoundRect(x - r * .35, y - r * .35, r * .7, r * .35, 5, 5);
            gc.setFill(Color.web("#111827"));
            gc.fillOval(x - r * .62, y + r * .2, r * .34, r * .34);
            gc.fillOval(x + r * .28, y + r * .2, r * .34, r * .34);
        } else {
            gc.setFill(Color.web("#f59e0b"));
            gc.fillRoundRect(x - r * .9, y - r * .42, r * 1.8, r * .84, 8, 8);
            gc.setStroke(Color.web("#111827"));
            gc.setLineWidth(7);
            gc.strokeLine(x - r * .6, y - r * .35, x - r * .05, y + r * .35);
            gc.strokeLine(x + r * .05, y - r * .35, x + r * .6, y + r * .35);
        }
    }

    private void drawCreature(Creature c) {
        if (c.getRetreatTimer() > 0) return;

        gc.save();
        drawMonsterSprite(c);

        if (c.getDangerLevel() > 0) {
            gc.setStroke(Color.web("#ff0055", c.getDangerLevel()));
            gc.setLineWidth(3.5);
            gc.strokeOval(c.getX() - 65, c.getY() - 65, 130, 130);
        }

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#ff0055"));
        gc.fillText("🐊 " + c.getName(), c.getX() - 45, c.getY() - c.getRadius() - 14);
        gc.restore();
    }

    private void drawMonsterSprite(Creature creature) {
        Image sprite = "Central Pond Monster".equalsIgnoreCase(creature.getName()) ? AssetManager.monsterAquaticImg :
                       "Auditorium Alley Croc".equalsIgnoreCase(creature.getName()) ? AssetManager.monsterCrocImg :
                       "Workshop Swamp Beast".equalsIgnoreCase(creature.getName()) ? AssetManager.monsterSwampImg : AssetManager.monsterBossImg;

        if (sprite != null) {
            double r = creature.getRadius();
            gc.drawImage(sprite, creature.getX() - r * 1.5, creature.getY() - r * 1.5, r * 3, r * 3);
            return;
        }

        double x = creature.getX();
        double y = creature.getY();
        double r = creature.getRadius();
        gc.setFill(Color.web("#020617", 0.42));
        gc.fillOval(x - r * 1.3, y + r * .2, r * 2.6, r * .9);
        gc.setFill(Color.web("#14532d"));
        gc.fillOval(x - r, y - r * .55, r * 2.1, r * 1.25);
        gc.setFill(Color.web("#22c55e"));
        gc.fillOval(x - r * .85, y - r * .45, r * 1.7, r * .65);
        gc.setFill(Color.web("#0f172a"));
        gc.setFill(Color.web("#fef08a"));
        gc.fillOval(x + r * .3, y - r * .2, r * .12, r * .12);
        gc.setFill(Color.web("#f8fafc"));
        for (int tooth = 0; tooth < 4; tooth++) {
            gc.fillPolygon(new double[]{x + r * .25 + tooth * r * .17, x + r * .38 + tooth * r * .17, x + r * .32 + tooth * r * .17},
                    new double[]{y + r * .3, y + r * .3, y + r * .52}, 3);
        }
        gc.setFill(Color.web("#15803d"));
        gc.fillPolygon(new double[]{x - r * .75, x - r * 1.25, x - r * .9},
                new double[]{y, y - r * .45, y + r * .45}, 3);
    }

    private void drawUsefulObject(UsefulObject u) {
        gc.save();
        gc.setFill(Color.web("#facc15", 0.35));
        gc.fillOval(u.getX() - 28, u.getY() - 28, 56, 56);

        gc.setFill(Color.web("#eab308"));
        gc.fillRect(u.getX() - 18, u.getY() - 16, 36, 32);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("📦 SUPPLY", u.getX() - 26, u.getY() + 30);
        gc.restore();
    }

    private void drawFloatingTexts(List<GameEngine.FloatingText> texts) {
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        for (GameEngine.FloatingText ft : texts) {
            gc.setFill(Color.web(ft.getColor(), ft.getOpacity()));
            gc.fillText(ft.getText(), ft.getX() - 60, ft.getY());
        }
    }

    private void drawHUD(GameEngine engine, double w, double h) {
        gc.setFill(Color.web("#090d16", 0.92));
        gc.fillRoundRect(20, 16, w - 40, 68, 20, 20);
        gc.setStroke(Color.web("#dc2626", 0.6));
        gc.setLineWidth(2);
        gc.strokeRoundRect(20, 16, w - 40, 68, 20, 20);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        gc.setFill(Color.web("#38bdf8"));
        gc.fillText("[" + engine.getDifficultyLabel() + "]", 35, 56);

        gc.setFill(Color.web("#94a3b8"));
        gc.fillText("🎯 GOAL: " + engine.getTotalDelivered() + "/" + engine.getEvacuationGoal(), 130, 56);

        if (engine.getRescueCombo() > 1) {
            gc.setFill(Color.web("#f97316"));
            gc.fillText("🔥 x" + engine.getRescueCombo(), 300, 56);
        }

        // Triage Inventory Stock Sub-bar
        CafeteriaState caf = engine.getCafeteriaState();
        MedicalCenterState med = engine.getMedicalCenterState();
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("KITCHEN: 🍛" + caf.getRiceCurryStock() + " 🍲" + caf.getFishStewStock() + " 🍵" + caf.getTeaSnacksStock() +
                    " | MEDICAL: 🩹" + med.getBandageStock() + " 🧪" + med.getSalineStock() + " 🚑" + med.getFirstAidStock(), 370, 34);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#f97316"));
        gc.fillText("🍲 HUNGRY: " + engine.getRemainingHungryCount(), 370, 60);

        gc.setFill(Color.web("#ef4444"));
        gc.fillText("💔 LOST: " + engine.getTotalPerishedCount() + "/" + engine.getMaxPerishedBeforeFail(), 680, 56);

        gc.setFill(Color.web("#00ff88"));
        gc.fillText("🟢 EVAC: " + engine.getTotalDelivered(), 850, 56);

        gc.setFill(Color.web("#facc15"));
        gc.fillText("🏆 " + engine.getScore(), 980, 56);

        // Bottom Controls Banner
        gc.setFill(Color.web("#090d16", 0.92));
        gc.fillRoundRect(20, h - 55, w - 40, 42, 14, 14);
        gc.setStroke(Color.web("#dc2626", 0.5));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(20, h - 55, w - 40, 42, 14, 14);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("🕹️ PILOT (P1): WASD", 35, h - 28);

        gc.setFill(Color.web("#ff0055"));
        gc.fillText("🧑‍🌾 CREW (P2): Arrow Keys", 210, h - 28);

        gc.setFill(Color.web("#00ff88"));
        gc.fillText("⚡ ROPE: [R] | 🍲 FEED: [F] | 🩺 TREAT: [T]", 410, h - 28);

        gc.setFill(Color.web("#f97316"));
        gc.fillText("🍽️ CAFETERIA: [C]", 650, h - 28);

        gc.setFill(Color.web("#38bdf8"));
        gc.fillText("🏥 MEDICAL: [M]", 780, h - 28);

        gc.setFill(Color.web("#e0e7ff"));
        gc.fillText("🛸 VEHICLE: [V] | 🪂 AIRDROP: [H]", 880, h - 28);

        drawSurvivorRadarArrow(engine, w, h);
    }

    private void drawSurvivorRadarArrow(GameEngine engine, double w, double h) {
        Survivor closest = engine.getClosestUnrescuedSurvivor();
        if (closest == null) return;

        Boat boat = engine.getBoat();
        double dx = closest.getX() - boat.getX();
        double dy = closest.getY() - boat.getY();
        double angle = Math.atan2(dy, dx);

        double centerX = w / 2.0;
        double centerY = h / 2.0;
        double radius = Math.min(w, h) * 0.38;

        double arrowX = centerX + Math.cos(angle) * radius;
        double arrowY = centerY + Math.sin(angle) * radius;

        gc.save();
        gc.translate(arrowX, arrowY);
        gc.rotate(Math.toDegrees(angle));

        // Pulsing glowing arrow pointing toward nearest survivor
        gc.setFill(Color.web("#00ffcc", 0.95));
        gc.fillPolygon(new double[]{18, -12, -6, -12}, new double[]{0, -10, 0, 10}, 4);
        gc.setStroke(Color.web("#ffffff", 0.9));
        gc.setLineWidth(1.5);
        gc.strokePolygon(new double[]{18, -12, -6, -12}, new double[]{0, -10, 0, 10}, 4);

        gc.restore();

        // Distance text
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#00ffcc"));
        int distMeters = (int)(Math.hypot(dx, dy) / 10.0);
        gc.fillText("🚨 SURVIVOR (" + distMeters + "m)", arrowX - 45, arrowY + 22);
    }

    private void drawRainOverlay(double w, double h) {
        if (!rainInitialized) {
            for (int i = 0; i < 120; i++) {
                rainDrops.add(new RainDrop(Math.random() * w, Math.random() * h, 4 + Math.random() * 8, 8 + Math.random() * 14));
            }
            rainInitialized = true;
        }
        gc.setStroke(Color.web("#93c5fd", 0.35));
        gc.setLineWidth(1.2);
        for (RainDrop drop : rainDrops) {
            drop.update(h, w);
            gc.strokeLine(drop.x, drop.y, drop.x - 2, drop.y + drop.length);
        }
    }

    private void drawMinimap(GameEngine engine, double w, double h) {
        double miniW = 190;
        double miniH = 140;
        double miniX = w - miniW - 35;
        double miniY = 100;

        gc.setFill(Color.web("#020617", 0.92));
        gc.fillRoundRect(miniX, miniY, miniW, miniH, 14, 14);
        gc.setStroke(Color.web("#dc2626", 0.7));
        gc.setLineWidth(2);
        gc.strokeRoundRect(miniX, miniY, miniW, miniH, 14, 14);

        double scaleX = miniW / engine.getMapWidth();
        double scaleY = miniH / engine.getMapHeight();

        for (Building b : engine.getBuildings()) {
            if ("MOSQUE".equals(b.getType())) {
                gc.setFill(Color.web("#dc2626"));
            } else if ("FIELD".equals(b.getType()) || "PARK_TREES".equals(b.getType())) {
                gc.setFill(Color.web("#15803d"));
            } else {
                gc.setFill(Color.web("#991b1b"));
            }
            gc.fillRect(miniX + (b.getX() * scaleX), miniY + (b.getY() * scaleY), Math.max(3, b.getWidth() * scaleX), Math.max(3, b.getHeight() * scaleY));
        }

        SafeZone sz = engine.getSafeZone();
        gc.setFill(Color.web("#00ff88"));
        gc.fillOval(miniX + (sz.getX() * scaleX) - 4, miniY + (sz.getY() * scaleY) - 4, 9, 9);

        for (Survivor s : engine.getSurvivors()) {
            if (!s.isRescued()) {
                boolean critical = s.getHealth() / s.getMaxHealth() < 0.35;
                if (critical) {
                    double pulse = 0.5 + Math.sin(animTimer * 6) * 0.5;
                    gc.setFill(Color.web("#ff0055", pulse));
                    gc.fillOval(miniX + (s.getX() * scaleX) - 5, miniY + (s.getY() * scaleY) - 5, 10, 10);
                }
                gc.setFill(s.getType() == SurvivorType.HUNGRY ? Color.web("#f97316") :
                           s.getType() == SurvivorType.INJURED ? Color.web("#ef4444") : Color.web("#38bdf8"));
                gc.fillOval(miniX + (s.getX() * scaleX) - 3, miniY + (s.getY() * scaleY) - 3, 6, 6);
            }
        }

        Boat b = engine.getBoat();
        gc.setFill(Color.web("#facc15"));
        gc.fillOval(miniX + (b.getX() * scaleX) - 4.5, miniY + (b.getY() * scaleY) - 4.5, 9, 9);
    }

    private void updateParticles(GameEngine engine) {
        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle p = iter.next();
            p.update(0.035);
            if (p.isDead()) iter.remove();
        }
    }

    private void drawParticles() {
        for (Particle p : particles) {
            gc.setFill(Color.web(toHex(p.color), p.getOpacity()));
            gc.fillOval(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
        }
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    private static class RainDrop {
        double x, y, speed, length;

        RainDrop(double x, double y, double speed, double length) {
            this.x = x; this.y = y; this.speed = speed; this.length = length;
        }

        void update(double screenH, double screenW) {
            y += speed;
            x -= 1.5;
            if (y > screenH) {
                y = -length;
                x = Math.random() * screenW;
            }
            if (x < 0) x = screenW;
        }
    }

    private static class Particle {
        double x, y, vx, vy, life, maxLife, size;
        Color color;

        Particle(double x, double y, double vx, double vy, double life, Color color, double size) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life;
            this.color = color; this.size = size;
        }

        void update(double dt) {
            x += vx; y += vy;
            life -= dt;
        }

        boolean isDead() { return life <= 0; }
        double getOpacity() { return Math.max(0, life / maxLife); }
    }
}
