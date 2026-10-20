package com.example.nutriscan.model;

import java.io.Serializable;

public class Food implements Serializable {
    private String id;
    private String barcode;
    private String name;
    private String brand;
    private Double kcal;
    private Double proteins;
    private Double carbohydrates;
    private Double fat;

    public Food() { }

    public Food(String barcode, String name, String brand,
                Double kcal, Double proteins, Double carbohydrates, Double fat) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.kcal = kcal;
        this.proteins = proteins;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getKcal() { return kcal; }
    public void setKcal(Double kcal) { this.kcal = kcal; }

    public Double getProteins() { return proteins; }
    public void setProteins(Double proteins) { this.proteins = proteins; }

    public Double getCarbohydrates() { return carbohydrates; }
    public void setCarbohydrates(Double carbohydrates) { this.carbohydrates = carbohydrates; }

    public Double getFat() { return fat; }
    public void setFat(Double fat) { this.fat = fat; }
}