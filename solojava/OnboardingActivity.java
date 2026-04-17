package com.example.solojava;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    EditText nameInput;
    RadioGroup avatarGroup;
    RadioButton avatar1, avatar2, avatar3, avatar4;
    Button beginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        nameInput   = findViewById(R.id.nameInput);
        avatarGroup = findViewById(R.id.avatarGroup);
        avatar1     = findViewById(R.id.avatar1);
        avatar2     = findViewById(R.id.avatar2);
        avatar3     = findViewById(R.id.avatar3);
        avatar4     = findViewById(R.id.avatar4);
        beginBtn    = findViewById(R.id.beginBtn);

        AnimationHelper.slideUp(nameInput, 200);
        AnimationHelper.slideUp(avatarGroup, 400);
        AnimationHelper.slideUp(beginBtn, 600);

        beginBtn.setOnClickListener(v -> {
            AnimationHelper.buttonPress(beginBtn);
            saveProfile();
        });
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            AnimationHelper.shake(nameInput);
            Toast.makeText(this, "Enter your hunter name!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = avatarGroup.getCheckedRadioButtonId();
        String avatar = "⚔️";
        if (selectedId == R.id.avatar2) avatar = "🗡️";
        else if (selectedId == R.id.avatar3) avatar = "🛡️";
        else if (selectedId == R.id.avatar4) avatar = "👑";

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        prefs.edit()
                .putString("hunterName", name)
                .putString("avatar", avatar)
                .putBoolean("setupDone", true)
                .putLong("joinDate", System.currentTimeMillis())
                .apply();

        NotificationHelper.scheduleDailyNotification(
                this, 20, 0,
                "⚡ System Alert",
                "Your daily quests await, Hunter!",
                1001
        );

        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}