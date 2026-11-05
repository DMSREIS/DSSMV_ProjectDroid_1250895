package com.example.nutriscan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.helper.AchievementPopup;
import com.example.nutriscan.helper.CalendarRenderer;
import com.example.nutriscan.helper.DateHelper;
import com.example.nutriscan.helper.ImageHelper;
import com.example.nutriscan.helper.MonthStatsCalculator;
import com.example.nutriscan.helper.ProgressRenderer;
import com.example.nutriscan.model.CurrentMeal;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MonthStats;
import com.example.nutriscan.service.AchievementService;
import com.example.nutriscan.service.FoodService;
import com.example.nutriscan.service.GoalService;
import com.example.nutriscan.service.MealService;
import com.example.nutriscan.service.ProfileService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutProgress;
    private TextView tvNoGoals, tvCalendarMonth;
    private GridLayout gridCalendar;
    private ImageView ivAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            goToLogin();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        layoutProgress = findViewById(R.id.layoutProgress);
        tvNoGoals = findViewById(R.id.tvNoGoals);
        tvCalendarMonth = findViewById(R.id.tvCalendarMonth);
        gridCalendar = findViewById(R.id.gridCalendar);
        ivAvatar = findViewById(R.id.ivAvatar);

        ivAvatar.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UserActivity.class)));

        Button btnRegisterMeal = findViewById(R.id.btnRegisterMeal);
        btnRegisterMeal.setOnClickListener(v -> {
            CurrentMeal.clear(); // começa uma refeição nova
            startActivity(new Intent(MainActivity.this, AddMealActivity.class));
        });

        Button btnGoals = findViewById(R.id.btnGoals);
        btnGoals.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, GoalsActivity.class)));

        findViewById(R.id.cardCalendar).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CalendarActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (layoutProgress != null) {
            refreshProgress(); // atualiza as barras e o calendário ao voltar de outro ecrã
            loadAvatar();
            checkAchievements();
        }
    }

    // Se houver conquistas novas, mostra o pop-up por cima do menu
    private void checkAchievements() {
        AchievementService.refresh(new FoodService.Callback<AchievementService.Result>() {
            @Override
            public void onSuccess(AchievementService.Result result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                AchievementPopup.show(MainActivity.this, result.newlyUnlocked);
            }

            @Override
            public void onError(Exception e) {
                // silencioso: as conquistas não podem atrapalhar o menu
            }
        });
    }

    private void loadAvatar() {
        ProfileService.loadPhoto(new FoodService.Callback<String>() {
            @Override
            public void onSuccess(String base64) {
                Bitmap bitmap = base64 == null ? null : ImageHelper.base64ToBitmap(base64);
                ivAvatar.setImageBitmap(bitmap); // sem foto: fica o círculo preto
            }

            @Override
            public void onError(Exception e) {
                // sem ligação: fica o círculo preto
            }
        });
    }

    private void refreshProgress() {
        Calendar now = Calendar.getInstance();
        final int year = now.get(Calendar.YEAR);
        final int month = now.get(Calendar.MONTH);
        final String todayKey = DateHelper.todayKey();
        final String firstKey = accountCreationKey();

        GoalService.loadGoals(new FoodService.Callback<Map<String, Goal>>() {
            @Override
            public void onSuccess(Map<String, Goal> goals) {
                MealService.loadMealsForMonth(year, month,
                        new FoodService.Callback<Map<String, List<Meal>>>() {
                            @Override
                            public void onSuccess(Map<String, List<Meal>> mealsByDay) {
                                List<Meal> today = mealsByDay.get(todayKey);
                                List<Meal> todayMeals = today == null ? new ArrayList<Meal>() : today;

                                boolean anyGoal = ProgressRenderer.render(
                                        MainActivity.this, layoutProgress, goals, todayMeals);
                                if (anyGoal) {
                                    tvNoGoals.setVisibility(View.GONE);
                                } else {
                                    tvNoGoals.setText(
                                            "Ainda não definiste metas. Toca em \"Definir metas\".");
                                    tvNoGoals.setVisibility(View.VISIBLE);
                                }

                                MonthStats stats = MonthStatsCalculator.calculate(
                                        year, month, goals, mealsByDay, todayKey, firstKey);
                                tvCalendarMonth.setText(
                                        "Calendário · " + DateHelper.monthTitle(year, month));
                                CalendarRenderer.render(MainActivity.this, gridCalendar,
                                        stats, todayKey, null);
                            }

                            @Override
                            public void onError(Exception e) {
                                showMessage("Não foi possível carregar o progresso.");
                            }
                        });
            }

            @Override
            public void onError(Exception e) {
                showMessage("Não foi possível carregar as metas.");
            }
        });
    }

    // Dia em que a conta foi criada: antes disso os dias ficam neutros
    private String accountCreationKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getMetadata() != null) {
            return DateHelper.toKey(new Date(user.getMetadata().getCreationTimestamp()));
        }
        return "0000-00-00";
    }

    private void showMessage(String message) {
        layoutProgress.removeAllViews();
        tvNoGoals.setText(message);
        tvNoGoals.setVisibility(View.VISIBLE);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}