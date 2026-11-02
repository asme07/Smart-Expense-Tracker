package com.example.expensetracker.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.expensetracker.R;
import com.example.expensetracker.activities.TransactionEditorActivity;
import com.example.expensetracker.adapters.TransactionAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.firebase.FirestoreManager;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.utils.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Transaction History fragment featuring search, filtering by type/category, and sorting.
 */
public class TransactionsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshTransactions;
    private EditText etSearchTransactions;
    private ImageView ivClearSearch, ivSortTransactions;
    private TextView chipFilterAll, chipFilterIncome, chipFilterExpense, chipFilterCategory;
    private TextView tvTransactionCount, tvCurrentSortMode;
    private RecyclerView rvTransactions;
    private LinearLayout llTransactionsEmptyState;

    private TransactionAdapter adapter;
    private DatabaseHelper dbHelper;
    private String currentUserId;

    private String currentTypeFilter = "ALL"; // ALL, INCOME, EXPENSE
    private String currentCategoryFilter = "ALL";
    private String currentSortBy = "date_desc"; // date_desc, date_asc, amount_desc, amount_asc
    private String currentSearchKeyword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        currentUserId = SessionManager.getInstance(requireContext()).getUserId();

        initViews(view);
        setupRecyclerView();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void initViews(View view) {
        swipeRefreshTransactions = view.findViewById(R.id.swipeRefreshTransactions);
        etSearchTransactions = view.findViewById(R.id.etSearchTransactions);
        ivClearSearch = view.findViewById(R.id.ivClearSearch);
        ivSortTransactions = view.findViewById(R.id.ivSortTransactions);
        chipFilterAll = view.findViewById(R.id.chipFilterAll);
        chipFilterIncome = view.findViewById(R.id.chipFilterIncome);
        chipFilterExpense = view.findViewById(R.id.chipFilterExpense);
        chipFilterCategory = view.findViewById(R.id.chipFilterCategory);
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount);
        tvCurrentSortMode = view.findViewById(R.id.tvCurrentSortMode);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        llTransactionsEmptyState = view.findViewById(R.id.llTransactionsEmptyState);
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(requireContext(), null, new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(getContext(), TransactionEditorActivity.class);
                intent.putExtra(TransactionEditorActivity.EXTRA_TRANSACTION_ID, transaction.getTransactionId());
                startActivity(intent);
            }

            @Override
            public void onTransactionLongClick(Transaction transaction) {
                showQuickOptionsDialog(transaction);
            }
        });
        rvTransactions.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefreshTransactions.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTransactions();
                swipeRefreshTransactions.setRefreshing(false);
            }
        });

        // Search text watcher
        etSearchTransactions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchKeyword = s.toString().trim();
                ivClearSearch.setVisibility(currentSearchKeyword.isEmpty() ? View.GONE : View.VISIBLE);
                loadTransactions();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearchTransactions.setText("");
            }
        });

        // Chips Filter
        chipFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTypeChip("ALL");
            }
        });

        chipFilterIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTypeChip(Transaction.TYPE_INCOME);
            }
        });

        chipFilterExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTypeChip(Transaction.TYPE_EXPENSE);
            }
        });

        chipFilterCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryFilterDialog();
            }
        });

        ivSortTransactions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortDialog();
            }
        });
    }

    private void selectTypeChip(String type) {
        currentTypeFilter = type;

        resetChipStyle(chipFilterAll);
        resetChipStyle(chipFilterIncome);
        resetChipStyle(chipFilterExpense);

        if ("ALL".equalsIgnoreCase(type)) {
            setChipSelected(chipFilterAll);
        } else if (Transaction.TYPE_INCOME.equalsIgnoreCase(type)) {
            setChipSelected(chipFilterIncome);
        } else if (Transaction.TYPE_EXPENSE.equalsIgnoreCase(type)) {
            setChipSelected(chipFilterExpense);
        }

        loadTransactions();
    }

    private void setChipSelected(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_selected);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_primary));
    }

    private void resetChipStyle(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_unselected);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
    }

    private void showCategoryFilterDialog() {
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.addAll(Arrays.asList(Transaction.INCOME_CATEGORIES));
        categories.addAll(Arrays.asList(Transaction.EXPENSE_CATEGORIES));

        final String[] items = categories.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Category")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            currentCategoryFilter = "ALL";
                            chipFilterCategory.setText("Category: All");
                            resetChipStyle(chipFilterCategory);
                        } else {
                            currentCategoryFilter = items[which];
                            chipFilterCategory.setText("Category: " + currentCategoryFilter);
                            setChipSelected(chipFilterCategory);
                        }
                        loadTransactions();
                    }
                })
                .show();
    }

    private void showSortDialog() {
        final String[] options = {"Newest First", "Oldest First", "Highest Amount", "Lowest Amount"};
        final String[] keys = {"date_desc", "date_asc", "amount_desc", "amount_asc"};

        int checked = 0;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equalsIgnoreCase(currentSortBy)) {
                checked = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Sort Transactions")
                .setSingleChoiceItems(options, checked, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentSortBy = keys[which];
                        tvCurrentSortMode.setText(options[which]);
                        loadTransactions();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showQuickOptionsDialog(Transaction t) {
        CharSequence[] options = new CharSequence[]{"Edit Transaction", "Delete Transaction"};
        new AlertDialog.Builder(requireContext())
                .setTitle(t.getCategory() + " (" + t.getType() + ")")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(getContext(), TransactionEditorActivity.class);
                            intent.putExtra(TransactionEditorActivity.EXTRA_TRANSACTION_ID, t.getTransactionId());
                            startActivity(intent);
                        } else {
                            dbHelper.deleteTransaction(t.getTransactionId(), currentUserId);
                            FirestoreManager.getInstance(requireContext())
                                    .deleteTransaction(currentUserId, t.getTransactionId());
                            Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                            loadTransactions();
                        }
                    }
                })
                .show();
    }

    private void loadTransactions() {
        if (getContext() == null) return;

        List<Transaction> list = dbHelper.getFilteredTransactions(
                currentUserId,
                currentTypeFilter,
                currentCategoryFilter,
                null,
                null,
                currentSearchKeyword,
                currentSortBy
        );

        tvTransactionCount.setText(list.size() + " Transactions");

        if (list.isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            llTransactionsEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvTransactions.setVisibility(View.VISIBLE);
            llTransactionsEmptyState.setVisibility(View.GONE);
            adapter.updateData(list);
        }
    }
}
