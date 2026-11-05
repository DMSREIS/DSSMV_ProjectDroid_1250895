package com.example.nutriscan.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.nutriscan.R;
import com.example.nutriscan.model.DayStatus;
import com.example.nutriscan.model.MonthStats;

public class CalendarRenderer {

    public interface OnDayClick {
        void onDayClick(int day);
    }

    private static final String[] WEEKDAYS = {"D", "S", "T", "Q", "Q", "S", "S"};

    public static void render(Context context, GridLayout grid, MonthStats stats,
                              String todayKey, OnDayClick listener) {
        grid.removeAllViews();
        grid.setColumnCount(7);
        LayoutInflater inflater = LayoutInflater.from(context);

        // Cabeçalho: D S T Q Q S S
        for (String w : WEEKDAYS) {
            View header = inflater.inflate(R.layout.item_day, grid, false);
            TextView letter = header.findViewById(R.id.dayNumber);
            letter.setText(w);
            letter.setTypeface(null, Typeface.BOLD);
            letter.setTextColor(ContextCompat.getColor(context, R.color.day_none));
            header.findViewById(R.id.dayMark).setVisibility(View.GONE);
            addCell(grid, header);
        }

        // Espaços vazios até ao primeiro dia do mês (a semana começa ao domingo)
        int offset = DateHelper.firstWeekdayOffset(stats.year, stats.month);
        for (int i = 0; i < offset; i++) {
            addCell(grid, new Space(context));
        }

        for (int day = 1; day <= stats.days.length; day++) {
            View cell = inflater.inflate(R.layout.item_day, grid, false);
            TextView number = cell.findViewById(R.id.dayNumber);
            TextView mark = cell.findViewById(R.id.dayMark);
            number.setText(String.valueOf(day));

            DayStatus status = stats.days[day - 1];
            int colorRes;
            String symbol;
            switch (status) {
                case MET:
                    colorRes = R.color.day_met;
                    symbol = "\u2713";
                    break;
                case MISSED:
                    colorRes = R.color.day_missed;
                    symbol = "\u2717";
                    break;
                default:
                    colorRes = R.color.day_none;
                    symbol = "";
                    break;
            }
            int color = ContextCompat.getColor(context, colorRes);
            number.setTextColor(color);
            mark.setText(symbol);
            mark.setTextColor(color);

            if (DateHelper.dateKey(stats.year, stats.month, day).equals(todayKey)) {
                cell.setBackgroundResource(R.drawable.bg_today);
                number.setTypeface(null, Typeface.BOLD);
            }

            if (listener != null) {
                final int d = day;
                cell.setOnClickListener(v -> listener.onDayClick(d));
            }
            addCell(grid, cell);
        }
    }

    // Colunas de largura igual (peso 1)
    private static void addCell(GridLayout grid, View cell) {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        cell.setLayoutParams(lp);
        grid.addView(cell);
    }
}