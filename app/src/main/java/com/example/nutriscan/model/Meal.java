package com.example.nutriscan.model;

import java.util.ArrayList;
import java.util.List;

public class Meal {
    private String id;         // chave na base de dados (não vai dentro do JSON)
    private String name;       // nome da refeição guardada; vazio = "Refeição N"
    private String dateKey;    // "yyyy-MM-dd"
    private long timestamp;    // momento em que foi concluída
    private double kcal, proteins, carbs, fat;
    private List<MealEntry> items = new ArrayList<>();

    public Meal() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Nome a mostrar: o da refeição guardada, ou "Refeição N" pela posição no dia
    public String getDisplayName(int index) {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return "Refeição " + (index + 1);
    }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getKcal() { return kcal; }
    public void setKcal(double kcal) { this.kcal = kcal; }

    public double getProteins() { return proteins; }
    public void setProteins(double proteins) { this.proteins = proteins; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public List<MealEntry> getItems() { return items; }
    public void setItems(List<MealEntry> items) { this.items = items; }
}