package com.example.nutriscan.helper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.nutriscan.R;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.Nutrient;

import java.util.List;
import java.util.Map;

public class ProgressRenderer {

    // Desenha uma barra por meta. Devolve false se não houver nenhuma meta.
    public static boolean render(Context context, LinearLayout container,
                                 Map<String, Goal> goals, List<Meal> meals) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);
        int green = ContextCompat.getColor(context, R.color.day_met);
        int red = ContextCompat.getColor(context, R.color.day_missed);
        boolean any = false;

        for (Nutrient n : Nutrient.values()) {
            Goal goal = goals.get(n.getKey());
            if (goal == null || goal.getTarget() <= 0) {
                continue;
            }
            any = true;

            double current = 0;
            for (Meal meal : meals) {
                current += n.of(meal);
            }
            double target = goal.getTarget();
            boolean isMax = Goal.TYPE_MAX.equals(goal.getType());
            boolean reached = current >= target;

            // Máximo: verde até atingir. Mínimo: vermelho até atingir.
            int color = isMax ? (reached ? red : green) : (reached ? green : red);

            View row = inflater.inflate(R.layout.item_progress, container, false);
            ((TextView) row.findViewById(R.id.progLabel)).setText(n.getLabel());

            TextView tvValue = row.findViewById(R.id.progValue);
            tvValue.setText(n.format(current) + " / " + n.format(target) + " " + n.getUnit());
            tvValue.setTextColor(color);

            ProgressBar bar = row.findViewById(R.id.progBar);
            bar.setProgress((int) Math.min(100, Math.round(current / target * 100)));
            bar.setProgressTintList(ColorStateList.valueOf(color));

            String remaining = n.format(Math.abs(target - current)) + " " + n.getUnit();
            String hint;
            if (isMax) {
                hint = reached ? "Máximo diário atingido ou ultrapassado"
                        : "Máximo diário --> " + remaining + " disponíveis";
            } else {
                hint = reached ? "Mínimo diário atingido"
                        : "Mínimo diário --> faltam " + remaining;
            }
            ((TextView) row.findViewById(R.id.progHint)).setText(hint);

            container.addView(row);
        }
        return any;
    }
}