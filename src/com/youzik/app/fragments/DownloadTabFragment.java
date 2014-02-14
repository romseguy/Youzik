package com.youzik.app.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.youzik.app.R;
import com.youzik.app.entities.Download;
import com.youzik.app.entities.database.DownloadDatabase;
import com.youzik.app.fragments.handlers.RequestPlayDownloadHandler;
import com.youzik.app.services.DownloadManagerService;
import com.youzik.app.services.MediaPlayerService;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadTabFragment extends ListFragment {

	private DownloadsAdapter adapter;
	
	public class DownloadsAdapter extends ArrayAdapter<Download> {

		public DownloadsAdapter(Context context, List<Download> data) {
			super(context, R.layout.download_item, data);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
                convertView = inflater.inflate(R.layout.download_item, parent, false);
			}
			
			final Download item = this.getItem(position);
			TextView name = (TextView) convertView.findViewById(R.id.download_item_name);
			name.setText(this.getItem(position).getName());
			
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v("DownloadTabFragment", "onClick() called at position=" + position);
					
					((RequestPlayDownloadHandler) DownloadTabFragment.this.getActivity()).handleRequestPlayDownload(item);
				}
			});
			
			convertView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Log.v("DownloadTabFragment", "onLongClick() called at position=" + position);
					
					Intent intent = new Intent(MediaPlayerService.ACTION_QUEUE_TRACK);
	        		intent.putExtra(DownloadManagerService.DATA, item);
	                DownloadTabFragment.this.getActivity().sendBroadcast(intent);
	                return true;
				}
			});
			
			return convertView;
		}
		
	}
	
	public class ProgressAsyncTask extends AsyncTask<Void, Integer, Void> {

		private Context context;
		private Download currentDownload;
		private ProgressBar currentProgressBar;

		public ProgressAsyncTask(Context context, long downloadId) {
			this.context = context;
			this.currentDownload = DownloadTabFragment.this.currentDownloads.get(downloadId);
			this.currentProgressBar = DownloadTabFragment.this.currentProgressBars.get(downloadId);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			DownloadTabFragment.this.progressBarsLayout.removeView(this.currentProgressBar);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(this.currentDownload.getId());
			Cursor cursor = downloadManager.query(q);
			
			if (!cursor.moveToFirst()) {
				cursor.close();
				return null;
			}
            
			int progress = 0;
			int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
			
			while (progress < 100) {
	            if (bytes_total > 0) {
	            	progress = (int) 100.0 * bytes_downloaded / bytes_total;
	            	publishProgress(progress);
	            }
	            
	            cursor.close();
	            SystemClock.sleep(500);
	            cursor = downloadManager.query(q);
	            
	            if (cursor.moveToFirst()) {
	            	bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
	            	bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
	            }
			}
			
			cursor.close();
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			this.currentProgressBar.setProgress(values[0]);
		}

	}
	
	/*
	 * Display section
	 */
	private List<Download> completedDownloads;
	private LinearLayout progressBarsLayout;
	
	private void createList() {
		this.completedDownloads = new ArrayList<Download>();
		this.adapter = new DownloadsAdapter(this.getActivity(), this.completedDownloads);
		this.setListAdapter(this.adapter);
		this.updateList();
	}
	
	public void updateList() {
		if (this.completedDownloads == null || this.adapter == null)
			this.createList();
		
		DownloadDatabase db = new DownloadDatabase(this.getActivity());
		this.completedDownloads.clear();
		this.completedDownloads.addAll(db.getDownloads());
		this.adapter.notifyDataSetChanged();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View downloadTabView = inflater.inflate(R.layout.download_tab, container, false);
		this.progressBarsLayout = (LinearLayout) downloadTabView.findViewById(R.id.progressbars);
		return downloadTabView;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	this.createList();
    }
	
	/*
	 * Downloading section
	 */
	private Map<Long, Download> currentDownloads = new HashMap<Long, Download>();
	private Map<Long, ProgressBar> currentProgressBars = new HashMap<Long, ProgressBar>();
	
    public void startDownload(String url) {
    	Intent service = new Intent(this.getActivity(), DownloadManagerService.class);
    	service.putExtra(DownloadManagerService.URL, url);
    	this.getActivity().startService(service);
	}
    
	private BroadcastReceiver downloadStartedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			final Download d = (Download) intent.getParcelableExtra(DownloadManagerService.DATA);
			long downloadId = d.getId();
			DownloadTabFragment.this.currentDownloads.put(downloadId, d);
			
			// get a new progressbar layout element
			LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ProgressBar p = (ProgressBar) layoutInflater.inflate(R.layout.download_progressbar, null);
			
			// and add it to the progressbars layout
			DownloadTabFragment.this.progressBarsLayout.addView(p);
			currentProgressBars.put(downloadId, p);
			
			new ProgressAsyncTask(context, downloadId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	};
	
	private BroadcastReceiver downloadCompletedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, DownloadTabFragment.this.currentDownloads.size()-1);
			Download d = DownloadTabFragment.this.currentDownloads.remove(downloadId);
			
			if (d == null)
				return;
			
			// we insert the download removed from currentDownloads into completedDownloads
			DownloadDatabase db = new DownloadDatabase(DownloadTabFragment.this.getActivity());
			db.insertDownload(d);
			DownloadTabFragment.this.updateList();
		}
	};
	
	@Override
	public void onResume() {
		IntentFilter intentFilter = new IntentFilter(DownloadManagerService.ACTION_DOWNLOAD_STARTED);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.getActivity().registerReceiver(this.downloadStartedReceiver, intentFilter);
		
		intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.getActivity().registerReceiver(this.downloadCompletedReceiver, intentFilter);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		this.getActivity().unregisterReceiver(this.downloadStartedReceiver);
		this.getActivity().unregisterReceiver(this.downloadCompletedReceiver);
		super.onPause();
	}
	
}
