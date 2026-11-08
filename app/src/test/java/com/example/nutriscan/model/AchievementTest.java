package com.example.nutriscan.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class AchievementTest {
    @Test
    public void verificaDesbloqueioDasConquistasPrincipais() {
        AchievementSnapshot snapshot = new AchievementSnapshot();
        snapshot.hasMeal = true;
        snapshot.hasGoal = true;
        snapshot.hasPhoto = true;
        snapshot.bestLogStreak = 7;
        snapshot.bestGoalStreak = 3;
        snapshot.foodCount = 5;
        snapshot.bestMonthPercent = 70.1;

        assertTrue(Achievement.FIRST_MEAL.isMet(snapshot));
        assertTrue(Achievement.FIRST_GOAL.isMet(snapshot));
        assertTrue(Achievement.PHOTO.isMet(snapshot));
        assertTrue(Achievement.LOG_STREAK_7.isMet(snapshot));
        assertTrue(Achievement.GOALS_STREAK_3.isMet(snapshot));
        assertTrue(Achievement.PANTRY_5.isMet(snapshot));
        assertTrue(Achievement.GOOD_MONTH.isMet(snapshot));
    }
}
