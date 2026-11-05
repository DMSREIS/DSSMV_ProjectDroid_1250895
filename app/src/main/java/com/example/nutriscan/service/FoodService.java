package com.example.nutriscan.service;

import com.example.nutriscan.helper.MainThread;
import com.example.nutriscan.json.FirebaseJson;
import com.example.nutriscan.model.Food;
import com.example.nutriscan.network.FirebaseRest;
import com.example.nutriscan.network.HttpResponse;

import java.util.List;
import java.util.UUID;

public class FoodService {

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // PUT users/{uid}/foods/{id}: cria ou substitui o alimento
    public static void saveFood(Food food) {
        if (food.getId() == null || food.getId().isEmpty()) {
            String barcode = food.getBarcode();
            food.setId((barcode != null && !barcode.isEmpty())
                    ? barcode : UUID.randomUUID().toString());
        }
        final String id = food.getId();
        new Thread(() -> {
            try {
                FirebaseRest.put("foods/" + id, FirebaseJson.foodToJson(food));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // GET users/{uid}/foods
    public static void loadFoods(Callback<List<Food>> callback) {
        new Thread(() -> {
            try {
                HttpResponse response = FirebaseRest.get("foods");
                List<Food> foods = FirebaseJson.jsonToFoodList(response.getBody());
                MainThread.post(() -> callback.onSuccess(foods));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // DELETE users/{uid}/foods/{id}
    public static void deleteFood(String id, Callback<Void> callback) {
        if (id == null || id.isEmpty()) {
            callback.onError(new IllegalArgumentException("Alimento inválido"));
            return;
        }
        new Thread(() -> {
            try {
                FirebaseRest.delete("foods/" + id);
                MainThread.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }
}