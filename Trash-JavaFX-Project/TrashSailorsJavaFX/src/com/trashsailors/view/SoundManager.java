package com.trashsailors.view;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SoundManager {
    private static boolean soundEnabled = true;

    private static void playTone(double frequency, int durationMs, double volume) {
        if (!soundEnabled) return;
        new Thread(() -> {
            try {
                float sampleRate = 22050f;
                byte[] buf = new byte[(int)(sampleRate * durationMs / 1000)];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (sampleRate / frequency) * 2.0 * Math.PI;
                    buf[i] = (byte)(Math.sin(angle) * 127.0 * volume);
                }
                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, true);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception ignored) {}
        }).start();
    }

    public static void playRescueSound() {
        playTone(523.25, 120, 0.5); // C5
        try { Thread.sleep(60); } catch (Exception ignored) {}
        playTone(659.25, 150, 0.6); // E5
    }

    public static void playRopeThrowSound() {
        playTone(350.00, 80, 0.5);
        try { Thread.sleep(40); } catch (Exception ignored) {}
        playTone(700.00, 120, 0.6); // Whip zip sound
    }

    public static void playDeliverySound() {
        playTone(587.33, 100, 0.5);
        playTone(739.99, 100, 0.5);
        playTone(880.00, 200, 0.7);
    }

    public static void playObstacleClearedSound() {
        playTone(220.00, 150, 0.5); // A3 rumble
    }

    public static void playCreatureScaredSound() {
        playTone(440.00, 100, 0.6);
        playTone(330.00, 180, 0.6);
    }

    public static void playCollectSound() {
        playTone(880.00, 90, 0.4);
        playTone(1046.50, 120, 0.5);
    }

    public static void playWinSound() {
        playTone(523.25, 150, 0.6);
        playTone(659.25, 150, 0.6);
        playTone(783.99, 150, 0.6);
        playTone(1046.50, 400, 0.8);
    }

    public static void playGameOverSound() {
        playTone(300.00, 200, 0.6);
        playTone(220.00, 250, 0.6);
        playTone(150.00, 400, 0.7);
    }
}
