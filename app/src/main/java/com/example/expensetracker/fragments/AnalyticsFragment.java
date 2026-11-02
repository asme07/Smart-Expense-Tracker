package com.example.expensetracker.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.expensetracker.R;
import com.example.expensetracker.database.DatabaseHelper;
import com.example.expensetracker.models.Transaction;
import com.example.expensetracker.utils.CurrencyManager;
import com.example.expensetracker.utils.DateUtils;
import com.example.expensetracker.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Analytics and Reporting fragment powered by MPAndroidChart.
 */
public class AnalyticsFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshAnalytics;
    private Spinner spinnerAnalyticsMonth;
    private TextView tvAnalyticsIncome, tvAnalyticsExpense, tvAnalyticsSavings, tvSavingsRateLabel, tvAnalyticsTopCategory;
    private PieChart pieChartCategoryExpenses;
    private BarChart barChartIncomeVsExpense;
    private LineChart lineChartSpendingTrend;

    private DatabaseHelper dbHelper;
    private String currentUserId;
    private List<String> monthKeyList;
    private String selectedMonthKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        currentUserId = SessionManager.getInstance(requireContext()).getUserId();

        initViews(view);
        setupMonthSpinner();
        setupChartAppearances();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalyticsData();
    }

    private void initViews(View view) {
        swipeRefreshAnalytics = view.findViewById(R.id.swipeRefreshAnalytics);
        spinnerAnalyticsMonth = view.findViewById(R.id.spinnerAnalyticsMonth);
        tvAnalyticsIncome = view.findViewById(R.id.tvAnalyticsIncome);
        tvAnalyticsExpense = view.findViewById(R.id.tvAnalyticsExpense);
        tvAnalyticsSavings = view.findViewById(R.id.tvAnalyticsSavings);
        tvSavingsRateLabel = view.findViewById(R.id.tvSavingsRateLabel);
        tvAnalyticsTopCategory = view.findViewById(R.id.tvAnalyticsTopCategory);

        pieChartCategoryExpenses = view.findViewById(R.id.pieChartCategoryExpenses);
        barChartIncomeVsExpense = view.findViewById(R.id.barChartIncomeVsExpense);
        lineChartSpendingTrend = view.findViewById(R.id.lineChartSpendingTrend);

        swipeRefreshAnalytics.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAnalyticsData();
                swipeRefreshAnalytics.setRefreshing(false);
            }
        });
    }

    private void setupMonthSpinner() {
        monthKeyList = new ArrayList<>();
        List<String> displayTitles = new ArrayList<>();

        // Generate past 6 months
        for (int i = 0; i < 6; i++) {
            String mKey = DateUtils.getPastMonth(i);
            monthKeyList.add(mKey);
            displayTitles.add(DateUtils.formatToMonthTitle(mKey));
        }

        selectedMonthKey = monthKeyList.get(0);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, displayTitles);
        spinnerAnalyticsMonth.setAdapter(adapter);

        spinnerAnalyticsMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonthKey = monthKeyList.get(position);
                loadAnalyticsData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupChartAppearances() {
        // Pie Chart
        pieChartCategoryExpenses.getDescription().setEnabled(false);
        pieChartCategoryExpenses.setDrawHoleEnabled(true);
        pieChartCategoryExpenses.setHoleColor(Color.TRANSPARENT);
        pieChartCategoryExpenses.setHoleRadius(54f);
        pieChartCategoryExpenses.setTransparentCircleRadius(58f);
        pieChartCategoryExpenses.setEntryLabelColor(Color.WHITE);
        pieChartCategoryExpenses.setEntryLabelTextSize(11f);
        pieChartCategoryExpenses.getLegend().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        pieChartCategoryExpenses.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        // Bar Chart
        barChartIncomeVsExpense.getDescription().setEnabled(false);
        barChartIncomeVsExpense.setDrawGridBackground(false);
        barChartIncomeVsExpense.getAxisRight().setEnabled(false);
        barChartIncomeVsExpense.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartIncomeVsExpense.getXAxis().setDrawGridLines(false);
        barChartIncomeVsExpense.getXAxis().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        barChartIncomeVsExpense.getAxisLeft().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        barChartIncomeVsExpense.getAxisLeft().setDrawGridLines(true);
        barChartIncomeVsExpense.getAxisLeft().setGridColor(Color.parseColor("#E2E8F0"));
        barChartIncomeVsExpense.getLegend().setEnabled(false);

        // Line Chart
        lineChartSpendingTrend.getDescription().setEnabled(false);
        lineChartSpendingTrend.setDrawGridBackground(false);
        lineChartSpendingTrend.getAxisRight().setEnabled(false);
        lineChartSpendingTrend.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartSpendingTrend.getXAxis().setDrawGridLines(false);
        lineChartSpendingTrend.getXAxis().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        lineChartSpendingTrend.getAxisLeft().setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        lineChartSpendingTrend.getAxisLeft().setGridColor(Color.parseColor("#E2E8F0"));
        lineChartSpendingTrend.getLegend().setEnabled(false);
    }

    private void loadAnalyticsData() {
        if (getContext() == null || selectedMonthKey == null) return;

        // 1. KPI Calculations
        double income = dbHelper.getTotalIncome(currentUserId, selectedMonthKey);
        double expense = dbHelper.getTotalExpense(currentUserId, selectedMonthKey);
        double savings = income - expense;

        tvAnalyticsIncome.setText(CurrencyManager.formatAmount(requireContext(), income));
        tvAnalyticsExpense.setText(CurrencyManager.formatAmount(requireContext(), expense));
        tvAnalyticsSavings.setText(CurrencyManager.formatAmount(requireContext(), savings));

        int savingsRate = income > 0 ? (int) Math.round((savings / income) * 100) : 0;
        tvSavingsRateLabel.setText("Savings (" + savingsRate + "%)");

        // Top Category
        Map<String, Double> categoryMap = dbHelper.getCategoryExpenses(currentUserId, selectedMonthKey);
        if (!categoryMap.isEmpty()) {
            Map.Entry<String, Double> top = categoryMap.entrySet().iterator().next();
            tvAnalyticsTopCategory.setText(top.getKey() + " (" + CurrencyManager.formatAmount(requireContext(), top.getValue()) + ")");
        } else {
            tvAnalyticsTopCategory.setText("None");
        }

        // 2. Populate Charts
        populatePieChart(categoryMap, expense);
        populateBarChart(income, expense);
        populateLineChart();
    }

    private void populatePieChart(Map<String, Double> categoryMap, double totalExpense) {
        if (categoryMap.isEmpty() || totalExpense <= 0) {
            pieChartCategoryExpenses.clear();
            pieChartCategoryExpenses.setNoDataText("No expense data for this period.");
            pieChartCategoryExpenses.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int[] palette = new int[]{
                ContextCompat.getColor(requireContext(), R.color.cat_food),
                ContextCompat.getColor(requireContext(), R.color.cat_shopping),
                ContextCompat.getColor(requireContext(), R.color.cat_transport),
                ContextCompat.getColor(requireContext(), R.color.cat_bills),
                ContextCompat.getColor(requireContext(), R.color.cat_entertainment),
                ContextCompat.getColor(requireContext(), R.color.cat_education),
                ContextCompat.getColor(requireContext(), R.color.cat_healthcare),
                ContextCompat.getColor(requireContext(), R.color.cat_travel),
                ContextCompat.getColor(requireContext(), R.color.cat_other)
        };

        int colorIdx = 0;
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            float pct = (float) ((entry.getValue() / totalExpense) * 100);
            entries.add(new PieEntry(pct, entry.getKey()));
            colors.add(palette[colorIdx % palette.length]);
            colorIdx++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartCategoryExpenses));
        pieChartCategoryExpenses.setData(data);
        pieChartCategoryExpenses.setCenterText("Expenses\n" + CurrencyManager.formatAmount(requireContext(), totalExpense));
        pieChartCategoryExpenses.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        pieChartCategoryExpenses.setCenterTextSize(13f);
        pieChartCategoryExpenses.animateY(800);
        pieChartCategoryExpenses.invalidate();
    }

    private void populateBarChart(double income, double expense) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) income));
        entries.add(new BarEntry(1f, (float) expense));

        BarDataSet dataSet = new BarDataSet(entries, "Income vs Expense");
        dataSet.setColors(new int[]{
                ContextCompat.getColor(requireContext(), R.color.income_green),
                ContextCompat.getColor(requireContext(), R.color.expense_red)
        });
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChartIncomeVsExpense.setData(data);
        barChartIncomeVsExpense.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"Income", "Expense"}));
        barChartIncomeVsExpense.getXAxis().setGranularity(1f);
        barChartIncomeVsExpense.animateY(800);
        barChartIncomeVsExpense.invalidate();
    }

    private void populateLineChart() {
        Map<String, Double> dailyExpenses = dbHelper.getDailyExpenses(currentUserId, selectedMonthKey);

        if (dailyExpenses.isEmpty()) {
            lineChartSpendingTrend.clear();
            lineChartSpendingTrend.setNoDataText("No spending trend data for this period.");
            lineChartSpendingTrend.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        int index = 0;
        double cumulative = 0.0;
        for (Map.Entry<String, Double> day : dailyExpenses.entrySet()) {
            cumulative += day.getValue();
            entries.add(new Entry(index, (float) cumulative));
            // Show day e.g. "05" or "12"
            String label = day.getKey().length() >= 10 ? day.getKey().substring(8) : String.valueOf(index + 1);
            xLabels.add(label);
            index++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cumulative Spending");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.secondary_light));

        LineData data = new LineData(dataSet);
        lineChartSpendingTrend.setData(data);
        lineChartSpendingTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLabels));
        lineChartSpendingTrend.getXAxis().setGranularity(1f);
        lineChartSpendingTrend.animateX(800);
        lineChartSpendingTrend.invalidate();
    }
}
