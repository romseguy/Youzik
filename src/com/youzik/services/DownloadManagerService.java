package com.youzik.services;

import com.youzik.app.entities.Download;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DownloadManagerService extends IntentService {

	public final static String ACTION_DOWNLOAD_STARTED= "com.youzik.app.intent.action.ACTION_DOWNLOAD_STARTED";
	public static final String DATA = "download";
	public static final String URL = "url";

	public DownloadManagerService() {
		super("DownloadManagerService");
	}
	
	@Override
	protected void onHandleIntent(Intent service) {
		// send the HTTP request to start the download with the url provided by the intent data 
		Request request = new Request(Uri.parse(service.getStringExtra(URL)));
				
		// append the download to the Android's download manager
		DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		long downloadId = downloadManager.enqueue(request);
		
		// get a cursor to it
		Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));

		if (!cursor.moveToFirst()) {
			Log.v("DownloadManagerService", "download list is empty");
			return;
		}
		
		Download dl = new Download();
		dl.setId(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
		dl.setName(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)));
		dl.setUrl(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
		cursor.close();
		
		// notifiy the BroadcastReceiver downloadStartedReceiver that the download has started
		Intent intent = new Intent();
		intent.setAction(ACTION_DOWNLOAD_STARTED);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(DownloadManagerService.DATA, dl);
	    sendBroadcast(intent);
	}
    
}
