package com.flow.model;

public class Crew {
    // Offset relative to boat center
    private double offsetX = 0;
    private double offsetY = 0;
    
    private final double maxOffsetRadius = 80;
    private final double speed = 3.5;
    private final double interactionRadius = 45;

    public Crew() {}

    public void update(boolean up, boolean down, boolean left, boolean right) {
        if (up) offsetY -= speed;
        if (down) offsetY += speed;
        if (left) offsetX -= speed;
        if (right) offsetX += speed;

        // Clamp crew movement to stay within boat deck & immediate vicinity
        double currentDist = Math.hypot(offsetX, offsetY);
        if (currentDist > maxOffsetRadius) {
            offsetX = (offsetX / currentDist) * maxOffsetRadius;
            offsetY = (offsetY / currentDist) * maxOffsetRadius;
        }
    }

    public double getWorldX(Boat boat) {
        double rad = Math.toRadians(boat.getAngle());
        return boat.getX() + (offsetX * Math.cos(rad) - offsetY * Math.sin(rad));
    }

    public double getWorldY(Boat boat) {
        double rad = Math.toRadians(boat.getAngle());
        return boat.getY() + (offsetX * Math.sin(rad) + offsetY * Math.cos(rad));
    }

    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getInteractionRadius() { return interactionRadius; }
}
