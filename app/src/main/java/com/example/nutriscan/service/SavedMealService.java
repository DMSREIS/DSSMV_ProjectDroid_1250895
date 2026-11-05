package com.example.nutriscan.service;

import com.example.nutriscan.helper.MainThread;
import com.example.nutriscan.helper.TextHelper;
import com.example.nutriscan.json.FirebaseJson;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.network.FirebaseRest;
import com.example.nutriscan.network.HttpResponse;

import java.util.ArrayList;
import java.util.List;

public class SavedMealService {

    public static class DuplicateNameException extends Exception {
        public DuplicateNameException() {
            super("Já existe uma refeição guardada com este nome");
        }
    }

    // GET users/{uid}/savedMeals
    public static void loadSavedMeals(FoodService.Callback<List<Meal>> callback) {
        new Thread(() -> {
            try {
                HttpResponse response = FirebaseRest.get("savedMeals");
                List<Meal> meals = FirebaseJson.jsonToSavedMealList(response.getBody());
                MainThread.post(() -> callback.onSuccess(meals));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // GET para ver se o nome já existe, e depois POST users/{uid}/savedMeals.
    // Devolve o nome com que ficou guardada (sem espaços a mais).
    public static void saveTemplate(Meal source, String name, FoodService.Callback<String> callback) {
        new Thread(() -> {
            try {
                HttpResponse response = FirebaseRest.get("savedMeals");
                List<Meal> existing = FirebaseJson.jsonToSavedMealList(response.getBody());
                String wanted = TextHelper.normalize(name);
                for (Meal m : existing) {
                    if (TextHelper.normalize(m.getName()).equals(wanted)) {
                        throw new DuplicateNameException();
                    }
                }

                final String cleanName = name.trim().replaceAll("\\s+", " ");
                Meal template = new Meal();
                template.setName(cleanName);
                template.setKcal(source.getKcal());
                template.setProteins(source.getProteins());
                template.setCarbs(source.getCarbs());
                template.setFat(source.getFat());
                template.setItems(new ArrayList<>(source.getItems()));

                FirebaseRest.post("savedMeals", FirebaseJson.savedMealToJson(template));
                MainThread.post(() -> callback.onSuccess(cleanName));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // DELETE users/{uid}/savedMeals/{id}
    public static void deleteSavedMeal(String id, FoodService.Callback<Void> callback) {
        if (id == null || id.isEmpty()) {
            callback.onError(new IllegalArgumentException("Refeição inválida"));
            return;
        }
        new Thread(() -> {
            try {
                FirebaseRest.delete("savedMeals/" + id);
                MainThread.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }
}