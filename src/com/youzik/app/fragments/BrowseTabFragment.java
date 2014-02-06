package com.youzik.app.fragments;

import com.youzik.app.R;

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
		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.browse_tab, container, false);
		webView = (WebView) ll.findViewById(R.id.webView);
		webView.setWebViewClient(new WebViewClient());
		
		if (webViewBundle == null) {
		    webView.loadUrl("http://tuto-geek.com/sample.html");
		} else {
		    webView.restoreState(webViewBundle);
		}
		
		return ll;
	}
	
	/**
	 * Save the state of the webView in the webViewBundle
	 * the method onPause is called when the Fragment is going into the background
	 * so it can be restored in the onCreateView method when going back to the browse tab
	 */
    @Override
    public void onPause() {
		super.onPause();
	
		webViewBundle = new Bundle();
		webView.saveState(webViewBundle);
    }

}
