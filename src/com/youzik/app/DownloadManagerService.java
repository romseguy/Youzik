package com.youzik.app;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

public class DownloadManagerService extends Service {

	private final IBinder binder = new LocalBinder();
	
	public class LocalBinder extends Binder {
		DownloadManagerService getService() {
			return DownloadManagerService.this;
		}
	}
	
	private long enqueue;
	private DownloadManager downloadManager;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		
		Request request = new Request(intent.getData());
		enqueue = downloadManager.enqueue(request);
		Log.v("DownloadManager", "Queued download id=" + enqueue);

		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
					Log.v("DownloadManager", "download is not yet completed");
					return;
				}
				
				Query query = new Query();
				query.setFilterById(enqueue);
				Cursor c = downloadManager.query(query);

				if (!c.moveToFirst()) {
					Log.v("DownloadManager", "download list is empty");
					return;
				}
				
				if (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL) {
					Log.v("DownloadManager", "download has completed but is not successful");
					return;
				}
				
				Log.v("DownloadManager", "uriString=" + c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
			}
		};

		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		return START_STICKY;
	}

	public void showDownload(View view) {
		Intent i = new Intent();
		i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
		startActivity(i);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
}
