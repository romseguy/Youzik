package com.youzik.app.fragments;

import java.util.ArrayList;
import java.util.List;

import com.youzik.app.DownloadManagerService;
import com.youzik.app.MainActivity;
import com.youzik.app.R;
import com.youzik.app.entities.Download;
import com.youzik.app.entities.database.DownloadDatabase;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
	
	private BroadcastReceiver downloadStartedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Download dl = intent.getParcelableExtra(DownloadManagerService.DATA);
			DownloadDatabase db = new DownloadDatabase(DownloadTabFragment.this.getActivity());
			db.insertDownload(dl);
			
			new ProgressAsyncTask(context, intent).execute();
		}
	};
	
	@Override
	public void onPause() {
		this.getActivity().unregisterReceiver(this.downloadStartedReceiver);
		super.onPause();
	}

	@Override
	public void onResume() {
		IntentFilter intentFilter = new IntentFilter(DownloadManagerService.ACTION_DOWNLOAD_STARTED);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.getActivity().registerReceiver(this.downloadStartedReceiver, intentFilter);
		super.onResume();
	}

	private ProgressBar progressBar;
	
	public class ProgressAsyncTask extends AsyncTask<Void, Integer, Void> {

		private Context context = null;
		private Download dl;

		public ProgressAsyncTask(Context context, Intent intent) {
			this.context = context;
			this.dl = intent.getParcelableExtra(DownloadManagerService.DATA);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			DownloadTabFragment.this.updateList();
		}

		@Override
		protected Void doInBackground(Void... arg0) {            
            boolean downloading = true;
            
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(dl.getId()));
			
			if (!cursor.moveToFirst()) {
				cursor.close();
				return null;
			}
            
			while (downloading) {
				int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
	            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
	            
	            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;
                }
	            
				publishProgress((bytes_downloaded / bytes_total) * 100);
				cursor.moveToNext();
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
	
    public void startDownloading(String url) {
    	Intent service = new Intent(this.getActivity(), DownloadManagerService.class);
    	service.putExtra(DownloadManagerService.URL, url);
    	this.getActivity().startService(service);
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	this.createList();
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View downloadTabView = inflater.inflate(R.layout.download_tab, container, false);
		((MainActivity) getActivity()).setDownloadTabFragmentTag(getTag());
		this.progressBar = (ProgressBar) downloadTabView.findViewById(R.id.progressbar);
		return downloadTabView;
	}

}
