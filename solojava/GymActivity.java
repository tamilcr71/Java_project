package com.example.solojava;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.*;

public class GymActivity extends AppCompatActivity {

    TextView gymTitle, streakText, bmiText, planText, suggestionText, gymXpText;
    LinearLayout exerciseContainer;
    Button saveGymBtn;
    SharedPreferences gymPrefs, profilePrefs;
    DatabaseHelper db;

    String[] beginner3Day = {
            "Push-ups|3 sets × 15 reps",
            "Squats|3 sets × 20 reps",
            "Plank|3 sets × 30 sec",
            "Jumping Jacks|3 sets × 30 reps",
            "Dumbbell Curl|3 sets × 12 reps",
            "Lunges|3 sets × 12 reps each"
    };

    String[] intermediate5Day = {
            "Bench Press|4 sets × 10 reps",
            "Pull-ups|4 sets × 8 reps",
            "Deadlift|4 sets × 6 reps",
            "Overhead Press|3 sets × 10 reps",
            "Barbell Row|4 sets × 8 reps",
            "Leg Press|4 sets × 12 reps",
            "Cable Fly|3 sets × 12 reps",
            "Face Pulls|3 sets × 15 reps"
    };

    String[] pro5Day = {
            "Heavy Squat|5 sets × 5 reps",
            "Weighted Pull-ups|5 sets × 6 reps",
            "Romanian Deadlift|4 sets × 8 reps",
            "Incline Bench Press|4 sets × 8 reps",
            "Barbell Row|4 sets × 6 reps",
            "Overhead Press|4 sets × 6 reps",
            "Dips|4 sets × 10 reps",
            "Ab Wheel Rollout|3 sets × 10 reps"
    };

