package com.example.nutriscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.helper.CalendarRenderer;
import com.example.nutriscan.helper.DateHelper;
import com.example.nutriscan.helper.MonthStatsCalculator;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MonthStats;
import com.example.nutriscan.service.FoodService;
import com.example.nutriscan.service.GoalService;
import com.example.nutriscan.service.MealService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private int year, month;                  // mês que está a ser mostrado (0 = janeiro)
    private int minYear, minMonth;            // primeiro mês navegável (criação da conta)
    private int maxYear, maxMonth;            // último mês navegável (o atual)
    private int requestCounter = 0;
    private String todayKey, firstKey;

    private TextView tvMonthTitle, tvStreak, tvGoalsReached, tvHint;
    private GridLayout gridCalendar;
    private Button btnPrev, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.calendar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        gridCalendar = findViewById(R.id.gridCalendar);
        tvStreak = findViewById(R.id.tvStreak);
        tvGoalsReached = findViewById(R.id.tvGoalsReached);
        tvHint = findViewById(R.id.tvHint);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        Calendar now = Calendar.getInstance();
        year = maxYear = now.get(Calendar.YEAR);
        month = maxMonth = now.get(Calendar.MONTH);
        todayKey = DateHelper.todayKey();

        // Primeiro mês navegável: aquele em que a conta foi criada
        Calendar created = Calendar.getInstance();
        if (user.getMetadata() != null) {
            created.setTimeInMillis(user.getMetadata().getCreationTimestamp());
        }
        minYear = created.get(Calendar.YEAR);
        minMonth = created.get(Calendar.MONTH);
        firstKey = DateHelper.toKey(created.getTime());

        btnPrev.setOnClickListener(v -> changeMonth(-1));
        btnNext.setOnClickListener(v -> changeMonth(1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvMonthTitle != null) {
            loadMonth();
        }
    }

    private void changeMonth(int delta) {
        month += delta;
        if (month < 0) {
            month = 11;
            year--;
        } else if (month > 11) {
            month = 0;
            year++;
        }
        loadMonth();
    }

    private void updateNavButtons() {
        int current = year * 12 + month;
        btnPrev.setEnabled(current > minYear * 12 + minMonth);
        btnNext.setEnabled(current < maxYear * 12 + maxMonth);
    }

    private void loadMonth() {
        final int reqYear = year;
        final int reqMonth = month;
        final int requestId = ++requestCounter;

        tvMonthTitle.setText(DateHelper.monthTitle(reqYear, reqMonth));
        updateNavButtons();

        GoalService.loadGoals(new FoodService.Callback<Map<String, Goal>>() {
            @Override
            public void onSuccess(Map<String, Goal> goals) {
                if (requestId != requestCounter) {
                    return; // entretanto mudaste de mês
                }
                MealService.loadMealsForMonth(reqYear, reqMonth,
                        new FoodService.Callback<Map<String, List<Meal>>>() {
                            @Override
                            public void onSuccess(Map<String, List<Meal>> mealsByDay) {
                                if (requestId == requestCounter) {
                                    show(reqYear, reqMonth, goals, mealsByDay);
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                if (requestId == requestCounter) {
                                    showError();
                                }
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                if (requestId == requestCounter) {
                    showError();
                }
            }
        });
    }

    private void show(int y, int m, Map<String, Goal> goals,
                      Map<String, List<Meal>> mealsByDay) {
        MonthStats stats = MonthStatsCalculator.calculate(
                y, m, goals, mealsByDay, todayKey, firstKey);

        CalendarRenderer.render(this, gridCalendar, stats, todayKey, day -> openDay(y, m, day));

        // Maior streak: dias verdes seguidos dentro do mês
        if (stats.bestStreak == 0) {
            tvStreak.setText("Maior streak: 0");
        } else {
            String start = DateHelper.dayLabel(stats.streakStartDay, m);
            String range = stats.bestStreak == 1
                    ? start : start + " - " + DateHelper.dayLabel(stats.streakEndDay, m);
            tvStreak.setText("Maior streak: " + stats.bestStreak + " (" + range + ")");
        }

        // Objetivo alcançado: dias verdes / dias avaliados
        if (stats.daysEvaluated == 0) {
            tvGoalsReached.setText("Objetivo alcançado: sem dados");
        } else {
            tvGoalsReached.setText(String.format(Locale.getDefault(),
                    "Objetivo alcançado: %d/%d (%.0f%% · %s)",
                    stats.daysMet, stats.daysEvaluated, stats.getPercent(), stats.getQuality()));
        }

        boolean hasGoals = false;
        for (Goal g : goals.values()) {
            if (g.getTarget() > 0) {
                hasGoals = true;
            }
        }
        if (hasGoals) {
            tvHint.setVisibility(View.GONE);
        } else {
            tvHint.setText("Ainda não definiste metas. Define-as no menu principal para veres "
                    + "as cores e as estatísticas.");
            tvHint.setVisibility(View.VISIBLE);
        }
    }

    private void showError() {
        tvStreak.setText("Não foi possível carregar o mês.");
        tvGoalsReached.setText("");
    }

    private void openDay(int y, int m, int day) {
        String dateKey = DateHelper.dateKey(y, m, day);
        if (dateKey.compareTo(todayKey) > 0) {
            Toast.makeText(this, "Este dia ainda não chegou", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, DayDetailActivity.class);
        intent.putExtra(DayDetailActivity.EXTRA_DATE_KEY, dateKey);
        startActivity(intent);
    }
}