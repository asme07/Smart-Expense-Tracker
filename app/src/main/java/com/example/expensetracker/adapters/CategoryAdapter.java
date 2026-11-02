package com.example.expensetracker.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.example.expensetracker.models.CategoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for category selection items in grid/list format.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private List<CategoryItem> categoryList;
    private String selectedCategory;
    private OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(CategoryItem item);
    }

    public CategoryAdapter(Context context, List<CategoryItem> list, String selectedCategory, OnCategorySelectedListener listener) {
        this.context = context;
        this.categoryList = list != null ? list : new ArrayList<>();
        this.selectedCategory = selectedCategory;
        this.listener = listener;
    }

    public void updateList(List<CategoryItem> list, String selected) {
        this.categoryList = list;
        this.selectedCategory = selected;
        notifyDataSetChanged();
    }

    public void setSelectedCategory(String selected) {
        this.selectedCategory = selected;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem item = categoryList.get(position);
        holder.tvCatName.setText(item.getName());
        holder.ivCatIcon.setImageResource(item.getIconRes());

        boolean isSelected = item.getName().equalsIgnoreCase(selectedCategory);

        if (isSelected) {
            holder.flCategoryBg.setBackgroundResource(R.drawable.bg_chip_selected);
            holder.ivCatIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.on_primary)));
            holder.tvCatName.setTextColor(ContextCompat.getColor(context, R.color.secondary));
        } else {
            holder.flCategoryBg.setBackgroundResource(R.drawable.bg_circle_neutral);
            holder.ivCatIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, item.getColorRes())));
            holder.tvCatName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCategory = item.getName();
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onCategorySelected(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flCategoryBg;
        ImageView ivCatIcon;
        TextView tvCatName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            flCategoryBg = itemView.findViewById(R.id.flCategoryBg);
            ivCatIcon = itemView.findViewById(R.id.ivCatIcon);
            tvCatName = itemView.findViewById(R.id.tvCatName);
        }
    }
}
