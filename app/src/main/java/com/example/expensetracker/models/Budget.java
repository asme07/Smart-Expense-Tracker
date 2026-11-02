package com.example.expensetracker.models;

import com.example.expensetracker.R;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing a monthly or category-specific spending budget.
 */
public class Budget implements Serializable {
    public static final String CATEGORY_ALL = "All Categories";

    private String budgetId;
    private String userId;
    private String category; // e.g. "All Categories" or "Food"
    private double monthlyLimit;
    private String month; // YYYY-MM
    private double spentAmount; // transient / computed dynamically

    public Budget() {
        // Required for Firestore
    }

    public Budget(String budgetId, String userId, String category, double monthlyLimit, String month) {
        this.budgetId = budgetId;
        this.userId = userId;
        this.category = category != null ? category : CATEGORY_ALL;
        this.monthlyLimit = monthlyLimit;
        this.month = month;
    }

    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getRemainingAmount() {
        return Math.max(0, monthlyLimit - spentAmount);
    }

    public int getProgressPercentage() {
        if (monthlyLimit <= 0) return 0;
        int pct = (int) Math.round((spentAmount / monthlyLimit) * 100);
        return Math.min(pct, 100);
    }

    public boolean isExceeded() {
        return spentAmount > monthlyLimit;
    }

    public boolean isWarningReached() {
        return (spentAmount >= monthlyLimit * 0.8) && !isExceeded();
    }

    public int getCategoryIconRes() {
        if (category == null || CATEGORY_ALL.equalsIgnoreCase(category)) {
            return R.drawable.ic_nav_budget;
        }
        Transaction temp = new Transaction();
        temp.setCategory(category);
        return temp.getCategoryIconRes();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("budgetId", budgetId);
        map.put("userId", userId);
        map.put("category", category);
        map.put("monthlyLimit", monthlyLimit);
        map.put("month", month);
        return map;
    }
}
