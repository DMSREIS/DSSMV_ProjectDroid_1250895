package com.example.nutriscan.helper;

import com.example.nutriscan.model.Meal;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DateHelperTest {

    @Test
    public void unnamedMealsReceiveSequentialDefaultNames() {
        List<Meal> meals = Arrays.asList(new Meal(), new Meal(), new Meal());

        assertEquals("Refeição 1", MealNamer.displayName(meals, 0));
        assertEquals("Refeição 2", MealNamer.displayName(meals, 1));
        assertEquals("Refeição 3", MealNamer.displayName(meals, 2));
    }

    @Test
    public void repeatedNamesAreNumberedWithoutConsideringCaseOrAccents() {
        Meal first = new Meal();
        first.setName("Almoço");
        Meal second = new Meal();
        second.setName("almoco");
        Meal third = new Meal();
        third.setName("ALMOÇO");
        List<Meal> meals = Arrays.asList(first, second, third);

        assertEquals("Almoço", MealNamer.displayName(meals, 0));
        assertEquals("almoco (2)", MealNamer.displayName(meals, 1));
        assertEquals("ALMOÇO (3)", MealNamer.displayName(meals, 2));
    }

    @Test
    public void uniqueNameIsKeptUnchanged() {
        Meal meal = new Meal();
        meal.setName("Pequeno-almoço");

        assertEquals("Pequeno-almoço", MealNamer.displayName(
                Collections.singletonList(meal), 0));
    }
}
