package com.example.nutriscan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nutriscan.R;
import com.example.nutriscan.model.Meal;
import com.example.nutriscan.model.MealEntry;

import java.util.List;
import java.util.Locale;

public class SavedMealAdapter extends BaseAdapter {

    private final Context context;
    private final List<Meal> meals;

    public SavedMealAdapter(Context context, List<Meal> meals) {
        this.context = context;
        this.meals = meals;
    }

    @Override
    public int getCount() { return meals.size(); }

    @Override
    public Object getItem(int position) { return meals.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.item_saved_meal, parent, false);
        }
        Meal meal = meals.get(position);

        ((TextView) row.findViewById(R.id.mealTitle)).setText(String.format(
                Locale.getDefault(), "%s \u00b7 %.0f kcal", meal.getName(), meal.getKcal()));

        StringBuilder items = new StringBuilder();
        for (MealEntry e : meal.getItems()) {
            if (items.length() > 0) {
                items.append("\n");
            }
            items.append(String.format(Locale.getDefault(),
                    "\u2022 %s \u2014 %.0f g \u00b7 %.0f kcal",
                    e.getName(), e.getGrams(), e.getKcal()));
        }
        ((TextView) row.findViewById(R.id.mealItems)).setText(items.toString());
        return row;
    }
}