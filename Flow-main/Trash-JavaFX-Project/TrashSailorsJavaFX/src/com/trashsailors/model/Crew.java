package com.trashsailors.model;

public class Crew {
    // Relative coordinates on the boat deck
    private double localX = 0; // -deckLength/2 to +deckLength/2
    private double localY = 0; // -deckWidth/2 to +deckWidth/2

    private final double moveSpeed = 2.5;
    private final double deckLength = 80;
    private final double deckWidth = 40;
    private final double interactionRadius = 65; // Proximity reach for Space action

    public void update(boolean up, boolean down, boolean left, boolean right) {
        if (up) localY -= moveSpeed;
        if (down) localY += moveSpeed;
        if (left) localX -= moveSpeed;
        if (right) localX += moveSpeed;

        // Clamp inside boat deck boundaries (with small margin to reach edges)
        double maxL = deckLength / 2;
        double maxW = deckWidth / 2;
        localX = Math.max(-maxL, Math.min(maxL, localX));
        localY = Math.max(-maxW, Math.min(maxW, localY));
    }

    public double getWorldX(Boat boat) {
        double rad = Math.toRadians(boat.getAngle());
        return boat.getX() + (localX * Math.cos(rad) - localY * Math.sin(rad));
    }

    public double getWorldY(Boat boat) {
        double rad = Math.toRadians(boat.getAngle());
        return boat.getY() + (localX * Math.sin(rad) + localY * Math.cos(rad));
    }

    public double getLocalX() { return localX; }
    public double getLocalY() { return localY; }
    public double getInteractionRadius() { return interactionRadius; }
}
