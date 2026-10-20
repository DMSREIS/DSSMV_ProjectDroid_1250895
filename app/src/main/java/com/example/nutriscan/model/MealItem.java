package com.example.nutriscan.model;

public class MealItem {
    private final Food food;
    private final double grams;

    public MealItem(Food food, double grams) {
        this.food = food;
        this.grams = grams;
    }

    public Food getFood() { return food; }
    public double getGrams() { return grams; }

    public double getKcal() { return calc(food.getKcal()); }
    public double getProteins() { return calc(food.getProteins()); }
    public double getCarbohydrates() { return calc(food.getCarbohydrates()); }
    public double getFat() { return calc(food.getFat()); }

    // valor por 100 g -> valor para as gramas deste item
    private double calc(Double per100g) {
        return per100g == null ? 0 : per100g * grams / 100.0;
    }
}
