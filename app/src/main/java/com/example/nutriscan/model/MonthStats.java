package com.example.nutriscan.model;

import java.util.Arrays;

public class MonthStats {
    public final int year;
    public final int month;            // 0 = janeiro
    public final DayStatus[] days;     // índice 0 = dia 1
    public int daysMet = 0;
    public int daysEvaluated = 0;      // dias verdes + vermelhos
    public int bestStreak = 0;
    public int streakStartDay = 0;
    public int streakEndDay = 0;

    public MonthStats(int year, int month, int daysInMonth) {
        this.year = year;
        this.month = month;
        this.days = new DayStatus[daysInMonth];
        Arrays.fill(days, DayStatus.NONE);
    }

    public double getPercent() {
        return daysEvaluated == 0 ? 0 : daysMet * 100.0 / daysEvaluated;
    }

    public String getQuality() {
        if (daysEvaluated == 0) {
            return "Sem dados";
        }
        double p = getPercent();
        if (p > 70) {
            return "Excelente";
        }
        if (p > 40) {
            return "Bom";
        }
        if (p >= 5) {
            return "Ok";
        }
        return "Fraco";
    }
}