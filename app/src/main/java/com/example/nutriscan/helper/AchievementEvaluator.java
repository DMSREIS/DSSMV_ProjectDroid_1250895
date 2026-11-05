package com.example.nutriscan.helper;

import com.example.nutriscan.model.AchievementSnapshot;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MonthStats;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AchievementEvaluator {

    // Hoje + 61 dias para trás: cobre sempre o mês atual e o anterior
    public static final int WINDOW_DAYS = 62;
    public static final int GOOD_MONTH_MIN_DAYS = 7;

    public static AchievementSnapshot evaluate(Map<String, Goal> goals,
                                               Map<String, List<Meal>> mealsByDay,
                                               int foodCount, boolean hasPhoto,
                                               Date today, String firstKey) {
        AchievementSnapshot s = new AchievementSnapshot();
        s.foodCount = foodCount;
        s.hasPhoto = hasPhoto;
        s.hasGoal = hasAnyGoal(goals);

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -(WINDOW_DAYS - 1));

        int logRun = 0;
        int goalRun = 0;
        for (int i = 0; i < WINDOW_DAYS; i++) {
            String key = DateHelper.toKey(cal.getTime());
            List<Meal> meals = mealsByDay.get(key);
            boolean hasMeals = meals != null && !meals.isEmpty();
            boolean isToday = i == WINDOW_DAYS - 1;

            // Streak de registo: conta também o dia de hoje
            if (hasMeals) {
                s.hasMeal = true;
                logRun++;
                s.bestLogStreak = Math.max(s.bestLogStreak, logRun);
            } else {
                logRun = 0;
            }

            // Streak de metas: só dias já terminados
            if (!isToday) {
                boolean met = s.hasGoal && hasMeals
                        && MonthStatsCalculator.allGoalsMet(goals, meals);
                if (met) {
                    goalRun++;
                    s.bestGoalStreak = Math.max(s.bestGoalStreak, goalRun);
                } else {
                    goalRun = 0;
                }
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Bom mês: mês atual e anterior, só com dias terminados (até ontem)
        if (s.hasGoal) {
            Calendar yesterday = Calendar.getInstance();
            yesterday.setTime(today);
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            String yesterdayKey = DateHelper.toKey(yesterday.getTime());

            Calendar month = Calendar.getInstance();
            month.setTime(today);
            for (int i = 0; i < 2; i++) {
                MonthStats stats = MonthStatsCalculator.calculate(
                        month.get(Calendar.YEAR), month.get(Calendar.MONTH),
                        goals, mealsByDay, yesterdayKey, firstKey);
                if (stats.daysEvaluated >= GOOD_MONTH_MIN_DAYS) {
                    s.bestMonthPercent = Math.max(s.bestMonthPercent, stats.getPercent());
                }
                month.add(Calendar.MONTH, -1);
            }
        }
        return s;
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