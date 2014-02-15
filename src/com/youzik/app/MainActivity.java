package com.youzik.app;

import java.util.Locale;

import com.youzik.app.entities.Download;
import com.youzik.app.fragments.*;
import com.youzik.app.fragments.handlers.RequestDownloadHandler;
import com.youzik.app.fragments.handlers.RequestPlayDownloadHandler;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener, RequestDownloadHandler, RequestPlayDownloadHandler {
	
	private SectionsPagerAdapter mSectionsPagerAdapter;
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		
        private static final int BROWSE_TAB 	= 0;
        private static final int DOWNLOAD_TAB 	= 1;
        private static final int PLAY_TAB 		= 2;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case BROWSE_TAB:
					return new BrowseTabFragment();
				case DOWNLOAD_TAB:
					return new DownloadTabFragment();
				case PLAY_TAB:
					return new PlayTabFragment();
				default:
					return new BrowseTabFragment();
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case BROWSE_TAB:
					return MainActivity.this.getString(R.string.browser_tab_title).toUpperCase(l);
				case DOWNLOAD_TAB:
					return MainActivity.this.getString(R.string.download_tab_title).toUpperCase(l);
				case PLAY_TAB:
					return MainActivity.this.getString(R.string.play_tab_title).toUpperCase(l);
				default:
					return null;
			}
		}
	}
	
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		final ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three primary sections of the app.
		this.mSectionsPagerAdapter = new SectionsPagerAdapter(this.getSupportFragmentManager());
		
		// Set up the pager from the MainActivity layout with the adapter.
		this.mViewPager = (ViewPager) this.findViewById(R.id.pager);
		this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
		this.mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < this.mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(
				actionBar.newTab()
					.setText(this.mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this)
			);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		this.mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
	
	private String getFragmentTag(int item) {
        return "android:switcher:" + R.id.pager + ":" + item;
    }

	@Override
	public void handleRequestDownload(String url) {
		mViewPager.setCurrentItem(SectionsPagerAdapter.DOWNLOAD_TAB);
		DownloadTabFragment f = (DownloadTabFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag(SectionsPagerAdapter.DOWNLOAD_TAB));
		f.startDownload(url);
	}

	@Override
	public void handleRequestPlayDownload(Download d) {
		mViewPager.setCurrentItem(SectionsPagerAdapter.PLAY_TAB);
		PlayTabFragment f = (PlayTabFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag(SectionsPagerAdapter.PLAY_TAB));
		f.playDownload(d);
	}

}
