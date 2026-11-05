package com.example.nutriscan.model;

public class AchievementSnapshot {
    public boolean hasMeal;
    public int bestLogStreak;        // dias seguidos com refeições registadas
    public int bestGoalStreak;       // dias seguidos (já terminados) com metas cumpridas
    public int foodCount;            // alimentos guardados
    public boolean hasGoal;
    public boolean hasPhoto;
    public double bestMonthPercent = -1; // -1: nenhum mês com dias suficientes
}