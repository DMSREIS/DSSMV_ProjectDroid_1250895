package com.example.nutriscan.service;

import com.example.nutriscan.helper.MainThread;
import com.example.nutriscan.json.FirebaseJson;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.network.FirebaseRest;
import com.example.nutriscan.network.HttpResponse;

import java.util.List;
import java.util.Map;

public class GoalService {

    // GET users/{uid}/goals
    public static void loadGoals(FoodService.Callback<Map<String, Goal>> callback) {
        new Thread(() -> {
            try {
                HttpResponse response = FirebaseRest.get("goals");
                Map<String, Goal> goals = FirebaseJson.jsonToGoalMap(response.getBody());
                MainThread.post(() -> callback.onSuccess(goals));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // PUT users/{uid}/goals: substitui todas as metas de uma só vez.
    // Por isso toDelete já não é preciso: o que não estiver em toSave deixa de existir.
    public static void saveGoals(List<Goal> toSave, List<String> toDelete,
                                 FoodService.Callback<Void> callback) {
        new Thread(() -> {
            try {
                FirebaseRest.put("goals", FirebaseJson.goalsToJson(toSave));
                MainThread.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }
}