package com.trashsailors.view;

import com.trashsailors.controller.GameEngine;
import com.trashsailors.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameRenderer {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private double animTimer = 0;

    private final List<Particle> particles = new ArrayList<>();

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public void render(GameEngine engine) {
        animTimer += 0.035;
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();

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

        // 1. Draw Flooded IUT Campus Avenues & Base Ground
        drawFloodedIUTCampusGround(engine, camX, camY, effectiveW, effectiveH);

        gc.save();
        gc.translate(-camX, -camY);

        // 2. Draw Immovable IUT Buildings, Mosque, Halls, Workshop & OIC Tree Park
        for (Building building : engine.getBuildings()) {
            drawIUTBuilding(building, boat);
        }

        // 3. Draw IUT Evacuation Helipad Safe Zone
        drawIUTEvacuationSafeZone(engine.getSafeZone());

        // 4. Draw World Particles
        drawParticles();

        // 5. Draw Useful Supply Chests
        for (UsefulObject obj : engine.getUsefulObjects()) {
            if (!obj.isCollected()) {
                drawUsefulObject(obj);
            }
        }

        // 6. Draw Avenue Obstacles
        for (Obstacle obs : engine.getObstacles()) {
            if (!obs.isCleared()) {
                drawObstacle(obs);
            }
        }

        // 7. Draw Campus Pond Monsters / Sewer Crocs
        for (Creature creature : engine.getCreatures()) {
            drawCreature(creature);
        }

        // 8. Draw Stranded Students & Staff
        for (Survivor survivor : engine.getSurvivors()) {
            if (!survivor.isRescued()) {
                drawSurvivor(survivor, engine.isLowTimerActive());
            }
        }

        // 8b. Draw Collectible Supply Boxes (Nails and Metal Crates)
        drawSupplyBoxes(engine.getSupplyBoxes());

        // 9. Draw Aim Line & Target Crosshair to Aimed Survivor or Supply Box
        drawAimLine(crew, boat, engine.getAimedSurvivor(), engine.getAimedSupplyBox());

        // 10. Draw Active Rope Throw Animation
        drawActiveRope(engine.getActiveRopeAnimation());

        // 11. Draw Detailed Trash Sailors Raft & Wake Trail
        drawTrashSailorsRaft(boat);

        // 12. Draw Crew Member
        drawCrew(crew, boat);

        // 13. Draw Floating Text Popups
        drawFloatingTexts(engine.getFloatingTexts());

        gc.restore(); // Restore world translation
        gc.restore(); // Restore camera zoom scaling

        // 14. Draw Fixed Screen HUD, Radar & Controls Bar
        drawHUD(engine, viewWidth, viewHeight);
        drawMinimap(engine, viewWidth, viewHeight);

        // 15. Draw Workshop Fabrication & Crafting Menu Overlay (Side View of Rover & Helicopter)
        if (engine.isWorkshopMenuOpen()) {
            drawWorkshopCraftingScreen(engine, viewWidth, viewHeight);
        }
    }

    private void updateParticles(GameEngine engine) {
        Boat boat = engine.getBoat();

        if (Math.hypot(boat.getVx(), boat.getVy()) > 0.4) {
            double rad = Math.toRadians(boat.getAngle() + 180);
            double px = boat.getX() + Math.cos(rad) * (boat.getWidth() / 2);
            double py = boat.getY() + Math.sin(rad) * (boat.getWidth() / 2);
            particles.add(new Particle(px + (Math.random() - 0.5) * 20, py + (Math.random() - 0.5) * 20,
                    (Math.random() - 0.5) * 0.5, (Math.random() - 0.5) * 0.5, 0.8, Color.web("#ffffff", 0.6), 8));
        }

        if (boat.isBoosted()) {
            double rad = Math.toRadians(boat.getAngle() + 180);
            double px = boat.getX() + Math.cos(rad) * (boat.getWidth() / 2);
            double py = boat.getY() + Math.sin(rad) * (boat.getWidth() / 2);
            particles.add(new Particle(px, py, (Math.random() - 0.5) * 2.5, (Math.random() - 0.5) * 2.5, 0.5, Color.web("#facc15", 0.9), 12));
        }

        Iterator<Particle> pIter = particles.iterator();
        while (pIter.hasNext()) {
            Particle p = pIter.next();
            p.update(0.016);
            if (p.isDead()) pIter.remove();
        }
    }

    private void drawParticles() {
        for (Particle p : particles) {
            gc.setFill(p.color);
            gc.setGlobalAlpha(p.getOpacity());
            gc.fillOval(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void drawFloodedIUTCampusGround(GameEngine engine, double camX, double camY, double w, double h) {
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(0, 0, w, h);

        double mapW = engine.getMapWidth();
        double mapH = engine.getMapHeight();

        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(600 - camX, -camY, 140, mapH);
        gc.fillRect(1600 - camX, -camY, 140, mapH);
        gc.fillRect(2500 - camX, -camY, 140, mapH);
        gc.fillRect(3400 - camX, -camY, 140, mapH);

        for (double y = 400; y < mapH; y += 600) {
            gc.fillRect(-camX, y - camY, mapW, 120);
        }

        gc.setFill(Color.web("#0e4d64", 0.45));
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.web("#0284c7", 0.3));
        for (int i = 0; i < 40; i++) {
            double wx = ((i * 210 + animTimer * 35) % mapW) - camX;
            double wy = ((i * 150 + Math.sin(animTimer + i) * 30) % mapH) - camY;
            if (wx >= -100 && wx <= w + 100 && wy >= -50 && wy <= h + 50) {
                gc.fillOval(wx, wy, 90 + Math.sin(animTimer + i) * 15, 18);
            }
        }
    }

    private void drawIUTBuilding(Building b, Boat boat) {
        gc.save();
        gc.setFill(Color.web("#020617", 0.55));
        gc.fillRect(b.getX() + 14, b.getY() + 14, b.getWidth(), b.getHeight());

        if ("WORKSHOP_DEPOT".equals(b.getType())) {
            drawWorkshopDepotAndSlipway(b, boat);
        } else if ("MOSQUE".equals(b.getType())) {
            gc.setFill(Color.web("#b91c1c"));
            gc.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());

            gc.setFill(Color.web("#f8fafc"));
            gc.fillRect(b.getX() + 30, b.getY() + 30, b.getWidth() - 60, b.getHeight() - 60);

            gc.setFill(Color.web("#dc2626"));
            gc.fillOval(b.getX() + b.getWidth() / 2 - 50, b.getY() + b.getHeight() / 2 - 50, 100, 100);
            gc.setFill(Color.web("#facc15"));
            gc.fillOval(b.getX() + b.getWidth() / 2 - 15, b.getY() + b.getHeight() / 2 - 15, 30, 30);

            gc.setFill(Color.web("#991b1b"));
            gc.fillRect(b.getX() + b.getWidth() - 45, b.getY() + 15, 35, 35);

            drawIUTLabelBadge(b);

        } else if ("PARK_TREES".equals(b.getType())) {
            // Flooded Forest Marsh Ground
            gc.setFill(Color.web("#14532d"));
            gc.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());

            // Flooded Water Puddles inside the Forest
            gc.setFill(Color.web("#0e7490", 0.45));
            for (double px = b.getX() + 50; px < b.getX() + b.getWidth() - 60; px += 170) {
                for (double py = b.getY() + 60; py < b.getY() + b.getHeight() - 60; py += 160) {
                    gc.fillOval(px, py, 95, 55);
                }
            }

            // Muddy Off-Road Vehicle Tracks
            gc.setStroke(Color.web("#78350f", 0.35));
            gc.setLineWidth(14);
            gc.strokeLine(b.getX() + 30, b.getY() + b.getHeight() / 2, b.getX() + b.getWidth() - 30, b.getY() + b.getHeight() / 2);
            gc.strokeLine(b.getX() + b.getWidth() / 2, b.getY() + 30, b.getX() + b.getWidth() / 2, b.getY() + b.getHeight() - 30);

            // Forest Trees with Trunks and Canopies
            for (double tx = b.getX() + 55; tx < b.getX() + b.getWidth() - 40; tx += 85) {
                for (double ty = b.getY() + 55; ty < b.getY() + b.getHeight() - 40; ty += 85) {
                    // Tree Trunk Shadow
                    gc.setFill(Color.web("#0f172a", 0.35));
                    gc.fillOval(tx - 18, ty + 12, 36, 16);

                    // Tree Trunk Center
                    gc.setFill(Color.web("#78350f"));
                    gc.fillOval(tx - 7, ty - 7, 14, 14);

                    // Dense Foliage Layers
                    gc.setFill(Color.web("#166534"));
                    gc.fillOval(tx - 36, ty - 36, 72, 72);
                    gc.setFill(Color.web("#15803d"));
                    gc.fillOval(tx - 26, ty - 26, 52, 52);
                    gc.setFill(Color.web("#22c55e", 0.85));
                    gc.fillOval(tx - 16, ty - 22, 32, 32);
                }
            }

            // Impassable Water Barrier Line for Boat
            if (boat.getVehicleType() == Boat.VehicleType.BOAT) {
                gc.setStroke(Color.web("#ef4444", 0.6 + Math.sin(animTimer * 4) * 0.2));
                gc.setLineWidth(3);
                gc.setLineDashes(12);
                gc.strokeRect(b.getX() + 2, b.getY() + 2, b.getWidth() - 4, b.getHeight() - 4);
                gc.setLineDashes(null);
            }

            drawIUTLabelBadge(b);

        } else if ("FIELD".equals(b.getType())) {
            gc.setFill(Color.web("#15803d"));
            gc.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());

            gc.setStroke(Color.web("#ffffff", 0.7));
            gc.setLineWidth(3);
            gc.strokeRect(b.getX() + 30, b.getY() + 30, b.getWidth() - 60, b.getHeight() - 60);
            gc.strokeOval(b.getX() + b.getWidth() / 2 - 60, b.getY() + b.getHeight() / 2 - 60, 120, 120);

            drawIUTLabelBadge(b);

        } else {
            gc.setFill(Color.web("#991b1b"));
            gc.fillRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());

            gc.setFill(Color.web(b.getColorHex()));
            gc.fillRect(b.getX() + 6, b.getY() + 6, b.getWidth() - 12, b.getHeight() - 12);

            gc.setStroke(Color.web("#fef08a", 0.6));
            gc.setLineWidth(2.5);
            gc.strokeRect(b.getX() + 6, b.getY() + 6, b.getWidth() - 12, b.getHeight() - 12);

            drawIUTLabelBadge(b);
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

        // Staged Alternate Vehicle Preview Hologram / Icon on Platform
        boolean isRover = (boat.getVehicleType() == Boat.VehicleType.ROVER);
        boolean isHeli = (boat.getVehicleType() == Boat.VehicleType.HELICOPTER);
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
        // Top/North Wall
        gc.strokeLine(bx + 4, by + 4, bx + bw - 4, by + 4);
        // Left/West Back Wall
        gc.strokeLine(bx + 4, by + 4, bx + 4, by + bh - 4);
        // Bottom/South Wall
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
        String title = "🏭 VEHICLE DEPOT & SLIPWAY [ENTER & PRESS V TO SWITCH]";
        double textW = title.length() * 7.5;
        gc.setFill(Color.web("#0f172a", 0.92));
        gc.fillRoundRect(bx + bw / 2 - textW / 2 - 10, by - 16, textW + 20, 24, 8, 8);
        gc.setStroke(Color.web("#38bdf8"));
        gc.setLineWidth(1.8);
        gc.strokeRoundRect(bx + bw / 2 - textW / 2 - 10, by - 16, textW + 20, 24, 8, 8);

        gc.setFill(Color.web("#38bdf8"));
        gc.fillText(title, bx + bw / 2 - textW / 2, by + 1);
    }

    private void drawIUTLabelBadge(Building b) {
        if (b.getNameLabel() == null || b.getNameLabel().isEmpty()) return;

        double centerX = b.getX() + b.getWidth() / 2;
        double centerY = b.getY() + b.getHeight() / 2;

        boolean isPark = "PARK_TREES".equals(b.getType());
        String label = isPark ? "🌲 OIC Tree Park (6×6 Rover Terrain)" : b.getNameLabel();

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        double textWidth = label.length() * 8.0;
        double textHeight = 24;

        gc.setFill(Color.web(isPark ? "#0f172a" : "#ffffff", 0.95));
        gc.fillRoundRect(centerX - (textWidth / 2) - 10, centerY - (textHeight / 2), textWidth + 20, textHeight, 10, 10);
        gc.setStroke(Color.web(isPark ? "#22c55e" : "#dc2626"));
        gc.setLineWidth(1.8);
        gc.strokeRoundRect(centerX - (textWidth / 2) - 10, centerY - (textHeight / 2), textWidth + 20, textHeight, 10, 10);

        gc.setFill(Color.web(isPark ? "#4ade80" : "#dc2626"));
        gc.fillText(label, centerX - (textWidth / 2), centerY + 4);
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

    private void drawAimLine(Crew crew, Boat boat, Survivor targetSurvivor, SupplyBox targetBox) {
        double crewX = crew.getWorldX(boat);
        double crewY = crew.getWorldY(boat);

        if (targetSurvivor != null && !targetSurvivor.isRescued()) {
            boolean isHeli = (boat.getVehicleType() == Boat.VehicleType.HELICOPTER);
            String actionText = targetSurvivor.isRooftopTrapped() ? "🚁 AIRLIFT WINCH [SPACE/R]" : (isHeli ? "🚁 WINCH RESCUE [SPACE/R]" : "🎯 THROW ROPE [SPACE/R]");

            // Pulsing Golden/Cyan Aim Line from Crew to Survivor
            gc.setStroke(Color.web(isHeli ? "#38bdf8" : "#facc15", 0.8 + Math.sin(animTimer * 6) * 0.2));
            gc.setLineWidth(3);
            gc.setLineDashes(8);
            gc.strokeLine(crewX, crewY, targetSurvivor.getX(), targetSurvivor.getY());
            gc.setLineDashes(null);

            // Targeting Crosshair Ring around Survivor
            gc.setStroke(Color.web(isHeli ? "#38bdf8" : "#facc15", 0.9));
            gc.setLineWidth(2.5);
            gc.strokeOval(targetSurvivor.getX() - 26, targetSurvivor.getY() - 26, 52, 52);

            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.setFill(Color.web(isHeli ? "#38bdf8" : "#facc15"));
            gc.fillText(actionText, targetSurvivor.getX() - 75, targetSurvivor.getY() - 32);
        } else if (targetBox != null && !targetBox.isCollected()) {
            String boxName = (targetBox.getType() == SupplyBox.BoxType.NAILS) ? "🔩 NAILS CRATE" : "⚙️ METAL CRATE";
            String actionText = "🎯 HOOK " + boxName + " [SPACE/R]";

            // Pulsing Amber Aim Line from Crew to Supply Box
            gc.setStroke(Color.web("#f59e0b", 0.85 + Math.sin(animTimer * 6) * 0.15));
            gc.setLineWidth(3);
            gc.setLineDashes(8);
            gc.strokeLine(crewX, crewY, targetBox.getX(), targetBox.getY());
            gc.setLineDashes(null);

            // Targeting Crosshair Box around Supply Box
            gc.setStroke(Color.web("#f59e0b", 0.95));
            gc.setLineWidth(2.5);
            gc.strokeRect(targetBox.getX() - 20, targetBox.getY() - 20, 40, 40);

            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.setFill(Color.web("#f59e0b"));
            gc.fillText(actionText, targetBox.getX() - 70, targetBox.getY() - 28);
        }
    }

    private void drawActiveRope(RopeAnimation rope) {
        if (!rope.isActive()) return;

        double currentX = rope.getStartX() + (rope.getTargetX() - rope.getStartX()) * rope.getProgress();
        double currentY = rope.getStartY() + (rope.getTargetY() - rope.getStartY()) * rope.getProgress();

        boolean isRooftop = (rope.getTargetSurvivor() != null && rope.getTargetSurvivor().isRooftopTrapped());
        boolean isBox = (rope.getTargetSupplyBox() != null);

        if (isRooftop) {
            // High-Strength Braided Steel Winch Cable Line
            gc.setStroke(Color.web("#94a3b8"));
            gc.setLineWidth(3);
            gc.strokeLine(rope.getStartX(), rope.getStartY(), currentX, currentY);

            // Rescue Harness / Stretcher Basket at Cable End
            gc.setFill(Color.web("#facc15"));
            gc.fillRoundRect(currentX - 10, currentY - 10, 20, 20, 4, 4);
            gc.setStroke(Color.web("#dc2626"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(currentX - 10, currentY - 10, 20, 20, 4, 4);
            gc.setStroke(Color.web("#ffffff"));
            gc.setLineWidth(2);
            gc.strokeLine(currentX - 6, currentY, currentX + 6, currentY);
            gc.strokeLine(currentX, currentY - 6, currentX, currentY + 6);
        } else if (isBox) {
            // Steel Cargo Grapple Winch Cable
            gc.setStroke(Color.web("#f59e0b"));
            gc.setLineWidth(3.5);
            gc.strokeLine(rope.getStartX(), rope.getStartY(), currentX, currentY);

            // Grapple Hook Jaws
            gc.setStroke(Color.web("#cbd5e1"));
            gc.setLineWidth(2.5);
            gc.strokeArc(currentX - 10, currentY - 10, 20, 20, 45, 180, ArcType.OPEN);
        } else {
            // Thick Rope Cable Line
            gc.setStroke(Color.web("#ca8a04"));
            gc.setLineWidth(4);
            gc.strokeLine(rope.getStartX(), rope.getStartY(), currentX, currentY);

            // Lasso Loop at rope end
            gc.setStroke(Color.web("#facc15"));
            gc.setLineWidth(3);
            gc.strokeOval(currentX - 12, currentY - 12, 24, 24);
        }
    }

    private void drawTrashSailorsRaft(Boat boat) {
        if (boat.getVehicleType() == Boat.VehicleType.ROVER) {
            drawAmphibiousRescueRover(boat);
        } else if (boat.getVehicleType() == Boat.VehicleType.HELICOPTER) {
            drawRescueHelicopter(boat);
        } else {
            drawRescueBoat(boat);
        }
    }

    private void drawRescueHelicopter(Boat boat) {
        gc.save();
        gc.translate(boat.getX(), boat.getY());
        gc.rotate(boat.getAngle());

        double bw = boat.getWidth();
        double bh = boat.getHeight();

        // 1. Hover Altitude Shadow & Rotor Downwash Ripple Effect on Surface
        drawHelicopterDownwash(bw, bh);

        // 2. Speed Boost Aura Glow
        if (boat.isBoosted()) {
            gc.setStroke(Color.web("#38bdf8", 0.95));
            gc.setLineWidth(5);
            gc.strokeRoundRect(-bw / 2 - 16, -bh / 2 - 12, bw + 32, bh + 24, 26, 26);
        }

        // 3. High-Intensity Gimbal Searchlight & Forward Floodlights
        drawHelicopterSearchlight(bw, bh);

        // 4. Heavy-Duty Titanium Landing Skids & Cross-Struts
        drawHelicopterLandingSkids(bw, bh);

        // 5. Tail Boom, Stabilizer Wings & Vertical Tail Fin
        drawHelicopterTailBoom(bw, bh);

        // 6. Main Armored Fuselage (Maritime Rescue Blue + Emergency Red Stripes + White Trim)
        drawHelicopterFuselage(bw, bh);

        // 7. Cockpit, Windshield Glass & Pilot P1 at Controls
        drawHelicopterCockpit(bw, bh);

        // 8. Open Side Rescue Cabin, Hoist Winch, Medical Supplies & Rescued Students
        drawHelicopterRescueCabin(boat, bw, bh);

        // 9. Spinning 2-Blade Tail Rotor with Yellow Safety Tips
        drawHelicopterTailRotor(bw, bh);

        // 10. Central Main Rotor Mast, 4-Blade Carbon Rotor & Spinning Motion Blur Disc
        drawHelicopterMainRotor(bw, bh);

        gc.restore();
    }

    private void drawHelicopterDownwash(double bw, double bh) {
        // Translucent Hover Altitude Shadow below the fuselage
        gc.setFill(Color.web("#020617", 0.32));
        gc.fillOval(-bw / 2 - 4, -bh / 2 + 10, bw + 14, bh + 6);

        // Pulsing Concentric Downwash Air Waves / Surface Ripples
        double ripplePhase = (animTimer * 6) % 1.0;
        double r1 = 38 + ripplePhase * 45;
        double r2 = 38 + ((ripplePhase + 0.5) % 1.0) * 45;

        gc.setStroke(Color.web("#38bdf8", (1.0 - ripplePhase) * 0.35));
        gc.setLineWidth(1.8);
        gc.strokeOval(-r1, -r1, r1 * 2, r1 * 2);

        double phase2 = (ripplePhase + 0.5) % 1.0;
        gc.setStroke(Color.web("#ffffff", (1.0 - phase2) * 0.25));
        gc.strokeOval(-r2, -r2, r2 * 2, r2 * 2);
    }

    private void drawHelicopterSearchlight(double bw, double bh) {
        double lightLength = 220;
        double noseX = bw / 2 - 8;

        // Wide High-Intensity Searchlight Cone
        gc.setFill(Color.web("#fef08a", 0.22));
        double[] xPoints = { noseX, noseX + lightLength, noseX + lightLength };
        double[] yPoints = { 0, -bh / 2 - 45, bh / 2 + 45 };
        gc.fillPolygon(xPoints, yPoints, 3);

        // Inner Piercing High-Beam Spotlight Cone
        gc.setFill(Color.web("#ffffff", 0.30));
        double[] xInner = { noseX, noseX + lightLength * 0.85, noseX + lightLength * 0.85 };
        double[] yInner = { 0, -bh / 4, bh / 4 };
        gc.fillPolygon(xInner, yInner, 3);

        // Gimbal Nose Spotlight Lens
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(noseX - 4, -5, 8, 10);
        gc.setFill(Color.web("#fef08a"));
        gc.fillOval(noseX - 2, -4, 7, 8);
        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(noseX, -2, 4, 4);
    }

    private void drawHelicopterLandingSkids(double bw, double bh) {
        double skidLength = bw * 0.72;
        double skidStartX = -bw / 3 + 2;
        double skidEndX = skidStartX + skidLength;
        double portY = -bh / 2 + 4;
        double starY = bh / 2 - 4;

        // Skids Metallic Shadow
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(4.5);
        // Port (Left) Skid
        gc.strokeLine(skidStartX, portY, skidEndX, portY);
        gc.strokeLine(skidEndX, portY, skidEndX + 8, portY + 4); // Curved Upward Toe Tip
        // Starboard (Right) Skid
        gc.strokeLine(skidStartX, starY, skidEndX, starY);
        gc.strokeLine(skidEndX, starY, skidEndX + 8, starY - 4); // Curved Upward Toe Tip

        // Titanium Skid Tubes (Dark Slate Metallic)
        gc.setStroke(Color.web("#64748b"));
        gc.setLineWidth(3);
        gc.strokeLine(skidStartX, portY, skidEndX, portY);
        gc.strokeLine(skidEndX, portY, skidEndX + 8, portY + 4);
        gc.strokeLine(skidStartX, starY, skidEndX, starY);
        gc.strokeLine(skidEndX, starY, skidEndX + 8, starY - 4);

        // Tubular Cross-Struts (Mounting Skids to Fuselage Belly)
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(3.5);
        double fwdStrutX = bw / 6;
        double aftStrutX = -bw / 5;
        gc.strokeLine(fwdStrutX, portY, fwdStrutX, starY);
        gc.strokeLine(aftStrutX, portY, aftStrutX, starY);

        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(1.8);
        gc.strokeLine(fwdStrutX, portY, fwdStrutX, starY);
        gc.strokeLine(aftStrutX, portY, aftStrutX, starY);
    }

    private void drawHelicopterTailBoom(double bw, double bh) {
        double boomStartX = -bw / 5;
        double boomEndX = -bw / 2 - 24;
        double boomLen = boomStartX - boomEndX;

        // Tapered Tail Boom Structure
        gc.setFill(Color.web("#0369a1")); // Rescue Blue
        double[] bx = { boomStartX, boomEndX, boomEndX, boomStartX };
        double[] by = { -11, -5, 5, 11 };
        gc.fillPolygon(bx, by, 4);

        // Red Chevron Warning Bands on Tail Boom
        gc.setFill(Color.web("#dc2626"));
        gc.fillRect(boomStartX - 20, -9, 12, 18);
        gc.fillRect(boomStartX - 42, -7.5, 10, 15);

        // White Emergency Marking Stripe
        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(boomStartX - 10, -1.5, -boomLen + 18, 3);

        // Horizontal Stabilizer Wings
        double stabX = boomEndX + 16;
        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(stabX - 4, -22, 9, 44);
        gc.setFill(Color.web("#0284c7"));
        gc.fillRect(stabX - 3, -20, 7, 40);

        // High-Vis Red Winglet Tips on Stabilizers
        gc.setFill(Color.web("#dc2626"));
        gc.fillRect(stabX - 3, -22, 7, 4);
        gc.fillRect(stabX - 3, 18, 7, 4);

        // Vertical Tail Fin (Aft Empennage)
        gc.setFill(Color.web("#0284c7"));
        double[] finX = { boomEndX + 10, boomEndX - 10, boomEndX - 6, boomEndX + 10 };
        double[] finY = { -3, -16, -18, -3 };
        gc.fillPolygon(finX, finY, 4);

        gc.setFill(Color.web("#dc2626"));
        double[] tipX = { boomEndX - 4, boomEndX - 10, boomEndX - 6, boomEndX - 2 };
        double[] tipY = { -12, -16, -18, -14 };
        gc.fillPolygon(tipX, tipY, 4);

        // Aft Navigation Strobe Beacon (Flashing White)
        boolean navFlash = ((int)(animTimer * 8) % 2 == 0);
        gc.setFill(navFlash ? Color.web("#ffffff", 0.95) : Color.web("#64748b", 0.7));
        gc.fillOval(boomEndX - 9, -17, 5, 5);
        if (navFlash) {
            gc.setFill(Color.web("#ffffff", 0.35));
            gc.fillOval(boomEndX - 13, -21, 13, 13);
        }
    }

    private void drawHelicopterFuselage(double bw, double bh) {
        double noseX = bw / 2 - 10;
        double aftCabinX = -bw / 4;
        double fuseH = bh - 16;
        double fuseY = -fuseH / 2;

        // Lower Fuselage Hull Body (Maritime Rescue Blue)
        gc.setFill(Color.web("#0369a1"));
        gc.fillRoundRect(aftCabinX, fuseY, noseX - aftCabinX, fuseH, 18, 18);

        // Nose Aerodynamic Cap
        gc.setFill(Color.web("#0284c7"));
        double[] nosePolyX = { bw / 6, noseX + 6, bw / 6 };
        double[] nosePolyY = { fuseY + 2, 0, fuseY + fuseH - 2 };
        gc.fillPolygon(nosePolyX, nosePolyY, 3);

        // High-Vis Emergency Red Side Flash / Chevrons
        gc.setFill(Color.web("#dc2626"));
        gc.fillRect(aftCabinX + 10, fuseY + 3, (noseX - aftCabinX) - 26, 4.5);
        gc.fillRect(aftCabinX + 10, fuseY + fuseH - 7.5, (noseX - aftCabinX) - 26, 4.5);

        // White Emergency Rescue Flank Stripe
        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(aftCabinX + 14, fuseY + 8, (noseX - aftCabinX) - 34, 3);
        gc.fillRect(aftCabinX + 14, fuseY + fuseH - 11, (noseX - aftCabinX) - 34, 3);

        // Dual Turboshaft Engine Cowlings (Roof Mounted)
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(-18, -12, 34, 24, 6, 6);
        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(-18, -12, 34, 24, 6, 6);

        // Engine Turbine Exhaust Ports (Dark Heat Resistant Titanium)
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(-22, -9, 6, 6);
        gc.fillOval(-22, 3, 6, 6);
        gc.setStroke(Color.web("#f97316", 0.6));
        gc.setLineWidth(1);
        gc.strokeOval(-22, -9, 6, 6);
        gc.strokeOval(-22, 3, 6, 6);

        // Flashing Emergency Beacon Light on Fuselage Roof (Red / White Strobe)
        boolean beaconRed = ((int)(animTimer * 10) % 2 == 0);
        gc.setFill(beaconRed ? Color.web("#ff0044", 0.95) : Color.web("#ffffff", 0.95));
        gc.fillOval(12, -4, 8, 8);
        gc.setFill(beaconRed ? Color.web("#ff0044", 0.3) : Color.web("#ffffff", 0.3));
        gc.fillOval(8, -8, 16, 16);
    }

    private void drawHelicopterCockpit(double bw, double bh) {
        double cpStartX = bw / 6;
        double cpEndX = bw / 2 - 8;
        double cpH = bh - 24;
        double cpY = -cpH / 2;

        // Cockpit Glare Shield & Dashboard
        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(cpStartX + 12, cpY + 2, cpEndX - cpStartX - 14, cpH - 4, 8, 8);

        // Glowing HUD Avionics Instrument Panel
        gc.setFill(Color.web("#22c55e"));
        gc.fillRect(cpStartX + 22, -4, 4, 3);
        gc.fillRect(cpStartX + 22, 1, 4, 3);
        gc.setFill(Color.web("#38bdf8"));
        gc.fillRect(cpStartX + 28, -3, 3, 6);

        // Curved Panoramic Tinted Windshield Glass
        gc.setFill(Color.web("#38bdf8", 0.72));
        double[] glassX = { cpStartX + 4, cpEndX, cpEndX, cpStartX + 4 };
        double[] glassY = { cpY + 2, cpY + 6, cpY + cpH - 6, cpY + cpH - 2 };
        gc.fillPolygon(glassX, glassY, 4);

        // Glass Reflection Glare Streak
        gc.setStroke(Color.web("#ffffff", 0.75));
        gc.setLineWidth(1.8);
        gc.strokeLine(cpStartX + 8, cpY + 4, cpEndX - 6, cpY + 8);

        // Pilot P1 at Cyclic Controls
        gc.setFill(Color.web("#0284c7")); // Flight Suit
        gc.fillOval(cpStartX + 4, -6, 12, 12);
        gc.setFill(Color.web("#facc15")); // Pilot Helmet
        gc.fillOval(cpStartX + 2, -8, 14, 8);
        gc.setFill(Color.web("#0f172a")); // Dark Helmet Visor
        gc.fillRect(cpStartX + 10, -5, 4, 6);

        // Pilot Label
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("P1", cpStartX - 6, -cpH / 2 - 2);
    }

    private void drawHelicopterRescueCabin(Boat boat, double bw, double bh) {
        double cabinStartX = -bw / 4 + 4;
        double cabinEndX = bw / 6 + 2;
        double cabinW = cabinEndX - cabinStartX;
        double cabinH = bh - 24;
        double cabinY = -cabinH / 2;

        // Interior Floor (Non-Slip Grip Deck)
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(cabinStartX, cabinY, cabinW, cabinH, 4, 4);

        // Floor Grid Lines
        gc.setStroke(Color.web("#334155", 0.6));
        gc.setLineWidth(1);
        for (double x = cabinStartX + 6; x < cabinEndX; x += 9) {
            gc.strokeLine(x, cabinY + 2, x, cabinY + cabinH - 2);
        }

        // Open Sliding Rescue Door Frames (Top & Bottom Flanks)
        gc.setFill(Color.web("#0284c7"));
        gc.fillRect(cabinStartX, cabinY - 2, cabinW, 3);
        gc.fillRect(cabinStartX, cabinY + cabinH - 1, cabinW, 3);

        // Heavy-Duty Rescue Winch Hoist Crane Arm (Mounted on Starboard Door Frame)
        double hoistX = 0;
        double hoistY = cabinY + cabinH;
        gc.setStroke(Color.web("#ca8a04"));
        gc.setLineWidth(3.5);
        gc.strokeLine(hoistX - 5, hoistY - 2, hoistX + 6, hoistY + 7);
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(2);
        gc.strokeLine(hoistX - 5, hoistY - 2, hoistX + 6, hoistY + 7);

        // Winch Cable Drum Housing
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(hoistX - 7, hoistY - 4, 7, 7);
        gc.setFill(Color.web("#94a3b8"));
        gc.fillOval(hoistX - 5, hoistY - 2, 3, 3);

        // Rescue Winch Hook / Pulley Ring
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(2);
        gc.strokeOval(hoistX + 4, hoistY + 6, 5, 5);

        // Medical Equipment Inside Cabin: First-Aid Stretcher / Trauma Pack
        gc.setFill(Color.web("#dc2626"));
        gc.fillRoundRect(cabinStartX + 3, -6, 12, 12, 2, 2);
        gc.setStroke(Color.web("#ffffff"));
        gc.setLineWidth(1.5);
        // Medical White Cross
        gc.strokeLine(cabinStartX + 5, 0, cabinStartX + 13, 0);
        gc.strokeLine(cabinStartX + 9, -4, cabinStartX + 9, 4);

        // Oxygen Cylinders & Rescue Lifebuoy
        gc.setFill(Color.web("#0284c7"));
        gc.fillRoundRect(cabinStartX + 17, -8, 6, 16, 2, 2);
        gc.setFill(Color.web("#ea580c"));
        gc.fillOval(cabinStartX + 25, -6, 11, 12);
        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(cabinStartX + 28, -3, 5, 6);

        // Passenger Seats & Seated Rescued Students
        int maxCapacity = boat.getMaxCargoCapacity();
        double seatSlotW = (cabinW - 6) / maxCapacity;

        for (int i = 0; i < maxCapacity; i++) {
            double seatX = cabinStartX + 3 + (i * seatSlotW);
            boolean isTopRow = (i % 2 == 0);
            double seatY = isTopRow ? (cabinY + 3) : (cabinY + cabinH - 11);

            // Empty Rescue Seat Slot
            gc.setStroke(Color.web("#64748b", 0.5));
            gc.setLineWidth(1);
            gc.strokeRect(seatX + 0.5, seatY, seatSlotW - 1.5, 8);

            // Rescued Student / Supply Box Sitting Inside Cabin
            if (i < boat.getCargoSlots().size()) {
                Boat.CargoType cType = boat.getCargoSlots().get(i);
                if (cType == Boat.CargoType.SURVIVOR) {
                    // Safety Orange Life Vest
                    gc.setFill(Color.web("#ea580c"));
                    gc.fillRoundRect(seatX + 1, seatY, seatSlotW - 2, 8, 2, 2);

                    // Student Head & Hair
                    gc.setFill(Color.web("#fed7aa"));
                    gc.fillOval(seatX + seatSlotW / 2 - 2.5, seatY + 1.5, 5, 5);
                    gc.setFill(Color.web("#0f172a"));
                    gc.fillOval(seatX + seatSlotW / 2 - 2.5, seatY + 0.5, 5, 2);
                } else if (cType == Boat.CargoType.NAILS) {
                    // Nails Crate in seat
                    gc.setFill(Color.web("#b45309"));
                    gc.fillRoundRect(seatX + 1, seatY, seatSlotW - 2, 8, 2, 2);
                    gc.setStroke(Color.web("#fef08a"));
                    gc.setLineWidth(1);
                    gc.strokeLine(seatX + 2, seatY + 2, seatX + seatSlotW - 3, seatY + 6);
                } else if (cType == Boat.CargoType.METAL) {
                    // Metal Parts in seat
                    gc.setFill(Color.web("#475569"));
                    gc.fillRoundRect(seatX + 1, seatY, seatSlotW - 2, 8, 2, 2);
                    gc.setFill(Color.web("#cbd5e1"));
                    gc.fillRect(seatX + 2, seatY + 2, seatSlotW - 4, 4);
                }
            }
        }
    }

    private void drawHelicopterTailRotor(double bw, double bh) {
        double trX = -bw / 2 - 20;
        double trY = -16;
        double trRadius = 14;

        // Tail Rotor Hub
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(trX - 3, trY - 3, 6, 6);

        // Spinning Tail Rotor Motion Blur Disc
        gc.setFill(Color.web("#94a3b8", 0.22));
        gc.fillOval(trX - trRadius, trY - trRadius, trRadius * 2, trRadius * 2);

        // Spinning 2 Blades
        double trAngle = Math.toRadians(animTimer * 45 * 60);
        double cosA = Math.cos(trAngle);
        double sinA = Math.sin(trAngle);

        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(2.5);
        gc.strokeLine(trX - cosA * trRadius, trY - sinA * trRadius, trX + cosA * trRadius, trY + sinA * trRadius);

        // Safety Yellow Tips on Tail Rotor
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(3);
        gc.strokeLine(trX + cosA * (trRadius - 3), trY + sinA * (trRadius - 3), trX + cosA * trRadius, trY + sinA * trRadius);
        gc.strokeLine(trX - cosA * (trRadius - 3), trY - sinA * (trRadius - 3), trX - cosA * trRadius, trY - sinA * trRadius);
    }

    private void drawHelicopterMainRotor(double bw, double bh) {
        double hubX = 0;
        double hubY = 0;
        double bladeRadius = 72; // Long wide rotor blades

        // 1. High-Speed Rotating Motion Blur Disc (Translucent Outer Ring)
        gc.setFill(Color.web("#94a3b8", 0.18));
        gc.fillOval(hubX - bladeRadius, hubY - bladeRadius, bladeRadius * 2, bladeRadius * 2);

        gc.setStroke(Color.web("#38bdf8", 0.28));
        gc.setLineWidth(1.5);
        gc.strokeOval(hubX - bladeRadius + 2, hubY - bladeRadius + 2, (bladeRadius - 2) * 2, (bladeRadius - 2) * 2);

        // 2. Spinning 4-Blade Carbon Fiber Rotor Assembly
        double rotorAngleDeg = (animTimer * 30 * 60) % 360;

        for (int i = 0; i < 4; i++) {
            double angleRad = Math.toRadians(rotorAngleDeg + (i * 90));
            double cos = Math.cos(angleRad);
            double sin = Math.sin(angleRad);

            // Blade Root Pitch Horn
            gc.setStroke(Color.web("#475569"));
            gc.setLineWidth(4);
            gc.strokeLine(hubX, hubY, hubX + cos * 14, hubY + sin * 14);

            // Carbon-Fiber Rotor Blade Body
            gc.setStroke(Color.web("#0f172a"));
            gc.setLineWidth(3.2);
            gc.strokeLine(hubX + cos * 14, hubY + sin * 14, hubX + cos * (bladeRadius - 8), hubY + sin * (bladeRadius - 8));

            // High-Visibility Safety Yellow Blade Tip
            gc.setStroke(Color.web("#facc15"));
            gc.setLineWidth(3.8);
            gc.strokeLine(hubX + cos * (bladeRadius - 8), hubY + sin * (bladeRadius - 8), hubX + cos * bladeRadius, hubY + sin * bladeRadius);
        }

        // 3. Central Titanium Rotor Mast Hub & Swashplate
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(hubX - 8, hubY - 8, 16, 16);
        gc.setFill(Color.web("#64748b"));
        gc.fillOval(hubX - 5, hubY - 5, 10, 10);
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillOval(hubX - 2.5, hubY - 2.5, 5, 5);
    }

    private void drawAmphibiousRescueRover(Boat boat) {
        gc.save();
        gc.translate(boat.getX(), boat.getY());
        gc.rotate(boat.getAngle());

        double bw = boat.getWidth();
        double bh = boat.getHeight();

        // 1. Forward High-Intensity LED Headlights & Glowing Light Beams
        drawRoverHeadlights(bw, bh);

        // 2. Speed Boost Aura Glow
        if (boat.isBoosted()) {
            gc.setStroke(Color.web("#facc15", 0.95));
            gc.setLineWidth(5);
            gc.strokeRoundRect(-bw / 2 - 12, -bh / 2 - 10, bw + 24, bh + 20, 22, 22);
        }

        // 3. Six Rugged Deep-Tread Off-Road Wheels (3 per side)
        drawRoverSixWheels(bw, bh);

        // 4. Main Waterproof Chassis & Armored Hull (Olive Green + Rescue Orange)
        drawRoverHullAndChassis(bw, bh);

        // 5. Front Heavy-Duty Bullbar & Electric Winch
        drawRoverBullbarAndWinch(bw, bh);

        // 6. Cab, Windshield & Flashing Roof Lightbar
        drawRoverCabAndLightbar(bw, bh);

        // 7. Side-Mounted Emergency Rescue Equipment (Ropes, Lifebuoys, First-Aid Kit)
        drawRoverRescueEquipment(bw, bh);

        // 8. Open Rear Student Rescue Compartment & Seated Rescued Students
        drawRoverRescueDeckAndPassengers(boat, bw, bh);

        gc.restore();
    }

    private void drawRescueBoat(Boat boat) {
        gc.save();
        gc.translate(boat.getX(), boat.getY());
        gc.rotate(boat.getAngle());

        double bw = boat.getWidth();
        double bh = boat.getHeight();

        // 1. Forward High-Intensity Floodlight Beams
        double lightLength = 175;
        double bowX = bw / 2;
        gc.setFill(Color.web("#fef08a", 0.22));
        double[] xPoints = { bowX - 4, bowX + lightLength, bowX + lightLength };
        double[] yPoints = { 0, -bh / 2 - 25, bh / 2 + 25 };
        gc.fillPolygon(xPoints, yPoints, 3);

        // 2. Speed Boost Aura Glow
        if (boat.isBoosted()) {
            gc.setStroke(Color.web("#facc15", 0.95));
            gc.setLineWidth(5);
            gc.strokeRoundRect(-bw / 2 - 14, -bh / 2 - 8, bw + 28, bh + 16, 22, 22);
        }

        // 3. Dual Outboard Marine Engines at Stern (Twin Motors)
        double motorX = -bw / 2 - 12;
        // Port Outboard Engine
        drawOutboardEngine(motorX, -14, 18, 12);
        // Starboard Outboard Engine
        drawOutboardEngine(motorX, 2, 18, 12);

        // Twin Propeller Cavitation Wake / Bubbles
        gc.setFill(Color.web("#ffffff", 0.7));
        gc.fillOval(motorX - 10, -13 + (Math.sin(animTimer * 12) * 2), 12, 6);
        gc.fillOval(motorX - 10, 3 + (Math.cos(animTimer * 12) * 2), 12, 6);

        // 4. Sleek Hydrodynamic Deep-V Hull (High-Vis Safety Orange & Maritime Blue)
        // Outer Inflatable / Solid Blue Sponson Rub-Rail Fender
        gc.setFill(Color.web("#0369a1"));
        double[] hullOuterX = { -bw / 2 + 4, bw / 4, bw / 2, bw / 4, -bw / 2 + 4 };
        double[] hullOuterY = { -bh / 2, -bh / 2, 0, bh / 2, bh / 2 };
        gc.fillPolygon(hullOuterX, hullOuterY, 5);

        // Main Safety Orange Deck Hull
        gc.setFill(Color.web("#ea580c"));
        double[] deckX = { -bw / 2 + 8, bw / 4 - 2, bw / 2 - 5, bw / 4 - 2, -bw / 2 + 8 };
        double[] deckY = { -bh / 2 + 5, -bh / 2 + 5, 0, bh / 2 - 5, bh / 2 - 5 };
        gc.fillPolygon(deckX, deckY, 5);

        // Perimeter Life-Line Safety Grab Ropes along gunwales
        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(2);
        gc.strokeLine(-bw / 2 + 10, -bh / 2 + 4, bw / 4, -bh / 2 + 4);
        gc.strokeLine(-bw / 2 + 10, bh / 2 - 4, bw / 4, bh / 2 - 4);
        gc.strokeLine(bw / 4, -bh / 2 + 4, bw / 2 - 4, 0);
        gc.strokeLine(bw / 4, bh / 2 - 4, bw / 2 - 4, 0);

        // 5. Bow Deck Equipment
        // Coiled Yellow Tow Ropes
        gc.setStroke(Color.web("#eab308"));
        gc.setLineWidth(2.5);
        gc.strokeOval(bw / 4 + 4, -10, 12, 6);
        gc.strokeOval(bw / 4 + 4, 4, 12, 6);

        // Bow Lifebuoy Ring (Safety Orange with White Reflectors)
        gc.setFill(Color.web("#ea580c"));
        gc.fillOval(bw / 4 + 8, -6, 12, 12);
        gc.setFill(Color.web("#0f172a"));
        gc.fillOval(bw / 4 + 11, -3, 6, 6);
        gc.setStroke(Color.web("#ffffff"));
        gc.setLineWidth(1.5);
        gc.strokeOval(bw / 4 + 9, -5, 10, 10);

        // Bow Tow Mooring Post
        gc.setFill(Color.web("#475569"));
        gc.fillOval(bw / 2 - 9, -3.5, 7, 7);

        // 6. Cockpit Console & Curved Tinted Windshield
        double consoleX = bw / 8 - 4;
        gc.setFill(Color.web("#0369a1"));
        gc.fillRoundRect(consoleX, -bh / 2 + 9, 8, bh - 18, 4, 4);

        // Curved Windshield Glass (Tinted Cyan/Sky Blue)
        gc.setFill(Color.web("#38bdf8", 0.75));
        double[] glassX = { consoleX + 2, consoleX + 9, consoleX + 9, consoleX + 2 };
        double[] glassY = { -bh / 2 + 11, -bh / 2 + 15, bh / 2 - 15, bh / 2 - 11 };
        gc.fillPolygon(glassX, glassY, 4);

        // Pilot P1 at Helm Console
        gc.setFill(Color.web("#38bdf8"));
        gc.fillOval(consoleX - 6, -5, 10, 10);
        gc.setFill(Color.web("#facc15"));
        gc.fillOval(consoleX - 8, -7, 12, 6); // captain's cap

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("P1", consoleX - 10, -bh / 2 + 4);

        // 7. Weatherproof Bimini Rain Canopy Shelter (Navy Blue)
        double canopyStartX = -bw / 4;
        double canopyW = bw / 3;
        double canopyH = bh - 14;
        double canopyY = -canopyH / 2;

        // Tubular Stainless Steel Canopy Frame
        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(2.5);
        gc.strokeRect(canopyStartX, canopyY, canopyW, canopyH);

        // Weatherproof Blue Bimini Fabric Cover
        gc.setFill(Color.web("#1e40af", 0.88));
        gc.fillRoundRect(canopyStartX + 2, canopyY + 2, canopyW - 4, canopyH - 4, 6, 6);
        gc.setStroke(Color.web("#3b82f6", 0.7));
        gc.setLineWidth(1.5);
        gc.strokeLine(canopyStartX + canopyW / 2, canopyY + 2, canopyStartX + canopyW / 2, canopyY + canopyH - 2);

        // 8. Passenger Rescue Seating & Rescued Students under Canopy
        int maxCapacity = boat.getMaxCargoCapacity();
        double seatSlotW = (canopyW - 6) / maxCapacity;

        for (int i = 0; i < maxCapacity; i++) {
            double seatX = canopyStartX + 3 + (i * seatSlotW);
            boolean isTopRow = (i % 2 == 0);
            double seatY = isTopRow ? (canopyY + 4) : (canopyY + canopyH - 12);

            // Empty Seat
            gc.setStroke(Color.web("#ffffff", 0.45));
            gc.setLineWidth(1);
            gc.strokeRect(seatX, seatY, seatSlotW - 1.5, 8);

            // Rescued Student / Supply Box Sitting under Canopy
            if (i < boat.getCargoSlots().size()) {
                Boat.CargoType cType = boat.getCargoSlots().get(i);
                if (cType == Boat.CargoType.SURVIVOR) {
                    // Safety Orange Life Jacket
                    gc.setFill(Color.web("#ea580c"));
                    gc.fillRoundRect(seatX + 0.5, seatY, seatSlotW - 2, 8, 2, 2);

                    // Student Head
                    gc.setFill(Color.web("#fed7aa"));
                    gc.fillOval(seatX + seatSlotW / 2 - 2.5, seatY + 1.5, 5, 5);
                    gc.setFill(Color.web("#0f172a"));
                    gc.fillOval(seatX + seatSlotW / 2 - 2.5, seatY + 0.5, 5, 2);
                } else if (cType == Boat.CargoType.NAILS) {
                    // Nails Crate in Seat
                    gc.setFill(Color.web("#b45309"));
                    gc.fillRoundRect(seatX + 0.5, seatY, seatSlotW - 2, 8, 2, 2);
                    gc.setStroke(Color.web("#fef08a"));
                    gc.setLineWidth(1);
                    gc.strokeLine(seatX + 2, seatY + 2, seatX + seatSlotW - 3, seatY + 6);
                } else if (cType == Boat.CargoType.METAL) {
                    // Metal Parts in Seat
                    gc.setFill(Color.web("#475569"));
                    gc.fillRoundRect(seatX + 0.5, seatY, seatSlotW - 2, 8, 2, 2);
                    gc.setFill(Color.web("#cbd5e1"));
                    gc.fillRect(seatX + 2, seatY + 2, seatSlotW - 4, 4);
                }
            }
        }

        // 9. Side Deployable Rescue Boarding Ladder (Port Side)
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(-bw / 6, -bh / 2 - 4, 16, 5);
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(1.5);
        gc.strokeLine(-bw / 6 + 4, -bh / 2 - 4, -bw / 6 + 4, -bh / 2 + 1);
        gc.strokeLine(-bw / 6 + 8, -bh / 2 - 4, -bw / 6 + 8, -bh / 2 + 1);
        gc.strokeLine(-bw / 6 + 12, -bh / 2 - 4, -bw / 6 + 12, -bh / 2 + 1);

        // 10. Stern Roll-Bar Arch & Quad LED Search Floodlights
        double rollBarX = -bw / 2 + 12;
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(3);
        gc.strokeLine(rollBarX, -bh / 2 + 6, rollBarX, bh / 2 - 6);

        // Quad LED Search Lights
        gc.setFill(Color.web("#fef08a"));
        gc.fillOval(rollBarX - 3, -bh / 2 + 8, 6, 6);
        gc.fillOval(rollBarX - 3, -8, 6, 6);
        gc.fillOval(rollBarX - 3, 2, 6, 6);
        gc.fillOval(rollBarX - 3, bh / 2 - 14, 6, 6);

        gc.restore();
    }

    private void drawOutboardEngine(double x, double y, double w, double h) {
        // Engine Cowling
        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(x, y, w, h, 4, 4);

        // Brand Accent Stripe
        gc.setFill(Color.web("#ea580c"));
        gc.fillRect(x + 2, y + 2, 3, h - 4);

        // Engine Steering Mount / Bracket
        gc.setFill(Color.web("#64748b"));
        gc.fillRect(x + w - 4, y + h / 2 - 2, 5, 4);
    }

    private void drawRoverHeadlights(double bw, double bh) {
        // Translucent Forward Light Cones penetrating floodwaters
        double lightLength = 160;
        double frontX = bw / 2 - 2;

        // Port (Left) Headlight Beam
        gc.setFill(Color.web("#fef08a", 0.18));
        double[] xPointsLeft = { frontX, frontX + lightLength, frontX + lightLength };
        double[] yPointsLeft = { -bh / 2 + 10, -bh / 2 - 35, -bh / 2 + 35 };
        gc.fillPolygon(xPointsLeft, yPointsLeft, 3);

        // Starboard (Right) Headlight Beam
        double[] xPointsRight = { frontX, frontX + lightLength, frontX + lightLength };
        double[] yPointsRight = { bh / 2 - 10, bh / 2 - 35, bh / 2 + 35 };
        gc.fillPolygon(xPointsRight, yPointsRight, 3);

        // Headlight Glow Orbs
        gc.setFill(Color.web("#ffffff", 0.95));
        gc.fillOval(frontX - 4, -bh / 2 + 7, 7, 7);
        gc.fillOval(frontX - 4, bh / 2 - 14, 7, 7);
    }

    private void drawRoverSixWheels(double bw, double bh) {
        // 3 Wheels on Left/Port side (y = -bh/2 - 6), 3 Wheels on Right/Starboard side (y = bh/2 - 6)
        double wheelLength = 26;
        double wheelThickness = 12;
        double[] wheelXOffsets = { bw / 2 - 24, 0, -bw / 2 + 24 };

        for (double wx : wheelXOffsets) {
            // Upper Wheel (Port)
            drawSingleWheel(wx - wheelLength / 2, -bh / 2 - 6, wheelLength, wheelThickness);
            // Lower Wheel (Starboard)
            drawSingleWheel(wx - wheelLength / 2, bh / 2 - 6, wheelLength, wheelThickness);
        }
    }

    private void drawSingleWheel(double x, double y, double w, double h) {
        // Heavy Rubber Tire
        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(x, y, w, h, 6, 6);

        // Deep Tread Notches
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(1.5);
        for (double tx = x + 3; tx < x + w - 2; tx += 4.5) {
            gc.strokeLine(tx, y + 1, tx, y + h - 1);
        }

        // Metallic Rim / Hubcap
        gc.setFill(Color.web("#64748b"));
        gc.fillRoundRect(x + 5, y + 2.5, w - 10, h - 5, 3, 3);
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillOval(x + w / 2 - 2, y + h / 2 - 2, 4, 4);
    }

    private void drawRoverHullAndChassis(double bw, double bh) {
        // Underbody Chassis / Skid Plate
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(-bw / 2 - 2, -bh / 2 + 2, bw + 4, bh - 4, 12, 12);

        // Armored Waterproof Hull (Olive Green)
        gc.setFill(Color.web("#365314"));
        gc.fillRoundRect(-bw / 2, -bh / 2 + 5, bw, bh - 10, 10, 10);

        // High-Visibility Rescue Orange Accent Flank Stripes
        gc.setFill(Color.web("#ea580c"));
        gc.fillRect(-bw / 2 + 4, -bh / 2 + 7, bw - 8, 4);
        gc.fillRect(-bw / 2 + 4, bh / 2 - 11, bw - 8, 4);

        // Protective Mudguard Flares above Wheels
        gc.setFill(Color.web("#020617"));
        gc.fillRect(-bw / 2 + 10, -bh / 2 + 3, bw - 20, 3);
        gc.fillRect(-bw / 2 + 10, bh / 2 - 6, bw - 20, 3);

        // Hull Seam Rivets
        gc.setFill(Color.web("#94a3b8"));
        for (double rx = -bw / 2 + 12; rx < bw / 2 - 10; rx += 14) {
            gc.fillOval(rx, -bh / 2 + 6, 2, 2);
            gc.fillOval(rx, bh / 2 - 8, 2, 2);
        }
    }

    private void drawRoverBullbarAndWinch(double bw, double bh) {
        double frontX = bw / 2;

        // Heavy-Duty Reinforced Steel Bullbar
        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(4);
        gc.strokeRoundRect(frontX - 6, -bh / 2 + 8, 10, bh - 16, 6, 6);

        gc.setStroke(Color.web("#64748b"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(frontX - 6, -bh / 2 + 8, 10, bh - 16, 6, 6);

        // Centered Electric Steel Winch Housing
        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(frontX - 4, -10, 8, 20, 4, 4);

        // Coiled Steel Winch Cable Drum
        gc.setFill(Color.web("#94a3b8"));
        gc.fillRect(frontX - 2, -7, 4, 14);
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(1);
        gc.strokeLine(frontX, -7, frontX, 7);

        // Front Recovery Tow Hook
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(2.5);
        gc.strokeOval(frontX + 4, -3.5, 7, 7);
    }

    private void drawRoverCabAndLightbar(double bw, double bh) {
        double cabStartX = 2;
        double cabEndX = bw / 2 - 6;
        double cabW = cabEndX - cabStartX;
        double cabH = bh - 16;
        double cabY = -cabH / 2;

        // Front Hood & Engine Bay (Olive Drab)
        gc.setFill(Color.web("#3f6212"));
        gc.fillRoundRect(cabStartX + 12, cabY + 2, cabW - 14, cabH - 4, 6, 6);

        // Engine Hood Vents
        gc.setFill(Color.web("#142504"));
        gc.fillRect(cabStartX + 20, -5, 12, 10);
        gc.setStroke(Color.web("#4d7c0f"));
        gc.setLineWidth(1);
        gc.strokeLine(cabStartX + 24, -5, cabStartX + 24, 5);
        gc.strokeLine(cabStartX + 28, -5, cabStartX + 28, 5);

        // Armored Cab Windshield Glass (Tinted Cyan)
        gc.setFill(Color.web("#0369a1"));
        gc.fillRoundRect(cabStartX + 6, cabY + 4, 9, cabH - 8, 3, 3);
        gc.setFill(Color.web("#38bdf8", 0.65));
        gc.fillRect(cabStartX + 8, cabY + 6, 3, cabH - 12);

        // Cab Roof
        gc.setFill(Color.web("#2e4a0e"));
        gc.fillRoundRect(cabStartX, cabY + 2, 8, cabH - 4, 4, 4);

        // Pilot P1 Driver Visible in Cab
        gc.setFill(Color.web("#38bdf8"));
        gc.fillOval(cabStartX + 4, -5, 10, 10);
        gc.setFill(Color.web("#facc15"));
        gc.fillOval(cabStartX + 2, -7, 12, 6);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("P1", cabStartX - 2, -cabH / 2 - 2);

        // Snorkel Air Intake on Flank
        gc.setFill(Color.web("#020617"));
        gc.fillRoundRect(cabStartX + 8, cabY - 3, 10, 4, 2, 2);

        // Roof-Mounted Flashing Emergency Lightbar (Red / Amber Strobes)
        double lightbarX = cabStartX - 1;
        double lightbarY = -12;
        double lightbarW = 6;
        double lightbarH = 24;

        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(lightbarX - 1, lightbarY - 1, lightbarW + 2, lightbarH + 2, 3, 3);

        boolean strobeRed = ((int)(animTimer * 10) % 2 == 0);
        gc.setFill(strobeRed ? Color.web("#ff0044", 0.95) : Color.web("#880022", 0.8));
        gc.fillRect(lightbarX, lightbarY + 1, lightbarW, lightbarH / 2 - 2);

        gc.setFill(!strobeRed ? Color.web("#facc15", 0.95) : Color.web("#785203", 0.8));
        gc.fillRect(lightbarX, lightbarY + lightbarH / 2 + 1, lightbarW, lightbarH / 2 - 2);

        // Emergency Lightbar Glow Aura
        gc.setFill(strobeRed ? Color.web("#ff0044", 0.25) : Color.web("#facc15", 0.25));
        gc.fillOval(lightbarX - 8, lightbarY - 6, lightbarW + 16, lightbarH + 12);
    }

    private void drawRoverRescueEquipment(double bw, double bh) {
        // Coiled Rescue Rope on Port Flank (Upper Side)
        gc.setStroke(Color.web("#ca8a04"));
        gc.setLineWidth(2.5);
        gc.strokeOval(-bw / 6, -bh / 2 + 7, 18, 6);
        gc.setStroke(Color.web("#eab308"));
        gc.setLineWidth(1.5);
        gc.strokeOval(-bw / 6 + 3, -bh / 2 + 8, 12, 4);

        // Orange Lifebuoy Rings on Starboard Flank (Lower Side)
        gc.setFill(Color.web("#ea580c"));
        gc.fillOval(-bw / 6, bh / 2 - 13, 11, 8);
        gc.fillOval(-bw / 6 + 13, bh / 2 - 13, 11, 8);
        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(-bw / 6 + 3, bh / 2 - 11, 5, 4);
        gc.fillOval(-bw / 6 + 16, bh / 2 - 11, 5, 4);

        // Medical First-Aid Kit Box on Rear
        gc.setFill(Color.web("#dc2626"));
        gc.fillRoundRect(-bw / 2 + 2, -6, 6, 12, 2, 2);
        gc.setStroke(Color.web("#ffffff"));
        gc.setLineWidth(1.5);
        gc.strokeLine(-bw / 2 + 3.5, -3, -bw / 2 + 6.5, -3);
        gc.strokeLine(-bw / 2 + 5, -4.5, -bw / 2 + 5, -1.5);
    }

    private void drawRoverRescueDeckAndPassengers(Boat boat, double bw, double bh) {
        double deckStartX = -bw / 2 + 8;
        double deckEndX = 2;
        double deckW = deckEndX - deckStartX;
        double deckH = bh - 18;
        double deckY = -deckH / 2;

        // Diamond-Plate Steel Rescue Floor Bed
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(deckStartX, deckY, deckW, deckH, 6, 6);

        // Non-Slip Floor Tread Mesh Pattern
        gc.setStroke(Color.web("#334155", 0.7));
        gc.setLineWidth(1);
        for (double x = deckStartX + 5; x < deckEndX; x += 8) {
            gc.strokeLine(x, deckY + 2, x, deckY + deckH - 2);
        }

        // Protective Tubular Roll-Cage Rails
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(2.5);
        // Top Rail
        gc.strokeLine(deckStartX, deckY, deckEndX, deckY);
        // Bottom Rail
        gc.strokeLine(deckStartX, deckY + deckH, deckEndX, deckY + deckH);
        // Rear Rail
        gc.strokeLine(deckStartX, deckY, deckStartX, deckY + deckH);

        // Safety Rail Uprights
        gc.setLineWidth(2);
        for (double rx = deckStartX + 10; rx < deckEndX; rx += 14) {
            gc.strokeLine(rx, deckY - 1, rx, deckY + 3);
            gc.strokeLine(rx, deckY + deckH - 3, rx, deckY + deckH + 1);
        }

        // Passenger Seating Benches
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(deckStartX + 4, deckY + 3, deckW - 8, 8); // Port Bench
        gc.fillRect(deckStartX + 4, deckY + deckH - 11, deckW - 8, 8); // Starboard Bench

        // Visual Slots for Rescued Students
        int maxCapacity = boat.getMaxCargoCapacity();
        double slotWidth = (deckW - 8) / maxCapacity;

        for (int i = 0; i < maxCapacity; i++) {
            double slotX = deckStartX + 4 + (i * slotWidth);
            boolean isTopBench = (i % 2 == 0);
            double slotY = isTopBench ? (deckY + 3) : (deckY + deckH - 11);

            // Empty Seat Indicator
            gc.setStroke(Color.web("#475569", 0.6));
            gc.setLineWidth(1);
            gc.strokeRect(slotX + 1, slotY, slotWidth - 2, 8);

            // Seated Rescued Student / Supply Box with Safety Life Jacket
            if (i < boat.getCargoSlots().size()) {
                Boat.CargoType cType = boat.getCargoSlots().get(i);
                if (cType == Boat.CargoType.SURVIVOR) {
                    // Safety Orange Life Vest
                    gc.setFill(Color.web("#ea580c"));
                    gc.fillRoundRect(slotX + 1.5, slotY, slotWidth - 3, 8, 3, 3);

                    // Student Head & Hair
                    gc.setFill(Color.web("#fed7aa"));
                    gc.fillOval(slotX + slotWidth / 2 - 3, slotY + 1.5, 6, 5);

                    gc.setFill(Color.web("#1e293b"));
                    gc.fillOval(slotX + slotWidth / 2 - 3, slotY + 0.5, 6, 2.5); // hair
                } else if (cType == Boat.CargoType.NAILS) {
                    // Nails Crate on Deck
                    gc.setFill(Color.web("#b45309"));
                    gc.fillRoundRect(slotX + 1, slotY, slotWidth - 2, 8, 2, 2);
                    gc.setStroke(Color.web("#fef08a"));
                    gc.setLineWidth(1);
                    gc.strokeLine(slotX + 2, slotY + 2, slotX + slotWidth - 3, slotY + 6);
                } else if (cType == Boat.CargoType.METAL) {
                    // Metal Parts on Deck
                    gc.setFill(Color.web("#475569"));
                    gc.fillRoundRect(slotX + 1, slotY, slotWidth - 2, 8, 2, 2);
                    gc.setFill(Color.web("#cbd5e1"));
                    gc.fillRect(slotX + 2, slotY + 2, slotWidth - 4, 4);
                }
            }
        }
    }

    private void drawCrew(Crew crew, Boat boat) {
        double worldX = crew.getWorldX(boat);
        double worldY = crew.getWorldY(boat);

        gc.setStroke(Color.web("#ff0055", 0.5));
        gc.setLineWidth(2);
        gc.setLineDashes(5);
        gc.strokeOval(worldX - crew.getInteractionRadius(), worldY - crew.getInteractionRadius(), crew.getInteractionRadius() * 2, crew.getInteractionRadius() * 2);
        gc.setLineDashes(null);

        gc.setFill(Color.web("#ff0055"));
        gc.fillOval(worldX - 13, worldY - 13, 26, 26);
        gc.setFill(Color.web("#eab308"));
        gc.fillOval(worldX - 16, worldY - 16, 32, 12);
        gc.setStroke(Color.web("#020617"));
        gc.setLineWidth(1.5);
        gc.strokeOval(worldX - 6, worldY - 8, 6, 6);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#ff0055"));
        gc.fillText("P2 (CREW)", worldX - 26, worldY - 20);
    }

    private void drawSurvivor(Survivor s, boolean isLowTimer) {
        if (s.isRooftopTrapped()) {
            // Rooftop Trapped Survivor (2D Top-Down View: Walking Legs, Torso, Head, Waving Hands)
            double rx = s.getX();
            double ry = s.getY();

            // 1. Top-Down Walking Legs Animation (Alternating Left & Right Footsteps)
            double walkCycle = animTimer * 10;
            double leftFootY = Math.sin(walkCycle) * 4.5;
            double rightFootY = -leftFootY;

            // Shoes / Feet stepping forward and backward in top-down view
            gc.setFill(Color.web("#0f172a")); // Dark Shoes
            gc.fillRoundRect(rx - 7, ry + 2 + leftFootY, 5, 8, 2.5, 2.5);  // Left Foot
            gc.fillRoundRect(rx + 2, ry + 2 + rightFootY, 5, 8, 2.5, 2.5); // Right Foot

            // 2. Torso / Shoulders (Top-Down Bird's-Eye View)
            gc.setFill(Color.web("#2563eb")); // Blue Student Shirt/Jacket
            gc.fillOval(rx - 11, ry - 7, 22, 14);
            gc.setStroke(Color.web("#1e40af"));
            gc.setLineWidth(1.2);
            gc.strokeOval(rx - 11, ry - 7, 22, 14);

            // 3. Waving Arms & Hands (Calling for rescue from above)
            double wavePhase = animTimer * 7;
            double waveX = Math.cos(wavePhase) * 5;
            double waveY = Math.sin(wavePhase) * 4;

            gc.setStroke(Color.web("#fed7aa"));
            gc.setLineWidth(2.5);
            // Left Arm
            gc.strokeLine(rx - 9, ry - 2, rx - 16 - waveX, ry - 10 + waveY);
            // Right Arm
            gc.strokeLine(rx + 9, ry - 2, rx + 16 + waveX, ry - 10 - waveY);

            // Hands / Palms
            gc.setFill(Color.web("#fed7aa"));
            gc.fillOval(rx - 18 - waveX, ry - 12 + waveY, 5, 5);
            gc.fillOval(rx + 14 + waveX, ry - 12 - waveY, 5, 5);

            // 4. Head & Hair (Directly centered above shoulders in top-down view)
            gc.setFill(Color.web("#fed7aa"));
            gc.fillOval(rx - 6, ry - 6, 12, 12);

            gc.setFill(Color.web("#1e293b")); // Dark Hair viewed from directly above
            gc.fillOval(rx - 5.5, ry - 6.5, 11, 9);

            // 5. Rooftop Name Label
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText(s.getName(), rx - 35, ry - 22);

            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            gc.setFill(Color.web("#facc15"));
            gc.fillText("AIRLIFT ONLY", rx - 30, ry + 22);

        } else {
            // Standard Floodwater Survivor (Bobbing in water with lifebuoy)
            double sy = s.getY() + s.getBobbingOffset();

            if (isLowTimer) {
                gc.setStroke(Color.web("#facc15", 0.8 + Math.sin(animTimer * 5) * 0.2));
                gc.setLineWidth(2.5);
                gc.strokeOval(s.getX() - 25, sy - 25, 50, 50);
            }

            gc.setFill(Color.web("#ff4500"));
            gc.fillOval(s.getX() - 20, sy - 20, 40, 40);
            gc.setFill(Color.web("#0f172a"));
            gc.fillOval(s.getX() - 11, sy - 11, 22, 22);

            gc.setFill(Color.web("#ffdbac"));
            gc.fillOval(s.getX() - 8, sy - 8, 16, 16);

            double waveArm = Math.sin(animTimer * 6) * 6;
            gc.setStroke(Color.web("#ffdbac"));
            gc.setLineWidth(2.5);
            gc.strokeLine(s.getX() - 8, sy, s.getX() - 14, sy - 10 + waveArm);
            gc.strokeLine(s.getX() + 8, sy, s.getX() + 14, sy - 10 - waveArm);

            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText(s.getName(), s.getX() - 22, sy - 24);

            if (isLowTimer) {
                gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                gc.setFill(Color.web("#facc15"));
                gc.fillText("🏊 SWIMMING!", s.getX() - 32, sy + 32);
            }
        }
    }

    private void drawObstacle(Obstacle obs) {
        gc.save();
        if ("SUBMERGED_CAR".equals(obs.getType())) {
            gc.setFill(Color.web("#0369a1"));
            gc.fillRoundRect(obs.getX() - obs.getRadius(), obs.getY() - obs.getRadius() / 2, obs.getRadius() * 2, obs.getRadius(), 12, 12);
            gc.setFill(Color.web("#38bdf8", 0.7));
            gc.fillRect(obs.getX() - 10, obs.getY() - 10, 20, 20);

        } else if ("ROAD_BARRIER".equals(obs.getType())) {
            gc.setFill(Color.web("#ea580c"));
            gc.fillRect(obs.getX() - obs.getRadius(), obs.getY() - obs.getRadius() / 2, obs.getRadius() * 2, obs.getRadius());
            gc.setStroke(Color.web("#fef08a"));
            gc.setLineWidth(3);
            gc.strokeRect(obs.getX() - obs.getRadius(), obs.getY() - obs.getRadius() / 2, obs.getRadius() * 2, obs.getRadius());

        } else {
            gc.setFill(Color.web("#78350f"));
            gc.fillRect(obs.getX() - obs.getRadius(), obs.getY() - obs.getRadius() / 2, obs.getRadius() * 2, obs.getRadius());
        }

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillText("🚧 " + obs.getType().replace("_", " "), obs.getX() - 40, obs.getY() + obs.getRadius() + 16);
        gc.restore();
    }

    private void drawCreature(Creature c) {
        if (c.getRetreatTimer() > 0) return;

        gc.save();
        gc.setFill(Color.web("#15803d"));
        gc.fillOval(c.getX() - c.getRadius(), c.getY() - c.getRadius(), c.getRadius() * 2, c.getRadius() * 2);

        gc.setFill(Color.web("#facc15"));
        gc.fillOval(c.getX() - 15, c.getY() - 12, 10, 10);
        gc.fillOval(c.getX() + 5, c.getY() - 12, 10, 10);
        gc.setFill(Color.web("#020617"));
        gc.fillOval(c.getX() - 12, c.getY() - 10, 4, 4);
        gc.fillOval(c.getX() + 8, c.getY() - 10, 4, 4);

        if (c.getDangerLevel() > 0) {
            gc.setStroke(Color.web("#ff0055", c.getDangerLevel()));
            gc.setLineWidth(3.5);
            gc.strokeOval(c.getX() - 65, c.getY() - 65, 130, 130);
        }

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#ff0055"));
        gc.fillText("🐊 " + c.getName(), c.getX() - 45, c.getY() - c.getRadius() - 10);
        gc.restore();
    }

    private void drawUsefulObject(UsefulObject u) {
        gc.save();
        gc.setFill(Color.web("#facc15", 0.35));
        gc.fillOval(u.getX() - 28, u.getY() - 28, 56, 56);

        gc.setFill(Color.web("#eab308"));
        gc.fillRect(u.getX() - 18, u.getY() - 16, 36, 32);
        gc.setStroke(Color.web("#ffffff"));
        gc.setLineWidth(2);
        gc.strokeRect(u.getX() - 18, u.getY() - 16, 36, 32);

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

    private void drawSupplyBoxes(List<SupplyBox> boxes) {
        if (boxes == null) return;
        for (SupplyBox box : boxes) {
            if (!box.isCollected()) {
                drawSingleSupplyBox(box);
            }
        }
    }

    private void drawSingleSupplyBox(SupplyBox box) {
        gc.save();
        double bx = box.getX();
        double by = box.getY() + Math.sin(box.getBobbingPhase() + animTimer * 2.5) * 4.0;
        boolean isNails = (box.getType() == SupplyBox.BoxType.NAILS);

        // 1. Water Ripple Effect below floating crate
        gc.setStroke(Color.web("#38bdf8", 0.45));
        gc.setLineWidth(1.8);
        gc.strokeOval(bx - 20, by + 12, 40, 14);
        gc.setStroke(Color.web("#ffffff", 0.25));
        gc.strokeOval(bx - 14, by + 10, 28, 10);

        // 2. Drop Shadow
        gc.setFill(Color.web("#020617", 0.4));
        gc.fillOval(bx - 16, by + 8, 32, 12);

        if (isNails) {
            // === WOODEN CRATE WITH CROSSED NAILS LOGO ===
            // Crate Body
            gc.setFill(Color.web("#b45309")); // Rich Wood Brown
            gc.fillRoundRect(bx - 16, by - 16, 32, 32, 6, 6);

            // Wood Planks & Grain lines
            gc.setFill(Color.web("#d97706"));
            gc.fillRect(bx - 14, by - 14, 28, 8);
            gc.fillRect(bx - 14, by - 4, 28, 8);
            gc.fillRect(bx - 14, by + 6, 28, 8);

            // Metal Reinforcement Brackets & Screws on Corners
            gc.setFill(Color.web("#475569"));
            gc.fillRect(bx - 16, by - 16, 6, 6);
            gc.fillRect(bx + 10, by - 16, 6, 6);
            gc.fillRect(bx - 16, by + 10, 6, 6);
            gc.fillRect(bx + 10, by + 10, 6, 6);

            // Silver Corner Studs
            gc.setFill(Color.web("#cbd5e1"));
            gc.fillOval(bx - 14, by - 14, 2.5, 2.5);
            gc.fillOval(bx + 11.5, by - 14, 2.5, 2.5);
            gc.fillOval(bx - 14, by + 11.5, 2.5, 2.5);
            gc.fillOval(bx + 11.5, by + 11.5, 2.5, 2.5);

            // Crate Border
            gc.setStroke(Color.web("#78350f"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(bx - 16, by - 16, 32, 32, 6, 6);

            // Center Nails Logo (Crossed Heavy Steel Nails with Heads)
            gc.setStroke(Color.web("#f8fafc"));
            gc.setLineWidth(2.5);
            // Nail 1
            gc.strokeLine(bx - 7, by - 7, bx + 7, by + 7);
            // Nail 1 Head
            gc.strokeLine(bx - 9, by - 5, bx - 5, by - 9);
            // Nail 2
            gc.strokeLine(bx + 7, by - 7, bx - 7, by + 7);
            // Nail 2 Head
            gc.strokeLine(bx + 9, by - 5, bx + 5, by - 9);

            // Label Badge
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.setFill(Color.web("#fef08a"));
            gc.fillText("🔩 NAILS (1x)", bx - 32, by - 22);

        } else {
            // === INDUSTRIAL REINFORCED STEEL CRATE WITH METAL LOGO ===
            // Dark Industrial Steel Base
            gc.setFill(Color.web("#334155"));
            gc.fillRoundRect(bx - 16, by - 16, 32, 32, 6, 6);

            // High-Tech Metal Armor Inset Plate
            gc.setFill(Color.web("#475569"));
            gc.fillRoundRect(bx - 13, by - 13, 26, 26, 4, 4);

            // Diamond-Plate Texture Lines
            gc.setStroke(Color.web("#64748b", 0.7));
            gc.setLineWidth(1);
            gc.strokeLine(bx - 10, by - 10, bx + 10, by + 10);
            gc.strokeLine(bx + 10, by - 10, bx - 10, by + 10);

            // Corner High-Vis Hazard Accent Brackets
            gc.setFill(Color.web("#0284c7"));
            gc.fillRect(bx - 16, by - 16, 5, 5);
            gc.fillRect(bx + 11, by - 16, 5, 5);
            gc.fillRect(bx - 16, by + 11, 5, 5);
            gc.fillRect(bx + 11, by + 11, 5, 5);

            // Steel Crate Border
            gc.setStroke(Color.web("#38bdf8"));
            gc.setLineWidth(1.8);
            gc.strokeRoundRect(bx - 16, by - 16, 32, 32, 6, 6);

            // Center Metal Ingot / Gear Logo
            gc.setFill(Color.web("#cbd5e1"));
            gc.fillRoundRect(bx - 8, by - 5, 16, 10, 2, 2);
            gc.setFill(Color.web("#f8fafc"));
            gc.fillPolygon(new double[]{bx - 8, bx - 4, bx + 4, bx}, new double[]{by - 5, by - 8, by - 8, by - 5}, 4);
            gc.setStroke(Color.web("#0284c7"));
            gc.setLineWidth(1);
            gc.strokeRoundRect(bx - 8, by - 5, 16, 10, 2, 2);

            // Label Badge
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.setFill(Color.web("#38bdf8"));
            gc.fillText("⚙️ METAL (1x)", bx - 34, by - 22);
        }

        gc.restore();
    }

    private void drawWorkshopCraftingScreen(GameEngine engine, double w, double h) {
        // 1. Translucent Dimmer Backdrop Overlay
        gc.setFill(Color.web("#020617", 0.94));
        gc.fillRect(0, 0, w, h);

        // Tech Blueprint Grid Mesh
        gc.setStroke(Color.web("#1e293b", 0.6));
        gc.setLineWidth(1);
        for (double gx = 0; gx < w; gx += 40) {
            gc.strokeLine(gx, 0, gx, h);
        }
        for (double gy = 0; gy < h; gy += 40) {
            gc.strokeLine(0, gy, w, gy);
        }

        // 2. Header Title Banner
        gc.setFill(Color.web("#0f172a", 0.95));
        gc.fillRoundRect(80, 25, w - 160, 105, 16, 16);
        gc.setStroke(Color.web("#0284c7", 0.8));
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(80, 25, w - 160, 105, 16, 16);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        gc.setFill(Color.web("#38bdf8"));
        gc.fillText("🏭 IUT EMERGENCY RESCUE WORKSHOP — FABRICATION TERMINAL", 100, 62);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        gc.setFill(Color.web("#94a3b8"));
        gc.fillText("Collect Nails 🔩 and Metal ⚙️ from flooded avenues to fabricate & deploy heavy rescue vehicles.", 100, 86);

        // Depot Stockpile Counter
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("DEPOT STOCKPILE: 🔩 " + engine.getWorkshopNails() + " NAILS", 100, 114);
        gc.setFill(Color.web("#38bdf8"));
        gc.fillText("⚙️ " + engine.getWorkshopMetal() + " METAL PARTS", 360, 114);

        // Close Button [ESC]
        gc.setFill(Color.web("#dc2626", 0.9));
        gc.fillRoundRect(880, 50, 110, 48, 10, 10);
        gc.setStroke(Color.web("#f87171"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(880, 50, 110, 48, 10, 10);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText("✕ CLOSE [ESC]", 890, 80);

        int curNails = engine.getWorkshopNails();
        int curMetal = engine.getWorkshopMetal();
        Boat activeBoat = engine.getBoat();

        // 3. Left Card: 6x6 Land Buggy (Amphibious Rover) - bounds mx: 100..520, my: 160..560
        double card1X = 100;
        double card1Y = 150;
        double card1W = 420;
        double card1H = 405;

        boolean isRoverCurrent = (activeBoat.getVehicleType() == Boat.VehicleType.ROVER);
        boolean roverCanCraft = (!engine.isRoverUnlocked() && curNails >= 5 && curMetal >= 5);

        gc.setFill(Color.web("#0f172a", 0.96));
        gc.fillRoundRect(card1X, card1Y, card1W, card1H, 16, 16);
        gc.setStroke(isRoverCurrent ? Color.web("#38bdf8") : (engine.isRoverUnlocked() || roverCanCraft ? Color.web("#22c55e") : Color.web("#475569")));
        gc.setLineWidth(isRoverCurrent ? 3 : 2);
        gc.strokeRoundRect(card1X, card1Y, card1W, card1H, 16, 16);

        // Card 1 Title & Description
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("🚗 6×6 LAND ROVER BUGGY", card1X + 18, card1Y + 32);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#4ade80"));
        gc.fillText("🌲 Traverses Dense Forest (OIC Tree Park) & Muddy Terrain", card1X + 18, card1Y + 52);

        // Card 1 Preview Box (Side View Canvas)
        double prev1X = card1X + 15;
        double prev1Y = card1Y + 62;
        double prev1W = card1W - 30;
        double prev1H = 135;

        gc.setFill(Color.web("#090d16"));
        gc.fillRoundRect(prev1X, prev1Y, prev1W, prev1H, 10, 10);
        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(prev1X, prev1Y, prev1W, prev1H, 10, 10);

        // Draw Side View of 6x6 Land Buggy
        drawRoverSideView(gc, prev1X + prev1W / 2, prev1Y + prev1H / 2, prev1W, prev1H);

        // Card 1 Material Requirements Progress Bars
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillText("FABRICATION BLUEPRINT REQUIREMENTS:", card1X + 18, card1Y + 220);

        drawProgressBar(gc, card1X + 18, card1Y + 232, card1W - 36, 24, curNails, 5, "#f59e0b", "🔩 Nails Required:");
        drawProgressBar(gc, card1X + 18, card1Y + 266, card1W - 36, 24, curMetal, 5, "#38bdf8", "⚙️ Metal Required:");

        // Card 1 Action Button (Deploy / Craft)
        double btn1X = card1X + 18;
        double btn1Y = card1Y + 310;
        double btn1W = card1W - 36;
        double btn1H = 50;

        if (isRoverCurrent) {
            gc.setFill(Color.web("#0284c7"));
            gc.fillRoundRect(btn1X, btn1Y, btn1W, btn1H, 10, 10);
            gc.setStroke(Color.web("#38bdf8"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(btn1X, btn1Y, btn1W, btn1H, 10, 10);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText("✅ ACTIVE VEHICLE (DEPLOYED)", btn1X + 65, btn1Y + 31);
        } else if (engine.isRoverUnlocked()) {
            gc.setFill(Color.web("#15803d"));
            gc.fillRoundRect(btn1X, btn1Y, btn1W, btn1H, 10, 10);
            gc.setStroke(Color.web("#4ade80"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(btn1X, btn1Y, btn1W, btn1H, 10, 10);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText("🚀 DEPLOY ROVER [PRESS 1]", btn1X + 80, btn1Y + 31);
        } else if (roverCanCraft) {
            boolean pulse = ((int)(animTimer * 6) % 2 == 0);
            gc.setFill(pulse ? Color.web("#d97706") : Color.web("#ca8a04"));
            gc.fillRoundRect(btn1X, btn1Y, btn1W, btn1H, 10, 10);
            gc.setStroke(Color.web("#fef08a"));
            gc.setLineWidth(2.5);
            gc.strokeRoundRect(btn1X, btn1Y, btn1W, btn1H, 10, 10);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText("🔨 FABRICATE & DEPLOY [PRESS 1]", btn1X + 50, btn1Y + 31);
        } else {
            int needN = Math.max(0, 5 - curNails);
            int needM = Math.max(0, 5 - curMetal);
            gc.setFill(Color.web("#334155"));
            gc.fillRoundRect(btn1X, btn1Y, btn1W, btn1H, 10, 10);
            gc.setStroke(Color.web("#64748b"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(btn1X, btn1Y, btn1W, btn1H, 10, 10);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            gc.setFill(Color.web("#fca5a5"));
            gc.fillText("🔒 LOCKED (NEED " + needN + " NAILS & " + needM + " METAL)", btn1X + 40, btn1Y + 30);
        }

        // 4. Right Card: Emergency Rescue Helicopter - bounds mx: 560..980, my: 160..560
        double card2X = 560;
        double card2Y = 150;
        double card2W = 420;
        double card2H = 405;

        boolean isHeliCurrent = (activeBoat.getVehicleType() == Boat.VehicleType.HELICOPTER);
        boolean heliCanCraft = (!engine.isHelicopterUnlocked() && curNails >= 10 && curMetal >= 7);

        gc.setFill(Color.web("#0f172a", 0.96));
        gc.fillRoundRect(card2X, card2Y, card2W, card2H, 16, 16);
        gc.setStroke(isHeliCurrent ? Color.web("#38bdf8") : (engine.isHelicopterUnlocked() || heliCanCraft ? Color.web("#38bdf8") : Color.web("#475569")));
        gc.setLineWidth(isHeliCurrent ? 3 : 2);
        gc.strokeRoundRect(card2X, card2Y, card2W, card2H, 16, 16);

        // Card 2 Title & Description
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        gc.setFill(Color.web("#38bdf8"));
        gc.fillText("🚁 RESCUE HELICOPTER", card2X + 18, card2Y + 32);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#e879f9"));
        gc.fillText("🏢 Flies Over Buildings & Airlifts Trapped Rooftop Students", card2X + 18, card2Y + 52);

        // Card 2 Preview Box (Side View Canvas)
        double prev2X = card2X + 15;
        double prev2Y = card2Y + 62;
        double prev2W = card2W - 30;
        double prev2H = 135;

        gc.setFill(Color.web("#090d16"));
        gc.fillRoundRect(prev2X, prev2Y, prev2W, prev2H, 10, 10);
        gc.setStroke(Color.web("#1e293b"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(prev2X, prev2Y, prev2W, prev2H, 10, 10);

        // Draw Side View of Rescue Helicopter
        drawHelicopterSideView(gc, prev2X + prev2W / 2, prev2Y + prev2H / 2, prev2W, prev2H);

        // Card 2 Material Requirements Progress Bars
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillText("FABRICATION BLUEPRINT REQUIREMENTS:", card2X + 18, card2Y + 220);

        drawProgressBar(gc, card2X + 18, card2Y + 232, card2W - 36, 24, curNails, 10, "#f59e0b", "🔩 Nails Required:");
        drawProgressBar(gc, card2X + 18, card2Y + 266, card2W - 36, 24, curMetal, 7, "#38bdf8", "⚙️ Metal Required:");

        // Card 2 Action Button (Deploy / Craft)
        double btn2X = card2X + 18;
        double btn2Y = card2Y + 310;
        double btn2W = card2W - 36;
        double btn2H = 50;

        if (isHeliCurrent) {
            gc.setFill(Color.web("#0284c7"));
            gc.fillRoundRect(btn2X, btn2Y, btn2W, btn2H, 10, 10);
            gc.setStroke(Color.web("#38bdf8"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(btn2X, btn2Y, btn2W, btn2H, 10, 10);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText("✅ ACTIVE VEHICLE (DEPLOYED)", btn2X + 65, btn2Y + 31);
        } else if (engine.isHelicopterUnlocked()) {
            gc.setFill(Color.web("#0284c7"));
            gc.fillRoundRect(btn2X, btn2Y, btn2W, btn2H, 10, 10);
            gc.setStroke(Color.web("#38bdf8"));
            gc.setLineWidth(2);
            gc.strokeRoundRect(btn2X, btn2Y, btn2W, btn2H, 10, 10);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText("🚀 DEPLOY HELICOPTER [PRESS 2]", btn2X + 65, btn2Y + 31);
        } else if (heliCanCraft) {
            boolean pulse = ((int)(animTimer * 6) % 2 == 0);
            gc.setFill(pulse ? Color.web("#d97706") : Color.web("#ca8a04"));
            gc.fillRoundRect(btn2X, btn2Y, btn2W, btn2H, 10, 10);
            gc.setStroke(Color.web("#fef08a"));
            gc.setLineWidth(2.5);
            gc.strokeRoundRect(btn2X, btn2Y, btn2W, btn2H, 10, 10);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText("🔨 FABRICATE & DEPLOY [PRESS 2]", btn2X + 50, btn2Y + 31);
        } else {
            int needN = Math.max(0, 10 - curNails);
            int needM = Math.max(0, 7 - curMetal);
            gc.setFill(Color.web("#334155"));
            gc.fillRoundRect(btn2X, btn2Y, btn2W, btn2H, 10, 10);
            gc.setStroke(Color.web("#64748b"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(btn2X, btn2Y, btn2W, btn2H, 10, 10);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            gc.setFill(Color.web("#fca5a5"));
            gc.fillText("🔒 LOCKED (NEED " + needN + " NAILS & " + needM + " METAL)", btn2X + 40, btn2Y + 30);
        }

        // 5. Bottom Card: Default Flood Rescue Boat - bounds mx: 300..800, my: 570..640
        double boatBarX = 300;
        double boatBarY = 570;
        double boatBarW = 480;
        double boatBarH = 65;

        boolean isBoatCurrent = (activeBoat.getVehicleType() == Boat.VehicleType.BOAT);

        gc.setFill(Color.web("#0f172a", 0.95));
        gc.fillRoundRect(boatBarX, boatBarY, boatBarW, boatBarH, 14, 14);
        gc.setStroke(isBoatCurrent ? Color.web("#38bdf8") : Color.web("#22c55e"));
        gc.setLineWidth(isBoatCurrent ? 2.5 : 1.5);
        gc.strokeRoundRect(boatBarX, boatBarY, boatBarW, boatBarH, 14, 14);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.setFill(Color.web("#38bdf8"));
        gc.fillText("🚤 FLOOD RESCUE BOAT (DEFAULT VEHICLE)", boatBarX + 18, boatBarY + 28);

        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        gc.setFill(Color.web("#cbd5e1"));
        gc.fillText("Standard water rescue craft for open flood avenues", boatBarX + 18, boatBarY + 48);

        if (isBoatCurrent) {
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            gc.setFill(Color.web("#00ff88"));
            gc.fillText("✅ ACTIVE", boatBarX + boatBarW - 110, boatBarY + 38);
        } else {
            gc.setFill(Color.web("#0284c7"));
            gc.fillRoundRect(boatBarX + boatBarW - 180, boatBarY + 14, 160, 36, 8, 8);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.setFill(Color.web("#ffffff"));
            gc.fillText("DEPLOY BOAT [3]", boatBarX + boatBarW - 160, boatBarY + 37);
        }
    }

    private void drawProgressBar(GraphicsContext gc, double x, double y, double w, double h, int current, int required, String colorHex, String label) {
        // Track Background
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(x, y, w, h, 6, 6);
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(x, y, w, h, 6, 6);

        // Fill Progress
        double progress = Math.min(1.0, (double) current / required);
        if (progress > 0) {
            gc.setFill(Color.web(colorHex));
            gc.fillRoundRect(x + 1, y + 1, (w - 2) * progress, h - 2, 5, 5);
        }

        // Label & Count text
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#ffffff"));
        gc.fillText(label, x + 8, y + 16);

        String countText = current + " / " + required + (current >= required ? " [READY]" : "");
        gc.setFill(current >= required ? Color.web("#4ade80") : Color.web("#f8fafc"));
        gc.fillText(countText, x + w - 85, y + 16);
    }

    private void drawRoverSideView(GraphicsContext gc, double cx, double cy, double w, double h) {
        gc.save();
        // Ground line / water ripple
        gc.setStroke(Color.web("#334155", 0.6));
        gc.setLineWidth(1.5);
        gc.strokeLine(cx - 160, cy + 38, cx + 160, cy + 38);

        // 1. Suspension Coil Springs & Shock Absorbers
        gc.setStroke(Color.web("#eab308"));
        gc.setLineWidth(2.5);
        for (double wx : new double[]{cx - 65, cx, cx + 65}) {
            gc.strokeLine(wx, cy + 12, wx, cy + 24);
        }

        // 2. Six Wheels in Side View (3 visible on side)
        double wheelRadius = 16;
        for (double wx : new double[]{cx - 65, cx, cx + 65}) {
            double wy = cy + 26;
            // Tire Rubber (Deep Tread)
            gc.setFill(Color.web("#0f172a"));
            gc.fillOval(wx - wheelRadius, wy - wheelRadius, wheelRadius * 2, wheelRadius * 2);

            // Tire Treads
            gc.setStroke(Color.web("#334155"));
            gc.setLineWidth(2);
            gc.strokeOval(wx - wheelRadius, wy - wheelRadius, wheelRadius * 2, wheelRadius * 2);

            // Steel Wheel Rim
            gc.setFill(Color.web("#64748b"));
            gc.fillOval(wx - wheelRadius + 5, wy - wheelRadius + 5, (wheelRadius - 5) * 2, (wheelRadius - 5) * 2);

            // Hub Cap & Lug Nuts
            gc.setFill(Color.web("#facc15"));
            gc.fillOval(wx - 3, wy - 3, 6, 6);
        }

        // 3. Lower Armored Chassis Frame / Rock Sliders
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(cx - 95, cy + 6, 190, 10, 4, 4);

        // 4. Rear Cargo Bed (High-Vis Safety Orange)
        gc.setFill(Color.web("#ea580c"));
        gc.fillRoundRect(cx - 90, cy - 14, 80, 22, 4, 4);

        // Rear Tubular Roll-Cage
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(2.5);
        gc.strokeRect(cx - 90, cy - 28, 80, 16);
        gc.strokeLine(cx - 50, cy - 28, cx - 50, cy - 12);

        // Mounted Equipment on Bed: Coiled Yellow Rope & Orange Lifebuoy
        gc.setStroke(Color.web("#eab308"));
        gc.setLineWidth(2);
        gc.strokeOval(cx - 82, cy - 24, 12, 10);
        gc.setFill(Color.web("#dc2626"));
        gc.fillOval(cx - 62, cy - 24, 12, 10);
        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(cx - 59, cy - 21, 6, 4);

        // 5. Cab & Armored Cockpit (Olive Drab Green)
        gc.setFill(Color.web("#3f6212"));
        gc.fillRoundRect(cx - 15, cy - 32, 60, 40, 6, 6);

        // Sloping Tinted Cyan Windshield Glass
        gc.setFill(Color.web("#38bdf8", 0.75));
        double[] glassX = { cx + 25, cx + 45, cx + 45, cx + 25 };
        double[] glassY = { cy - 30, cy - 8, cy - 4, cy - 30 };
        gc.fillPolygon(glassX, glassY, 4);
        gc.setStroke(Color.web("#ffffff", 0.8));
        gc.setLineWidth(1.5);
        gc.strokeLine(cx + 27, cy - 28, cx + 43, cy - 8);

        // Cab Door & Window
        gc.setFill(Color.web("#38bdf8", 0.5));
        gc.fillRect(cx - 10, cy - 28, 28, 14);
        gc.setStroke(Color.web("#283618"));
        gc.setLineWidth(1.5);
        gc.strokeRect(cx - 12, cy - 30, 55, 38);

        // Snorkel Air Intake on A-Pillar
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(cx + 18, cy - 36, 4, 28);
        gc.fillOval(cx + 15, cy - 40, 10, 6);

        // Front Engine Hood
        gc.setFill(Color.web("#3f6212"));
        gc.fillRoundRect(cx + 42, cy - 8, 48, 16, 4, 4);

        // 6. Front Bullbar, Winch & Headlight
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(3.5);
        gc.strokeLine(cx + 90, cy - 14, cx + 95, cy + 12);
        gc.strokeLine(cx + 85, cy + 12, cx + 98, cy + 12);

        // Headlight Beams
        gc.setFill(Color.web("#fef08a"));
        gc.fillOval(cx + 88, cy - 6, 7, 7);
        gc.setFill(Color.web("#fef08a", 0.25));
        gc.fillPolygon(new double[]{cx + 95, cx + 150, cx + 150}, new double[]{cy - 3, cy - 25, cy + 18}, 3);

        // Roof Flashing Emergency Lightbar
        boolean strobe = ((int)(animTimer * 8) % 2 == 0);
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(cx - 4, cy - 36, 24, 5);
        gc.setFill(strobe ? Color.web("#ff0044") : Color.web("#880022"));
        gc.fillRect(cx - 2, cy - 35, 10, 4);
        gc.setFill(!strobe ? Color.web("#facc15") : Color.web("#785203"));
        gc.fillRect(cx + 10, cy - 35, 10, 4);

        gc.restore();
    }

    private void drawHelicopterSideView(GraphicsContext gc, double cx, double cy, double w, double h) {
        gc.save();

        // 1. Titanium Landing Skids & Struts
        double skidY = cy + 32;
        gc.setStroke(Color.web("#64748b"));
        gc.setLineWidth(3);
        // Main skid bar
        gc.strokeLine(cx - 65, skidY, cx + 55, skidY);
        gc.strokeLine(cx + 55, skidY, cx + 66, skidY - 6); // Curved front toe
        // Skid support struts connecting to fuselage
        gc.setStroke(Color.web("#334155"));
        gc.setLineWidth(2.5);
        gc.strokeLine(cx - 25, cy + 15, cx - 35, skidY);
        gc.strokeLine(cx + 25, cy + 15, cx + 18, skidY);

        // 2. Streamlined Maritime Fuselage (Rescue Blue + Red Stripe)
        // Main Cabin Body
        gc.setFill(Color.web("#0369a1"));
        gc.fillRoundRect(cx - 50, cy - 22, 105, 38, 14, 14);

        // Aerodynamic Nose Cap
        gc.setFill(Color.web("#0284c7"));
        gc.fillPolygon(new double[]{cx + 45, cx + 75, cx + 45}, new double[]{cy - 20, cy + 5, cy + 14}, 3);

        // Emergency Red Chevron Stripe & White Trim
        gc.setFill(Color.web("#dc2626"));
        gc.fillRect(cx - 45, cy + 2, 85, 5);
        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(cx - 45, cy + 8, 85, 2.5);

        // 3. Cockpit Windshield & Pilot
        gc.setFill(Color.web("#38bdf8", 0.78));
        double[] glassX = { cx + 25, cx + 68, cx + 48, cx + 25 };
        double[] glassY = { cy - 20, cy - 2, cy + 8, cy - 4 };
        gc.fillPolygon(glassX, glassY, 4);

        // Windshield Glare Line
        gc.setStroke(Color.web("#ffffff", 0.8));
        gc.setLineWidth(1.8);
        gc.strokeLine(cx + 28, cy - 18, cx + 62, cy - 2);

        // Pilot Helmet in Cockpit
        gc.setFill(Color.web("#facc15"));
        gc.fillOval(cx + 32, cy - 12, 10, 8);
        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(cx + 37, cy - 10, 4, 4);

        // 4. Open Side Rescue Cabin & Hoist Winch
        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(cx - 22, cy - 14, 40, 24); // Open Door

        // Medical Stretcher & Red Cross inside Cabin
        gc.setFill(Color.web("#dc2626"));
        gc.fillRect(cx - 18, cy - 5, 14, 10);
        gc.setStroke(Color.web("#ffffff"));
        gc.setLineWidth(1.5);
        gc.strokeLine(cx - 15, cy, cx - 7, cy);
        gc.strokeLine(cx - 11, cy - 4, cx - 11, cy + 4);

        // Rescue Hoist Winch Arm & Cable
        gc.setStroke(Color.web("#ca8a04"));
        gc.setLineWidth(3);
        gc.strokeLine(cx + 5, cy - 24, cx + 15, cy - 32);
        gc.strokeLine(cx + 15, cy - 32, cx + 15, cy - 10);
        // Rescue Hook
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(2);
        gc.strokeOval(cx + 13, cy - 10, 4, 6);

        // 5. Dual Turboshaft Engine Cowling (Fuselage Top)
        gc.setFill(Color.web("#1e293b"));
        gc.fillRoundRect(cx - 24, cy - 34, 46, 14, 4, 4);
        gc.setFill(Color.web("#0f172a")); // Exhaust Port
        gc.fillOval(cx - 26, cy - 30, 6, 6);

        // 6. Main Rotor Mast & Spinning 4-Blade Rotor (Side View Motion)
        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(4);
        gc.strokeLine(cx - 2, cy - 34, cx - 2, cy - 42); // Mast Hub

        // Spinning Rotor Blade Blur Disc & Blades
        gc.setFill(Color.web("#94a3b8", 0.22));
        gc.fillOval(cx - 110, cy - 46, 220, 8); // Disc side profile

        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(3);
        gc.strokeLine(cx - 105, cy - 42, cx + 105, cy - 42);

        // Safety Yellow Tips
        gc.setStroke(Color.web("#facc15"));
        gc.setLineWidth(3.5);
        gc.strokeLine(cx - 105, cy - 42, cx - 85, cy - 42);
        gc.strokeLine(cx + 85, cy - 42, cx + 105, cy - 42);

        // 7. Tail Boom, Stabilizers & Tail Rotor
        // Tapering Tail Boom
        gc.setFill(Color.web("#0369a1"));
        gc.fillPolygon(new double[]{cx - 48, cx - 115, cx - 115, cx - 48}, new double[]{cy - 12, cy - 4, cy + 2, cy - 2}, 4);

        // Red Chevron Bands on Tail Boom
        gc.setFill(Color.web("#dc2626"));
        gc.fillRect(cx - 75, cy - 10, 8, 8);
        gc.fillRect(cx - 95, cy - 8, 8, 7);

        // Horizontal Stabilizer Wing
        gc.setFill(Color.web("#0284c7"));
        gc.fillRect(cx - 105, cy - 14, 6, 16);
        gc.setFill(Color.web("#dc2626"));
        gc.fillRect(cx - 105, cy - 14, 6, 3); // Winglet

        // Angled Vertical Tail Fin
        gc.setFill(Color.web("#0284c7"));
        gc.fillPolygon(new double[]{cx - 112, cx - 124, cx - 118, cx - 112}, new double[]{cy - 3, cy - 28, cy - 30, cy - 3}, 4);
        gc.setFill(Color.web("#dc2626"));
        gc.fillPolygon(new double[]{cx - 119, cx - 124, cx - 118, cx - 117}, new double[]{cy - 20, cy - 28, cy - 30, cy - 22}, 4);

        // Flashing Strobe Beacon at top of tail fin
        boolean beacon = ((int)(animTimer * 8) % 2 == 0);
        gc.setFill(beacon ? Color.web("#ffffff") : Color.web("#dc2626"));
        gc.fillOval(cx - 122, cy - 32, 5, 5);

        // Spinning Tail Rotor (Side Disc)
        gc.setFill(Color.web("#94a3b8", 0.3));
        gc.fillOval(cx - 132, cy - 24, 18, 18);
        gc.setStroke(Color.web("#0f172a"));
        gc.setLineWidth(2);
        double trPhase = animTimer * 30;
        gc.strokeLine(cx - 123 + Math.cos(trPhase) * 9, cy - 15 + Math.sin(trPhase) * 9,
                      cx - 123 - Math.cos(trPhase) * 9, cy - 15 - Math.sin(trPhase) * 9);

        gc.restore();
    }

    private void drawHUD(GameEngine engine, double w, double h) {
        gc.setFill(Color.web("#090d16", 0.92));
        gc.fillRoundRect(20, 16, w - 40, 68, 20, 20);
        gc.setStroke(Color.web("#dc2626", 0.6));
        gc.setLineWidth(2);
        gc.strokeRoundRect(20, 16, w - 40, 68, 20, 20);

        double time = engine.getTimeRemaining();
        int mins = (int) (time / 60);
        int secs = (int) (time % 60);
        String timeStr = String.format("%02d:%02d", mins, secs);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        if (engine.isLowTimerActive()) {
            gc.setFill(Color.web("#ff0055"));
            gc.fillText("⏰ TIME: " + timeStr + " (SWIM PHASE!)", 35, 56);
        } else {
            gc.setFill(Color.web("#38bdf8"));
            gc.fillText("⏰ TIME: " + timeStr, 35, 56);
        }

        int cargo = engine.getBoat().getCargoCount();
        int maxCargo = engine.getBoat().getMaxCargoCapacity();
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        gc.setFill(Color.web("#f8fafc"));
        gc.fillText("CARGO: " + cargo + "/" + maxCargo, 255, 56);

        gc.setFill(Color.web("#00ff88"));
        gc.fillText("SAVED: " + engine.getTotalDelivered() + "/" + engine.getTotalSurvivorsInLevel(), 375, 56);

        // Depot Stockpile Counter in HUD
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("🔩 " + engine.getWorkshopNails(), 515, 56);
        gc.setFill(Color.web("#38bdf8"));
        gc.fillText("⚙️ " + engine.getWorkshopMetal(), 575, 56);

        gc.setFill(Color.web("#facc15"));
        gc.fillText("PTS: " + engine.getScore(), 635, 56);

        // Active Vehicle Status Tag
        Boat bObj = engine.getBoat();
        boolean isBoat = (bObj.getVehicleType() == Boat.VehicleType.BOAT);
        boolean isRover = (bObj.getVehicleType() == Boat.VehicleType.ROVER);
        if (isBoat) {
            gc.setFill(Color.web("#38bdf8"));
            gc.fillText("🚤 BOAT", 740, 56);
        } else if (isRover) {
            gc.setFill(Color.web("#facc15"));
            gc.fillText("🚗 ROVER", 740, 56);
        } else {
            gc.setFill(Color.web("#ec4899"));
            gc.fillText("🚁 HELI", 740, 56);
        }

        // Next wave countdown
        int nextWaveSec = (int) Math.ceil(engine.getNextWaveIn());
        gc.setFill(Color.web("#f97316"));
        gc.fillText("WAVE " + engine.getWaveNumber() + " (" + nextWaveSec + "s)", 840, 56);

        // Bottom Controls Banner
        gc.setFill(Color.web("#090d16", 0.92));
        gc.fillRoundRect(20, h - 55, w - 40, 42, 14, 14);
        gc.setStroke(Color.web("#dc2626", 0.5));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(20, h - 55, w - 40, 42, 14, 14);

        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#facc15"));
        gc.fillText("🕹️ PILOT: WASD", 30, h - 28);

        gc.setFill(Color.web("#ff0055"));
        gc.fillText("🧑‍🌾 CREW: Arrows", 150, h - 28);

        gc.setFill(Color.web("#00ff88"));
        gc.fillText("⚡ WINCH/ROPE: [SPACE]/[R]", 285, h - 28);

        boolean inWorkshop = engine.isInsideWorkshop(engine.getBoat());
        if (inWorkshop) {
            gc.setFill(Color.web("#facc15"));
            gc.fillText("🏭 WORKSHOP MENU: [E] (OPEN TERMINAL)", 510, h - 28);
            gc.setFill(Color.web("#00ff88"));
            gc.fillText("🔁 CYCLE: [V]", 820, h - 28);
        } else {
            gc.setFill(Color.web("#94a3b8"));
            gc.fillText("🏭 WORKSHOP MENU: (Enter Workshop & [E])", 510, h - 28);
            gc.setFill(Color.web("#64748b"));
            gc.fillText("🔍 ZOOM: Scroll/[Q]", 820, h - 28);
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
            if ("WORKSHOP_DEPOT".equals(b.getType())) {
                gc.setFill(Color.web("#0284c7"));
            } else if ("MOSQUE".equals(b.getType())) {
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
                if (s.isRooftopTrapped()) {
                    gc.setFill(Color.web("#e879f9")); // Magenta for rooftop trapped survivors
                    gc.fillOval(miniX + (s.getX() * scaleX) - 3.5, miniY + (s.getY() * scaleY) - 3.5, 7, 7);
                } else {
                    gc.setFill(Color.web("#38bdf8")); // Cyan for water survivors
                    gc.fillOval(miniX + (s.getX() * scaleX) - 3, miniY + (s.getY() * scaleY) - 3, 6, 6);
                }
            }
        }

        // Draw supply boxes on minimap
        for (SupplyBox box : engine.getSupplyBoxes()) {
            if (!box.isCollected()) {
                if (box.getType() == SupplyBox.BoxType.NAILS) {
                    gc.setFill(Color.web("#f59e0b"));
                } else {
                    gc.setFill(Color.web("#cbd5e1"));
                }
                gc.fillRect(miniX + (box.getX() * scaleX) - 2, miniY + (box.getY() * scaleY) - 2, 4, 4);
            }
        }

        Boat b = engine.getBoat();
        if (b.getVehicleType() == Boat.VehicleType.HELICOPTER) {
            gc.setFill(Color.web("#ec4899"));
        } else if (b.getVehicleType() == Boat.VehicleType.ROVER) {
            gc.setFill(Color.web("#22c55e"));
        } else {
            gc.setFill(Color.web("#facc15"));
        }
        gc.fillOval(miniX + (b.getX() * scaleX) - 4.5, miniY + (b.getY() * scaleY) - 4.5, 9, 9);
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