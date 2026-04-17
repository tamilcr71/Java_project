package com.example.solojava;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExpenseActivity extends AppCompatActivity {

    EditText targetInput, dailyInput, noteInput;
    TextView lastMonthText, totalText, remainText, dayText, savingsText;
    Button changeTargetBtn, saveDayBtn;
    Spinner categorySpinner;
    GridLayout calendarGrid;
    BarChart weeklyChart;
    PieChart categoryChart;
    SharedPreferences prefs;
    DatabaseHelper db;
    int day, totalSpent;
    String[] categories = {"Food","Transport","Entertainment","Health","Shopping","Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        db = DatabaseHelper.getInstance(this);
        prefs = getSharedPreferences("ExpenseData", MODE_PRIVATE);
        day = prefs.getInt("day", 1);
        totalSpent = prefs.getInt("totalSpent", 0);

        initViews();
        setupCategorySpinner();
        loadData();
        setupClickListeners();
        AnimationHelper.staggerList((LinearLayout) findViewById(R.id.contentLayout));
    }

    private void initViews() {
        targetInput    = findViewById(R.id.targetInput);
        dailyInput     = findViewById(R.id.dailyInput);
        noteInput      = findViewById(R.id.noteInput);
        lastMonthText  = findViewById(R.id.lastMonthText);
        totalText      = findViewById(R.id.totalText);
        remainText     = findViewById(R.id.remainText);
        dayText        = findViewById(R.id.dayText);
        savingsText    = findViewById(R.id.savingsText);
        changeTargetBtn= findViewById(R.id.changeTargetBtn);
        saveDayBtn     = findViewById(R.id.saveDayBtn);
        categorySpinner= findViewById(R.id.categorySpinner);
        calendarGrid   = findViewById(R.id.calendarGrid);
        weeklyChart    = findViewById(R.id.weeklyChart);
        categoryChart  = findViewById(R.id.categoryChart);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void loadData() {
        lastMonthText.setText(prefs.getString("lastMonth", "Last Gate: —"));
        dayText.setText("Day " + day + " / 30");

        if (prefs.contains("target")) {
            targetInput.setText(String.valueOf(prefs.getInt("target", 0)));
            targetInput.setEnabled(false);
        }

        updateSummary();
        renderCalendar();
        renderWeeklyChart();
        renderCategoryChart();
    }

    private void setupClickListeners() {
        changeTargetBtn.setOnClickListener(v -> {
            AnimationHelper.buttonPress(changeTargetBtn);
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Reset Target?")
                    .setMessage("This will unlock the monthly target.")
                    .setPositiveButton("Unlock", (d, w) -> {
                        targetInput.setEnabled(true);
                        targetInput.setText("");
                        prefs.edit().remove("target").apply();
                    })
                    .setNegativeButton("Cancel", null).show();
        });

        saveDayBtn.setOnClickListener(v -> {
            AnimationHelper.buttonPress(saveDayBtn);
            saveDay();
        });
    }

    private void saveDay() {
        String t = targetInput.getText().toString().trim();
        String d = dailyInput.getText().toString().trim();
        String note = noteInput.getText().toString().trim();

        if (t.isEmpty() || d.isEmpty()) {
            AnimationHelper.shake(dailyInput);
            Toast.makeText(this, "Fill all fields, Hunter.", Toast.LENGTH_SHORT).show();
            return;
        }

        int target = Integer.parseInt(t);
        int daily  = Integer.parseInt(d);
        String category = categories[categorySpinner.getSelectedItemPosition()];
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        SharedPreferences.Editor editor = prefs.edit();
        if (!prefs.contains("target")) editor.putInt("target", target);

        totalSpent += daily;
        editor.putInt("day_" + day, daily);
        editor.putInt("day_cat_" + day, categorySpinner.getSelectedItemPosition());
        editor.putInt("lastExpense", daily);

        db.insertExpense(date, daily, category, note);
        XPManager.addXP(this, XPManager.XP_EXPENSE_LOG, "Expense logged (Day " + day + ")");
        db.insertXPHistory(date, XPManager.XP_EXPENSE_LOG, "Expense Gate");

        showXPFloat(XPManager.XP_EXPENSE_LOG);
        day++;

        if (day > 30) {
            endMonth(editor, target);
        } else {
            editor.putInt("day", day);
            editor.putInt("totalSpent", totalSpent);
            editor.apply();
        }

        dailyInput.setText("");
        noteInput.setText("");
        dayText.setText("Day " + day + " / 30");
        updateSummary();
        renderCalendar();
        renderWeeklyChart();
        renderCategoryChart();
    }

    private void endMonth(SharedPreferences.Editor editor, int savedTarget) {
        double diff = ((double)(totalSpent - savedTarget) / savedTarget) * 100;
        int xp;
        String result;
        String emoji;

        if (totalSpent <= savedTarget) {
            xp = XPManager.XP_EXPENSE_BUDGET;
            result = "Under budget! Hunter efficiency: MAXIMUM";
            emoji = "✅";
        } else if (diff <= 10) {
            xp = XPManager.XP_EXPENSE_OVER / 2;
            result = "Slightly over budget. Stay disciplined.";
            emoji = "⚠️";
        } else {
            xp = XPManager.XP_EXPENSE_OVER;
            result = "Way over budget. The System is disappointed.";
            emoji = "❌";
        }

        XPManager.addXP(this, xp, "Month Complete");

        new AlertDialog.Builder(this)
                .setTitle("📊 Gate Cleared!")
                .setMessage(emoji + " Month Summary\n\n" +
                        "Total Spent: ₹" + totalSpent + "\n" +
                        "Target: ₹" + savedTarget + "\n\n" +
                        result + "\n\nXP: " + (xp >= 0 ? "+" : "") + xp)
                .setPositiveButton("ACKNOWLEDGED", null)
                .show();

        editor.clear();
        editor.putString("lastMonth", "Last Gate: ₹" + totalSpent + " / ₹" + savedTarget);
        day = 1;
        totalSpent = 0;
        editor.putInt("day", day);
        editor.putInt("totalSpent", totalSpent);
        editor.apply();
    }

    private void updateSummary() {
        int target = prefs.getInt("target", 0);
        int remain = target - totalSpent;
        double savingsRate = target > 0
                ? ((double)(target - totalSpent) / target) * 100 : 0;

        totalText.setText("Spent: ₹" + totalSpent);
        remainText.setText(remain >= 0 ? "Remaining: ₹" + remain : "Over: ₹" + Math.abs(remain));
        remainText.setTextColor(remain >= 0 ? 0xFF00FFAA : 0xFFFF1744);

        if (savingsText != null) {
            savingsText.setText(String.format(Locale.getDefault(),
                    "Savings rate: %.1f%%", Math.max(savingsRate, 0)));
            savingsText.setTextColor(savingsRate >= 0 ? 0xFF00FFAA : 0xFFFF1744);
        }
    }

    private void renderCalendar() {
        calendarGrid.removeAllViews();
        for (int i = 1; i <= 30; i++) {
            int value = prefs.getInt("day_" + i, 0);
            boolean isToday = (i == day);

            TextView cell = new TextView(this);
            cell.setText("D" + i + "\n" + (value > 0 ? "₹" + value : "—"));
            cell.setTextSize(9f);
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(4, 8, 4, 8);

            if (isToday) {
                cell.setTextColor(0xFFFFD700);
                cell.setBackgroundResource(R.drawable.card_gold);
            } else if (value > 0) {
                cell.setTextColor(0xFF00FFAA);
                cell.setBackgroundResource(R.drawable.card_green);
            } else {
                cell.setTextColor(0xFF334455);
                cell.setBackgroundResource(R.drawable.card_dark);
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(3, 3, 3, 3);
            cell.setLayoutParams(params);
            calendarGrid.addView(cell);
        }
    }

    private void renderWeeklyChart() {
        if (weeklyChart == null) return;
        List<BarEntry> entries = new ArrayList<>();
        String[] days7 = {"D"+Math.max(1,day-6),"D"+Math.max(1,day-5),
                "D"+Math.max(1,day-4),"D"+Math.max(1,day-3),
                "D"+Math.max(1,day-2),"D"+Math.max(1,day-1),"Today"};

        for (int i = 0; i < 7; i++) {
            int d = Math.max(1, day - (6 - i));
            entries.add(new BarEntry(i, prefs.getInt("day_" + d, 0)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Spend");
        dataSet.setColor(Color.parseColor("#FF1744"));
        dataSet.setValueTextColor(Color.parseColor("#F1F5F9"));
        dataSet.setValueTextSize(9f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        weeklyChart.setData(data);
        weeklyChart.setBackgroundColor(Color.parseColor("#0A0012"));
        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.getLegend().setEnabled(false);
        weeklyChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days7));
        weeklyChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        weeklyChart.getXAxis().setTextColor(Color.parseColor("#94A3B8"));
        weeklyChart.getAxisLeft().setTextColor(Color.parseColor("#94A3B8"));
        weeklyChart.getAxisRight().setEnabled(false);
        weeklyChart.animateY(800);
        weeklyChart.invalidate();
    }

    private void renderCategoryChart() {
        if (categoryChart == null) return;
        List<PieEntry> entries = new ArrayList<>();
        int[] colors = {
                Color.parseColor("#FF6D00"),
                Color.parseColor("#38BDF8"),
                Color.parseColor("#A855F7"),
                Color.parseColor("#00FFAA"),
                Color.parseColor("#FF0080"),
                Color.parseColor("#94A3B8")
        };

        for (int i = 0; i < categories.length; i++) {
            float total = 0;
            for (int d = 1; d < day; d++) {
                if (prefs.getInt("day_cat_" + d, -1) == i) {
                    total += prefs.getInt("day_" + d, 0);
                }
            }
            if (total > 0) entries.add(new PieEntry(total, categories[i]));
        }

        if (entries.isEmpty()) {
            categoryChart.setVisibility(View.GONE);
            return;
        }

        categoryChart.setVisibility(View.VISIBLE);
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        categoryChart.setData(data);
        categoryChart.setBackgroundColor(Color.parseColor("#0A0012"));
        categoryChart.getDescription().setEnabled(false);
        categoryChart.getLegend().setTextColor(Color.parseColor("#94A3B8"));
        categoryChart.setHoleColor(Color.parseColor("#05000A"));
        categoryChart.setHoleRadius(38f);
        categoryChart.setCenterText("Spending\nBreakdown");
        categoryChart.setCenterTextColor(Color.parseColor("#94A3B8"));
        categoryChart.setCenterTextSize(11f);
        categoryChart.animateY(1000);
        categoryChart.invalidate();
    }

    private void showXPFloat(int xp) {
        View root = findViewById(android.R.id.content);
        if (root instanceof ViewGroup) {
            AnimationHelper.showXPFloat((ViewGroup) root, xp, 200, 400);
        }
    }
}