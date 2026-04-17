package com.example.solojava;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class GymSetupActivity extends AppCompatActivity {

    EditText heightInput, weightInput, ageInput;
    RadioGroup levelGroup, planGroup;
    RadioButton levelBeginner, levelIntermediate, levelPro;
    RadioButton plan3day, plan5day;
    Button saveSetupBtn;
    TextView bmiText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_setup);

        heightInput     = findViewById(R.id.heightInput);
        weightInput     = findViewById(R.id.weightInput);
        ageInput        = findViewById(R.id.ageInput);
        levelGroup      = findViewById(R.id.levelGroup);
        planGroup       = findViewById(R.id.planGroup);
        levelBeginner   = findViewById(R.id.levelBeginner);
        levelIntermediate= findViewById(R.id.levelIntermediate);
        levelPro        = findViewById(R.id.levelPro);
        plan3day        = findViewById(R.id.plan3day);
        plan5day        = findViewById(R.id.plan5day);
        saveSetupBtn    = findViewById(R.id.saveSetupBtn);
        bmiText         = findViewById(R.id.bmiText);

        heightInput.setOnFocusChangeListener((v, f) -> { if (!f) updateBMI(); });
        weightInput.setOnFocusChangeListener((v, f) -> { if (!f) updateBMI(); });

        saveSetupBtn.setOnClickListener(v -> {
            AnimationHelper.buttonPress(saveSetupBtn);
            saveSetup();
        });

        AnimationHelper.staggerList((android.view.ViewGroup) findViewById(R.id.gymSetupContent));
    }

    private void updateBMI() {
        String hStr = heightInput.getText().toString().trim();
        String wStr = weightInput.getText().toString().trim();
        if (hStr.isEmpty() || wStr.isEmpty()) return;

        try {
            double h = Double.parseDouble(hStr) / 100.0;
            double w = Double.parseDouble(wStr);
            double bmi = w / (h * h);
            String category;
            int color;

            if (bmi < 18.5) { category = "Underweight"; color = 0xFF38BDF8; }
            else if (bmi < 25) { category = "Normal ✓"; color = 0xFF00FFAA; }
            else if (bmi < 30) { category = "Overweight"; color = 0xFFFF6D00; }
            else { category = "Obese"; color = 0xFFFF1744; }

            bmiText.setText(String.format(java.util.Locale.getDefault(),
                    "BMI: %.1f — %s", bmi, category));
            bmiText.setTextColor(color);
        } catch (NumberFormatException ignored) {}
    }

    private void saveSetup() {
        String hStr = heightInput.getText().toString().trim();
        String wStr = weightInput.getText().toString().trim();
        String aStr = ageInput.getText().toString().trim();

        if (hStr.isEmpty() || wStr.isEmpty() || aStr.isEmpty()) {
            AnimationHelper.shake(saveSetupBtn);
            Toast.makeText(this, "Fill all fields, Hunter.", Toast.LENGTH_SHORT).show();
            return;
        }

        int levelId = levelGroup.getCheckedRadioButtonId();
        String level = "Beginner";
        if (levelId == R.id.levelIntermediate) level = "Intermediate";
        else if (levelId == R.id.levelPro) level = "Pro";

        int planId = planGroup.getCheckedRadioButtonId();
        String plan = planId == R.id.plan5day ? "5-Day" : "3-Day";

        SharedPreferences prefs = getSharedPreferences("GymProfile", MODE_PRIVATE);
        prefs.edit()
                .putFloat("height", Float.parseFloat(hStr))
                .putFloat("weight", Float.parseFloat(wStr))
                .putInt("age", Integer.parseInt(aStr))
                .putString("level", level)
                .putString("plan", plan)
                .putBoolean("setupDone", true)
                .apply();

        startActivity(new Intent(this, GymActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}