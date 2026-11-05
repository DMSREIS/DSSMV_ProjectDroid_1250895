package com.example.nutriscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Nutrient;
import com.example.nutriscan.service.FoodService;
import com.example.nutriscan.service.GoalService;
import com.example.nutriscan.view.SliderInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoalsActivity extends AppCompatActivity {

    private final List<View> rows = new ArrayList<>();
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goals);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.goals), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout container = findViewById(R.id.goalsContainer);
        btnSave = findViewById(R.id.btnSaveGoals);

        // Uma linha por nutriente
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Nutrient n : Nutrient.values()) {
            View row = inflater.inflate(R.layout.item_goal_row, container, false);
            row.setTag(n);
            ((TextView) row.findViewById(R.id.goalLabel))
                    .setText(n.getLabel() + " (" + n.getUnit() + ")");

            SliderInput slider = row.findViewById(R.id.goalTarget);
            slider.setup(goalMax(n), 1);
            slider.setLabel("Meta de " + n.getLabel());

            // por defeito: proteína é um mínimo, o resto é um máximo
            RadioGroup type = row.findViewById(R.id.goalType);
            type.check(n == Nutrient.PROTEINS ? R.id.goalMin : R.id.goalMax);

            container.addView(row);
            rows.add(row);
        }

        btnSave.setOnClickListener(v -> save());
        loadGoals();
    }

    private double goalMax(Nutrient n) {
        switch (n) {
            case KCAL: return 5000;
            case PROTEINS: return 400;
            case CARBS: return 800;
            case FAT: return 300;
            default: return 1000;
        }
    }

    private void loadGoals() {
        GoalService.loadGoals(new FoodService.Callback<Map<String, Goal>>() {
            @Override
            public void onSuccess(Map<String, Goal> goals) {
                for (View row : rows) {
                    Nutrient n = (Nutrient) row.getTag();
                    Goal goal = goals.get(n.getKey());
                    if (goal == null) {
                        continue;
                    }
                    ((SliderInput) row.findViewById(R.id.goalTarget)).setValue(goal.getTarget());
                    ((RadioGroup) row.findViewById(R.id.goalType)).check(
                            Goal.TYPE_MIN.equals(goal.getType()) ? R.id.goalMin : R.id.goalMax);
                }
                btnSave.setEnabled(true);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(GoalsActivity.this,
                        "Não foi possível carregar as metas", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void save() {
        List<Goal> toSave = new ArrayList<>();
        List<String> toDelete = new ArrayList<>();

        for (View row : rows) {
            Nutrient n = (Nutrient) row.getTag();
            double target = ((SliderInput) row.findViewById(R.id.goalTarget)).getValue();

            if (target <= 0) {
                toDelete.add(n.getKey()); // 0 = sem meta
                continue;
            }
            RadioGroup type = row.findViewById(R.id.goalType);
            String goalType = type.getCheckedRadioButtonId() == R.id.goalMin
                    ? Goal.TYPE_MIN : Goal.TYPE_MAX;
            toSave.add(new Goal(n.getKey(), target, goalType));
        }

        btnSave.setEnabled(false);
        GoalService.saveGoals(toSave, toDelete, new FoodService.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(GoalsActivity.this, "Metas guardadas", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                btnSave.setEnabled(true);
                Toast.makeText(GoalsActivity.this,
                        "Não foi possível guardar as metas", Toast.LENGTH_LONG).show();
            }
        });
    }
}