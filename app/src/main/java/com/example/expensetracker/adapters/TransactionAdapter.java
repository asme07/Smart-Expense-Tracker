package com.example.expensetracker.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.R;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.utils.CurrencyManager;
import com.example.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying financial transactions.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private List<Transaction> transactionList;
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> list, OnTransactionClickListener listener) {
        this.context = context;
        this.transactionList = list != null ? list : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Transaction> newList) {
        this.transactionList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction item = transactionList.get(position);

        holder.tvCategoryName.setText(item.getCategory());
        String desc = item.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            holder.tvDescription.setText(item.getType());
        } else {
            holder.tvDescription.setText(desc);
        }

        holder.tvDate.setText(DateUtils.formatToDisplay(item.getDate()));

        boolean isIncome = item.isIncome();
        String formattedAmt = CurrencyManager.formatAmountSigned(context, item.getAmount(), isIncome);
        holder.tvAmount.setText(formattedAmt);

        if (isIncome) {
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green));
            holder.iconContainer.setBackgroundResource(R.drawable.bg_circle_income);
            holder.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.income_green)));
        } else {
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            holder.iconContainer.setBackgroundResource(R.drawable.bg_circle_expense);
            holder.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.expense_red)));
        }

        holder.ivCategoryIcon.setImageResource(item.getCategoryIconRes());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTransactionClick(item);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onTransactionLongClick(item);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconContainer;
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvDescription;
        TextView tvAmount;
        TextView tvDate;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
