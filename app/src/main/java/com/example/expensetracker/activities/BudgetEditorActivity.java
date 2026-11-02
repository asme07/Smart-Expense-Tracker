package com.example.expensetracker.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.adapters.CategoryAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.firebase.FirestoreManager;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.CategoryItem;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.utils.CurrencyManager;
import com.example.expensetracker.utils.DateUtils;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity for configuring overall monthly budgets and category budgets.
 */
public class BudgetEditorActivity extends AppCompatActivity {

    public static final String EXTRA_BUDGET_ID = "extra_budget_id";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private ImageView ivBackBudgetEditor, ivDeleteBudget;
    private TextView tvBudgetEditorTitle, tvBudgetMonthSelected, tvBudgetCurrencyPrefix;
    private EditText etBudgetLimit;
    private RecyclerView rvBudgetCategoryPicker;
    private MaterialButton btnSaveBudget;

    private CategoryAdapter categoryAdapter;
    private String selectedCategory = Budget.CATEGORY_ALL;
    private String activeMonth;
    private String editingBudgetId = null;

    private DatabaseHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_editor);

        dbHelper = DatabaseHelper.getInstance(this);
        currentUserId = SessionManager.getInstance(this).getUserId();
        activeMonth = DateUtils.getCurrentMonth();

        initViews();
        setupCategoryList();
        checkExtras();
        setupListeners();
    }

    private void initViews() {
        ivBackBudgetEditor = findViewById(R.id.ivBackBudgetEditor);
        ivDeleteBudget = findViewById(R.id.ivDeleteBudget);
        tvBudgetEditorTitle = findViewById(R.id.tvBudgetEditorTitle);
        tvBudgetMonthSelected = findViewById(R.id.tvBudgetMonthSelected);
        tvBudgetCurrencyPrefix = findViewById(R.id.tvBudgetCurrencyPrefix);
        etBudgetLimit = findViewById(R.id.etBudgetLimit);
        rvBudgetCategoryPicker = findViewById(R.id.rvBudgetCategoryPicker);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);

        tvBudgetMonthSelected.setText(DateUtils.getCurrentMonthTitle());
        String symbol = CurrencyManager.getSymbol(SessionManager.getInstance(this).getCurrency());
        tvBudgetCurrencyPrefix.setText(symbol);
    }

    private void setupCategoryList() {
        List<CategoryItem> list = new ArrayList<>();
        // Option 1: All categories / Overall budget
        list.add(new CategoryItem(Budget.CATEGORY_ALL, Transaction.TYPE_EXPENSE, R.drawable.ic_nav_budget, R.color.secondary));

        // Other Expense categories
        for (String cat : Transaction.EXPENSE_CATEGORIES) {
            Transaction temp = new Transaction();
            temp.setCategory(cat);
            list.add(new CategoryItem(cat, Transaction.TYPE_EXPENSE, temp.getCategoryIconRes(), temp.getCategoryColorRes()));
        }

        rvBudgetCategoryPicker.setLayoutManager(new GridLayoutManager(this, 4));
        categoryAdapter = new CategoryAdapter(this, list, selectedCategory, new CategoryAdapter.OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(CategoryItem item) {
                selectedCategory = item.getName();
            }
        });
        rvBudgetCategoryPicker.setAdapter(categoryAdapter);
    }

    private void checkExtras() {
        if (getIntent().hasExtra(EXTRA_BUDGET_ID)) {
            editingBudgetId = getIntent().getStringExtra(EXTRA_BUDGET_ID);
            tvBudgetEditorTitle.setText(R.string.edit_budget);
            ivDeleteBudget.setVisibility(View.VISIBLE);

            if (getIntent().hasExtra(EXTRA_CATEGORY_NAME)) {
                selectedCategory = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
                categoryAdapter.setSelectedCategory(selectedCategory);
            }
        } else if (getIntent().hasExtra(EXTRA_CATEGORY_NAME)) {
            selectedCategory = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
            categoryAdapter.setSelectedCategory(selectedCategory);
        }
    }

    private void setupListeners() {
        ivBackBudgetEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBudget();
            }
        });

        ivDeleteBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteBudget();
            }
        });
    }

    private void saveBudget() {
        String limitStr = etBudgetLimit.getText() != null ? etBudgetLimit.getText().toString().trim() : "";
        if (TextUtils.isEmpty(limitStr)) {
            etBudgetLimit.setError("Please enter spending limit");
            etBudgetLimit.requestFocus();
            return;
        }

        double limit;
        try {
            limit = Double.parseDouble(limitStr);
            if (limit <= 0) {
                etBudgetLimit.setError("Limit must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            etBudgetLimit.setError("Invalid number");
            return;
        }

        String id = editingBudgetId != null ? editingBudgetId : "bgt_" + UUID.randomUUID().toString();
        Budget b = new Budget(id, currentUserId, selectedCategory, limit, activeMonth);

        dbHelper.insertOrUpdateBudget(b);
        FirestoreManager.getInstance(this).uploadBudget(b);

        Toast.makeText(this, "Budget saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDeleteBudget() {
        if (editingBudgetId == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to remove this budget?")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteBudget(editingBudgetId, currentUserId);
                        FirestoreManager.getInstance(BudgetEditorActivity.this)
                                .deleteBudget(currentUserId, editingBudgetId);
                        Toast.makeText(BudgetEditorActivity.this, "Budget removed.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
