package com.example.solojava;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class BossBattleActivity extends AppCompatActivity {

    TextView bossName, bossHpText, playerDmgText, bossStatusText, timerText;
    ProgressBar bossHpBar;
    Button attackBtn, retreatBtn;
    LinearLayout goalsList;
    SharedPreferences bossPrefs;

    int bossMaxHp = 10000;
    int currentBossHp;
    boolean bossDefeated = false;

    String[][] monthlyGoals = {
            {"Log expenses 20+ days", "expense_days", "20", "1500"},
            {"Sleep 7+ hrs for 15 nights", "sleep_nights", "15", "1500"},
            {"Train 12+ sessions", "gym_sessions", "12", "2000"},
            {"Complete 30+ quests", "quest_count", "30", "1500"},
            {"Maintain 7-day streak", "streak_week", "1", "2500"},
            {"Earn 500+ XP this month", "monthly_xp", "500", "2000"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_battle);

        bossPrefs     = getSharedPreferences("BossData", MODE_PRIVATE);
        bossName      = findViewById(R.id.bossName);
        bossHpText    = findViewById(R.id.bossHpText);
        playerDmgText = findViewById(R.id.playerDmgText);
        bossStatusText= findViewById(R.id.bossStatusText);
        timerText     = findViewById(R.id.timerText);
        bossHpBar     = findViewById(R.id.bossHpBar);
        attackBtn     = findViewById(R.id.attackBtn);
        retreatBtn    = findViewById(R.id.retreatBtn);
        goalsList     = findViewById(R.id.goalsList);

        resetIfNewMonth();
        currentBossHp = bossPrefs.getInt("bossHp", bossMaxHp);
        bossDefeated  = bossPrefs.getBoolean("defeated", false);

        loadBossUI();
        buildGoalsList();
        setupButtons();
        AnimationHelper.glowPulse(bossName, 3);
    }

    private void resetIfNewMonth() {
        String currentMonth = new java.text.SimpleDateFormat("yyyy-MM",
                java.util.Locale.getDefault()).format(new java.util.Date());
        String savedMonth = bossPrefs.getString("month", "");
        if (!currentMonth.equals(savedMonth)) {
            bossPrefs.edit()
                    .putInt("bossHp", bossMaxHp)
                    .putBoolean("defeated", false)
                    .putString("month", currentMonth)
                    .putInt("totalDamage", 0)
                    .apply();
        }
    }

    private void loadBossUI() {
        String month = new java.text.SimpleDateFormat("MMMM",
                java.util.Locale.getDefault()).format(new java.util.Date());

        bossName.setText("👹 " + month.toUpperCase() + " OVERLORD");
        int hp = bossPrefs.getInt("bossHp", bossMaxHp);
        int totalDmg = bossPrefs.getInt("totalDamage", 0);

        bossHpText.setText("Boss HP: " + hp + " / " + bossMaxHp);
        playerDmgText.setText("Your damage dealt: " + totalDmg);
        bossHpBar.setMax(bossMaxHp);
        bossHpBar.setProgress(hp);
        bossHpBar.setProgressDrawable(getDrawable(R.drawable.boss_hp_bar));

        if (bossDefeated) {
            bossStatusText.setText("✅ BOSS DEFEATED! You claimed the victory this month!");
            bossStatusText.setTextColor(0xFF00FFAA);
            attackBtn.setEnabled(false);
            attackBtn.setAlpha(0.4f);
        } else {
            bossStatusText.setText("The boss awaits your challenge, Hunter.");
            bossStatusText.setTextColor(0xFF475569);
        }
    }

    private void buildGoalsList() {
        goalsList.removeAllViews();

        SharedPreferences expPrefs  = getSharedPreferences("ExpenseData", MODE_PRIVATE);
        SharedPreferences sleepPrefs= getSharedPreferences("SleepData", MODE_PRIVATE);
        SharedPreferences gymPrefs  = getSharedPreferences("GymData", MODE_PRIVATE);
        SharedPreferences questPrefs= getSharedPreferences("QuestData", MODE_PRIVATE);
        SharedPreferences mainPrefs = getSharedPreferences("MainData", MODE_PRIVATE);

        int[] current = {
                expPrefs.getInt("day", 1) - 1,
                sleepPrefs.getInt("day", 1) - 1,
                gymPrefs.getInt("day", 1) - 1,
                questPrefs.getInt("totalCompleted", 0),
                mainPrefs.getInt("streak", 0) >= 7 ? 1 : 0,
                Math.min(XPManager.getXP(this), 500)
        };

        for (int i = 0; i < monthlyGoals.length; i++) {
            String label  = monthlyGoals[i][0];
            int target    = Integer.parseInt(monthlyGoals[i][2]);
            int damage    = Integer.parseInt(monthlyGoals[i][3]);
            int cur       = current[i];
            boolean done  = cur >= target;
            int prog      = Math.min((cur * 100) / Math.max(target, 1), 100);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(14, 12, 14, 12);
            row.setBackgroundResource(done ? R.drawable.card_green : R.drawable.card_dark);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 8);
            row.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView labelView = new TextView(this);
            labelView.setText((done ? "✅ " : "⬜ ") + label);
            labelView.setTextColor(done ? 0xFF00FFAA : 0xFF94A3B8);
            labelView.setTextSize(12f);
            labelView.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView dmgView = new TextView(this);
            dmgView.setText("-" + damage + " HP");
            dmgView.setTextColor(0xFFFF1744);
            dmgView.setTextSize(11f);

            topRow.addView(labelView);
            topRow.addView(dmgView);

            ProgressBar pb = new ProgressBar(this, null,
                    android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);
            pb.setProgress(prog);
            LinearLayout.LayoutParams pbp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 6);
            pbp.setMargins(0, 6, 0, 0);
            pb.setLayoutParams(pbp);
            pb.setProgressDrawable(getDrawable(R.drawable.xp_bar_progress));

            row.addView(topRow);
            row.addView(pb);
            goalsList.addView(row);
        }
    }

    private void setupButtons() {
        attackBtn.setOnClickListener(v -> {
            AnimationHelper.buttonPress(attackBtn);
            calculateAndDealDamage();
        });
        retreatBtn.setOnClickListener(v -> finish());
    }

    private void calculateAndDealDamage() {
        if (bossDefeated) return;

        SharedPreferences expPrefs  = getSharedPreferences("ExpenseData", MODE_PRIVATE);
        SharedPreferences sleepPrefs= getSharedPreferences("SleepData", MODE_PRIVATE);
        SharedPreferences gymPrefs  = getSharedPreferences("GymData", MODE_PRIVATE);
        SharedPreferences questPrefs= getSharedPreferences("QuestData", MODE_PRIVATE);
        SharedPreferences mainPrefs = getSharedPreferences("MainData", MODE_PRIVATE);

        int totalDamage = 0;
        int[] current = {
                expPrefs.getInt("day", 1) - 1,
                sleepPrefs.getInt("day", 1) - 1,
                gymPrefs.getInt("day", 1) - 1,
                questPrefs.getInt("totalCompleted", 0),
                mainPrefs.getInt("streak", 0) >= 7 ? 1 : 0,
                Math.min(XPManager.getXP(this), 500)
        };

        for (int i = 0; i < monthlyGoals.length; i++) {
            int target = Integer.parseInt(monthlyGoals[i][2]);
            int damage = Integer.parseInt(monthlyGoals[i][3]);
            if (current[i] >= target) totalDamage += damage;
        }

        int prevDmg = bossPrefs.getInt("totalDamage", 0);
        int newDmg  = totalDamage - prevDmg;
        if (newDmg <= 0) {
            Toast.makeText(this, "Complete more goals to deal damage!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentBossHp = Math.max(0, currentBossHp - newDmg);
        bossPrefs.edit()
                .putInt("bossHp", currentBossHp)
                .putInt("totalDamage", totalDamage)
                .apply();

        ObjectAnimator.ofInt(bossHpBar, "progress", bossHpBar.getProgress(), currentBossHp)
                .setDuration(800).start();
        bossHpText.setText("Boss HP: " + currentBossHp + " / " + bossMaxHp);
        playerDmgText.setText("Your damage dealt: " + totalDamage);

        showXPFloat(newDmg / 10);

        if (currentBossHp <= 0) {
            bossDefeated = true;
            bossPrefs.edit().putBoolean("defeated", true).apply();
            XPManager.addXP(this, XPManager.XP_BOSS_VICTORY, "Monthly Boss Defeated!");
            bossStatusText.setText("🏆 BOSS DEFEATED! +500 XP claimed!");
            bossStatusText.setTextColor(0xFF00FFAA);
            attackBtn.setEnabled(false);
            AnimationHelper.rankUpPulse(bossName);
            Toast.makeText(this, "🏆 BOSS DEFEATED! +500 XP!", Toast.LENGTH_LONG).show();
        } else {
            bossStatusText.setText("Dealt " + newDmg + " damage! Keep going!");
            bossStatusText.setTextColor(0xFFFF6D00);
        }

        buildGoalsList();
    }

    private void showXPFloat(int dmg) {
        View root = findViewById(android.R.id.content);
        if (root instanceof ViewGroup)
            AnimationHelper.showXPFloat((ViewGroup) root, dmg, 200, 300);
    }
}