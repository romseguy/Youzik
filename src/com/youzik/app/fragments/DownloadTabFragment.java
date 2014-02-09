package com.youzik.app.fragments;

import java.util.ArrayList;
import java.util.List;

import com.youzik.app.DownloadManagerService;
import com.youzik.app.MainActivity;
import com.youzik.app.R;
import com.youzik.app.entities.Download;
import com.youzik.app.entities.database.DownloadDatabase;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
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
	
	private ProgressBar progressBar;
	
	public class ProgressAsyncTask extends AsyncTask<Void, Integer, Void> {

		private Context context = null;
		private Intent intent = null;
		private DownloadManagerService service = null;
		private int progressValue;

		public ProgressAsyncTask(Context context, Intent intent) {
			this.context = context;
			this.intent = intent;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressValue = 0;
			
			if (context != null && intent != null) {
				context.startService(intent);
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			DownloadTabFragment.this.updateList();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			while (progressValue < 100) {
				progressValue++;
				publishProgress(progressValue);
				SystemClock.sleep(100);
			}
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
	
    public void startDownloading(Uri url) {
    	Intent intent = new Intent(getActivity(), DownloadManagerService.class);
    	intent.setDataAndType(url, "audio/mpeg");
    	new ProgressAsyncTask(getActivity(), intent).execute();
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
		progressBar = (ProgressBar) downloadTabView.findViewById(R.id.progressbar);
		return downloadTabView;
	}

}
