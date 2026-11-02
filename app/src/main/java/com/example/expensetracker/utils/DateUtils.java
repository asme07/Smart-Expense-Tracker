package com.example.expensetracker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility helper for date parsing, formatting, and manipulation.
 */
public class DateUtils {

    public static final String FORMAT_SQL = "yyyy-MM-dd";
    public static final String FORMAT_MONTH_KEY = "yyyy-MM";
    public static final String FORMAT_DISPLAY = "dd MMM yyyy";
    public static final String FORMAT_MONTH_TITLE = "MMMM yyyy";
    public static final String FORMAT_DAY_MONTH = "dd MMM";

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_SQL, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_MONTH_KEY, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentMonthTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_MONTH_TITLE, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatToDisplay(String dateSql) {
        if (dateSql == null || dateSql.isEmpty()) return "";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat(FORMAT_SQL, Locale.getDefault());
            Date d = inFormat.parse(dateSql);
            if (d != null) {
                SimpleDateFormat outFormat = new SimpleDateFormat(FORMAT_DISPLAY, Locale.getDefault());
                return outFormat.format(d);
            }
        } catch (ParseException e) {
            // fallback
        }
        return dateSql;
    }

    public static String formatToMonthTitle(String monthKey) {
        if (monthKey == null || monthKey.isEmpty()) return "";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat(FORMAT_MONTH_KEY, Locale.getDefault());
            Date d = inFormat.parse(monthKey);
            if (d != null) {
                SimpleDateFormat outFormat = new SimpleDateFormat(FORMAT_MONTH_TITLE, Locale.getDefault());
                return outFormat.format(d);
            }
        } catch (ParseException e) {
            // fallback
        }
        return monthKey;
    }

    public static String getPastMonth(int monthsAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -monthsAgo);
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_MONTH_KEY, Locale.getDefault());
        return sdf.format(cal.getTime());
    }
}
