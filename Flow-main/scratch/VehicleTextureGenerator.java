import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class VehicleTextureGenerator {
    public static void main(String[] args) {
        File outputDir = new File("src/Resources/Images");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try {
            generateRover(new File(outputDir, "rover.png"));
            generateHelicopter(new File(outputDir, "helicopter.png"));
            System.out.println("Vehicle textures generated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateRover(File file) throws Exception {
        int w = 230, h = 112; // 2x resolution of 115x56
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Chassis Shadow
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(20, 20, 190, 72, 20, 20);

        // 6x6 Heavy All-Terrain Tires
        g.setColor(new Color(30, 41, 59));
        int[] wheelXs = {30, 95, 160};
        for (int x : wheelXs) {
            // Top wheels
            g.fillRoundRect(x, 6, 40, 24, 8, 8);
            // Bottom wheels
            g.fillRoundRect(x, 82, 40, 24, 8, 8);
        }

        // Buggy Main Body (Rugged Rescue Red/Orange & Offroad Slate)
        g.setColor(new Color(234, 88, 12));
        g.fillRoundRect(30, 22, 170, 68, 24, 24);

        // Cabin Roll Cage & Glass Roof
        g.setColor(new Color(15, 23, 42));
        g.fillRoundRect(70, 32, 80, 48, 16, 16);
        g.setColor(new Color(56, 189, 248, 180));
        g.fillRoundRect(80, 38, 60, 36, 10, 10);

        // Front Headlights
        g.setColor(new Color(254, 240, 138));
        g.fillOval(190, 32, 14, 14);
        g.fillOval(190, 66, 14, 14);

        // Roof Warning Lightbar
        g.setColor(new Color(239, 68, 68));
        g.fillRect(100, 24, 20, 8);
        g.setColor(new Color(59, 130, 246));
        g.fillRect(120, 24, 20, 8);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateHelicopter(File file) throws Exception {
        int w = 260, h = 128; // 2x resolution of 130x64
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Main Fuselage Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(40, 30, 180, 68);

        // Landing Skids
        g.setStroke(new BasicStroke(6f));
        g.setColor(new Color(71, 85, 105));
        g.drawLine(60, 16, 180, 16);
        g.drawLine(60, 112, 180, 112);
        g.drawLine(90, 16, 90, 34);
        g.drawLine(150, 16, 150, 34);
        g.drawLine(90, 94, 90, 112);
        g.drawLine(150, 94, 150, 112);

        // Fuselage Body (Emergency Rescue White & Bright Yellow)
        g.setColor(new Color(248, 250, 252));
        g.fillRoundRect(50, 32, 160, 64, 30, 30);
        g.setColor(new Color(234, 179, 8));
        g.fillRect(80, 32, 70, 64);

        // Cockpit Glass Bubble
        g.setColor(new Color(14, 165, 233, 200));
        g.fillRoundRect(150, 38, 54, 52, 20, 20);

        // Tail Boom & Rear Rotor
        g.setColor(new Color(225, 29, 72));
        g.fillRect(10, 56, 50, 16);
        g.fillRect(6, 44, 10, 40); // Tail fin

        // Red Cross Medical Symbol on Roof
        g.setColor(new Color(225, 29, 72));
        g.fillRect(106, 44, 12, 40);
        g.fillRect(92, 58, 40, 12);

        // Main Rotor Blade (Blurred spinning rotor disk)
        g.setColor(new Color(148, 163, 184, 140));
        g.fillOval(30, 4, 200, 120);
        g.setStroke(new BasicStroke(4f));
        g.setColor(new Color(30, 41, 59, 180));
        g.drawLine(30, 64, 230, 64);
        g.drawLine(130, 4, 130, 124);

        g.dispose();
        ImageIO.write(img, "png", file);
    }
}
