package com.example.SAFEFALL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "FallSafety.db";
    public static final String TABLE_GUARDIANS = "guardians";
    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_SETTINGS = "settings";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 5);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_GUARDIANS + "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, guardian_id TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + "(id INTEGER PRIMARY KEY AUTOINCREMENT, date_time TEXT, status TEXT, location TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + "(key TEXT PRIMARY KEY, value TEXT)");
        
        // Initial setup
        db.execSQL("INSERT INTO " + TABLE_SETTINGS + " (key, value) VALUES ('user_role', 'Person')");
        db.execSQL("INSERT INTO " + TABLE_SETTINGS + " (key, value) VALUES ('my_id', 'SAFE_" + (int)(Math.random() * 9000 + 1000) + "')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GUARDIANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    public void updateSetting(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.insertWithOnConflict(TABLE_SETTINGS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getSetting(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS, new String[]{"value"}, "key=?", new String[]{key}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String val = cursor.getString(0);
            cursor.close();
            return val;
        }
        return null;
    }

    public boolean insertGuardian(String name, String guardianId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("guardian_id", guardianId);
        return db.insert(TABLE_GUARDIANS, null, cv) != -1;
    }

    public void deleteGuardian(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GUARDIANS, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllGuardians() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_GUARDIANS, null);
    }

    public boolean insertHistory(String dateTime, String status, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date_time", dateTime);
        cv.put("status", status);
        cv.put("location", location);
        return db.insert(TABLE_HISTORY, null, cv) != -1;
    }
    
    public Cursor getAllHistory() {
        return this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_HISTORY + " ORDER BY id DESC", null);
    }

    public void deleteHistoryItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, "id=?", new String[]{String.valueOf(id)});
    }

    public void clearAllHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
    }
}
