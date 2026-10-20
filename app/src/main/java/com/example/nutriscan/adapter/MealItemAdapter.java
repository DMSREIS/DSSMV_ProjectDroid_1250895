package com.example.nutriscan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.nutriscan.R;
import com.example.nutriscan.model.MealItem;

import java.util.List;
import java.util.Locale;

public class MealItemAdapter extends BaseAdapter {

    private final Context context;
    private final int rowLayout;
    private final List<MealItem> items;

    public MealItemAdapter(Context context, int rowLayout, List<MealItem> items) {
        this.context = context;
        this.rowLayout = rowLayout;
        this.items = items;
    }

    @Override
    public int getCount() { return items.size(); }

    @Override
    public Object getItem(int position) { return items.get(position); }

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

        MealItem item = items.get(position);
        name.setText(item.getFood().getName());
        detail.setText(String.format(Locale.getDefault(), "%.0f g · %.0f kcal",
                item.getGrams(), item.getKcal()));
        return row;
    }
}
