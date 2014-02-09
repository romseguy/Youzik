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
	private final String[] fields = {
		DatabaseHelper.DOWNLOAD_TABLE_FIELD_ID,
		DatabaseHelper.DOWNLOAD_TABLE_FIELD_NAME,
		DatabaseHelper.DOWNLOAD_TABLE_FIELD_URL
	};
	
    public DownloadDatabase(Context context) {
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }
    
    private Download cursorToDownload(Cursor cursor) {
    	Download download = new Download();
    	download.setId(cursor.getLong(0));
    	download.setName(cursor.getString(1));
    	download.setUrl(cursor.getString(2));
    	return download;
    }
    
    public void insertDownload(Download dl) {
    	ContentValues values = new ContentValues();
    	values.put(DatabaseHelper.DOWNLOAD_TABLE_FIELD_ID, dl.getId());
    	values.put(DatabaseHelper.DOWNLOAD_TABLE_FIELD_NAME, dl.getName());
    	values.put(DatabaseHelper.DOWNLOAD_TABLE_FIELD_URL, dl.getUrl());
    	
    	SQLiteDatabase db = this.databaseHelper.getDatabase();
    	db.insert(DatabaseHelper.DOWNLOAD_TABLE_NAME, null, values);
    	db.query(DatabaseHelper.DOWNLOAD_TABLE_NAME, this.fields, DatabaseHelper.DOWNLOAD_TABLE_FIELD_ID + " = " + dl.getId(), null, null, null, null);
    	this.databaseHelper.closeDatabase();
    }
    
    public List<Download> getDownloads() {
    	SQLiteDatabase db = this.databaseHelper.getDatabase();
    	List<Download> downloads = new ArrayList<Download>();
    	
    	Cursor cursor = db.query(DatabaseHelper.DOWNLOAD_TABLE_NAME, this.fields, null, null, null, null, null);
    	cursor.moveToFirst();
    	while (!cursor.isAfterLast()) {
    		downloads.add(this.cursorToDownload(cursor));
    		cursor.moveToNext();
    	}
    	cursor.close();
    	
    	this.databaseHelper.closeDatabase();
    	return downloads;
    	
    }

}
