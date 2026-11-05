package com.example.nutriscan.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateHelper {

    private static final String[] MONTHS = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio",
            "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
    private static final String[] MONTHS_SHORT = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
            "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

    public static String toKey(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    }

    public static String todayKey() {
        return toKey(new Date());
    }

    // month: 0 = janeiro
    public static String dateKey(int year, int month, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
    }

    public static int daysInMonth(int year, int month) {
        return new GregorianCalendar(year, month, 1).getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    // 0 = domingo ... 6 = sábado
    public static int firstWeekdayOffset(int year, int month) {
        return new GregorianCalendar(year, month, 1).get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static String monthTitle(int year, int month) {
        return MONTHS[month] + " " + year;
    }

    // ex.: "4 Out"
    public static String dayLabel(int day, int month) {
        return day + " " + MONTHS_SHORT[month];
    }
}