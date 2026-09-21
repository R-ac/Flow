package com.flow.model;

public enum InjuryType {
    MINOR_CUT("Laceration / Cut", "🩹", "Bandages & Antiseptic"),
    DEHYDRATION_SHOCK("Dehydration & Shock", "🧪", "IV Drip & Saline Solution"),
    CRITICAL_TRAUMA("Critical Trauma", "🚑", "Emergency First Aid Kit");

    private final String displayName;
    private final String icon;
    private final String requiredTreatmentName;

    InjuryType(String displayName, String icon, String requiredTreatmentName) {
        this.displayName = displayName;
        this.icon = icon;
        this.requiredTreatmentName = requiredTreatmentName;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public String getRequiredTreatmentName() { return requiredTreatmentName; }
}
