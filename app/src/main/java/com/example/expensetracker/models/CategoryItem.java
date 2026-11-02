package com.example.expensetracker.models;

import java.io.Serializable;

/**
 * Model representing a visual category selection item.
 */
public class CategoryItem implements Serializable {
    private String name;
    private String type; // INCOME or EXPENSE
    private int iconRes;
    private int colorRes;

    public CategoryItem(String name, String type, int iconRes, int colorRes) {
        this.name = name;
        this.type = type;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getColorRes() {
        return colorRes;
    }
}
