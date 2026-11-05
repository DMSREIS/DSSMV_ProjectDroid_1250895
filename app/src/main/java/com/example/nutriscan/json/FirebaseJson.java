package com.example.nutriscan.json;

import com.example.nutriscan.model.Food;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MealEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FirebaseJson {

    // A base de dados devolve o texto "null" quando não há dados
    private static JSONObject parseObject(String json) {
        if (json == null || json.trim().isEmpty() || json.trim().equals("null")) {
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return null;
        }
    }

    private static Double getDouble(JSONObject o, String key) {
        double value = o.optDouble(key, Double.NaN);
        return Double.isNaN(value) ? null : value;
    }

    // ---------------- Food ----------------

    public static String foodToJson(Food food) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("barcode", food.getBarcode());
        o.put("name", food.getName());
        o.put("brand", food.getBrand());
        o.put("kcal", food.getKcal());
        o.put("proteins", food.getProteins());
        o.put("carbohydrates", food.getCarbohydrates());
        o.put("fat", food.getFat());
        return o.toString();
    }

    private static Food jsonToFood(String id, JSONObject o) {
        Food f = new Food();
        f.setId(id);
        f.setBarcode(o.optString("barcode", ""));
        f.setName(o.optString("name", ""));
        f.setBrand(o.optString("brand", ""));
        f.setKcal(getDouble(o, "kcal"));
        f.setProteins(getDouble(o, "proteins"));
        f.setCarbohydrates(getDouble(o, "carbohydrates"));
        f.setFat(getDouble(o, "fat"));
        return f;
    }

    public static List<Food> jsonToFoodList(String json) {
        List<Food> foods = new ArrayList<>();
        JSONObject root = parseObject(json);
        if (root == null) {
            return foods;
        }
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject o = root.optJSONObject(key);
            if (o != null) {
                foods.add(jsonToFood(key, o));
            }
        }
        Collections.sort(foods, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return foods;
    }

    // ---------------- Alimentos de uma refeição ----------------

    private static JSONArray entriesToJson(List<MealEntry> entries) throws JSONException {
        JSONArray items = new JSONArray();
        for (MealEntry e : entries) {
            JSONObject i = new JSONObject();
            i.put("name", e.getName());
            i.put("brand", e.getBrand());
            i.put("grams", e.getGrams());
            i.put("kcal", e.getKcal());
            i.put("proteins", e.getProteins());
            i.put("carbs", e.getCarbs());
            i.put("fat", e.getFat());
            items.put(i);
        }
        return items;
    }

    private static List<MealEntry> jsonToEntries(JSONObject o) {
        List<MealEntry> items = new ArrayList<>();
        JSONArray arr = o.optJSONArray("items");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject e = arr.optJSONObject(i);
                if (e == null) {
                    continue;
                }
                MealEntry entry = new MealEntry();
                entry.setName(e.optString("name", ""));
                entry.setBrand(e.optString("brand", ""));
                entry.setGrams(e.optDouble("grams", 0));
                entry.setKcal(e.optDouble("kcal", 0));
                entry.setProteins(e.optDouble("proteins", 0));
                entry.setCarbs(e.optDouble("carbs", 0));
                entry.setFat(e.optDouble("fat", 0));
                items.add(entry);
            }
        }
        return items;
    }

    // ---------------- Meal (refeição registada num dia) ----------------

    public static String mealToJson(Meal meal) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("dateKey", meal.getDateKey());
        o.put("timestamp", meal.getTimestamp());
        o.put("kcal", meal.getKcal());
        o.put("proteins", meal.getProteins());
        o.put("carbs", meal.getCarbs());
        o.put("fat", meal.getFat());
        if (meal.getName() != null && !meal.getName().trim().isEmpty()) {
            o.put("name", meal.getName());
        }
        o.put("items", entriesToJson(meal.getItems()));
        return o.toString();
    }

    // Refeição guardada (modelo): nome, totais e alimentos, sem dia nem hora
    public static String savedMealToJson(Meal meal) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("name", meal.getName());
        o.put("kcal", meal.getKcal());
        o.put("proteins", meal.getProteins());
        o.put("carbs", meal.getCarbs());
        o.put("fat", meal.getFat());
        o.put("items", entriesToJson(meal.getItems()));
        return o.toString();
    }

    private static Meal jsonToMeal(String id, JSONObject o) {
        Meal m = new Meal();
        m.setId(id);
        m.setName(o.optString("name", ""));
        m.setDateKey(o.optString("dateKey", ""));
        m.setTimestamp(o.optLong("timestamp", 0));
        m.setKcal(o.optDouble("kcal", 0));
        m.setProteins(o.optDouble("proteins", 0));
        m.setCarbs(o.optDouble("carbs", 0));
        m.setFat(o.optDouble("fat", 0));
        m.setItems(jsonToEntries(o));
        return m;
    }

    public static List<Meal> jsonToMealList(String json) {
        List<Meal> meals = new ArrayList<>();
        JSONObject root = parseObject(json);
        if (root == null) {
            return meals;
        }
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject o = root.optJSONObject(key);
            if (o != null) {
                meals.add(jsonToMeal(key, o));
            }
        }
        Collections.sort(meals, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        return meals;
    }

    // {"-Nabc": {refeição guardada}, ...}  →  lista por ordem alfabética
    public static List<Meal> jsonToSavedMealList(String json) {
        List<Meal> meals = new ArrayList<>();
        JSONObject root = parseObject(json);
        if (root == null) {
            return meals;
        }
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject o = root.optJSONObject(key);
            if (o != null) {
                meals.add(jsonToMeal(key, o));
            }
        }
        Collections.sort(meals, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return meals;
    }

    // {"2026-10-20": {"-Nabc": {refeição}, ...}, ...}  →  dia → refeições
    public static Map<String, List<Meal>> jsonToMealsByDay(String json) {
        Map<String, List<Meal>> byDay = new TreeMap<>();
        JSONObject root = parseObject(json);
        if (root == null) {
            return byDay;
        }
        Iterator<String> days = root.keys();
        while (days.hasNext()) {
            String dayKey = days.next();
            JSONObject dayObj = root.optJSONObject(dayKey);
            if (dayObj == null) {
                continue;
            }
            List<Meal> meals = new ArrayList<>();
            Iterator<String> ids = dayObj.keys();
            while (ids.hasNext()) {
                String id = ids.next();
                JSONObject o = dayObj.optJSONObject(id);
                if (o != null) {
                    meals.add(jsonToMeal(id, o));
                }
            }
            Collections.sort(meals, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
            byDay.put(dayKey, meals);
        }
        return byDay;
    }

    // ---------------- Goal ----------------

    // Todas as metas num só objeto: {"kcal": {...}, "proteins": {...}}
    public static String goalsToJson(List<Goal> goals) throws JSONException {
        JSONObject root = new JSONObject();
        for (Goal g : goals) {
            JSONObject o = new JSONObject();
            o.put("nutrient", g.getNutrient());
            o.put("target", g.getTarget());
            o.put("type", g.getType());
            root.put(g.getNutrient(), o);
        }
        return root.toString();
    }

    public static Map<String, Goal> jsonToGoalMap(String json) {
        Map<String, Goal> goals = new HashMap<>();
        JSONObject root = parseObject(json);
        if (root == null) {
            return goals;
        }
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject o = root.optJSONObject(key);
            if (o != null) {
                goals.put(key, new Goal(key, o.optDouble("target", 0),
                        o.optString("type", Goal.TYPE_MAX)));
            }
        }
        return goals;
    }
}