package com.youzik.app;

import com.youzik.app.entities.Download;
import com.youzik.app.entities.database.DownloadDatabase;

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
import android.os.IInterface;
import android.util.Log;

public class DownloadManagerService extends Service {
	
	/**
	 *  MainActivity will implement this interface so we can notify DownloadTabFragment
	 *  to refresh the completed downloads list once the download is completed
	 */
	public interface OnDownloadCompletedHandler extends IInterface {
		public void updateDownloadList();
	}

	private final IBinder binder = new LocalBinder();
	private OnDownloadCompletedHandler downloadCompletedHandler = null;
	
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
				Cursor cursor = downloadManager.query(query);

				if (!cursor.moveToFirst()) {
					Log.v("DownloadManager", "download list is empty");
					return;
				}
				
				if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL) {
					Log.v("DownloadManager", "download has completed but is not successful");
					return;
				}
				
				Download dl = new Download();
				dl.setId(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
				dl.setName(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)));
				dl.setUrl(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
				
				DownloadDatabase db = new DownloadDatabase(DownloadManagerService.this.getBaseContext());
				db.insertDownload(dl);
				downloadCompletedHandler.updateDownloadList();
			}
		};

		registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
    
}
