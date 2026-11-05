package com.example.nutriscan.model;

import java.util.ArrayList;
import java.util.List;

public class CurrentMeal {
    private static final List<MealItem> items = new ArrayList<>();
    private static String dateKey = null;      // dia da refeição em curso; null = hoje
    private static Meal pendingTemplate = null; // refeição concluída, à espera do "guardar?"

    public static List<MealItem> getItems() { return items; }
    public static void add(MealItem item) { items.add(item); }
    public static void remove(int index) { items.remove(index); }

    public static String getDateKey() { return dateKey; }
    public static void setDateKey(String key) { dateKey = key; }

    public static Meal getPendingTemplate() { return pendingTemplate; }
    public static void setPendingTemplate(Meal meal) { pendingTemplate = meal; }

    public static void clear() {
        items.clear();
        dateKey = null;
        pendingTemplate = null;
    }

    public static double totalKcal() {
        double total = 0;
        for (MealItem item : items) {
            total += item.getKcal();
        }
        return total;
    }
}