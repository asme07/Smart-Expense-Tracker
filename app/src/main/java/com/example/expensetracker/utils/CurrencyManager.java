package com.example.expensetracker.utils;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formats monetary amounts with the user's selected currency symbol.
 */
public class CurrencyManager {

    public static final String[] SUPPORTED_CURRENCIES = {
            "USD ($)", "EUR (€)", "GBP (£)", "INR (₹)", "JPY (¥)", "CAD (C$)", "AUD (A$)"
    };

    public static final String[] CURRENCY_CODES = {
            "USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD"
    };

    public static String getSymbol(String currencyCode) {
        if (currencyCode == null) return "$";
        switch (currencyCode.toUpperCase().trim()) {
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            case "INR":
                return "₹";
            case "JPY":
                return "¥";
            case "CAD":
                return "C$";
            case "AUD":
                return "A$";
            case "USD":
            default:
                return "$";
        }
    }

    public static String formatAmount(Context context, double amount) {
        String code = SessionManager.getInstance(context).getCurrency();
        String symbol = getSymbol(code);
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return symbol + " " + formatter.format(amount);
    }

    public static String formatAmountSigned(Context context, double amount, boolean isIncome) {
        String formatted = formatAmount(context, Math.abs(amount));
        return isIncome ? "+ " + formatted : "- " + formatted;
    }
}
