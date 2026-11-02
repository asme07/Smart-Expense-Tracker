package com.example.expensetracker.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.expensetracker.activities.BudgetEditorActivity;
import com.example.expensetracker.adapters.BudgetAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.firebase.FirestoreManager;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.utils.CurrencyManager;
import com.example.expensetracker.utils.DateUtils;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Budget Management fragment featuring overall monthly limits, category-wise budgets, and visual threshold alerts.
 */
public class BudgetFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshBudget;
    private TextView tvBudgetActiveMonth, tvOverallAlertBadge;
    private TextView tvOverallBudgetLimit, tvOverallBudgetSpent, tvOverallBudgetRemaining;
    private ProgressBar pbOverallBudget;
    private MaterialButton btnSetOverallBudget, btnAddCategoryBudget;
    private RecyclerView rvCategoryBudgets;
    private LinearLayout llBudgetEmptyState;

    private BudgetAdapter adapter;
    private DatabaseHelper dbHelper;
    private String currentUserId;
    private String currentMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        currentUserId = SessionManager.getInstance(requireContext()).getUserId();
        currentMonth = DateUtils.getCurrentMonth();

        initViews(view);
        setupRecyclerView();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgetData();
    }

    private void initViews(View view) {
        swipeRefreshBudget = view.findViewById(R.id.swipeRefreshBudget);
        tvBudgetActiveMonth = view.findViewById(R.id.tvBudgetActiveMonth);
        tvOverallAlertBadge = view.findViewById(R.id.tvOverallAlertBadge);
        tvOverallBudgetLimit = view.findViewById(R.id.tvOverallBudgetLimit);
        tvOverallBudgetSpent = view.findViewById(R.id.tvOverallBudgetSpent);
        tvOverallBudgetRemaining = view.findViewById(R.id.tvOverallBudgetRemaining);
        pbOverallBudget = view.findViewById(R.id.pbOverallBudget);
        btnSetOverallBudget = view.findViewById(R.id.btnSetOverallBudget);
        btnAddCategoryBudget = view.findViewById(R.id.btnAddCategoryBudget);
        rvCategoryBudgets = view.findViewById(R.id.rvCategoryBudgets);
        llBudgetEmptyState = view.findViewById(R.id.llBudgetEmptyState);

        tvBudgetActiveMonth.setText(DateUtils.getCurrentMonthTitle());
    }

    private void setupRecyclerView() {
        rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BudgetAdapter(requireContext(), null, new BudgetAdapter.OnBudgetClickListener() {
            @Override
            public void onBudgetClick(Budget budget) {
                Intent intent = new Intent(getContext(), BudgetEditorActivity.class);
                intent.putExtra(BudgetEditorActivity.EXTRA_BUDGET_ID, budget.getBudgetId());
                intent.putExtra(BudgetEditorActivity.EXTRA_CATEGORY_NAME, budget.getCategory());
                startActivity(intent);
            }

            @Override
            public void onBudgetLongClick(Budget budget) {
                showDeleteDialog(budget);
            }
        });
        rvCategoryBudgets.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefreshBudget.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBudgetData();
                swipeRefreshBudget.setRefreshing(false);
            }
        });

        btnSetOverallBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Budget overall = dbHelper.getOverallBudget(currentUserId, currentMonth);
                Intent intent = new Intent(getContext(), BudgetEditorActivity.class);
                intent.putExtra(BudgetEditorActivity.EXTRA_CATEGORY_NAME, Budget.CATEGORY_ALL);
                if (overall != null) {
                    intent.putExtra(BudgetEditorActivity.EXTRA_BUDGET_ID, overall.getBudgetId());
                }
                startActivity(intent);
            }
        });

        btnAddCategoryBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BudgetEditorActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDeleteDialog(Budget budget) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Budget")
                .setMessage("Delete budget for " + budget.getCategory() + "?")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteBudget(budget.getBudgetId(), currentUserId);
                        FirestoreManager.getInstance(requireContext())
                                .deleteBudget(currentUserId, budget.getBudgetId());
                        Toast.makeText(getContext(), "Budget deleted.", Toast.LENGTH_SHORT).show();
                        loadBudgetData();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadBudgetData() {
        if (getContext() == null) return;

        // 1. Load Overall Budget
        Budget overall = dbHelper.getOverallBudget(currentUserId, currentMonth);
        double totalExpense = dbHelper.getTotalExpense(currentUserId, currentMonth);

        if (overall != null && overall.getMonthlyLimit() > 0) {
            double limit = overall.getMonthlyLimit();
            double remaining = overall.getRemainingAmount();
            int progress = overall.getProgressPercentage();

            tvOverallBudgetLimit.setText(CurrencyManager.formatAmount(requireContext(), limit));
            tvOverallBudgetSpent.setText(CurrencyManager.formatAmount(requireContext(), totalExpense));
            tvOverallBudgetRemaining.setText(CurrencyManager.formatAmount(requireContext(), remaining));
            pbOverallBudget.setProgress(progress);

            if (overall.isExceeded()) {
                tvOverallAlertBadge.setText(R.string.budget_exceeded);
                tvOverallAlertBadge.setBackgroundResource(R.drawable.bg_expense_badge);
                tvOverallAlertBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red_dark));
                pbOverallBudget.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.progress_custom_danger));
                tvOverallBudgetRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
            } else if (overall.isWarningReached()) {
                tvOverallAlertBadge.setText(R.string.budget_warning);
                tvOverallAlertBadge.setBackgroundResource(R.drawable.bg_warning_badge);
                tvOverallAlertBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_amber_dark));
                pbOverallBudget.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.progress_custom_warning));
                tvOverallBudgetRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_amber));
            } else {
                tvOverallAlertBadge.setText(R.string.budget_safe);
                tvOverallAlertBadge.setBackgroundResource(R.drawable.bg_income_badge);
                tvOverallAlertBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green_dark));
                pbOverallBudget.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.progress_custom_green));
                tvOverallBudgetRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green));
            }
        } else {
            tvOverallBudgetLimit.setText("$ 0.00");
            tvOverallBudgetSpent.setText(CurrencyManager.formatAmount(requireContext(), totalExpense));
            tvOverallBudgetRemaining.setText("$ 0.00");
            pbOverallBudget.setProgress(0);
            tvOverallAlertBadge.setText("No Limit Set");
            tvOverallAlertBadge.setBackgroundResource(R.drawable.bg_chip_unselected);
            tvOverallAlertBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }

        // 2. Load Category Budgets (excluding "All Categories" since it's displayed in overall card)
        List<Budget> allBudgets = dbHelper.getBudgets(currentUserId, currentMonth);
        List<Budget> categoryBudgets = new ArrayList<>();
        for (Budget b : allBudgets) {
            if (!Budget.CATEGORY_ALL.equalsIgnoreCase(b.getCategory())) {
                categoryBudgets.add(b);
            }
        }

        if (categoryBudgets.isEmpty()) {
            rvCategoryBudgets.setVisibility(View.GONE);
            llBudgetEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvCategoryBudgets.setVisibility(View.VISIBLE);
            llBudgetEmptyState.setVisibility(View.GONE);
            adapter.updateData(categoryBudgets);
        }
    }
}
