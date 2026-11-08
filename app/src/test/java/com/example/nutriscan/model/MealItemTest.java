package com.example.nutriscan.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MealItemTest {

    @Test
    public void calculatesNutrientsForPortionOf150Grams() {
        Food food = new Food("5600000000000", "Iogurte", "Marca",
                150.0, 10.0, 20.0, 5.0);
        MealItem item = new MealItem(food, 150.0);

        assertEquals(225.0, item.getKcal(), 0.001);
        assertEquals(15.0, item.getProteins(), 0.001);
        assertEquals(30.0, item.getCarbohydrates(), 0.001);
        assertEquals(7.5, item.getFat(), 0.001);
    }

    @Test
    public void missingNutrientValuesAreTreatedAsZero() {
        Food food = new Food(null, "Alimento sem dados", "", null, null, null, null);
        MealItem item = new MealItem(food, 100.0);

        assertEquals(0.0, item.getKcal(), 0.001);
        assertEquals(0.0, item.getProteins(), 0.001);
        assertEquals(0.0, item.getCarbohydrates(), 0.001);
        assertEquals(0.0, item.getFat(), 0.001);
    }
}