    List<CheckBox> exerciseCheckboxes = new ArrayList<>();
    List<String> exerciseNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym);

        db = DatabaseHelper.getInstance(this);
        gymPrefs     = getSharedPreferences("GymData", MODE_PRIVATE);
        profilePrefs = getSharedPreferences("GymProfile", MODE_PRIVATE);

        gymTitle        = findViewById(R.id.gymTitle);
        streakText      = findViewById(R.id.gymStreakText);
        bmiText         = findViewById(R.id.gymBmiText);
        planText        = findViewById(R.id.gymPlanText);
        suggestionText  = findViewById(R.id.suggestionText);
        gymXpText       = findViewById(R.id.gymXpText);
        exerciseContainer = findViewById(R.id.exerciseContainer);
        saveGymBtn      = findViewById(R.id.saveGymBtn);

        loadProfile();
        buildExerciseList();
        updateStreak();
        AnimationHelper.staggerList(exerciseContainer);

        saveGymBtn.setOnClickListener(v -> {
            AnimationHelper.buttonPress(saveGymBtn);
            saveGym();
        });
    }

    private void loadProfile() {
        float height = profilePrefs.getFloat("height", 170);
        float weight = profilePrefs.getFloat("weight", 70);
        String level = profilePrefs.getString("level", "Beginner");
        String plan  = profilePrefs.getString("plan", "3-Day");

        double bmi = weight / Math.pow(height / 100.0, 2);
        String bmiCat = bmi < 18.5 ? "Underweight" : bmi < 25 ? "Normal" : bmi < 30 ? "Overweight" : "Obese";

        bmiText.setText(String.format(Locale.getDefault(), "BMI: %.1f (%s)", bmi, bmiCat));
        planText.setText("Plan: " + plan + "  |  Level: " + level);
        buildSuggestion(level, bmi);
    }

    private void buildSuggestion(String level, double bmi) {
        StringBuilder sb = new StringBuilder("💡 System Suggestion:\n");
        if (bmi < 18.5) {
            sb.append("Focus on compound lifts and caloric surplus.\nEat more protein. Rest well.");
        } else if (bmi < 25) {
            if (level.equals("Beginner"))
                sb.append("Perfect baseline! Follow the 3-day plan strictly.\nConsistency beats intensity.");
            else if (level.equals("Intermediate"))
                sb.append("Increase progressive overload weekly.\nTrack your lifts in sets + reps.");
            else
                sb.append("Prioritize recovery. Add deload weeks.\nMaximize intensity on heavy lifts.");
        } else {
            sb.append("Mix cardio + strength training.\nMaintain caloric deficit. Stay disciplined.");
        }
        suggestionText.setText(sb.toString());
    }

    private void buildExerciseList() {
        String level = profilePrefs.getString("level", "Beginner");
        String plan  = profilePrefs.getString("plan", "3-Day");

        String[] exercises = level.equals("Pro") ? pro5Day
                : level.equals("Intermediate") ? intermediate5Day
                : beginner3Day;

        exerciseContainer.removeAllViews();
        exerciseCheckboxes.clear();
        exerciseNames.clear();

        for (String exStr : exercises) {
            String[] parts = exStr.split("\\|");
            String exName = parts[0];
            String exDetail = parts.length > 1 ? parts[1] : "";
            exerciseNames.add(exName);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(16, 16, 16, 16);
            row.setBackgroundResource(R.drawable.card_dark);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 8);
            row.setLayoutParams(lp);

            CheckBox cb = new CheckBox(this);
            cb.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFFFF1744));
            exerciseCheckboxes.add(cb);

            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams tcp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            textCol.setLayoutParams(tcp);
            textCol.setPadding(12, 0, 0, 0);

            TextView nameView = new TextView(this);
            nameView.setText(exName);
            nameView.setTextColor(0xFFF1F5F9);
            nameView.setTextSize(14f);
            nameView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

            TextView detailView = new TextView(this);
            detailView.setText(exDetail);
            detailView.setTextColor(0xFF475569);
            detailView.setTextSize(11f);

            textCol.addView(nameView);
            textCol.addView(detailView);

            TextView xpTag = new TextView(this);
            xpTag.setText("+20 XP");
            xpTag.setTextColor(0xFFFF1744);
            xpTag.setTextSize(10f);

            row.addView(cb);
            row.addView(textCol);
            row.addView(xpTag);
            exerciseContainer.addView(row);
        }
    }

    private void saveGym() {
        int count = 0;
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (int i = 0; i < exerciseCheckboxes.size(); i++) {
            if (exerciseCheckboxes.get(i).isChecked()) {
                count++;
                db.insertGymLog(date, exerciseNames.get(i), 3, 12, 0);
            }
        }

        if (count == 0) {
            AnimationHelper.shake(saveGymBtn);
            Toast.makeText(this, "Select at least one exercise, Hunter!", Toast.LENGTH_SHORT).show();
            return;
        }

        int xp = count * XPManager.XP_GYM_PER_EXERCISE;
        if (count == exerciseCheckboxes.size()) xp += XPManager.XP_GYM_BONUS;

        XPManager.addXP(this, xp, "Training (" + count + " exercises)");
        db.insertXPHistory(date, xp, "Training Ground");

        gymXpText.setText("⚡ +" + xp + " XP earned!");
        gymXpText.setTextColor(0xFF00FFAA);

        int day = gymPrefs.getInt("day", 1);
        gymPrefs.edit()
                .putInt("day_" + day, count)
                .putInt("day", day >= 30 ? 1 : day + 1)
                .apply();

        updateStreak();
        showXPFloat(xp);
        Toast.makeText(this, "⚔️ Training complete! +" + xp + " XP", Toast.LENGTH_SHORT).show();

        for (CheckBox cb : exerciseCheckboxes) cb.setChecked(false);
    }

    private void updateStreak() {
        int streak = 0;
        int day = gymPrefs.getInt("day", 1);
        for (int i = day - 1; i >= 1; i--) {
            if (gymPrefs.getInt("day_" + i, 0) > 0) streak++;
            else break;
        }
        gymPrefs.edit().putInt("streak", streak).apply();
        if (streakText != null) streakText.setText("🔥 Training Streak: " + streak + " days");
    }

    private void showXPFloat(int xp) {
        View root = findViewById(android.R.id.content);
        if (root instanceof ViewGroup)
            AnimationHelper.showXPFloat((ViewGroup) root, xp, 200, 400);
    }
}