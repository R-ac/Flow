import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MedicalTextureGenerator {
    public static void main(String[] args) {
        File outputDir = new File("src/Resources/Images");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try {
            generateMedKit(new File(outputDir, "med_kit.png"));
            generateIVDrip(new File(outputDir, "med_iv.png"));
            generateBandage(new File(outputDir, "med_bandage.png"));
            generateBadgeHungry(new File(outputDir, "badge_hungry.png"));
            generateBadgeInjured(new File(outputDir, "badge_injured.png"));
            generateBadgeHealthy(new File(outputDir, "badge_healthy.png"));
            System.out.println("Medical assets generated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateMedKit(File file) throws Exception {
        int w = 128, h = 128;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Box shadow
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRoundRect(16, 32, 96, 76, 20, 20);

        // White Red Cross Box
        g.setColor(new Color(240, 240, 245));
        g.fillRoundRect(14, 28, 96, 76, 20, 20);
        g.setColor(new Color(200, 200, 210));
        g.drawRoundRect(14, 28, 96, 76, 20, 20);

        // Handle
        g.setStroke(new BasicStroke(8f));
        g.setColor(new Color(180, 30, 30));
        g.drawArc(44, 12, 40, 32, 0, 180);

        // Red Cross Symbol
        g.setColor(new Color(220, 38, 38));
        g.fillRect(52, 44, 24, 44);
        g.fillRect(42, 54, 44, 24);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateIVDrip(File file) throws Exception {
        int w = 128, h = 128;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Stand / Hanger
        g.setStroke(new BasicStroke(6f));
        g.setColor(new Color(150, 160, 175));
        g.drawLine(64, 10, 64, 118);
        g.drawLine(44, 20, 84, 20);

        // Saline Bottle
        g.setColor(new Color(225, 245, 254, 210));
        g.fillRoundRect(46, 28, 36, 64, 16, 16);
        g.setColor(new Color(56, 189, 248));
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(46, 28, 36, 64, 16, 16);

        // Fluid line
        g.setColor(new Color(14, 165, 233, 180));
        g.fillRect(48, 50, 32, 40);

        // Blue Cross emblem on bottle
        g.setColor(new Color(2, 132, 199));
        g.fillRect(60, 36, 8, 16);
        g.fillRect(56, 40, 16, 8);

        // Drip tube
        g.setStroke(new BasicStroke(3f));
        g.setColor(new Color(224, 242, 254));
        g.drawLine(64, 92, 64, 118);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateBandage(File file) throws Exception {
        int w = 128, h = 128;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Crossed Bandages
        g.rotate(Math.toRadians(30), 64, 64);

        g.setColor(new Color(245, 215, 170));
        g.fillRoundRect(16, 48, 96, 32, 12, 12);
        g.setColor(new Color(255, 255, 255));
        g.fillRoundRect(48, 48, 32, 32, 4, 4);

        g.rotate(Math.toRadians(-60), 64, 64);
        g.setColor(new Color(240, 200, 150));
        g.fillRoundRect(16, 48, 96, 32, 12, 12);
        g.setColor(new Color(255, 255, 255));
        g.fillRoundRect(48, 48, 32, 32, 4, 4);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateBadgeHungry(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Orange Circle Badge
        g.setColor(new Color(249, 115, 22));
        g.fillOval(4, 4, 56, 56);
        g.setColor(new Color(255, 255, 255));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(4, 4, 56, 56);

        // Fork and Knife icon
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.drawLine(24, 20, 24, 44);
        g.drawLine(20, 20, 28, 20);
        g.drawLine(40, 20, 40, 44);
        g.drawArc(34, 20, 12, 16, 0, 180);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateBadgeInjured(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Red Circle Badge
        g.setColor(new Color(220, 38, 38));
        g.fillOval(4, 4, 56, 56);
        g.setColor(new Color(255, 255, 255));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(4, 4, 56, 56);

        // White Red Cross Symbol
        g.setColor(Color.WHITE);
        g.fillRect(26, 16, 12, 32);
        g.fillRect(16, 26, 32, 12);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    private static void generateBadgeHealthy(File file) throws Exception {
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Green Circle Badge
        g.setColor(new Color(34, 197, 94));
        g.fillOval(4, 4, 56, 56);
        g.setColor(new Color(255, 255, 255));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(4, 4, 56, 56);

        // White Checkmark
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.WHITE);
        g.drawLine(18, 32, 28, 42);
        g.drawLine(28, 42, 46, 20);

        g.dispose();
        ImageIO.write(img, "png", file);
    }
}
