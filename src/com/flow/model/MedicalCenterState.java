package com.flow.model;

public class MedicalCenterState {
    private boolean insideMedicalCenter = false;
    private double transitionAlpha = 0.0;
    private boolean transitioning = false;
    private boolean entering = true;

    private int bandageStock = 2;
    private int salineStock = 2;
    private int firstAidStock = 2;
    private boolean isTreating = false;
    private double treatmentProgress = 0.0;
    private String currentTreatment = "";

    public void startTransition(boolean entering) {
        this.transitioning = true;
        this.entering = entering;
        this.transitionAlpha = 0.0;
    }

    public void updateTransition(double dt) {
        if (!transitioning) return;

        transitionAlpha += dt * 3.0; // 0.33s fade speed
        if (transitionAlpha >= 1.0) {
            transitionAlpha = 1.0;
            transitioning = false;
            if (entering) {
                insideMedicalCenter = true;
            } else {
                insideMedicalCenter = false;
            }
        }
    }

    public void startTreatment(String treatmentName, double durationSec) {
        if (isTreating) return;
        this.currentTreatment = treatmentName;
        this.treatmentProgress = 0.0;
        this.isTreating = true;
    }

    public boolean updateTreatment(double dt, double durationSec) {
        if (!isTreating) return false;
        treatmentProgress += dt / durationSec;
        if (treatmentProgress >= 1.0) {
            treatmentProgress = 1.0;
            isTreating = false;
            if ("Bandages & Antiseptic".equalsIgnoreCase(currentTreatment)) {
                bandageStock += 2;
            } else if ("IV Drip & Saline Solution".equalsIgnoreCase(currentTreatment)) {
                salineStock += 2;
            } else {
                firstAidStock += 2;
            }
            return true; // Treatment completed (yields 2 kits)!
        }
        return false;
    }

    public boolean isInsideMedicalCenter() { return insideMedicalCenter; }
    public boolean isTransitioning() { return transitioning; }
    public double getTransitionAlpha() { return transitionAlpha; }
    public boolean isEntering() { return entering; }

    public int getTreatedSurvivorsCount() { return bandageStock + salineStock + firstAidStock; }
    public int getBandageStock() { return bandageStock; }
    public int getSalineStock() { return salineStock; }
    public int getFirstAidStock() { return firstAidStock; }

    public boolean isTreating() { return isTreating; }
    public double getTreatmentProgress() { return treatmentProgress; }
    public String getCurrentTreatment() { return currentTreatment; }

    public boolean consumeInjuryKit(InjuryType type) {
        if (type == InjuryType.MINOR_CUT && bandageStock > 0) {
            bandageStock--;
            return true;
        } else if (type == InjuryType.DEHYDRATION_SHOCK && salineStock > 0) {
            salineStock--;
            return true;
        } else if (type == InjuryType.CRITICAL_TRAUMA && firstAidStock > 0) {
            firstAidStock--;
            return true;
        }
        return false;
    }

    public void consumeAnyKit() {
        if (bandageStock > 0) bandageStock--;
        else if (salineStock > 0) salineStock--;
        else if (firstAidStock > 0) firstAidStock--;
    }

    public void addSupplies(int amount) {
        bandageStock += amount;
        salineStock += amount;
        firstAidStock += amount;
    }
}
