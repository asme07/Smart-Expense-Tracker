package com.example.expensetracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.expensetracker.R;
import com.example.expensetracker.activities.MainActivity;
import com.example.expensetracker.activities.TransactionEditorActivity;
import com.example.expensetracker.adapters.TransactionAdapter;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.utils.CurrencyManager;
import com.example.expensetracker.utils.DateUtils;
import com.example.expensetracker.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Home dashboard displaying financial summary, balance, budget progress, and recent transactions.
 */
public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshHome;
    private TextView tvHomeGreeting, tvHomeUserName, tvHomeCurrentMonth;
    private TextView tvTotalBalance, tvMonthlyIncome, tvMonthlyExpense;
    private ImageView ivToggleBalance;
    private MaterialButton btnQuickAddIncome, btnQuickAddExpense;
    private MaterialCardView cardHomeBudget;
    private ProgressBar pbHomeBudget;
    private TextView tvHomeBudgetStatusBadge, tvHomeSpentVsBudget, tvHomeRemainingBudget;
    private TextView tvHomeSeeAll;
    private RecyclerView rvRecentTransactions;
    private LinearLayout llRecentEmptyState;

    private TransactionAdapter transactionAdapter;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private String currentUserId;
    private boolean isBalanceVisible = true;
    private double currentBalance = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());
        currentUserId = sessionManager.getUserId();

        initViews(view);
        setupRecyclerView();
        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews(View view) {
        swipeRefreshHome = view.findViewById(R.id.swipeRefreshHome);
        tvHomeGreeting = view.findViewById(R.id.tvHomeGreeting);
        tvHomeUserName = view.findViewById(R.id.tvHomeUserName);
        tvHomeCurrentMonth = view.findViewById(R.id.tvHomeCurrentMonth);
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvMonthlyIncome = view.findViewById(R.id.tvMonthlyIncome);
        tvMonthlyExpense = view.findViewById(R.id.tvMonthlyExpense);
        ivToggleBalance = view.findViewById(R.id.ivToggleBalance);
        btnQuickAddIncome = view.findViewById(R.id.btnQuickAddIncome);
        btnQuickAddExpense = view.findViewById(R.id.btnQuickAddExpense);
        cardHomeBudget = view.findViewById(R.id.cardHomeBudget);
        pbHomeBudget = view.findViewById(R.id.pbHomeBudget);
        tvHomeBudgetStatusBadge = view.findViewById(R.id.tvHomeBudgetStatusBadge);
        tvHomeSpentVsBudget = view.findViewById(R.id.tvHomeSpentVsBudget);
        tvHomeRemainingBudget = view.findViewById(R.id.tvHomeRemainingBudget);
        tvHomeSeeAll = view.findViewById(R.id.tvHomeSeeAll);
        rvRecentTransactions = view.findViewById(R.id.rvRecentTransactions);
        llRecentEmptyState = view.findViewById(R.id.llRecentEmptyState);

        tvHomeUserName.setText(sessionManager.getUserName());
        tvHomeCurrentMonth.setText(DateUtils.getCurrentMonthTitle());
    }

    private void setupRecyclerView() {
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        transactionAdapter = new TransactionAdapter(requireContext(), null, new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(Transaction transaction) {
                Intent intent = new Intent(getContext(), TransactionEditorActivity.class);
                intent.putExtra(TransactionEditorActivity.EXTRA_TRANSACTION_ID, transaction.getTransactionId());
                startActivity(intent);
            }

            @Override
            public void onTransactionLongClick(Transaction transaction) {
                Intent intent = new Intent(getContext(), TransactionEditorActivity.class);
                intent.putExtra(TransactionEditorActivity.EXTRA_TRANSACTION_ID, transaction.getTransactionId());
                startActivity(intent);
            }
        });
        rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        swipeRefreshHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDashboardData();
                swipeRefreshHome.setRefreshing(false);
            }
        });

        ivToggleBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBalanceVisible = !isBalanceVisible;
                updateBalanceDisplay();
            }
        });

        btnQuickAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TransactionEditorActivity.class);
                intent.putExtra(TransactionEditorActivity.EXTRA_INITIAL_TYPE, Transaction.TYPE_INCOME);
                startActivity(intent);
            }
        });

        btnQuickAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TransactionEditorActivity.class);
                intent.putExtra(TransactionEditorActivity.EXTRA_INITIAL_TYPE, Transaction.TYPE_EXPENSE);
                startActivity(intent);
            }
        });

        tvHomeSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab(R.id.nav_transactions);
                }
            }
        });

        cardHomeBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToTab(R.id.nav_budget);
                }
            }
        });
    }

    private void loadDashboardData() {
        if (getContext() == null) return;

        String currentMonth = DateUtils.getCurrentMonth();

        // 1. Balance & Totals
        currentBalance = dbHelper.getTotalBalance(currentUserId);
        updateBalanceDisplay();

        double monthlyIncome = dbHelper.getTotalIncome(currentUserId, currentMonth);
        double monthlyExpense = dbHelper.getTotalExpense(currentUserId, currentMonth);

        tvMonthlyIncome.setText("+ " + CurrencyManager.formatAmount(requireContext(), monthlyIncome));
        tvMonthlyExpense.setText("- " + CurrencyManager.formatAmount(requireContext(), monthlyExpense));

        // 2. Budget Overview
        Budget overallBudget = dbHelper.getOverallBudget(currentUserId, currentMonth);
        if (overallBudget != null && overallBudget.getMonthlyLimit() > 0) {
            double limit = overallBudget.getMonthlyLimit();
            double remaining = overallBudget.getRemainingAmount();
            int progress = overallBudget.getProgressPercentage();

            pbHomeBudget.setProgress(progress);
            tvHomeSpentVsBudget.setText("Spent: " + CurrencyManager.formatAmount(requireContext(), monthlyExpense)
                    + " / " + CurrencyManager.formatAmount(requireContext(), limit));
            tvHomeRemainingBudget.setText("Remaining: " + CurrencyManager.formatAmount(requireContext(), remaining));

            if (overallBudget.isExceeded()) {
                tvHomeBudgetStatusBadge.setText(R.string.budget_exceeded);
                tvHomeBudgetStatusBadge.setBackgroundResource(R.drawable.bg_expense_badge);
                tvHomeBudgetStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red_dark));
                pbHomeBudget.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.progress_custom_danger));
                tvHomeRemainingBudget.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red));
            } else if (overallBudget.isWarningReached()) {
                tvHomeBudgetStatusBadge.setText(R.string.budget_warning);
                tvHomeBudgetStatusBadge.setBackgroundResource(R.drawable.bg_warning_badge);
                tvHomeBudgetStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_amber_dark));
                pbHomeBudget.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.progress_custom_warning));
                tvHomeRemainingBudget.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning_amber));
            } else {
                tvHomeBudgetStatusBadge.setText(R.string.budget_safe);
                tvHomeBudgetStatusBadge.setBackgroundResource(R.drawable.bg_income_badge);
                tvHomeBudgetStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green_dark));
                pbHomeBudget.setProgressDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.progress_custom_green));
                tvHomeRemainingBudget.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green));
            }
        } else {
            pbHomeBudget.setProgress(0);
            tvHomeBudgetStatusBadge.setText("No Budget Set");
            tvHomeBudgetStatusBadge.setBackgroundResource(R.drawable.bg_chip_unselected);
            tvHomeBudgetStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvHomeSpentVsBudget.setText("Spent: " + CurrencyManager.formatAmount(requireContext(), monthlyExpense));
            tvHomeRemainingBudget.setText("Tap to set budget");
            tvHomeRemainingBudget.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        }

        // 3. Recent Transactions
        List<Transaction> recentList = dbHelper.getRecentTransactions(currentUserId, 5);
        if (recentList.isEmpty()) {
            rvRecentTransactions.setVisibility(View.GONE);
            llRecentEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvRecentTransactions.setVisibility(View.VISIBLE);
            llRecentEmptyState.setVisibility(View.GONE);
            transactionAdapter.updateData(recentList);
        }
    }

    private void updateBalanceDisplay() {
        if (isBalanceVisible) {
            tvTotalBalance.setText(CurrencyManager.formatAmount(requireContext(), currentBalance));
            ivToggleBalance.setImageResource(R.drawable.ic_visibility);
        } else {
            tvTotalBalance.setText("••••••••");
            ivToggleBalance.setImageResource(R.drawable.ic_visibility_off);
        }
    }
}
