package com.example.expensetracker.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.adapters.CategoryAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.firebase.FirestoreManager;
import com.example.expensetracker.models.CategoryItem;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.utils.CurrencyManager;
import com.example.expensetracker.utils.DateUtils;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Activity for Adding, Viewing, Editing, and Deleting a financial transaction.
 */
public class TransactionEditorActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "extra_transaction_id";
    public static final String EXTRA_INITIAL_TYPE = "extra_initial_type";

    private ImageView ivBackEditor, ivDeleteTransaction;
    private TextView tvEditorTitle, btnTypeExpense, btnTypeIncome;
    private TextView tvAmountCurrencyPrefix, tvSelectedDate;
    private EditText etTransactionAmount;
    private TextInputEditText etTransactionDesc;
    private MaterialCardView cardDatePicker;
    private RecyclerView rvCategoryPicker;
    private MaterialButton btnSaveTransaction;

    private CategoryAdapter categoryAdapter;
    private String currentType = Transaction.TYPE_EXPENSE;
    private String selectedCategory = "Food";
    private String selectedDate = "";
    private String editingTransactionId = null;

    private DatabaseHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_editor);

        dbHelper = DatabaseHelper.getInstance(this);
        currentUserId = SessionManager.getInstance(this).getUserId();

        initViews();
        setupCategoryRecyclerView();
        checkIntentExtras();

        setupListeners();
    }

    private void initViews() {
        ivBackEditor = findViewById(R.id.ivBackEditor);
        ivDeleteTransaction = findViewById(R.id.ivDeleteTransaction);
        tvEditorTitle = findViewById(R.id.tvEditorTitle);
        btnTypeExpense = findViewById(R.id.btnTypeExpense);
        btnTypeIncome = findViewById(R.id.btnTypeIncome);
        tvAmountCurrencyPrefix = findViewById(R.id.tvAmountCurrencyPrefix);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        etTransactionAmount = findViewById(R.id.etTransactionAmount);
        etTransactionDesc = findViewById(R.id.etTransactionDesc);
        cardDatePicker = findViewById(R.id.cardDatePicker);
        rvCategoryPicker = findViewById(R.id.rvCategoryPicker);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);

        // Display user's currency symbol
        String symbol = CurrencyManager.getSymbol(SessionManager.getInstance(this).getCurrency());
        tvAmountCurrencyPrefix.setText(symbol);

        // Default Date
        selectedDate = DateUtils.getCurrentDate();
        tvSelectedDate.setText(DateUtils.formatToDisplay(selectedDate));
    }

    private void setupCategoryRecyclerView() {
        rvCategoryPicker.setLayoutManager(new GridLayoutManager(this, 4));
        categoryAdapter = new CategoryAdapter(this, getCategoriesForType(currentType), selectedCategory, new CategoryAdapter.OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(CategoryItem item) {
                selectedCategory = item.getName();
            }
        });
        rvCategoryPicker.setAdapter(categoryAdapter);
    }

    private void checkIntentExtras() {
        if (getIntent().hasExtra(EXTRA_TRANSACTION_ID)) {
            editingTransactionId = getIntent().getStringExtra(EXTRA_TRANSACTION_ID);
            loadTransactionForEdit(editingTransactionId);
        } else if (getIntent().hasExtra(EXTRA_INITIAL_TYPE)) {
            String initType = getIntent().getStringExtra(EXTRA_INITIAL_TYPE);
            if (Transaction.TYPE_INCOME.equalsIgnoreCase(initType)) {
                switchType(Transaction.TYPE_INCOME);
            } else {
                switchType(Transaction.TYPE_EXPENSE);
            }
        } else {
            switchType(Transaction.TYPE_EXPENSE);
        }
    }

    private void loadTransactionForEdit(String transactionId) {
        Transaction t = dbHelper.getTransactionById(transactionId);
        if (t == null) {
            Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvEditorTitle.setText(R.string.edit_transaction);
        ivDeleteTransaction.setVisibility(View.VISIBLE);

        etTransactionAmount.setText(String.valueOf(t.getAmount()));
        etTransactionDesc.setText(t.getDescription());
        selectedDate = t.getDate();
        tvSelectedDate.setText(DateUtils.formatToDisplay(selectedDate));
        selectedCategory = t.getCategory();

        switchType(t.getType());
    }

    private void switchType(String newType) {
        currentType = newType;
        boolean isExpense = Transaction.TYPE_EXPENSE.equalsIgnoreCase(newType);

        if (isExpense) {
            btnTypeExpense.setBackgroundResource(R.drawable.bg_chip_selected);
            btnTypeExpense.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.expense_red)));
            btnTypeExpense.setTextColor(ContextCompat.getColor(this, R.color.on_primary));

            btnTypeIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
            btnTypeIncome.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            if (!isCategoryInList(selectedCategory, Transaction.EXPENSE_CATEGORIES)) {
                selectedCategory = Transaction.EXPENSE_CATEGORIES[0];
            }
        } else {
            btnTypeIncome.setBackgroundResource(R.drawable.bg_chip_selected);
            btnTypeIncome.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.income_green)));
            btnTypeIncome.setTextColor(ContextCompat.getColor(this, R.color.on_primary));

            btnTypeExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
            btnTypeExpense.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            if (!isCategoryInList(selectedCategory, Transaction.INCOME_CATEGORIES)) {
                selectedCategory = Transaction.INCOME_CATEGORIES[0];
            }
        }

        categoryAdapter.updateList(getCategoriesForType(currentType), selectedCategory);
    }

    private boolean isCategoryInList(String cat, String[] list) {
        for (String s : list) {
            if (s.equalsIgnoreCase(cat)) return true;
        }
        return false;
    }

    private List<CategoryItem> getCategoriesForType(String type) {
        List<CategoryItem> list = new ArrayList<>();
        if (Transaction.TYPE_INCOME.equalsIgnoreCase(type)) {
            for (String cat : Transaction.INCOME_CATEGORIES) {
                Transaction temp = new Transaction();
                temp.setCategory(cat);
                list.add(new CategoryItem(cat, Transaction.TYPE_INCOME, temp.getCategoryIconRes(), temp.getCategoryColorRes()));
            }
        } else {
            for (String cat : Transaction.EXPENSE_CATEGORIES) {
                Transaction temp = new Transaction();
                temp.setCategory(cat);
                list.add(new CategoryItem(cat, Transaction.TYPE_EXPENSE, temp.getCategoryIconRes(), temp.getCategoryColorRes()));
            }
        }
        return list;
    }

    private void setupListeners() {
        ivBackEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnTypeExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchType(Transaction.TYPE_EXPENSE);
            }
        });

        btnTypeIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchType(Transaction.TYPE_INCOME);
            }
        });

        cardDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnSaveTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTransaction();
            }
        });

        ivDeleteTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteTransaction();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                selectedDate = formatted;
                tvSelectedDate.setText(DateUtils.formatToDisplay(selectedDate));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void saveTransaction() {
        String amountStr = etTransactionAmount.getText() != null ? etTransactionAmount.getText().toString().trim() : "";
        if (TextUtils.isEmpty(amountStr)) {
            etTransactionAmount.setError("Please enter amount");
            etTransactionAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etTransactionAmount.setError("Amount must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            etTransactionAmount.setError("Invalid number");
            return;
        }

        String description = etTransactionDesc.getText() != null ? etTransactionDesc.getText().toString().trim() : "";

        if (editingTransactionId != null) {
            // Update existing
            Transaction t = new Transaction(editingTransactionId, currentUserId, amount, currentType,
                    selectedCategory, description, selectedDate, System.currentTimeMillis());
            dbHelper.updateTransaction(t);
            FirestoreManager.getInstance(this).uploadTransaction(t);
            Toast.makeText(this, "Transaction updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // Insert new
            String id = "txn_" + UUID.randomUUID().toString();
            Transaction t = new Transaction(id, currentUserId, amount, currentType,
                    selectedCategory, description, selectedDate, System.currentTimeMillis());
            dbHelper.insertTransaction(t);
            FirestoreManager.getInstance(this).uploadTransaction(t);
            Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void confirmDeleteTransaction() {
        if (editingTransactionId == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_transaction)
                .setMessage("Are you sure you want to permanently delete this transaction?")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteTransaction(editingTransactionId, currentUserId);
                        FirestoreManager.getInstance(TransactionEditorActivity.this)
                                .deleteTransaction(currentUserId, editingTransactionId);
                        Toast.makeText(TransactionEditorActivity.this, "Transaction deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
