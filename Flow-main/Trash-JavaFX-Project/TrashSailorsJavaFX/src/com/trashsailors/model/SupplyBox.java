package com.trashsailors.model;

public class SupplyBox {
    public enum BoxType {
        NAILS("Nails Crate", 1, "#f59e0b"),
        METAL("Metal Parts Crate", 1, "#94a3b8");

        private final String displayName;
        private final int materialCount;
        private final String color;

        BoxType(String displayName, int materialCount, String color) {
            this.displayName = displayName;
            this.materialCount = materialCount;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public int getMaterialCount() { return materialCount; }
        public String getColor() { return color; }
    }

    private double x;
    private double y;
    private final BoxType type;
    private boolean collected = false;
    private double bobbingPhase;

    public SupplyBox(double x, double y, BoxType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.bobbingPhase = Math.random() * Math.PI * 2;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public BoxType getType() { return type; }
    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }
    public double getBobbingPhase() { return bobbingPhase; }
    public void update(double dt) { bobbingPhase += dt * 3.0; }
}
