package com.youzik.app.entities.database;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    /* database structure */
    private static final int      DATABASE_VERSION          = 1;
    private static final String   DATABASE_NAME             = "youzik_downloads.db";

    public static final String    DOWNLOAD_TABLE_NAME       = "Download";
    public static final String    DOWNLOAD_TABLE_FIELD_ID   = "_id";
    public static final String    DOWNLOAD_TABLE_FIELD_NAME = "name";
    public static final String    DOWNLOAD_TABLE_FIELD_URL  = "url";

    /* queries */
    private static final String   DOWNLOAD_TABLE_CREATE     = "CREATE TABLE " + DOWNLOAD_TABLE_NAME + " (" + DOWNLOAD_TABLE_FIELD_ID + " INTEGER PRIMARY KEY, " + DOWNLOAD_TABLE_FIELD_NAME + " TEXT NOT NULL, " + DOWNLOAD_TABLE_FIELD_URL + " TEXT NOT NULL" + ");";

    /* singletons */
    private static DatabaseHelper instance                  = null;
    private SQLiteDatabase        database;
    private final AtomicInteger   getDatabaseCounter        = new AtomicInteger();

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // context.deleteDatabase(DATABASE_NAME);
    }

    /**
     * @param context
     * @return the helper singleton
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    /**
     * @return the database singleton
     */
    public SQLiteDatabase getDatabase() {
        if (this.getDatabaseCounter.incrementAndGet() == 1)
            this.database = DatabaseHelper.instance.getWritableDatabase();
        return this.database;
    }

    public void closeDatabase() {
        if (this.getDatabaseCounter.decrementAndGet() == 0)
            this.database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DOWNLOAD_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    }

}
