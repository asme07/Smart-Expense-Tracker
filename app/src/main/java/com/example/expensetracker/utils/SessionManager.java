package com.example.expensetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Manages user session state and preferences using SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "SmartExpenseUserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_CURRENCY = "currencyCode";
    private static final String KEY_THEME_MODE = "themeMode";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private static SessionManager instance;

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(String userId, String name, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "default_user");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    public void setUserName(String name) {
        editor.putString(KEY_USER_NAME, name).apply();
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getCurrency() {
        return prefs.getString(KEY_CURRENCY, "USD");
    }

    public void setCurrency(String currencyCode) {
        editor.putString(KEY_CURRENCY, currencyCode).apply();
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int themeMode) {
        editor.putInt(KEY_THEME_MODE, themeMode).apply();
    }

    public void logout() {
        String currentTheme = prefs.getString(KEY_CURRENCY, "USD");
        int themeMode = getThemeMode();
        editor.clear();
        // Preserve currency & theme preference even after logout
        editor.putString(KEY_CURRENCY, currentTheme);
        editor.putInt(KEY_THEME_MODE, themeMode);
        editor.apply();
    }
}
