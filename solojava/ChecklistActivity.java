package com.example.solojava;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChecklistActivity extends AppCompatActivity {

    EditText questInput;
    Button addQuestBtn;
    LinearLayout questList;
    TextView streakText, sessionXpText, totalXpText;
    SharedPreferences questPrefs;
    int sessionXP = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        questPrefs    = getSharedPreferences("QuestData", MODE_PRIVATE);
        questInput    = findViewById(R.id.questInput);
        addQuestBtn   = findViewById(R.id.addQuestBtn);
        questList     = findViewById(R.id.questList);
        streakText    = findViewById(R.id.questStreakText);
        sessionXpText = findViewById(R.id.questXpText);
        totalXpText   = findViewById(R.id.totalXpText);

        sessionXP = 0;
        updateStreak();
        loadSavedQuests();
        updateTotalXP();

        addQuestBtn.setOnClickListener(v -> {
            AnimationHelper.buttonPress(addQuestBtn);
            addQuest();
        });
    }

    private void addQuest() {
        String text = questInput.getText().toString().trim();
        if (text.isEmpty()) {
            AnimationHelper.shake(questInput);
            Toast.makeText(this, "Enter a quest!", Toast.LENGTH_SHORT).show();
            return;
        }
        saveQuestToPrefs(text);
        addQuestView(text, true);
        questInput.setText("");
    }

    private void addQuestView(String text, boolean animate) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(16, 18, 16, 18);
        row.setBackgroundResource(R.drawable.card_purple);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 10);
        row.setLayoutParams(rowParams);

        CheckBox cb = new CheckBox(this);
        cb.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFF1F5F9);
        tv.setTextSize(14f);
        tv.setPadding(12, 0, 0, 0);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView xpBadge = new TextView(this);
        xpBadge.setText("+15 XP");
        xpBadge.setTextColor(0xFFA855F7);
        xpBadge.setTextSize(11f);
        xpBadge.setPadding(8, 4, 8, 4);

        TextView delBtn = new TextView(this);
        delBtn.setText("✕");
        delBtn.setTextColor(0xFF334455);
        delBtn.setTextSize(15f);
        delBtn.setPadding(14, 0, 0, 0);

        row.addView(cb);
        row.addView(tv);
        row.addView(xpBadge);
        row.addView(delBtn);
        questList.addView(row, 0);

        if (animate) {
            row.setAlpha(0f);
            row.setTranslationX(-60f);
            row.animate().alpha(1f).translationX(0f).setDuration(300).start();
        }

        cb.setOnCheckedChangeListener((btn, checked) -> {
            if (!checked) return;
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tv.setTextColor(0xFF334455);
            xpBadge.setVisibility(View.GONE);

            try {
                MediaPlayer mp = MediaPlayer.create(this, R.raw.levelup);
                if (mp != null) { mp.start(); mp.setOnCompletionListener(MediaPlayer::release); }
            } catch (Exception ignored) {}

            XPManager.addXP(this, XPManager.XP_QUEST_PER_ITEM, "Quest: " + text);
            sessionXP += XPManager.XP_QUEST_PER_ITEM;
            updateTotalXP();

            if (sessionXpText != null) {
                sessionXpText.setText("+15 XP  ⚡");
                sessionXpText.setTextColor(0xFFA855F7);
                sessionXpText.setAlpha(1f);
                sessionXpText.animate().alpha(0f).setDuration(600).setStartDelay(1500).start();
            }

            AnimationHelper.vanishAndRemove(row, questList);
            removeQuestFromPrefs(text);
            updateStreak();
            showXPFloat();
        });

        delBtn.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Remove Quest?")
                        .setMessage("\"" + text + "\"")
                        .setPositiveButton("Remove", (d, w) -> {
                            row.animate().alpha(0f).translationX(120f).setDuration(280)
                                    .withEndAction(() -> {
                                        questList.removeView(row);
                                        removeQuestFromPrefs(text);
                                    }).start();
                        })
                        .setNegativeButton("Cancel", null).show());
    }

    private void updateTotalXP() {
        if (totalXpText != null)
            totalXpText.setText("Session XP: +" + sessionXP);
    }

    private void updateStreak() {
        int streak = questPrefs.getInt("streak", 0);
        if (streakText != null) streakText.setText("⚡ Quest Streak: " + streak + " days");
    }

    private void saveQuestToPrefs(String text) {
        try {
            JSONArray arr = new JSONArray(questPrefs.getString("quests", "[]"));
            arr.put(text);
            questPrefs.edit().putString("quests", arr.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void removeQuestFromPrefs(String text) {
        try {
            JSONArray arr = new JSONArray(questPrefs.getString("quests", "[]"));
            JSONArray updated = new JSONArray();
            for (int i = 0; i < arr.length(); i++)
                if (!arr.getString(i).equals(text)) updated.put(arr.getString(i));
            questPrefs.edit().putString("quests", updated.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void loadSavedQuests() {
        try {
            JSONArray arr = new JSONArray(questPrefs.getString("quests", "[]"));
            for (int i = 0; i < arr.length(); i++) addQuestView(arr.getString(i), false);
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void showXPFloat() {
        View root = findViewById(android.R.id.content);
        if (root instanceof ViewGroup)
            AnimationHelper.showXPFloat((ViewGroup) root, 15, 200, 500);
    }
}