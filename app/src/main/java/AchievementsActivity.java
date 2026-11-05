package com.example.nutriscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.helper.AchievementPopup;
import com.example.nutriscan.helper.DateHelper;
import com.example.nutriscan.model.Achievement;
import com.example.nutriscan.service.AchievementService;
import com.example.nutriscan.service.FoodService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class AchievementsActivity extends AppCompatActivity {

    private GridLayout grid;
    private TextView tvCount, tvStatus;
    private ProgressBar progressTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_achievements);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.achievements), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        grid = findViewById(R.id.gridAchievements);
        tvCount = findViewById(R.id.tvCount);
        tvStatus = findViewById(R.id.tvStatus);
        progressTotal = findViewById(R.id.progressTotal);
        progressTotal.setMax(Achievement.values().length);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (grid != null) {
            load();
        }
    }

    private void load() {
        tvStatus.setText("A carregar...");
        tvStatus.setVisibility(View.VISIBLE);

        AchievementService.refresh(new FoodService.Callback<AchievementService.Result>() {
            @Override
            public void onSuccess(AchievementService.Result result) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                render(result);
                AchievementPopup.show(AchievementsActivity.this, result.newlyUnlocked);
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                tvStatus.setText("Não foi possível carregar as conquistas.");
            }
        });
    }

    private void render(AchievementService.Result result) {
        tvStatus.setVisibility(View.GONE);

        int total = Achievement.values().length;
        int unlockedCount = result.getUnlockedCount();
        tvCount.setText(unlockedCount + "/" + total + " desbloqueadas");
        progressTotal.setProgress(unlockedCount);

        grid.removeAllViews();
        grid.setColumnCount(2);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Achievement a : Achievement.values()) {
            Long unlockedAt = result.unlocked.get(a.name());
            boolean isUnlocked = unlockedAt != null;

            View cell = inflater.inflate(R.layout.item_achievement, grid, false);
            TextView emoji = cell.findViewById(R.id.medalEmoji);
            TextView title = cell.findViewById(R.id.medalTitle);
            TextView info = cell.findViewById(R.id.medalInfo);
            TextView progress = cell.findViewById(R.id.medalProgress);

            emoji.setText(a.getEmoji());
            title.setText(a.getTitle());

            if (isUnlocked) {
                emoji.setBackgroundResource(R.drawable.bg_medal_unlocked);
                info.setText(formatDate(unlockedAt));
                progress.setVisibility(View.GONE);
            } else {
                emoji.setBackgroundResource(R.drawable.bg_medal_locked);
                emoji.setAlpha(0.5f);
                info.setText(a.getDescription());
                String text = a.progressText(result.snapshot);
                if (text.isEmpty()) {
                    progress.setVisibility(View.GONE);
                } else {
                    progress.setText(text);
                    progress.setVisibility(View.VISIBLE);
                }
            }
            addCell(cell);
        }
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0) {
            return "Desbloqueada";
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return DateHelper.dayLabel(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH))
                + " " + c.get(Calendar.YEAR);
    }

    // Duas colunas de largura igual
    private void addCell(View cell) {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        cell.setLayoutParams(lp);
        grid.addView(cell);
    }
}