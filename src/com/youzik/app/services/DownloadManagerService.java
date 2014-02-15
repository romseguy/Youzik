package com.youzik.app.services;

import com.youzik.app.entities.Download;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DownloadManagerService extends IntentService {

    public static final String ACTION_DOWNLOAD_STARTED = "com.youzik.app.intent.action.ACTION_DOWNLOAD_STARTED";
    public static final String DATA = "download";
    public static final String URL = "url";

    public DownloadManagerService() {
        super("DownloadManagerService");
    }

    @Override
    protected void onHandleIntent(Intent service) {
        // send the HTTP request to start the download with the url provided by
        // the intent data
        Request request = new Request(Uri.parse(service.getStringExtra(URL)));

        // append the download to the Android's download manager
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        // get a cursor to it
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(q);

        if (!cursor.moveToFirst()) {
            Log.d("DownloadManagerService", "download list is empty");
            return;
        }

        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

        // wait for download to start
        while (status == DownloadManager.STATUS_PENDING || "".equals(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)))) {
            cursor.close();
            cursor = downloadManager.query(q);

            if (cursor.moveToFirst())
                status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        }

        if (status == DownloadManager.STATUS_RUNNING) {
            String downloadName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));

            Download d = new Download();
            d.setId(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
            d.setName(downloadName.substring(0, (downloadName.length() - 4)));
            d.setUrl(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)));

            // notifiy the BroadcastReceiver downloadStartedReceiver that the
            // download has started
            Intent intent = new Intent();
            intent.setAction(ACTION_DOWNLOAD_STARTED);
            intent.putExtra(DownloadManagerService.DATA, d);
            sendBroadcast(intent);
        }

        cursor.close();
    }

}
