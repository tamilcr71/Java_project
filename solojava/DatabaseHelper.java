package com.example.solojava;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "solojava.db";
    private static final int    DB_VERSION = 1;

    // TABLE: expense_logs
    public static final String TABLE_EXPENSE = "expense_logs";
    public static final String COL_EXP_DATE  = "date";
    public static final String COL_EXP_AMT   = "amount";
    public static final String COL_EXP_CAT   = "category";
    public static final String COL_EXP_NOTE  = "note";

    // TABLE: sleep_logs
    public static final String TABLE_SLEEP   = "sleep_logs";
    public static final String COL_SLP_DATE  = "date";
    public static final String COL_SLP_HRS   = "hours";
    public static final String COL_SLP_QUAL  = "quality";
    public static final String COL_SLP_BED   = "bedtime";
    public static final String COL_SLP_WAKE  = "waketime";

    // TABLE: gym_logs
    public static final String TABLE_GYM     = "gym_logs";
    public static final String COL_GYM_DATE  = "date";
    public static final String COL_GYM_EX    = "exercise";
    public static final String COL_GYM_SETS  = "sets";
    public static final String COL_GYM_REPS  = "reps";
    public static final String COL_GYM_WT    = "weight_kg";

    // TABLE: xp_history
    public static final String TABLE_XP      = "xp_history";
    public static final String COL_XP_DATE   = "date";
    public static final String COL_XP_AMT    = "amount";
    public static final String COL_XP_SRC    = "source";

    private static DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_EXPENSE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_EXP_DATE + " TEXT," +
                COL_EXP_AMT  + " REAL," +
                COL_EXP_CAT  + " TEXT," +
                COL_EXP_NOTE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_SLEEP + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_SLP_DATE + " TEXT," +
                COL_SLP_HRS  + " REAL," +
                COL_SLP_QUAL + " INTEGER," +
                COL_SLP_BED  + " TEXT," +
                COL_SLP_WAKE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_GYM + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_GYM_DATE + " TEXT," +
                COL_GYM_EX   + " TEXT," +
                COL_GYM_SETS + " INTEGER," +
                COL_GYM_REPS + " INTEGER," +
                COL_GYM_WT   + " REAL)");

        db.execSQL("CREATE TABLE " + TABLE_XP + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_XP_DATE  + " TEXT," +
                COL_XP_AMT   + " INTEGER," +
                COL_XP_SRC   + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLEEP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GYM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_XP);
        onCreate(db);
    }

    // ── INSERT EXPENSE ───────────────────────────────────────
    public void insertExpense(String date, double amount, String category, String note) {
        ContentValues cv = new ContentValues();
        cv.put(COL_EXP_DATE, date);
        cv.put(COL_EXP_AMT, amount);
        cv.put(COL_EXP_CAT, category);
        cv.put(COL_EXP_NOTE, note);
        getWritableDatabase().insert(TABLE_EXPENSE, null, cv);
    }

    // ── INSERT SLEEP ─────────────────────────────────────────
    public void insertSleep(String date, double hours, int quality, String bed, String wake) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SLP_DATE, date);
        cv.put(COL_SLP_HRS, hours);
        cv.put(COL_SLP_QUAL, quality);
        cv.put(COL_SLP_BED, bed);
        cv.put(COL_SLP_WAKE, wake);
        getWritableDatabase().insert(TABLE_SLEEP, null, cv);
    }

    // ── INSERT GYM LOG ───────────────────────────────────────
    public void insertGymLog(String date, String exercise, int sets, int reps, double weight) {
        ContentValues cv = new ContentValues();
        cv.put(COL_GYM_DATE, date);
        cv.put(COL_GYM_EX, exercise);
        cv.put(COL_GYM_SETS, sets);
        cv.put(COL_GYM_REPS, reps);
        cv.put(COL_GYM_WT, weight);
        getWritableDatabase().insert(TABLE_GYM, null, cv);
    }

    // ── INSERT XP HISTORY ────────────────────────────────────
    public void insertXPHistory(String date, int amount, String source) {
        ContentValues cv = new ContentValues();
        cv.put(COL_XP_DATE, date);
        cv.put(COL_XP_AMT, amount);
        cv.put(COL_XP_SRC, source);
        getWritableDatabase().insert(TABLE_XP, null, cv);
    }

    // ── GET EXPENSE SUM FOR MONTH ─────────────────────────────
    public double getMonthExpenseTotal(String monthPrefix) {
        double total = 0;
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT SUM(" + COL_EXP_AMT + ") FROM " + TABLE_EXPENSE +
                        " WHERE " + COL_EXP_DATE + " LIKE ?",
                new String[]{monthPrefix + "%"}
        );
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }

    // ── GET EXPENSE BY CATEGORY ───────────────────────────────
    public List<float[]> getExpenseByCategory(String monthPrefix) {
        List<float[]> result = new ArrayList<>();
        String[] cats = {"Food","Transport","Entertainment","Health","Shopping","Other"};
        for (String cat : cats) {
            Cursor c = getReadableDatabase().rawQuery(
                    "SELECT SUM(" + COL_EXP_AMT + ") FROM " + TABLE_EXPENSE +
                            " WHERE " + COL_EXP_DATE + " LIKE ? AND " + COL_EXP_CAT + "=?",
                    new String[]{monthPrefix + "%", cat}
            );
            float val = 0;
            if (c.moveToFirst() && !c.isNull(0)) val = (float) c.getDouble(0);
            result.add(new float[]{val});
            c.close();
        }
        return result;
    }

    // ── GET LAST 7 SLEEP ENTRIES ─────────────────────────────
    public List<double[]> getLastSevenSleep() {
        List<double[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COL_SLP_HRS + "," + COL_SLP_QUAL +
                        " FROM " + TABLE_SLEEP + " ORDER BY id DESC LIMIT 7", null
        );
        while (c.moveToNext()) result.add(new double[]{c.getDouble(0), c.getDouble(1)});
        c.close();
        return result;
    }

    // ── GET LAST 30 EXPENSES ─────────────────────────────────
    public List<String[]> getLast30Expenses() {
        List<String[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COL_EXP_DATE + "," + COL_EXP_AMT + "," + COL_EXP_CAT +
                        " FROM " + TABLE_EXPENSE + " ORDER BY id DESC LIMIT 30", null
        );
        while (c.moveToNext()) {
            result.add(new String[]{c.getString(0), c.getString(1), c.getString(2)});
        }
        c.close();
        return result;
    }

    // ── GET LAST 10 XP HISTORY ───────────────────────────────
    public List<String[]> getXPHistory() {
        List<String[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COL_XP_DATE + "," + COL_XP_AMT + "," + COL_XP_SRC +
                        " FROM " + TABLE_XP + " ORDER BY id DESC LIMIT 10", null
        );
        while (c.moveToNext()) {
            result.add(new String[]{c.getString(0), c.getString(1), c.getString(2)});
        }
        c.close();
        return result;
    }
}