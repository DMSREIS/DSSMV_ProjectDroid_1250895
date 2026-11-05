package com.example.nutriscan.helper;

import com.example.nutriscan.model.DayStatus;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MonthStats;
import com.example.nutriscan.model.Nutrient;

import java.util.List;
import java.util.Map;

public class MonthStatsCalculator {

    public static MonthStats calculate(int year, int month, Map<String, Goal> goals,
                                       Map<String, List<Meal>> mealsByDay,
                                       String todayKey, String firstKey) {
        int daysInMonth = DateHelper.daysInMonth(year, month);
        MonthStats stats = new MonthStats(year, month, daysInMonth);
        boolean hasGoals = hasAnyGoal(goals);

        int run = 0;
        for (int day = 1; day <= daysInMonth; day++) {
            String key = DateHelper.dateKey(year, month, day);
            DayStatus status = DayStatus.NONE;

            // as chaves "yyyy-MM-dd" comparam-se bem como texto
            boolean evaluable = hasGoals
                    && key.compareTo(todayKey) <= 0
                    && key.compareTo(firstKey) >= 0;

            if (evaluable) {
                List<Meal> meals = mealsByDay.get(key);
                boolean hasMeals = meals != null && !meals.isEmpty();
                if (hasMeals) {
                    status = allGoalsMet(goals, meals) ? DayStatus.MET : DayStatus.MISSED;
                } else if (key.compareTo(todayKey) < 0) {
                    status = DayStatus.MISSED; // dia passado sem refeições
                }
                // hoje sem refeições: continua neutro (o dia ainda não acabou)
            }
            stats.days[day - 1] = status;

            if (status != DayStatus.NONE) {
                stats.daysEvaluated++;
            }
            if (status == DayStatus.MET) {
                stats.daysMet++;
                run++;
                if (run > stats.bestStreak) {
                    stats.bestStreak = run;
                    stats.streakEndDay = day;
                    stats.streakStartDay = day - run + 1;
                }
            } else {
                run = 0;
            }
        }
        return stats;
    }

    // Máximo: o total tem de ficar abaixo do valor. Mínimo: tem de o atingir.
    public static boolean allGoalsMet(Map<String, Goal> goals, List<Meal> meals) {
        for (Nutrient n : Nutrient.values()) {
            Goal goal = goals.get(n.getKey());
            if (goal == null || goal.getTarget() <= 0) {
                continue;
            }
            double current = 0;
            for (Meal meal : meals) {
                current += n.of(meal);
            }
            boolean isMax = Goal.TYPE_MAX.equals(goal.getType());
            boolean reached = current >= goal.getTarget();
            boolean ok = isMax ? !reached : reached;
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasAnyGoal(Map<String, Goal> goals) {
        for (Goal g : goals.values()) {
            if (g.getTarget() > 0) {
                return true;
            }
        }
        return false;
    }
}