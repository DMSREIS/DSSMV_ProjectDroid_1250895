package com.example.nutriscan.helper;

import com.example.nutriscan.model.Meal;

import java.util.List;

public class MealNamer {

    // Nome a mostrar para a refeição na posição index do dia.
    // Nomes repetidos no mesmo dia ganham " (2)", " (3)"... (a primeira fica sem número).
    public static String displayName(List<Meal> meals, int index) {
        String base = meals.get(index).getDisplayName(index);
        String key = TextHelper.normalize(base);

        int sameBefore = 0;
        for (int i = 0; i < index; i++) {
            if (TextHelper.normalize(meals.get(i).getDisplayName(i)).equals(key)) {
                sameBefore++;
            }
        }
        return sameBefore == 0 ? base : base + " (" + (sameBefore + 1) + ")";
    }
}