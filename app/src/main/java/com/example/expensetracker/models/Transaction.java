package com.example.expensetracker.models;

import com.example.expensetracker.R;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing an income or expense transaction.
 */
public class Transaction implements Serializable {
    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    // Standard Categories
    public static final String[] INCOME_CATEGORIES = {
            "Salary", "Freelancing", "Business", "Investments", "Other"
    };

    public static final String[] EXPENSE_CATEGORIES = {
            "Food", "Shopping", "Transportation", "Education",
            "Entertainment", "Healthcare", "Bills", "Travel", "Other"
    };

    private String transactionId;
    private String userId;
    private double amount;
    private String type; // INCOME or EXPENSE
    private String category;
    private String description;
    private String date; // YYYY-MM-DD
    private long createdAt; // epoch milliseconds

    public Transaction() {
        // Required for Firestore
    }

    public Transaction(String transactionId, String userId, double amount, String type,
                       String category, String description, String date, long createdAt) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.type = type != null ? type : TYPE_EXPENSE;
        this.category = category != null ? category : "Other";
        this.description = description != null ? description : "";
        this.date = date;
        this.createdAt = createdAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIncome() {
        return TYPE_INCOME.equalsIgnoreCase(type);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getCategoryIconRes() {
        if (category == null) return R.drawable.ic_cat_other;
        switch (category.toLowerCase().trim()) {
            case "salary":
                return R.drawable.ic_cat_salary;
            case "freelancing":
            case "freelance":
                return R.drawable.ic_cat_freelance;
            case "business":
                return R.drawable.ic_cat_business;
            case "investments":
            case "investment":
                return R.drawable.ic_cat_investments;
            case "food":
            case "dining":
            case "groceries":
                return R.drawable.ic_cat_food;
            case "shopping":
                return R.drawable.ic_cat_shopping;
            case "transportation":
            case "transport":
            case "fuel":
                return R.drawable.ic_cat_transport;
            case "education":
                return R.drawable.ic_cat_education;
            case "entertainment":
            case "movies":
                return R.drawable.ic_cat_entertainment;
            case "healthcare":
            case "health":
            case "medical":
                return R.drawable.ic_cat_healthcare;
            case "bills":
            case "utilities":
                return R.drawable.ic_cat_bills;
            case "travel":
                return R.drawable.ic_cat_travel;
            default:
                return R.drawable.ic_cat_other;
        }
    }

    public int getCategoryColorRes() {
        if (category == null) return R.color.cat_other;
        switch (category.toLowerCase().trim()) {
            case "salary":
                return R.color.cat_salary;
            case "freelancing":
            case "freelance":
                return R.color.cat_freelance;
            case "business":
                return R.color.cat_business;
            case "investments":
            case "investment":
                return R.color.cat_investments;
            case "food":
                return R.color.cat_food;
            case "shopping":
                return R.color.cat_shopping;
            case "transportation":
            case "transport":
                return R.color.cat_transport;
            case "education":
                return R.color.cat_education;
            case "entertainment":
                return R.color.cat_entertainment;
            case "healthcare":
                return R.color.cat_healthcare;
            case "bills":
                return R.color.cat_bills;
            case "travel":
                return R.color.cat_travel;
            default:
                return R.color.cat_other;
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("transactionId", transactionId);
        map.put("userId", userId);
        map.put("amount", amount);
        map.put("type", type);
        map.put("category", category);
        map.put("description", description);
        map.put("date", date);
        map.put("createdAt", createdAt);
        return map;
    }
}
