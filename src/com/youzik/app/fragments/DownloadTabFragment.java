package com.youzik.app.fragments;

import java.util.ArrayList;
import java.util.List;

import com.youzik.app.R;
import com.youzik.app.entities.Download;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
			
			// display item content on download_list_element layout
			final Download item = this.getItem(position);
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
	
	private void updateList() {
		if (this.data == null || this.adapter == null)
			this.createList();
		
		this.data.clear();
		/** @TODO this.data.addAll(getDownloads()) */
		this.adapter.notifyDataSetChanged();
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	this.createList();
    }

}
