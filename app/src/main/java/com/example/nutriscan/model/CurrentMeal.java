package com.example.nutriscan.model;

import java.util.ArrayList;
import java.util.List;

public class CurrentMeal {
    private static final List<MealItem> items = new ArrayList<>();

    public static List<MealItem> getItems() { return items; }
    public static void add(MealItem item) { items.add(item); }
    public static void remove(int index) { items.remove(index); }
    public static void clear() { items.clear(); }

    public static double totalKcal() {
        double total = 0;
        for (MealItem item : items) {
            total += item.getKcal();
        }
        return total;
    }
}