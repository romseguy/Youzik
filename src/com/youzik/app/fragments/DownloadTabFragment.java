package com.youzik.app.fragments;

import java.util.ArrayList;
import java.util.List;

import com.youzik.app.MainActivity;
import com.youzik.app.R;
import com.youzik.app.entities.Download;
import com.youzik.app.entities.database.DownloadDatabase;
import com.youzik.services.DownloadManagerService;

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
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadTabFragment extends ListFragment {

	private DownloadsAdapter adapter;
	
	public class DownloadsAdapter extends ArrayAdapter<Download> {

		public DownloadsAdapter(Context context, List<Download> data) {
			super(context, R.layout.download_item, data);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
                convertView = inflater.inflate(R.layout.download_item, parent, false);
			}
			
			/* display item content on download_item layout */
			final Download item = this.getItem(position);
			TextView name = (TextView) convertView.findViewById(R.id.download_item_name);
			name.setText(item.getName());
			
			return convertView;
		}
		
	}

	private ProgressBar progressBar;
	
	public class ProgressAsyncTask extends AsyncTask<Void, Integer, Void> {

		private Context context;

		public ProgressAsyncTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			DownloadTabFragment.this.currentDownload = null;
			DownloadTabFragment.this.updateList();
		}

		@Override
		protected Void doInBackground(Void... arg0) {			
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(DownloadTabFragment.this.currentDownload.getId());
			Cursor cursor = downloadManager.query(q);
			
			if (!cursor.moveToFirst()) {
				cursor.close();
				return null;
			}
            
			int progress = 0;
			int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
			
			while (progress < 100) {
	            Log.v("ProgressAsyncTask", bytes_downloaded + "/" + bytes_total + "=" + 100.0 * bytes_downloaded / bytes_total);
	            
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
			progressBar.setProgress(values[0]);
		}

	}
	
	private List<Download> data;
	private Download currentDownload = null;

	private void createList() {
		this.data = new ArrayList<Download>();
		this.adapter = new DownloadsAdapter(this.getActivity(), this.data);
		this.setListAdapter(this.adapter);
		this.updateList();
	}
	
	public void updateList() {
		if (this.data == null || this.adapter == null)
			this.createList();
		
		DownloadDatabase db = new DownloadDatabase(this.getActivity());
		this.data.clear();
		this.data.addAll(db.getDownloads());
		this.adapter.notifyDataSetChanged();
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	this.createList();
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		((MainActivity) getActivity()).setDownloadTabFragmentTag(getTag());
		View downloadTabView = inflater.inflate(R.layout.download_tab, container, false);
		this.progressBar = (ProgressBar) downloadTabView.findViewById(R.id.progressbar);
		return downloadTabView;
	}
	
    public void startDownloading(String url) {
    	Intent service = new Intent(this.getActivity(), DownloadManagerService.class);
    	service.putExtra(DownloadManagerService.URL, url);
    	this.getActivity().startService(service);
	}
    
	private BroadcastReceiver downloadStartedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadTabFragment.this.currentDownload = intent.getParcelableExtra(DownloadManagerService.DATA);
			DownloadDatabase db = new DownloadDatabase(DownloadTabFragment.this.getActivity());
			db.insertDownload(DownloadTabFragment.this.currentDownload);
			new ProgressAsyncTask(context).execute();
		}
	};
	
	@Override
	public void onResume() {
		IntentFilter intentFilter = new IntentFilter(DownloadManagerService.ACTION_DOWNLOAD_STARTED);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.getActivity().registerReceiver(this.downloadStartedReceiver, intentFilter);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		this.getActivity().unregisterReceiver(this.downloadStartedReceiver);
		super.onPause();
	}

}
