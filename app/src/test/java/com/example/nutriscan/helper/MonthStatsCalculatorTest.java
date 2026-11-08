package com.example.nutriscan.helper;

import com.example.nutriscan.model.DayStatus;
import com.example.nutriscan.model.Goal;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MonthStats;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MonthStatsCalculatorTest {

    @Test
    public void maximumGoalPassesBelowLimitAndFailsAtOrAboveLimit() {
        Map<String, Goal> goals = new HashMap<>();
        goals.put("kcal", new Goal("kcal", 2000.0, Goal.TYPE_MAX));

        assertTrue(MonthStatsCalculator.allGoalsMet(goals,
                Collections.singletonList(meal(1999.0, 0.0))));
        assertFalse(MonthStatsCalculator.allGoalsMet(goals,
                Collections.singletonList(meal(2000.0, 0.0))));
        assertFalse(MonthStatsCalculator.allGoalsMet(goals,
                Collections.singletonList(meal(2200.0, 0.0))));
    }

    @Test
    public void minimumGoalPassesAtOrAboveTarget() {
        Map<String, Goal> goals = new HashMap<>();
        goals.put("proteins", new Goal("proteins", 50.0, Goal.TYPE_MIN));

        assertFalse(MonthStatsCalculator.allGoalsMet(goals,
                Collections.singletonList(meal(0.0, 49.0))));
        assertTrue(MonthStatsCalculator.allGoalsMet(goals,
                Collections.singletonList(meal(0.0, 50.0))));
        assertTrue(MonthStatsCalculator.allGoalsMet(goals,
                Collections.singletonList(meal(0.0, 60.0))));
    }

    @Test
    public void goalsAreCalculatedAcrossAllMealsOfTheDay() {
        Map<String, Goal> goals = new HashMap<>();
        goals.put("kcal", new Goal("kcal", 2000.0, Goal.TYPE_MAX));
        goals.put("proteins", new Goal("proteins", 50.0, Goal.TYPE_MIN));
        List<Meal> meals = Arrays.asList(
                meal(800.0, 20.0),
                meal(900.0, 35.0));

        assertTrue(MonthStatsCalculator.allGoalsMet(goals, meals));
    }

    @Test
    public void monthCalculationMarksMetMissedAndCurrentIncompleteDays() {
        Map<String, Goal> goals = new HashMap<>();
        goals.put("kcal", new Goal("kcal", 2000.0, Goal.TYPE_MAX));

        Map<String, List<Meal>> mealsByDay = new HashMap<>();
        mealsByDay.put("2026-01-01", Collections.singletonList(meal(1800.0, 0.0)));
        // 2 de janeiro não tem refeições: por ser um dia passado, fica como não cumprido.
        // 3 de janeiro é o dia atual no cenário de teste e ainda está incompleto.

        MonthStats stats = MonthStatsCalculator.calculate(
                2026, 0, goals, mealsByDay, "2026-01-03", "2026-01-01");

        assertEquals(DayStatus.MET, stats.days[0]);
        assertEquals(DayStatus.MISSED, stats.days[1]);
        assertEquals(DayStatus.NONE, stats.days[2]);
        assertEquals(2, stats.daysEvaluated);
        assertEquals(1, stats.daysMet);
    }

    private Meal meal(double kcal, double proteins) {
        Meal meal = new Meal();
        meal.setKcal(kcal);
        meal.setProteins(proteins);
        meal.setCarbs(0.0);
        meal.setFat(0.0);
        return meal;
    }
}
