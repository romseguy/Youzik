package com.youzik.app.fragments;

import com.youzik.app.R;
import com.youzik.app.fragments.handlers.RequestDownloadHandler;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class BrowseTabFragment extends Fragment {
	
	private WebView webView;
	private Bundle webViewBundle;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout browseTabView = (LinearLayout) inflater.inflate(R.layout.browse_tab, container, false);
		this.webView = (WebView) browseTabView.findViewById(R.id.webView);
		this.webView.setWebViewClient(new WebViewClient() {
	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            if (url.endsWith(".mp3")) {
	            	((RequestDownloadHandler) BrowseTabFragment.this.getActivity()).handleRequestDownload(url);
	                return true;
	            }
	            return super.shouldOverrideUrlLoading(view, url);
	        }
		});
		
		if (this.webViewBundle == null) {
			this.webView.loadUrl("http://tuto-geek.com/sample.html");
			this.webView.getSettings();
			this.webView.setBackgroundColor(0);
		} else {
			this.webView.restoreState(webViewBundle);
		}
		
		return browseTabView;
	}
	
	/**
	 * Saves the state of the webView in the webViewBundle.
	 * Called when the Fragment is going into the background
	 * so it can be restored in the onCreateView method
	 * when going back to the browse tab.
	 */
    @Override
    public void onPause() {
		super.onPause();
	
		this.webViewBundle = new Bundle();
		this.webView.saveState(this.webViewBundle);
    }

}
