package com.flow.model;

public class CafeteriaState {
    private boolean insideCafeteria = false;
    private double transitionAlpha = 0.0; // Fade transition
    private boolean transitioning = false;
    private boolean entering = true;

    private int riceCurryStock = 2;
    private int fishStewStock = 2;
    private int teaSnacksStock = 2;
    private boolean isCooking = false;
    private double cookingProgress = 0.0;
    private String currentRecipe = "";

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
                insideCafeteria = true;
            } else {
                insideCafeteria = false;
            }
        }
    }

    public void startCooking(String recipeName, double durationSec) {
        if (isCooking) return;
        this.currentRecipe = recipeName;
        this.cookingProgress = 0.0;
        this.isCooking = true;
    }

    public boolean updateCooking(double dt, double durationSec) {
        if (!isCooking) return false;
        cookingProgress += dt / durationSec;
        if (cookingProgress >= 1.0) {
            cookingProgress = 1.0;
            isCooking = false;
            if ("Warm Rice & Curry".equalsIgnoreCase(currentRecipe)) {
                riceCurryStock += 2;
            } else if ("Grilled Fish Stew".equalsIgnoreCase(currentRecipe)) {
                fishStewStock += 2;
            } else {
                teaSnacksStock += 2;
            }
            return true; // Cooking completed (yields 2 portions)!
        }
        return false;
    }

    public boolean isInsideCafeteria() { return insideCafeteria; }
    public boolean isTransitioning() { return transitioning; }
    public double getTransitionAlpha() { return transitionAlpha; }
    public boolean isEntering() { return entering; }

    public int getCookedMealsCount() { return riceCurryStock + fishStewStock + teaSnacksStock; }
    public int getRiceCurryStock() { return riceCurryStock; }
    public int getFishStewStock() { return fishStewStock; }
    public int getTeaSnacksStock() { return teaSnacksStock; }

    public boolean isCooking() { return isCooking; }
    public double getCookingProgress() { return cookingProgress; }
    public String getCurrentRecipe() { return currentRecipe; }

    public void consumeMeal() {
        if (riceCurryStock > 0) riceCurryStock--;
        else if (fishStewStock > 0) fishStewStock--;
        else if (teaSnacksStock > 0) teaSnacksStock--;
    }

    public boolean consumeCravingMeal(SurvivorCraving craving) {
        if (craving == SurvivorCraving.RICE_CURRY && riceCurryStock > 0) {
            riceCurryStock--;
            return true;
        } else if (craving == SurvivorCraving.FISH_STEW && fishStewStock > 0) {
            fishStewStock--;
            return true;
        } else if (craving == SurvivorCraving.TEA_SNACKS && teaSnacksStock > 0) {
            teaSnacksStock--;
            return true;
        }
        return false;
    }

    public void addSupplies(int amount) {
        riceCurryStock += amount;
        fishStewStock += amount;
        teaSnacksStock += amount;
    }
}
