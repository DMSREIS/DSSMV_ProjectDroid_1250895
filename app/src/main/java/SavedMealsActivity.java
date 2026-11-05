package com.example.nutriscan;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.adapter.SavedMealAdapter;
import com.example.nutriscan.helper.DateHelper;
import com.example.nutriscan.model.CurrentMeal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.service.FoodService;
import com.example.nutriscan.service.MealService;
import com.example.nutriscan.service.SavedMealService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SavedMealsActivity extends AppCompatActivity {

    private final List<Meal> meals = new ArrayList<>();
    private SavedMealAdapter adapter;
    private TextView tvEmpty;
    private boolean busy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_meals);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.savedMeals), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView listMeals = findViewById(R.id.listMeals);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new SavedMealAdapter(this, meals);
        listMeals.setAdapter(adapter);
        listMeals.setEmptyView(tvEmpty);

        // Toque: adicionar (com confirmação)
        listMeals.setOnItemClickListener((parent, view, position, id) ->
                confirmAdd(meals.get(position)));

        // Toque longo: apagar (com confirmação)
        listMeals.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(meals.get(position));
            return true;
        });

        loadMeals();
    }

    private void loadMeals() {
        SavedMealService.loadSavedMeals(new FoodService.Callback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                meals.clear();
                meals.addAll(result);
                adapter.notifyDataSetChanged();
                tvEmpty.setText("Ainda não tens refeições guardadas.\n"
                        + "Quando concluíres uma refeição, podes guardá-la aqui.");
            }

            @Override
            public void onError(Exception e) {
                if (!isFinishing() && !isDestroyed()) {
                    tvEmpty.setText("Erro ao carregar as refeições guardadas.");
                }
            }
        });
    }

    private void confirmAdd(Meal meal) {
        if (busy) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Adicionar refeição?")
                .setMessage(String.format(Locale.getDefault(),
                        "%s \u00b7 %.0f kcal\n\nSerá registada %s.",
                        meal.getName(), meal.getKcal(), dayText()))
                .setPositiveButton("Adicionar", (dialog, which) -> addMeal(meal))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void addMeal(Meal meal) {
        busy = true;
        MealService.addSavedMeal(meal, new FoodService.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(SavedMealsActivity.this,
                        "\u00ab" + meal.getName() + "\u00bb registada", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception e) {
                busy = false;
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(SavedMealsActivity.this,
                            "Não foi possível registar a refeição", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void confirmDelete(Meal meal) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar refeição guardada?")
                .setMessage(meal.getName())
                .setPositiveButton("Apagar", (dialog, which) -> deleteMeal(meal))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteMeal(Meal meal) {
        SavedMealService.deleteSavedMeal(meal.getId(), new FoodService.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                meals.remove(meal);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(SavedMealsActivity.this,
                            "Não foi possível apagar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // "hoje" ou "no dia 4 Out"
    private String dayText() {
        String key = CurrentMeal.getDateKey();
        if (key == null || key.equals(DateHelper.todayKey())) {
            return "hoje";
        }
        String[] p = key.split("-");
        return "no dia " + DateHelper.dayLabel(Integer.parseInt(p[2]), Integer.parseInt(p[1]) - 1);
    }
}