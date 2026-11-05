package com.example.nutriscan.model;

public class Goal {
    public static final String TYPE_MAX = "max";
    public static final String TYPE_MIN = "min";

    private String nutrient;   // chave do Nutrient: "kcal", "proteins", ...
    private double target;
    private String type;       // "max" ou "min"

    public Goal() { }

    public Goal(String nutrient, double target, String type) {
        this.nutrient = nutrient;
        this.target = target;
        this.type = type;
    }

    public String getNutrient() { return nutrient; }
    public void setNutrient(String nutrient) { this.nutrient = nutrient; }

    public double getTarget() { return target; }
    public void setTarget(double target) { this.target = target; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}