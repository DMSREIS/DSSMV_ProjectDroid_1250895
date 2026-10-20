package com.example.nutriscan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nutriscan.R;
import com.example.nutriscan.model.Food;

import java.util.List;
import java.util.Locale;

public class FoodAdapter extends BaseAdapter {

    private final Context context;
    private final int rowLayout;
    private final List<Food> foods;

    public FoodAdapter(Context context, int rowLayout, List<Food> foods) {
        this.context = context;
        this.rowLayout = rowLayout;
        this.foods = foods;
    }

    @Override
    public int getCount() { return foods.size(); }

    @Override
    public Object getItem(int position) { return foods.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(context).inflate(rowLayout, parent, false);
        }
        TextView name = row.findViewById(R.id.itemName);
        TextView detail = row.findViewById(R.id.itemDetail);

        Food food = foods.get(position);
        name.setText(food.getName());

        String brand = (food.getBrand() == null || food.getBrand().isEmpty())
                ? "" : food.getBrand() + " · ";
        Double kcal = food.getKcal();
        String kcalText = kcal == null ? "-" : String.format(Locale.getDefault(), "%.0f", kcal);
        detail.setText(brand + kcalText + " kcal / 100 g");
        return row;
    }
}