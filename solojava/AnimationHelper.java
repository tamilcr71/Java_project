package com.example.solojava;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

public class AnimationHelper {

    // ── XP FLOAT TEXT ────────────────────────────────────────
    // Shows "+60 XP" floating up and fading
    public static void showXPFloat(ViewGroup parent, int xp, float startX, float startY) {
        TextView tv = new TextView(parent.getContext());
        String text = (xp >= 0 ? "+" : "") + xp + " XP";
        tv.setText(text);
        tv.setTextSize(22f);
        tv.setTextColor(xp >= 0 ? 0xFFFFD700 : 0xFFFF1744);
        tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tv.setAlpha(0f);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        parent.addView(tv, lp);
        tv.setX(startX);
        tv.setY(startY);

        tv.animate()
                .alpha(1f).translationYBy(-80f)
                .setDuration(300)
                .withEndAction(() ->
                        tv.animate()
                                .alpha(0f).translationYBy(-60f)
                                .setDuration(500)
                                .setStartDelay(600)
                                .withEndAction(() -> parent.removeView(tv))
                                .start()
                ).start();
    }

    // ── RANK UP PULSE ────────────────────────────────────────
    public static void rankUpPulse(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.35f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.35f, 1f);
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.start();
    }

    // ── SHAKE (WRONG INPUT) ──────────────────────────────────
    public static void shake(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(
                view, "translationX",
                0f, -20f, 20f, -15f, 15f, -10f, 10f, 0f
        );
        anim.setDuration(500);
        anim.start();
    }

    // ── BUTTON PRESS EFFECT ──────────────────────────────────
    public static void buttonPress(View view) {
        view.animate()
                .scaleX(0.93f).scaleY(0.93f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(150)
                                .setInterpolator(new OvershootInterpolator())
                                .start()
                ).start();
    }

    // ── FADE IN VIEW ─────────────────────────────────────────
    public static void fadeIn(View view, int duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate().alpha(1f).setDuration(duration).start();
    }

    // ── SLIDE UP INTO VIEW ───────────────────────────────────
    public static void slideUp(View view, int delay) {
        view.setAlpha(0f);
        view.setTranslationY(60f);
        view.animate()
                .alpha(1f).translationY(0f)
                .setDuration(400)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    // ── VANISH + REMOVE ──────────────────────────────────────
    public static void vanishAndRemove(View view, ViewGroup parent) {
        view.animate()
                .alpha(0f).scaleX(0.8f).scaleY(0.8f).translationX(120f)
                .setDuration(450)
                .setStartDelay(600)
                .withEndAction(() -> parent.removeView(view))
                .start();
    }

    // ── STREAK FIRE BOUNCE ───────────────────────────────────
    public static void streakBounce(View view) {
        ObjectAnimator bounce = ObjectAnimator.ofFloat(
                view, "translationY", 0f, -12f, 0f, -6f, 0f
        );
        bounce.setDuration(500);
        bounce.setInterpolator(new AccelerateDecelerateInterpolator());
        bounce.start();
    }

    // ── COUNT UP NUMBER ANIMATION ────────────────────────────
    public static void countUp(TextView tv, int from, int to, int duration, String suffix) {
        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.setDuration(duration);
        anim.addUpdateListener(a -> {
            int val = (int) a.getAnimatedValue();
            tv.setText(val + suffix);
        });
        anim.start();
    }

    // ── PROGRESS BAR ANIMATE ─────────────────────────────────
    public static void animateProgress(android.widget.ProgressBar bar, int to, int duration) {
        ObjectAnimator anim = ObjectAnimator.ofInt(bar, "progress", bar.getProgress(), to);
        anim.setDuration(duration);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }

    // ── STAGGERED LIST ENTRANCE ──────────────────────────────
    public static void staggerList(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(40f);
            child.animate()
                    .alpha(1f).translationY(0f)
                    .setDuration(350)
                    .setStartDelay(i * 80L)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    // ── GLOW PULSE ───────────────────────────────────────────
    public static void glowPulse(View view, int repeatCount) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.4f, 1f);
        anim.setDuration(800);
        anim.setRepeatCount(repeatCount);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.start();
    }
}