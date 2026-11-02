package com.example.expensetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.models.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite Database Helper for local storage of users, transactions, and budgets.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "smart_expense_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "userId";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_CURRENCY = "currency";
    public static final String COL_USER_CREATED_AT = "createdAt";

    // Transactions Table
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_TRANS_ID = "transactionId";
    public static final String COL_TRANS_USER_ID = "userId";
    public static final String COL_TRANS_AMOUNT = "amount";
    public static final String COL_TRANS_TYPE = "type";
    public static final String COL_TRANS_CATEGORY = "category";
    public static final String COL_TRANS_DESCRIPTION = "description";
    public static final String COL_TRANS_DATE = "date";
    public static final String COL_TRANS_CREATED_AT = "createdAt";

    // Budgets Table
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COL_BUDGET_ID = "budgetId";
    public static final String COL_BUDGET_USER_ID = "userId";
    public static final String COL_BUDGET_CATEGORY = "category";
    public static final String COL_BUDGET_LIMIT = "monthlyLimit";
    public static final String COL_BUDGET_MONTH = "month";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " TEXT PRIMARY KEY, "
                + COL_USER_NAME + " TEXT, "
                + COL_USER_EMAIL + " TEXT, "
                + COL_USER_CURRENCY + " TEXT, "
                + COL_USER_CREATED_AT + " INTEGER"
                + ")";

        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + " ("
                + COL_TRANS_ID + " TEXT PRIMARY KEY, "
                + COL_TRANS_USER_ID + " TEXT, "
                + COL_TRANS_AMOUNT + " REAL, "
                + COL_TRANS_TYPE + " TEXT, "
                + COL_TRANS_CATEGORY + " TEXT, "
                + COL_TRANS_DESCRIPTION + " TEXT, "
                + COL_TRANS_DATE + " TEXT, "
                + COL_TRANS_CREATED_AT + " INTEGER"
                + ")";

        String CREATE_BUDGETS_TABLE = "CREATE TABLE " + TABLE_BUDGETS + " ("
                + COL_BUDGET_ID + " TEXT PRIMARY KEY, "
                + COL_BUDGET_USER_ID + " TEXT, "
                + COL_BUDGET_CATEGORY + " TEXT, "
                + COL_BUDGET_LIMIT + " REAL, "
                + COL_BUDGET_MONTH + " TEXT"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        db.execSQL(CREATE_BUDGETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        onCreate(db);
    }

    // ==========================================
    // USER OPERATIONS
    // ==========================================

    public long insertOrUpdateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, user.getUserId());
        values.put(COL_USER_NAME, user.getName());
        values.put(COL_USER_EMAIL, user.getEmail());
        values.put(COL_USER_CURRENCY, user.getCurrency());
        values.put(COL_USER_CREATED_AT, user.getCreatedAt());

        return db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public User getUser(String userId) {
        if (userId == null) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_USER_ID + "=?", new String[]{userId},
                null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_CURRENCY)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_CREATED_AT))
            );
            cursor.close();
        }
        return user;
    }

    public int updateUserCurrency(String userId, String currency) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_CURRENCY, currency);
        return db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{userId});
    }

    public int updateUserName(String userId, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        return db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{userId});
    }

    // ==========================================
    // TRANSACTION OPERATIONS
    // ==========================================

    public long insertTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_ID, transaction.getTransactionId());
        values.put(COL_TRANS_USER_ID, transaction.getUserId());
        values.put(COL_TRANS_AMOUNT, transaction.getAmount());
        values.put(COL_TRANS_TYPE, transaction.getType());
        values.put(COL_TRANS_CATEGORY, transaction.getCategory());
        values.put(COL_TRANS_DESCRIPTION, transaction.getDescription());
        values.put(COL_TRANS_DATE, transaction.getDate());
        values.put(COL_TRANS_CREATED_AT, transaction.getCreatedAt());

        return db.insertWithOnConflict(TABLE_TRANSACTIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int updateTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_AMOUNT, transaction.getAmount());
        values.put(COL_TRANS_TYPE, transaction.getType());
        values.put(COL_TRANS_CATEGORY, transaction.getCategory());
        values.put(COL_TRANS_DESCRIPTION, transaction.getDescription());
        values.put(COL_TRANS_DATE, transaction.getDate());

        return db.update(TABLE_TRANSACTIONS, values, COL_TRANS_ID + "=? AND " + COL_TRANS_USER_ID + "=?",
                new String[]{transaction.getTransactionId(), transaction.getUserId()});
    }

    public int deleteTransaction(String transactionId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TRANSACTIONS, COL_TRANS_ID + "=? AND " + COL_TRANS_USER_ID + "=?",
                new String[]{transactionId, userId});
    }

    public Transaction getTransactionById(String transactionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, COL_TRANS_ID + "=?", new String[]{transactionId},
                null, null, null);
        Transaction transaction = null;
        if (cursor != null && cursor.moveToFirst()) {
            transaction = cursorToTransaction(cursor);
            cursor.close();
        }
        return transaction;
    }

    public List<Transaction> getAllTransactions(String userId) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, COL_TRANS_USER_ID + "=?", new String[]{userId},
                null, null, COL_TRANS_DATE + " DESC, " + COL_TRANS_CREATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Transaction> getRecentTransactions(String userId, int limit) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, COL_TRANS_USER_ID + "=?", new String[]{userId},
                null, null, COL_TRANS_DATE + " DESC, " + COL_TRANS_CREATED_AT + " DESC", String.valueOf(limit));

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Transaction> getFilteredTransactions(String userId, String typeFilter,
                                                    String categoryFilter, String startDate,
                                                    String endDate, String query, String sortBy) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selection = new StringBuilder(COL_TRANS_USER_ID + "=?");
        List<String> argsList = new ArrayList<>();
        argsList.add(userId);

        if (!TextUtils.isEmpty(typeFilter) && !"ALL".equalsIgnoreCase(typeFilter)) {
            selection.append(" AND ").append(COL_TRANS_TYPE).append("=?");
            argsList.add(typeFilter);
        }

        if (!TextUtils.isEmpty(categoryFilter) && !"ALL".equalsIgnoreCase(categoryFilter)) {
            selection.append(" AND ").append(COL_TRANS_CATEGORY).append("=?");
            argsList.add(categoryFilter);
        }

        if (!TextUtils.isEmpty(startDate)) {
            selection.append(" AND ").append(COL_TRANS_DATE).append(">=?");
            argsList.add(startDate);
        }

        if (!TextUtils.isEmpty(endDate)) {
            selection.append(" AND ").append(COL_TRANS_DATE).append("<=?");
            argsList.add(endDate);
        }

        if (!TextUtils.isEmpty(query)) {
            selection.append(" AND (").append(COL_TRANS_DESCRIPTION).append(" LIKE ? OR ")
                    .append(COL_TRANS_CATEGORY).append(" LIKE ?)");
            argsList.add("%" + query + "%");
            argsList.add("%" + query + "%");
        }

        String orderBy = COL_TRANS_DATE + " DESC, " + COL_TRANS_CREATED_AT + " DESC";
        if ("date_asc".equalsIgnoreCase(sortBy)) {
            orderBy = COL_TRANS_DATE + " ASC, " + COL_TRANS_CREATED_AT + " ASC";
        } else if ("amount_desc".equalsIgnoreCase(sortBy)) {
            orderBy = COL_TRANS_AMOUNT + " DESC";
        } else if ("amount_asc".equalsIgnoreCase(sortBy)) {
            orderBy = COL_TRANS_AMOUNT + " ASC";
        }

        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, selection.toString(),
                argsList.toArray(new String[0]), null, null, orderBy);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public double getTotalIncome(String userId, String monthPrefix) {
        return calculateTotal(userId, Transaction.TYPE_INCOME, monthPrefix);
    }

    public double getTotalExpense(String userId, String monthPrefix) {
        return calculateTotal(userId, Transaction.TYPE_EXPENSE, monthPrefix);
    }

    private double calculateTotal(String userId, String type, String monthPrefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        String[] args;
        if (TextUtils.isEmpty(monthPrefix)) {
            query = "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS
                    + " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=?";
            args = new String[]{userId, type};
        } else {
            query = "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS
                    + " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=? AND "
                    + COL_TRANS_DATE + " LIKE ?";
            args = new String[]{userId, type, monthPrefix + "%"};
        }

        Cursor cursor = db.rawQuery(query, args);
        double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public double getTotalBalance(String userId) {
        double income = calculateTotal(userId, Transaction.TYPE_INCOME, null);
        double expense = calculateTotal(userId, Transaction.TYPE_EXPENSE, null);
        return income - expense;
    }

    public Map<String, Double> getCategoryExpenses(String userId, String monthPrefix) {
        Map<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query;
        String[] args;
        if (TextUtils.isEmpty(monthPrefix)) {
            query = "SELECT " + COL_TRANS_CATEGORY + ", SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS
                    + " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=? GROUP BY " + COL_TRANS_CATEGORY
                    + " ORDER BY SUM(" + COL_TRANS_AMOUNT + ") DESC";
            args = new String[]{userId, Transaction.TYPE_EXPENSE};
        } else {
            query = "SELECT " + COL_TRANS_CATEGORY + ", SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS
                    + " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=? AND " + COL_TRANS_DATE + " LIKE ?"
                    + " GROUP BY " + COL_TRANS_CATEGORY + " ORDER BY SUM(" + COL_TRANS_AMOUNT + ") DESC";
            args = new String[]{userId, Transaction.TYPE_EXPENSE, monthPrefix + "%"};
        }

        Cursor cursor = db.rawQuery(query, args);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String cat = cursor.getString(0);
                double amt = cursor.getDouble(1);
                map.put(cat, amt);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return map;
    }

    public Map<String, Double> getDailyExpenses(String userId, String monthPrefix) {
        Map<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COL_TRANS_DATE + ", SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS
                + " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=? AND " + COL_TRANS_DATE + " LIKE ?"
                + " GROUP BY " + COL_TRANS_DATE + " ORDER BY " + COL_TRANS_DATE + " ASC";
        String[] args = new String[]{userId, Transaction.TYPE_EXPENSE, monthPrefix + "%"};

        Cursor cursor = db.rawQuery(query, args);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                double amt = cursor.getDouble(1);
                map.put(date, amt);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return map;
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        return new Transaction(
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_USER_ID)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANS_AMOUNT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DATE)),
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_CREATED_AT))
        );
    }

    // ==========================================
    // BUDGET OPERATIONS
    // ==========================================

    public long insertOrUpdateBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BUDGET_ID, budget.getBudgetId());
        values.put(COL_BUDGET_USER_ID, budget.getUserId());
        values.put(COL_BUDGET_CATEGORY, budget.getCategory());
        values.put(COL_BUDGET_LIMIT, budget.getMonthlyLimit());
        values.put(COL_BUDGET_MONTH, budget.getMonth());

        return db.insertWithOnConflict(TABLE_BUDGETS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int deleteBudget(String budgetId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_BUDGETS, COL_BUDGET_ID + "=? AND " + COL_BUDGET_USER_ID + "=?",
                new String[]{budgetId, userId});
    }

    public List<Budget> getBudgets(String userId, String month) {
        List<Budget> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BUDGETS, null, COL_BUDGET_USER_ID + "=? AND " + COL_BUDGET_MONTH + "=?",
                new String[]{userId, month}, null, null, COL_BUDGET_CATEGORY + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Budget b = new Budget(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_CATEGORY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BUDGET_LIMIT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_MONTH))
                );

                // Compute spent amount for this category in the month
                if (Budget.CATEGORY_ALL.equalsIgnoreCase(b.getCategory())) {
                    b.setSpentAmount(getTotalExpense(userId, month));
                } else {
                    b.setSpentAmount(getExpenseByCategory(userId, b.getCategory(), month));
                }
                list.add(b);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Budget getOverallBudget(String userId, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BUDGETS, null,
                COL_BUDGET_USER_ID + "=? AND " + COL_BUDGET_MONTH + "=? AND " + COL_BUDGET_CATEGORY + "=?",
                new String[]{userId, month, Budget.CATEGORY_ALL}, null, null, null);

        Budget budget = null;
        if (cursor != null && cursor.moveToFirst()) {
            budget = new Budget(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_CATEGORY)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BUDGET_LIMIT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_BUDGET_MONTH))
            );
            budget.setSpentAmount(getTotalExpense(userId, month));
            cursor.close();
        }
        return budget;
    }

    public double getExpenseByCategory(String userId, String category, String monthPrefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS
                + " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_TYPE + "=? AND "
                + COL_TRANS_CATEGORY + "=? AND " + COL_TRANS_DATE + " LIKE ?";
        String[] args = new String[]{userId, Transaction.TYPE_EXPENSE, category, monthPrefix + "%"};

        Cursor cursor = db.rawQuery(query, args);
        double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, null, null);
        db.delete(TABLE_BUDGETS, null, null);
        db.delete(TABLE_USERS, null, null);
    }
}
