package com.youzik.app.entities.database;

import java.util.ArrayList;
import java.util.List;

import com.youzik.app.entities.Download;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DownloadDatabase {

    private final DatabaseHelper databaseHelper;
    private final String[] fields = { DatabaseHelper.DOWNLOAD_TABLE_FIELD_ID, DatabaseHelper.DOWNLOAD_TABLE_FIELD_NAME, DatabaseHelper.DOWNLOAD_TABLE_FIELD_URL };

    public DownloadDatabase(Context context) {
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }
    
    public void dropDatabase() {
        this.databaseHelper.deleteDatabase();
    }

    public void insertDownload(Download d) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.DOWNLOAD_TABLE_FIELD_NAME, d.getName());
        values.put(DatabaseHelper.DOWNLOAD_TABLE_FIELD_URL, d.getUrl());

        SQLiteDatabase db = this.databaseHelper.getDatabase();
        long insertId = db.insert(DatabaseHelper.DOWNLOAD_TABLE_NAME, null, values);
        db.query(DatabaseHelper.DOWNLOAD_TABLE_NAME, this.fields, DatabaseHelper.DOWNLOAD_TABLE_FIELD_ID + " = " + insertId, null, null, null, null);
        this.databaseHelper.closeDatabase();
    }

    public List<Download> getDownloads() {
        SQLiteDatabase db = this.databaseHelper.getDatabase();
        List<Download> downloads = new ArrayList<Download>();

        Cursor cursor = db.query(DatabaseHelper.DOWNLOAD_TABLE_NAME, this.fields, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Download d = new Download();
            d.setId(cursor.getLong(0));
            d.setName(cursor.getString(1));
            d.setUrl(cursor.getString(2));
            downloads.add(d);
            cursor.moveToNext();
        }
        cursor.close();

        this.databaseHelper.closeDatabase();
        return downloads;

    }

}
