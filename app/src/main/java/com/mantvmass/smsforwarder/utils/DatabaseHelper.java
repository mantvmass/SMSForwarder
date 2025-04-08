package com.mantvmass.smsforwarder.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SMSForwarder.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SETTINGS = "settings";
    private static final String KEY_ID = "id";
    private static final String KEY_URL = "url";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // สร้างตาราง settings
        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_URL + " TEXT)";
        db.execSQL(CREATE_SETTINGS_TABLE);

        // เพิ่มค่าเริ่มต้น
        ContentValues values = new ContentValues();
        values.put(KEY_URL, "http://192.168.1.8:5000");
        db.insert(TABLE_SETTINGS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    // บันทึก URL ใหม่
    public void saveUrl(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_URL, url);

        // อัพเดท URL (ใช้ id=1 เพราะเราจะเก็บแค่ 1 URL)
        db.update(TABLE_SETTINGS, values, KEY_ID + "=?", new String[]{"1"});
        if (db.update(TABLE_SETTINGS, values, KEY_ID + "=?", new String[]{"1"}) == 0) {
            values.put(KEY_ID, 1);
            db.insert(TABLE_SETTINGS, null, values);
        }
        db.close();
    }

    // ดึง URL จากฐานข้อมูล
    public String getUrl() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS, new String[]{KEY_URL}, KEY_ID + "=?",
                new String[]{"1"}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String url = cursor.getString(cursor.getColumnIndexOrThrow(KEY_URL));
            cursor.close();
            return url;
        }
        return "http://192.168.1.8:5000"; // ค่าเริ่มต้นถ้าไม่มีในฐานข้อมูล
    }
}