package com.example.nutriscan.service;

import android.util.Log;

import com.example.nutriscan.helper.AchievementEvaluator;
import com.example.nutriscan.helper.DateHelper;
import com.example.nutriscan.helper.MainThread;
import com.example.nutriscan.json.FirebaseJson;
import com.example.nutriscan.model.Achievement;
import com.example.nutriscan.model.AchievementSnapshot;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.network.FirebaseRest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AchievementService {

    public static class Result {
        public final AchievementSnapshot snapshot;
        public final Map<String, Long> unlocked;        // id -> momento em que foi desbloqueada
        public final List<Achievement> newlyUnlocked;   // as que acabaram de ser desbloqueadas

        Result(AchievementSnapshot snapshot, Map<String, Long> unlocked,
               List<Achievement> newlyUnlocked) {
            this.snapshot = snapshot;
            this.unlocked = unlocked;
            this.newlyUnlocked = newlyUnlocked;
        }

        public int getUnlockedCount() {
            int count = 0;
            for (Achievement a : Achievement.values()) {
                if (unlocked.containsKey(a.name())) {
                    count++;
                }
            }
            return count;
        }
    }

    // Verifica tudo e desbloqueia o que for novo (em segundo plano)
    public static void refresh(FoodService.Callback<Result> callback) {
        new Thread(() -> {
            try {
                final Result result = doRefresh();
                MainThread.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                MainThread.post(() -> callback.onError(e));
            }
        }).start();
    }

    // synchronized: se dois ecrãs pedirem ao mesmo tempo, o segundo espera pelo primeiro
    private static synchronized Result doRefresh() throws Exception {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Sem sessão iniciada");
        }

        // GET achievements: o que já está desbloqueado
        Map<String, Long> unlocked = parseUnlocked(FirebaseRest.get("achievements").getBody());

        // GET goals
        Map<String, Goal> goals = FirebaseJson.jsonToGoalMap(FirebaseRest.get("goals").getBody());

        // GET meals: só a janela dos últimos dias
        Date today = new Date();
        Calendar start = Calendar.getInstance();
        start.setTime(today);
        start.add(Calendar.DAY_OF_MONTH, -(AchievementEvaluator.WINDOW_DAYS - 1));
        String query = "&orderBy=%22%24key%22&startAt=%22" + DateHelper.toKey(start.getTime())
                + "%22&endAt=%22" + DateHelper.toKey(today) + "%22";
        Map<String, List<Meal>> mealsByDay =
                FirebaseJson.jsonToMealsByDay(FirebaseRest.get("meals", query).getBody());

        // GET foods?shallow=true: só as chaves, para contar os alimentos
        int foodCount = countKeys(FirebaseRest.get("foods", "&shallow=true").getBody());

        // A foto só se verifica se a conquista ainda estiver por desbloquear
        boolean hasPhoto = true;
        if (!unlocked.containsKey(Achievement.PHOTO.name())) {
            hasPhoto = hasValue(FirebaseRest.get("profile/photo").getBody());
        }

        String firstKey = "0000-00-00";
        if (user.getMetadata() != null) {
            firstKey = DateHelper.toKey(new Date(user.getMetadata().getCreationTimestamp()));
        }

        AchievementSnapshot snapshot = AchievementEvaluator.evaluate(
                goals, mealsByDay, foodCount, hasPhoto, today, firstKey);

        // PUT achievements/{id}: grava cada nova conquista
        long now = System.currentTimeMillis();
        List<Achievement> newly = new ArrayList<>();
        for (Achievement a : Achievement.values()) {
            if (!unlocked.containsKey(a.name()) && a.isMet(snapshot)) {
                try {
                    FirebaseRest.put("achievements/" + a.name(), "{\"unlockedAt\":" + now + "}");
                    unlocked.put(a.name(), now);
                    newly.add(a);
                } catch (Exception e) {
                    Log.w("AchievementService", "Não foi possível gravar " + a.name(), e);
                }
            }
        }
        return new Result(snapshot, unlocked, newly);
    }

    private static Map<String, Long> parseUnlocked(String json) {
        Map<String, Long> result = new HashMap<>();
        if (!hasValue(json)) {
            return result;
        }
        try {
            JSONObject root = new JSONObject(json);
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject o = root.optJSONObject(key);
                result.put(key, o == null ? 0L : o.optLong("unlockedAt", 0));
            }
        } catch (JSONException e) {
            // sem dados válidos: fica vazio
        }
        return result;
    }

    private static int countKeys(String json) {
        if (!hasValue(json)) {
            return 0;
        }
        try {
            return new JSONObject(json).length();
        } catch (JSONException e) {
            return 0;
        }
    }

    // A base de dados devolve o texto "null" quando não há nada
    private static boolean hasValue(String body) {
        return body != null && !body.trim().isEmpty() && !body.trim().equals("null");
    }
}