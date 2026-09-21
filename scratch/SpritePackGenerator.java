import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SpritePackGenerator {
    private static final File OUTPUT_DIR = new File("src/Resources/Images");

    public static void main(String[] args) {
        if (!OUTPUT_DIR.exists()) {
            OUTPUT_DIR.mkdirs();
        }

        try {
            // Category 1: Rescue Boats
            generateMainBoat(new File(OUTPUT_DIR, "boat_main.png"));
            generateMotorBoat(new File(OUTPUT_DIR, "boat_motor.png"));
            generateDamagedBoat(new File(OUTPUT_DIR, "boat_damaged.png"));
            generateSurvivorBoat(new File(OUTPUT_DIR, "boat_survivors.png"));

            // Category 2: Captain / Player
            generateCaptainIdle(new File(OUTPUT_DIR, "captain_idle.png"));
            generateCaptainSteering(new File(OUTPUT_DIR, "captain_steering.png"));
            generateCaptainRescue(new File(OUTPUT_DIR, "captain_rescue.png"));

            // Category 3: Crew Members
            generateCrewMedic(new File(OUTPUT_DIR, "crew_medic.png"));
            generateCrewEngineer(new File(OUTPUT_DIR, "crew_engineer.png"));
            generateCrewDiver(new File(OUTPUT_DIR, "crew_diver.png"));
            generateCrewRadio(new File(OUTPUT_DIR, "crew_radio.png"));

            // Category 4: Survivors
            generateSurvivorWaving(new File(OUTPUT_DIR, "survivor_waving.png"));
            generateSurvivorInjured(new File(OUTPUT_DIR, "survivor_injured.png"));
            generateSurvivorRooftop(new File(OUTPUT_DIR, "survivor_rooftop.png"));
            generateSurvivorSwimming(new File(OUTPUT_DIR, "survivor_swimming.png"));

            // Category 5: Monsters
            generateMonsterAquatic(new File(OUTPUT_DIR, "monster_aquatic.png"));
            generateMonsterSwamp(new File(OUTPUT_DIR, "monster_swamp.png"));
            generateMonsterCroc(new File(OUTPUT_DIR, "monster_croc.png"));
            generateMonsterFish(new File(OUTPUT_DIR, "monster_fish.png"));
            generateMonsterTentacle(new File(OUTPUT_DIR, "monster_tentacle.png"));
            generateMonsterBoss(new File(OUTPUT_DIR, "monster_boss.png"));

            // Category 6: Debris / Environment
            generateDebrisCrate(new File(OUTPUT_DIR, "debris_crate.png"));
            generateDebrisBarrel(new File(OUTPUT_DIR, "debris_barrel.png"));
            generateDebrisTire(new File(OUTPUT_DIR, "debris_tire.png"));
            generateDebrisCar(new File(OUTPUT_DIR, "debris_car.png"));
            generateDebrisTree(new File(OUTPUT_DIR, "debris_tree.png"));
            generateDebrisPlank(new File(OUTPUT_DIR, "debris_plank.png"));
            generateDebrisTrash(new File(OUTPUT_DIR, "debris_trash.png"));

            System.out.println("2D Top-Down Sprite Asset Pack generated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Category 1: Boats ---
    private static void generateMainBoat(File file) throws Exception {
        int w = 220, h = 110;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        // Water Shadow
        g.setColor(new Color(2, 6, 23, 110));
        g.fillOval(10, 20, 200, 80);

        // Navy Outer Hull
        int[] hx = {10, 30, 180, 210, 180, 30};
        int[] hy = {25, 10, 15, 55, 95, 100};
        g.setColor(new Color(11, 30, 58));
        g.fillPolygon(hx, hy, 6);
        g.setColor(new Color(226, 232, 240));
        g.setStroke(new BasicStroke(4f));
        g.drawPolygon(hx, hy, 6);

        // Orange Rescue Sponsons
        g.setColor(new Color(249, 115, 22));
        int[] sx = {25, 38, 175, 195, 175, 38};
        int[] sy = {30, 20, 22, 55, 88, 90};
        g.fillPolygon(sx, sy, 6);

        // Light Deck Floor
        g.setColor(new Color(234, 244, 255));
        g.fillRoundRect(45, 28, 130, 54, 16, 16);

        // Red Rescue Cross Banner
        g.setColor(new Color(220, 38, 38));
        g.fillRect(100, 30, 14, 50);
        g.fillRect(82, 48, 50, 14);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateMotorBoat(File file) throws Exception {
        int w = 220, h = 110;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateMainBoat(file); // Base hull shape
        // Add dual outboard motors & propellers
        g.setColor(new Color(30, 41, 59));
        g.fillRect(4, 25, 16, 22);
        g.fillRect(4, 63, 16, 22);
        g.setColor(new Color(56, 189, 248));
        g.fillOval(0, 30, 10, 12);
        g.fillOval(0, 68, 10, 12);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateDamagedBoat(File file) throws Exception {
        int w = 220, h = 110;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateMainBoat(file);
        // Burn marks & cracks
        g.setColor(new Color(15, 23, 42, 200));
        g.fillOval(120, 35, 45, 40);
        g.setStroke(new BasicStroke(3f));
        g.setColor(new Color(30, 41, 59));
        g.drawLine(100, 20, 130, 60);
        g.drawLine(130, 60, 160, 40);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateSurvivorBoat(File file) throws Exception {
        int w = 220, h = 110;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateMainBoat(file);
        // Draw survivors sitting in deck slots
        g.setColor(new Color(244, 199, 161));
        g.fillOval(70, 38, 16, 16);
        g.fillOval(110, 38, 16, 16);
        g.fillOval(140, 56, 16, 16);
        g.setColor(new Color(34, 197, 94));
        g.fillRoundRect(66, 52, 24, 20, 6, 6);
        g.fillRoundRect(106, 52, 24, 20, 6, 6);
        g.fillRoundRect(136, 70, 24, 20, 6, 6);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    // --- Category 2: Captain ---
    private static void generateCaptainIdle(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        // Body Shadow
        g.setColor(new Color(2, 6, 23, 110));
        g.fillOval(14, 40, 36, 18);

        // Waterproof Bright Yellow Jacket
        g.setColor(new Color(245, 158, 11));
        g.fillRoundRect(16, 18, 32, 34, 12, 12);

        // Orange Life Vest
        g.setColor(new Color(249, 115, 22));
        g.fillRoundRect(20, 22, 24, 26, 8, 8);
        g.setColor(new Color(255, 255, 255));
        g.fillRect(22, 30, 20, 4);

        // Head & Rescue Helmet
        g.setColor(new Color(244, 199, 161));
        g.fillOval(22, 14, 20, 20);
        g.setColor(new Color(30, 41, 59));
        g.fillRoundRect(20, 10, 24, 12, 6, 6);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateCaptainSteering(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateCaptainIdle(file);
        // Arms extending to steering wheel
        g.setColor(new Color(245, 158, 11));
        g.fillRect(10, 28, 14, 8);
        g.fillRect(40, 28, 14, 8);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateCaptainRescue(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateCaptainIdle(file);
        // Reaching out arm with rope
        g.setColor(new Color(249, 115, 22));
        g.fillRect(38, 20, 20, 8);
        g.setColor(new Color(202, 138, 4));
        g.drawOval(48, 18, 14, 14);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    // --- Category 3: Crew Members ---
    private static void generateCrewMedic(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateCaptainIdle(file);
        // Red Cross Medical Helmet
        g.setColor(new Color(248, 250, 252));
        g.fillRoundRect(20, 10, 24, 12, 6, 6);
        g.setColor(new Color(220, 38, 38));
        g.fillRect(30, 11, 4, 10);
        g.fillRect(27, 14, 10, 4);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateCrewEngineer(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateCaptainIdle(file);
        // High-vis Tech Engineer
        g.setColor(new Color(34, 197, 94));
        g.fillRoundRect(16, 18, 32, 34, 12, 12);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateCrewDiver(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        // Black Wetsuit & Scuba Tank
        g.setColor(new Color(15, 23, 42));
        g.fillRoundRect(16, 18, 32, 34, 12, 12);
        g.setColor(new Color(234, 179, 8));
        g.fillRoundRect(26, 22, 12, 24, 4, 4);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateCrewRadio(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateCaptainIdle(file);
        // Comms Headset
        g.setColor(new Color(15, 23, 42));
        g.drawArc(18, 8, 28, 20, 0, 180);
        g.fillOval(16, 18, 6, 8);
        g.fillOval(42, 18, 6, 8);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    // --- Category 4: Survivors ---
    private static void generateSurvivorWaving(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        // Waving Arms
        g.setColor(new Color(244, 199, 161));
        g.fillOval(22, 16, 20, 20); // Head
        g.setColor(new Color(59, 130, 246));
        g.fillRoundRect(20, 32, 24, 26, 8, 8); // Shirt

        g.setStroke(new BasicStroke(4f));
        g.setColor(new Color(244, 199, 161));
        g.drawLine(20, 34, 8, 14); // Left arm up
        g.drawLine(44, 34, 56, 14); // Right arm up

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateSurvivorInjured(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateSurvivorWaving(file);
        // Head Bandage & Sling
        g.setColor(new Color(248, 250, 252));
        g.fillRect(20, 18, 24, 6);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateSurvivorRooftop(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        generateSurvivorWaving(file);
        // Seated Pose Shadow
        g.setColor(new Color(2, 6, 23, 120));
        g.fillOval(16, 44, 32, 14);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateSurvivorSwimming(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        // Water Ripple & Life Vest Floating
        g.setColor(new Color(56, 189, 248, 140));
        g.fillOval(12, 12, 40, 40);
        g.setColor(new Color(249, 115, 22));
        g.fillOval(20, 20, 24, 24);
        g.setColor(new Color(244, 199, 161));
        g.fillOval(24, 24, 16, 16);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    // --- Category 5: Monsters ---
    private static void generateMonsterAquatic(File file) throws Exception {
        int w = 96, h = 96;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(2, 6, 23, 110));
        g.fillOval(10, 40, 76, 40);

        g.setColor(new Color(20, 83, 45));
        g.fillOval(16, 20, 64, 56);
        g.setColor(new Color(34, 197, 94));
        g.fillOval(24, 26, 48, 44);

        // Glowing Red Eyes
        g.setColor(new Color(239, 68, 68));
        g.fillOval(32, 38, 10, 10);
        g.fillOval(54, 38, 10, 10);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateMonsterSwamp(File file) throws Exception {
        generateMonsterAquatic(file);
    }

    private static void generateMonsterCroc(File file) throws Exception {
        int w = 110, h = 60;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(21, 128, 61));
        g.fillRoundRect(10, 15, 90, 30, 14, 14);
        g.setColor(new Color(254, 240, 138));
        g.fillOval(75, 20, 8, 8);
        g.fillOval(75, 32, 8, 8);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateMonsterFish(File file) throws Exception {
        generateMonsterAquatic(file);
    }

    private static void generateMonsterTentacle(File file) throws Exception {
        generateMonsterAquatic(file);
    }

    private static void generateMonsterBoss(File file) throws Exception {
        int w = 140, h = 140;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(15, 23, 42));
        g.fillOval(20, 20, 100, 100);
        g.setColor(new Color(225, 29, 72));
        g.fillOval(45, 45, 50, 50);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    // --- Category 6: Debris ---
    private static void generateDebrisCrate(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(180, 83, 9));
        g.fillRoundRect(12, 12, 40, 40, 6, 6);
        g.setColor(new Color(120, 53, 15));
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(12, 12, 40, 40, 6, 6);
        g.drawLine(12, 12, 52, 52);
        g.drawLine(52, 12, 12, 52);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateDebrisBarrel(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(220, 38, 38));
        g.fillOval(14, 14, 36, 36);
        g.setColor(new Color(248, 250, 252));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(20, 20, 24, 24);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateDebrisTire(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(30, 41, 59));
        g.fillOval(14, 14, 36, 36);
        g.setColor(new Color(15, 23, 42));
        g.fillOval(24, 24, 16, 16);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateDebrisCar(File file) throws Exception {
        int w = 110, h = 60;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(3, 105, 161));
        g.fillRoundRect(10, 10, 90, 40, 12, 12);
        g.setColor(new Color(56, 189, 248, 180));
        g.fillRect(35, 18, 40, 24);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateDebrisTree(File file) throws Exception {
        int w = 110, h = 60;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(120, 53, 15));
        g.setStroke(new BasicStroke(12f));
        g.drawLine(10, 40, 100, 20);
        g.setColor(new Color(22, 101, 52));
        g.fillOval(30, 10, 40, 30);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateDebrisPlank(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(180, 83, 9));
        g.fillRect(10, 20, 44, 12);
        g.fillRect(16, 36, 36, 12);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateDebrisTrash(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(new Color(15, 23, 42));
        g.fillOval(16, 16, 32, 32);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static Graphics2D createGraphics(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }
}
