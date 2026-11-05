package com.example.nutriscan.model;

import java.util.Locale;

public enum Achievement {
    FIRST_MEAL("🍽️", "Primeira refeição", "Regista a tua primeira refeição", 1, ""),
    LOG_STREAK_3("🔥", "Rotina", "Regista refeições 3 dias seguidos", 3, "dias"),
    LOG_STREAK_7("📅", "Semana cheia", "Regista refeições 7 dias seguidos", 7, "dias"),
    LOG_STREAK_14("🗓️", "Duas semanas", "Regista refeições 14 dias seguidos", 14, "dias"),
    FIRST_GOAL("🎯", "Com objetivos", "Define a tua primeira meta", 1, ""),
    PERFECT_DAY("✅", "Dia certo", "Cumpre todas as metas num dia", 1, ""),
    GOALS_STREAK_3("🥉", "Em forma", "Cumpre as metas 3 dias seguidos", 3, "dias"),
    GOALS_STREAK_7("🥈", "Disciplina", "Cumpre as metas 7 dias seguidos", 7, "dias"),
    GOALS_STREAK_14("🥇", "Imparável", "Cumpre as metas 14 dias seguidos", 14, "dias"),
    GOOD_MONTH("🌟", "Bom mês",
            "Cumpre as metas em mais de 70% dos dias de um mês (mínimo de 7 dias)", 1, ""),
    PANTRY_5("🥫", "Despensa", "Guarda 5 alimentos", 5, "alimentos"),
    PANTRY_15("🧺", "Despensa cheia", "Guarda 15 alimentos", 15, "alimentos"),
    PHOTO("📷", "Com cara", "Adiciona uma foto de perfil", 1, "");

    private final String emoji;
    private final String title;
    private final String description;
    private final int target;
    private final String unit;

    Achievement(String emoji, String title, String description, int target, String unit) {
        this.emoji = emoji;
        this.title = title;
        this.description = description;
        this.target = target;
        this.unit = unit;
    }

    public String getEmoji() { return emoji; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    // Valor atual para esta conquista
    public int current(AchievementSnapshot s) {
        switch (this) {
            case FIRST_MEAL:
                return s.hasMeal ? 1 : 0;
            case LOG_STREAK_3:
            case LOG_STREAK_7:
            case LOG_STREAK_14:
                return s.bestLogStreak;
            case FIRST_GOAL:
                return s.hasGoal ? 1 : 0;
            case PERFECT_DAY:
            case GOALS_STREAK_3:
            case GOALS_STREAK_7:
            case GOALS_STREAK_14:
                return s.bestGoalStreak;
            case GOOD_MONTH:
                return s.bestMonthPercent > 70 ? 1 : 0;
            case PANTRY_5:
            case PANTRY_15:
                return s.foodCount;
            case PHOTO:
                return s.hasPhoto ? 1 : 0;
            default:
                return 0;
        }
    }

    public boolean isMet(AchievementSnapshot s) {
        return current(s) >= target;
    }

    // Texto de progresso das conquistas ainda bloqueadas ("5/7 dias")
    public String progressText(AchievementSnapshot s) {
        if (this == GOOD_MONTH) {
            if (s.bestMonthPercent < 0) {
                return "Precisa de 7 dias avaliados num mês";
            }
            return String.format(Locale.getDefault(),
                    "Melhor mês: %.0f%% (precisa de mais de 70%%)", s.bestMonthPercent);
        }
        if (target <= 1) {
            return "";
        }
        return Math.min(current(s), target) + "/" + target + " " + unit;
    }
}