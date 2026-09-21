import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CropAndSaveSprites {
    public static void main(String[] args) {
        File brainDir = new File("C:/Users/hp/.gemini/antigravity/brain/b0bfb406-a6b4-4bb4-8e43-5dc5eb47583a");
        File outputDir = new File("src/Resources/Images");
        if (!outputDir.exists()) outputDir.mkdirs();

        try {
            // Locate generated pack images
            File captainPackFile = findFileStartingWith(brainDir, "captain_pixel_pack_");
            File crewPackFile = findFileStartingWith(brainDir, "crew_survivor_pack_");
            File vehiclePackFile = findFileStartingWith(brainDir, "vehicles_monsters_pack_");

            if (captainPackFile != null && captainPackFile.exists()) {
                BufferedImage img = ImageIO.read(captainPackFile);
                // Crop Captain Front Idle (Top Left region approx)
                BufferedImage captainImg = cropWithKey(img, 0.08, 0.08, 0.18, 0.18);
                saveTransparent(captainImg, new File(outputDir, "pilot.png"));
                saveTransparent(captainImg, new File(outputDir, "captain_idle.png"));
                System.out.println("Extracted Captain sprite!");
            }

            if (crewPackFile != null && crewPackFile.exists()) {
                BufferedImage img = ImageIO.read(crewPackFile);
                // Crop Crew Front (Top Left)
                BufferedImage crewImg = cropWithKey(img, 0.08, 0.06, 0.18, 0.18);
                saveTransparent(crewImg, new File(outputDir, "crew.png"));

                // Crop Survivor Front (Middle Left)
                BufferedImage survivorImg = cropWithKey(img, 0.08, 0.58, 0.18, 0.18);
                saveTransparent(survivorImg, new File(outputDir, "survivor.png"));
                saveTransparent(survivorImg, new File(outputDir, "survivor_waving.png"));
                System.out.println("Extracted Crew & Survivor sprites!");
            }

            if (vehiclePackFile != null && vehiclePackFile.exists()) {
                BufferedImage img = ImageIO.read(vehiclePackFile);
                // Crop Motorboat (Top Left)
                BufferedImage boatImg = cropWithKey(img, 0.05, 0.06, 0.18, 0.18);
                saveTransparent(boatImg, new File(outputDir, "Boat.png"));
                saveTransparent(boatImg, new File(outputDir, "boat_main.png"));

                // Crop Land Rover (Top Middle-Right)
                BufferedImage roverImg = cropWithKey(img, 0.52, 0.06, 0.18, 0.18);
                saveTransparent(roverImg, new File(outputDir, "rover.png"));

                // Crop Helicopter (Top Right)
                BufferedImage heliImg = cropWithKey(img, 0.76, 0.04, 0.20, 0.20);
                saveTransparent(heliImg, new File(outputDir, "helicopter.png"));

                // Crop Monster (Crocodile / Swamp Beast)
                BufferedImage monsterImg = cropWithKey(img, 0.06, 0.34, 0.20, 0.20);
                saveTransparent(monsterImg, new File(outputDir, "monster.png"));
                saveTransparent(monsterImg, new File(outputDir, "monster_croc.png"));

                // Crop Debris (Barrels / Crates)
                BufferedImage debrisImg = cropWithKey(img, 0.05, 0.62, 0.20, 0.18);
                saveTransparent(debrisImg, new File(outputDir, "debris.png"));
                saveTransparent(debrisImg, new File(outputDir, "debris_crate.png"));
                System.out.println("Extracted Vehicles, Monsters & Debris sprites!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File findFileStartingWith(File dir, String prefix) {
        File[] files = dir.listFiles((d, name) -> name.startsWith(prefix) && name.endsWith(".jpg"));
        return (files != null && files.length > 0) ? files[0] : null;
    }

    private static BufferedImage cropWithKey(BufferedImage src, double rx, double ry, double rw, double rh) {
        int x = (int) (src.getWidth() * rx);
        int y = (int) (src.getHeight() * ry);
        int w = Math.min(src.getWidth() - x, (int) (src.getWidth() * rw));
        int h = Math.min(src.getHeight() - y, (int) (src.getHeight() * rh));

        BufferedImage cropped = src.getSubimage(x, y, w, h);
        BufferedImage transparent = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // Make white/light background transparent
        for (int cx = 0; cx < w; cx++) {
            for (int cy = 0; cy < h; cy++) {
                int rgb = cropped.getRGB(cx, cy);
                Color c = new Color(rgb);
                if (c.getRed() > 240 && c.getGreen() > 240 && c.getBlue() > 240) {
                    transparent.setRGB(cx, cy, 0x00000000); // transparent
                } else {
                    transparent.setRGB(cx, cy, rgb);
                }
            }
        }
        return transparent;
    }

    private static void saveTransparent(BufferedImage img, File file) throws Exception {
        ImageIO.write(img, "png", file);
    }
}
