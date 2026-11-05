package com.example.nutriscan.service;

import com.example.nutriscan.helper.DateHelper;
import com.example.nutriscan.helper.MainThread;
import com.example.nutriscan.json.FirebaseJson;
import com.example.nutriscan.model.CurrentMeal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MealEntry;
import com.example.nutriscan.model.MealItem;
import com.example.nutriscan.network.FirebaseRest;
import com.example.nutriscan.network.HttpResponse;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MealService {

    // Monta a refeição a partir do que está em curso. O dia é o do CurrentMeal (ou hoje).
    public static Meal buildFromCurrent() {
        Date now = new Date();
        String dateKey = CurrentMeal.getDateKey();
        if (dateKey == null) {
            dateKey = DateHelper.toKey(now);
        }

        Meal meal = new Meal();
        meal.setTimestamp(now.getTime()); // momento real do registo
        meal.setDateKey(dateKey);

        List<MealEntry> entries = new ArrayList<>();
        double kcal = 0, proteins = 0, carbs = 0, fat = 0;
        for (MealItem item : CurrentMeal.getItems()) {
            MealEntry e = new MealEntry();
            e.setName(item.getFood().getName());
            e.setBrand(item.getFood().getBrand());
            e.setGrams(item.getGrams());
            e.setKcal(item.getKcal());
            e.setProteins(item.getProteins());
            e.setCarbs(item.getCarbohydrates());
            e.setFat(item.getFat());
            entries.add(e);

            kcal += item.getKcal();
            proteins += item.getProteins();
            carbs += item.getCarbohydrates();
            fat += item.getFat();
        }
        meal.setItems(entries);
        meal.setKcal(kcal);
        meal.setProteins(proteins);
        meal.setCarbs(carbs);
        meal.setFat(fat);
        return meal;
    }

    // POST users/{uid}/meals/{dia}: devolve a chave que a base de dados gerou
    public static void saveMeal(Meal meal, FoodService.Callback<String> callback) {
        final String path = "meals/" + meal.getDateKey();
        new Thread(() -> {
            try {
                HttpResponse response = FirebaseRest.post(path, FirebaseJson.mealToJson(meal));
                // A resposta do POST é {"name":"-Nabc..."}: a chave da refeição nova
                String id = new JSONObject(response.getBody()).optString("name", "");
                MainThread.post(() -> callback.onSuccess(id));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // Regista uma refeição guardada como refeição nova, com o nome dela.
    // O dia é o do CurrentMeal (ou hoje).
    public static void addSavedMeal(Meal template, FoodService.Callback<Void> callback) {
        Date now = new Date();
        String dateKey = CurrentMeal.getDateKey();
        if (dateKey == null) {
            dateKey = DateHelper.toKey(now);
        }

        Meal meal = new Meal();
        meal.setName(template.getName());
        meal.setDateKey(dateKey);
        meal.setTimestamp(now.getTime());
        meal.setKcal(template.getKcal());
        meal.setProteins(template.getProteins());
        meal.setCarbs(template.getCarbs());
        meal.setFat(template.getFat());
        meal.setItems(new ArrayList<>(template.getItems()));

        saveMeal(meal, new FoodService.Callback<String>() {
            @Override
            public void onSuccess(String id) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    // PUT users/{uid}/meals/{dia}/{id}/name: dá nome a uma refeição já registada
    public static void renameMeal(String dateKey, String mealId, String name,
                                  FoodService.Callback<Void> callback) {
        if (dateKey == null || mealId == null || mealId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Refeição inválida"));
            return;
        }
        new Thread(() -> {
            try {
                // O corpo de um PUT com texto tem de ser um JSON válido: o texto entre aspas
                FirebaseRest.put("meals/" + dateKey + "/" + mealId + "/name",
                        JSONObject.quote(name));
                MainThread.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // GET users/{uid}/meals/{dia}
    public static void loadMealsForDay(String dateKey, FoodService.Callback<List<Meal>> callback) {
        new Thread(() -> {
            try {
                HttpResponse response = FirebaseRest.get("meals/" + dateKey);
                List<Meal> meals = FirebaseJson.jsonToMealList(response.getBody());
                MainThread.post(() -> callback.onSuccess(meals));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // DELETE users/{uid}/meals/{dia}/{id}
    public static void deleteMeal(String dateKey, String mealId, FoodService.Callback<Void> callback) {
        if (dateKey == null || mealId == null || mealId.isEmpty()) {
            callback.onError(new IllegalArgumentException("Refeição inválida"));
            return;
        }
        new Thread(() -> {
            try {
                FirebaseRest.delete("meals/" + dateKey + "/" + mealId);
                MainThread.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // GET users/{uid}/meals?orderBy="$key"&startAt="2026-10-01"&endAt="2026-10-31"
    public static void loadMealsForMonth(int year, int month,
                                         FoodService.Callback<Map<String, List<Meal>>> callback) {
        String first = DateHelper.dateKey(year, month, 1);
        String last = DateHelper.dateKey(year, month, DateHelper.daysInMonth(year, month));
        // %22 = aspas, %24 = $ (os valores têm de levar aspas e vão codificados)
        final String query = "&orderBy=%22%24key%22&startAt=%22" + first
                + "%22&endAt=%22" + last + "%22";

        new Thread(() -> {
            try {
                HttpResponse response = FirebaseRest.get("meals", query);
                Map<String, List<Meal>> byDay = FirebaseJson.jsonToMealsByDay(response.getBody());
                MainThread.post(() -> callback.onSuccess(byDay));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }
}