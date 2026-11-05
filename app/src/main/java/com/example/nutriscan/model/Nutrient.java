package com.example.nutriscan.model;

import java.util.Locale;

public enum Nutrient {
    KCAL("kcal", "Calorias", "kcal"),
    PROTEINS("proteins", "Proteínas", "g"),
    CARBS("carbs", "Hidratos", "g"),
    FAT("fat", "Gorduras", "g");

    private final String key;
    private final String label;
    private final String unit;

    Nutrient(String key, String label, String unit) {
        this.key = key;
        this.label = label;
        this.unit = unit;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public String getUnit() { return unit; }

    // valor deste nutriente numa refeição
    public double of(Meal meal) {
        switch (this) {
            case KCAL: return meal.getKcal();
            case PROTEINS: return meal.getProteins();
            case CARBS: return meal.getCarbs();
            default: return meal.getFat();
        }
    }

    // 2000 -> "2000", 12.5 -> "12,5"
    public String format(double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.getDefault(), "%.0f", value);
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }
}