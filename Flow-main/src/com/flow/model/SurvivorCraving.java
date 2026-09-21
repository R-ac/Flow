package com.flow.model;

public enum SurvivorCraving {
    RICE_CURRY("Warm Rice & Curry", "🍛"),
    FISH_STEW("Grilled Fish Stew", "🍲"),
    TEA_SNACKS("Hot Tea & Snacks", "🍵");

    private final String displayName;
    private final String icon;

    SurvivorCraving(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
}
