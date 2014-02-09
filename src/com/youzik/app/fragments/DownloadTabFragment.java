package com.youzik.app.fragments;

import java.util.ArrayList;
import java.util.List;

import com.youzik.app.R;
import com.youzik.app.entities.Download;
import com.youzik.app.entities.database.DownloadDatabase;
import com.youzik.app.MainActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DownloadTabFragment extends ListFragment implements MainActivity.OnDownloadCompletedCallback {

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
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	this.createList();
    }

	@Override
	public void downloadCompleted() {
		this.updateList();
	}

}
