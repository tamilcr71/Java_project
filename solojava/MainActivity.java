package com.example.solojava;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    View cardExpense, cardSleep, cardGym, cardChecklist, cardBoss, cardProfile;
    TextView navRank, navHome;
    TextView playerName, playerRankText, xpText, greetText, avatarText;
    TextView streakText, totalDaysText;
    ProgressBar xpBar;
    View rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        updateHunterCard();
        checkSystemPopup();
        animateEntrance();

        setupClicks();
    }

    private void initViews() {
        rootLayout      = findViewById(R.id.rootLayout);
        cardExpense     = findViewById(R.id.cardExpense);
        cardSleep       = findViewById(R.id.cardSleep);
        cardGym         = findViewById(R.id.cardGym);
        cardChecklist   = findViewById(R.id.cardChecklist);
        cardBoss        = findViewById(R.id.cardBoss);
        cardProfile     = findViewById(R.id.cardProfile);
        navRank         = findViewById(R.id.navRank);
        navHome         = findViewById(R.id.navHome);
        playerName      = findViewById(R.id.playerName);
        playerRankText  = findViewById(R.id.playerRankText);
        xpText          = findViewById(R.id.xpText);
        greetText       = findViewById(R.id.greetText);
        avatarText      = findViewById(R.id.avatarText);
        streakText      = findViewById(R.id.streakText);
        totalDaysText   = findViewById(R.id.totalDaysText);
        xpBar           = findViewById(R.id.xpBar);
    }

    private void updateHunterCard() {
        int xp = XPManager.getXP(this);
        int color = XPManager.getRankColor(xp);
        int progress = XPManager.getRankProgress(xp);
        int next = XPManager.getXPForNextRank(xp);

        SharedPreferences profile = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String name   = profile.getString("hunterName", "Hunter");
        String avatar = profile.getString("avatar", "⚔️");

        SharedPreferences mainPrefs = getSharedPreferences("MainData", MODE_PRIVATE);
        int streak    = mainPrefs.getInt("streak", 0);
        int totalDays = XPManager.getTotalDaysActive(this);

        avatarText.setText(avatar);
        playerName.setText(name.toUpperCase());
        playerRankText.setText(XPManager.getRankTitle(xp));
        playerRankText.setTextColor(color);
        xpText.setText("⚡ " + xp + " XP   →   Next: " + next + " XP");
        greetText.setText("Welcome back, " + name + ".");

        if (streakText != null)
            streakText.setText("🔥 " + streak + " day streak");
        if (totalDaysText != null)
            totalDaysText.setText("📅 " + totalDays + " days active");

        xpBar.setMax(100);
        ObjectAnimator.ofInt(xpBar, "progress", 0, progress)
                .setDuration(1200).start();
    }

    private void animateEntrance() {
        View hunterCard = findViewById(R.id.hunterCard);
        View gatesLabel = findViewById(R.id.gatesLabel);
        View row1 = findViewById(R.id.row1);
        View row2 = findViewById(R.id.row2);
        View row3 = findViewById(R.id.row3);

        if (hunterCard != null) AnimationHelper.slideUp(hunterCard, 100);
        if (gatesLabel != null) AnimationHelper.slideUp(gatesLabel, 250);
        if (row1 != null) AnimationHelper.slideUp(row1, 350);
        if (row2 != null) AnimationHelper.slideUp(row2, 450);
        if (row3 != null) AnimationHelper.slideUp(row3, 550);
    }

    private void setupClicks() {
        cardExpense.setOnClickListener(v -> {
            AnimationHelper.buttonPress(cardExpense);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, ExpenseActivity.class)), 120);
        });

        cardSleep.setOnClickListener(v -> {
            AnimationHelper.buttonPress(cardSleep);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, SleepActivity.class)), 120);
        });

        cardGym.setOnClickListener(v -> {
            AnimationHelper.buttonPress(cardGym);
            SharedPreferences gym = getSharedPreferences("GymProfile", MODE_PRIVATE);
            boolean gymSetup = gym.getBoolean("setupDone", false);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, gymSetup
                            ? GymActivity.class
                            : GymSetupActivity.class)), 120);
        });

        cardChecklist.setOnClickListener(v -> {
            AnimationHelper.buttonPress(cardChecklist);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, ChecklistActivity.class)), 120);
        });

        cardBoss.setOnClickListener(v -> {
            AnimationHelper.buttonPress(cardBoss);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, BossBattleActivity.class)), 120);
        });

        cardProfile.setOnClickListener(v -> {
            AnimationHelper.buttonPress(cardProfile);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, ProfileActivity.class)), 120);
        });

        navRank.setOnClickListener(v ->
                startActivity(new Intent(this, RankActivity.class)));
    }

    private void checkSystemPopup() {
        int lastXP = XPManager.getLastXP(this);
        if (lastXP == 0) return;

        int xp = XPManager.getXP(this);
        String source = XPManager.getLastSource(this);
        boolean rankUp = XPManager.didRankUp(this);

        String msg =
                "⚡ SYSTEM NOTIFICATION ⚡\n\n" +
                        "Last Activity: " + source + "\n" +
                        "XP Change: " + (lastXP >= 0 ? "+" : "") + lastXP + "\n" +
                        "Total XP: " + xp + "\n" +
                        "Current Rank: " + XPManager.getRankTitle(xp) +
                        (rankUp ? "\n\n🏆 RANK UP! You have evolved!" : "");

        new Handler().postDelayed(() ->
                new AlertDialog.Builder(this)
                        .setTitle("[ SYSTEM ]")
                        .setMessage(msg)
                        .setPositiveButton("ACKNOWLEDGE", null)
                        .show(), 600);

        getSharedPreferences("MainData", MODE_PRIVATE)
                .edit().putInt("lastXP", 0).apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHunterCard();
    }
}