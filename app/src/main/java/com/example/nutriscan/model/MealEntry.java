package com.example.nutriscan.model;

public class MealEntry {
    private String name;
    private String brand;
    private double grams;
    private double kcal, proteins, carbs, fat; // já calculados para estas gramas

    public MealEntry() { }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public double getGrams() { return grams; }
    public void setGrams(double grams) { this.grams = grams; }

    public double getKcal() { return kcal; }
    public void setKcal(double kcal) { this.kcal = kcal; }

    public double getProteins() { return proteins; }
    public void setProteins(double proteins) { this.proteins = proteins; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
}