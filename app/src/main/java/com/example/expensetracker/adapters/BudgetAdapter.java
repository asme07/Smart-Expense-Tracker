package com.example.expensetracker.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.models.Budget;
import com.example.expensetracker.utils.CurrencyManager;
import com.example.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying budget progress and status.
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final Context context;
    private List<Budget> budgetList;
    private OnBudgetClickListener listener;

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
        void onBudgetLongClick(Budget budget);
    }

    public BudgetAdapter(Context context, List<Budget> list, OnBudgetClickListener listener) {
        this.context = context;
        this.budgetList = list != null ? list : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Budget> newList) {
        this.budgetList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget item = budgetList.get(position);

        holder.tvBudgetCategory.setText(item.getCategory());
        holder.tvBudgetMonth.setText(DateUtils.formatToMonthTitle(item.getMonth()));

        holder.tvLimitAmount.setText(CurrencyManager.formatAmount(context, item.getMonthlyLimit()));
        holder.tvSpentAmount.setText(CurrencyManager.formatAmount(context, item.getSpentAmount()));
        holder.tvRemainingAmount.setText(CurrencyManager.formatAmount(context, item.getRemainingAmount()));

        int progress = item.getProgressPercentage();
        holder.pbBudgetProgress.setProgress(progress);

        holder.ivBudgetIcon.setImageResource(item.getCategoryIconRes());

        // Status badge and progress bar styling
        if (item.isExceeded()) {
            holder.tvStatusBadge.setText(R.string.budget_exceeded);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_expense_badge);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.expense_red_dark));
            holder.pbBudgetProgress.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_custom_danger));
            holder.tvRemainingAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
        } else if (item.isWarningReached()) {
            holder.tvStatusBadge.setText(R.string.budget_warning);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_warning_badge);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning_amber_dark));
            holder.pbBudgetProgress.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_custom_warning));
            holder.tvRemainingAmount.setTextColor(ContextCompat.getColor(context, R.color.warning_amber));
        } else {
            holder.tvStatusBadge.setText(R.string.budget_safe);
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_income_badge);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.income_green_dark));
            holder.pbBudgetProgress.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_custom_green));
            holder.tvRemainingAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onBudgetClick(item);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onBudgetLongClick(item);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        FrameLayout budgetIconContainer;
        ImageView ivBudgetIcon;
        TextView tvBudgetCategory;
        TextView tvBudgetMonth;
        TextView tvStatusBadge;
        ProgressBar pbBudgetProgress;
        TextView tvSpentAmount;
        TextView tvRemainingAmount;
        TextView tvLimitAmount;

        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            budgetIconContainer = itemView.findViewById(R.id.budgetIconContainer);
            ivBudgetIcon = itemView.findViewById(R.id.ivBudgetIcon);
            tvBudgetCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvBudgetMonth = itemView.findViewById(R.id.tvBudgetMonth);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            pbBudgetProgress = itemView.findViewById(R.id.pbBudgetProgress);
            tvSpentAmount = itemView.findViewById(R.id.tvSpentAmount);
            tvRemainingAmount = itemView.findViewById(R.id.tvRemainingAmount);
            tvLimitAmount = itemView.findViewById(R.id.tvLimitAmount);
        }
    }
}
