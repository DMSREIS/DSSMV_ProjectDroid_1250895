package com.example.nutriscan;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.helper.MealNamer;
import com.example.nutriscan.helper.DateHelper;
import com.example.nutriscan.helper.MonthStatsCalculator;
import com.example.nutriscan.helper.ProgressRenderer;
import com.example.nutriscan.model.CurrentMeal;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MealEntry;
import com.example.nutriscan.service.FoodService;
import com.example.nutriscan.service.GoalService;
import com.example.nutriscan.service.MealService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DayDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DATE_KEY = "dateKey";

    private String dateKey;
    private LinearLayout layoutGoals, layoutMeals;
    private TextView tvDayStatus, tvNoGoals, tvNoMeals, tvMealsHint;
    private int defaultStatusColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dateKey = getIntent().getStringExtra(EXTRA_DATE_KEY);
        if (dateKey == null) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_day_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dayDetail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        layoutGoals = findViewById(R.id.layoutGoals);
        layoutMeals = findViewById(R.id.layoutMeals);
        tvDayStatus = findViewById(R.id.tvDayStatus);
        tvNoGoals = findViewById(R.id.tvNoGoals);
        tvNoMeals = findViewById(R.id.tvNoMeals);
        tvMealsHint = findViewById(R.id.tvMealsHint);
        defaultStatusColor = tvDayStatus.getCurrentTextColor();

        // "2026-10-04" -> "4 Out 2026"
        String[] parts = dateKey.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]) - 1;
        int day = Integer.parseInt(parts[2]);
        ((TextView) findViewById(R.id.tvDayTitle))
                .setText(DateHelper.dayLabel(day, month) + " " + year);

        // Adicionar uma refeição a este dia (hoje ou passado)
        findViewById(R.id.btnAddMeal).setOnClickListener(v -> {
            CurrentMeal.clear();
            CurrentMeal.setDateKey(dateKey);
            startActivity(new Intent(DayDetailActivity.this, AddMealActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutMeals != null) {
            load(); // também recarrega ao voltar do registo de uma refeição
        }
    }

    private void load() {
        GoalService.loadGoals(new FoodService.Callback<Map<String, Goal>>() {
            @Override
            public void onSuccess(Map<String, Goal> goals) {
                MealService.loadMealsForDay(dateKey, new FoodService.Callback<List<Meal>>() {
                    @Override
                    public void onSuccess(List<Meal> meals) {
                        if (!isFinishing() && !isDestroyed()) {
                            render(goals, meals);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isFinishing() && !isDestroyed()) {
                            tvNoMeals.setText("Não foi possível carregar as refeições.");
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isFinishing() && !isDestroyed()) {
                    tvNoMeals.setText("Não foi possível carregar os dados.");
                }
            }
        });
    }

    private void render(Map<String, Goal> goals, List<Meal> meals) {
        boolean anyGoal = ProgressRenderer.render(this, layoutGoals, goals, meals);
        tvNoGoals.setVisibility(anyGoal ? View.GONE : View.VISIBLE);

        tvDayStatus.setTextColor(defaultStatusColor);
        if (meals.isEmpty()) {
            tvDayStatus.setText("Sem refeições registadas neste dia.");
        } else if (anyGoal) {
            boolean met = MonthStatsCalculator.allGoalsMet(goals, meals);
            tvDayStatus.setText(met ? "\u2713 Metas cumpridas" : "\u2717 Metas não cumpridas");
            tvDayStatus.setTextColor(ContextCompat.getColor(this,
                    met ? R.color.day_met : R.color.day_missed));
        } else {
            tvDayStatus.setText("");
        }

        renderMeals(meals);
    }

    private void renderMeals(List<Meal> meals) {
        layoutMeals.removeAllViews();
        if (meals.isEmpty()) {
            tvNoMeals.setText("Sem refeições registadas neste dia.");
            tvNoMeals.setVisibility(View.VISIBLE);
            tvMealsHint.setVisibility(View.GONE);
            return;
        }
        tvNoMeals.setVisibility(View.GONE);
        tvMealsHint.setVisibility(View.VISIBLE);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Efeito de toque (ondulação) nos blocos das refeições
        TypedValue ripple = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true);

        for (int i = 0; i < meals.size(); i++) {
            final Meal meal = meals.get(i);
            final String name = MealNamer.displayName(meals, i);

            // A hora só aparece se a refeição foi registada nesse mesmo dia
            boolean showTime = DateHelper.toKey(new Date(meal.getTimestamp())).equals(dateKey);
            String time = showTime ? " \u00b7 " + timeFormat.format(new Date(meal.getTimestamp())) : "";

            LinearLayout group = new LinearLayout(this);
            group.setOrientation(LinearLayout.VERTICAL);
            group.setPadding(dp(8), dp(8), dp(8), dp(8));
            group.setBackgroundResource(ripple.resourceId);
            group.setLongClickable(true);
            group.setOnLongClickListener(v -> {
                confirmDelete(meal, name);
                return true;
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = i == 0 ? 0 : dp(8);
            group.setLayoutParams(lp);

            addText(group, String.format(Locale.getDefault(), "%s%s \u00b7 %.0f kcal",
                    name, time, meal.getKcal()), 16, true, 0);
            for (MealEntry e : meal.getItems()) {
                addText(group, String.format(Locale.getDefault(),
                        "\u2022 %s \u2014 %.0f g \u00b7 %.0f kcal",
                        e.getName(), e.getGrams(), e.getKcal()), 14, false, 4);
            }
            layoutMeals.addView(group);
        }
    }

    private void confirmDelete(Meal meal, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Apagar refeição?")
                .setMessage(String.format(Locale.getDefault(), "%s \u00b7 %.0f kcal",
                        name, meal.getKcal()))
                .setPositiveButton("Apagar", (dialog, which) -> deleteMeal(meal))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteMeal(Meal meal) {
        MealService.deleteMeal(dateKey, meal.getId(), new FoodService.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(DayDetailActivity.this, "Refeição apagada",
                        Toast.LENGTH_SHORT).show();
                load(); // a lista e a numeração acertam-se sozinhas
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Toast.makeText(DayDetailActivity.this,
                        "Não foi possível apagar a refeição", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addText(LinearLayout parent, String text, float sizeSp, boolean bold,
                         int topMarginDp) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        if (bold) {
            tv.setTypeface(null, Typeface.BOLD);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(topMarginDp);
        tv.setLayoutParams(lp);
        parent.addView(tv);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}