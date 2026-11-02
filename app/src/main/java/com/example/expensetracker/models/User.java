package com.example.expensetracker.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing an application user.
 */
public class User implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String currency;
    private long createdAt;

    public User() {
        // Required for Firebase
    }

    public User(String userId, String name, String email, String currency, long createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.currency = currency != null ? currency : "USD";
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrency() {
        return currency != null ? currency : "USD";
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("email", email);
        map.put("currency", currency);
        map.put("createdAt", createdAt);
        return map;
    }
}
