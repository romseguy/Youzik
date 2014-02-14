package com.youzik.app.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.youzik.app.R;
import com.youzik.app.helpers.Convert;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlayTabFragment extends Fragment implements
		OnCompletionListener, SeekBar.OnSeekBarChangeListener {
	
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	//private ImageButton btnPlaylist;
	private ImageButton btnRepeat;
	private ImageButton btnShuffle;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	
	private MediaPlayer mp;
	private Handler mHandler = new Handler();
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	//private SongsManager songManager;
	
	private int seekForwardTime = 5000; // ms
	private int seekBackwardTime = 5000; // ms
	private int currentSongIndex = 0; 
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mp = new MediaPlayer();
		this.mp.setOnCompletionListener(this);
		this.songProgressBar.setOnSeekBarChangeListener(this);
		
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mp == null) return;
				
				if (mp.isPlaying()) {
					mp.pause();
					btnPlay.setImageResource(R.drawable.btn_play);
				} else {
					btnPlay.setImageResource(R.drawable.btn_pause);
				}
			}
		});
		
		btnForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int currentPosition = mp.getCurrentPosition();
				
				// check if seekForward time is lesser than song duration
				if (currentPosition + seekForwardTime <= mp.getDuration()) {
					// forward song
					mp.seekTo(currentPosition + seekForwardTime);
				} else {
					// forward to end position
					mp.seekTo(mp.getDuration());
				}
			}
		});
		
		btnBackward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int currentPosition = mp.getCurrentPosition();
				// check if seekBackward time is greater than 0 sec
				if (currentPosition - seekBackwardTime >= 0) {
					// forward song
					mp.seekTo(currentPosition - seekBackwardTime);
				} else {
					// backward to starting position
					mp.seekTo(0);
				}
			}
		});
		
		btnNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// check if next song is there or not
				if (currentSongIndex < (songsList.size() - 1)) {
					playSong(currentSongIndex + 1);
					currentSongIndex = currentSongIndex + 1;
				} else {
					// play first song
					playSong(0);
					currentSongIndex = 0;
				}
			}
		});
		
		btnPrevious.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (currentSongIndex > 0) {
					playSong(currentSongIndex - 1);
					currentSongIndex = currentSongIndex - 1;
				} else {
					// play last song
					playSong(songsList.size() - 1);
					currentSongIndex = songsList.size() - 1;
				}
			}
		});
		
		btnRepeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isRepeat) {
					isRepeat = false;
					Toast.makeText(getActivity().getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				} else {
					isRepeat = true;
					Toast.makeText(getActivity().getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
					isShuffle = false;
					btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}
			}
		});
		
		btnShuffle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isShuffle) {
					isShuffle = false;
					Toast.makeText(getActivity().getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				} else {
					isShuffle = true;
					Toast.makeText(getActivity().getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
					isRepeat = false;
					btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}
			}
		});
		
		/**
		 * Button Click event for Play list click event Launches list activity
		 * which displays list of songs
		 */
		/*btnPlaylist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getActivity().getApplicationContext(), PlayListActivity.class);
				startActivityForResult(i, 100);
			}
		});*/
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View playTabView = inflater.inflate(R.layout.play_tab, container, false);
		this.btnPlay = (ImageButton) playTabView.findViewById(R.id.btnPlay);
		this.btnForward = (ImageButton) playTabView.findViewById(R.id.btnForward);
		this.btnBackward = (ImageButton) playTabView.findViewById(R.id.btnBackward);
		this.btnNext = (ImageButton) playTabView.findViewById(R.id.btnNext);
		this.btnPrevious = (ImageButton) playTabView.findViewById(R.id.btnPrevious);
		//this.btnPlaylist = (ImageButton) playTabView.findViewById(R.id.btnPlaylist);
		this.btnRepeat = (ImageButton) playTabView.findViewById(R.id.btnRepeat);
		this.btnShuffle = (ImageButton) playTabView.findViewById(R.id.btnShuffle);
		this.songProgressBar = (SeekBar) playTabView.findViewById(R.id.songProgressBar);
		this.songTitleLabel = (TextView) playTabView.findViewById(R.id.songTitle);
		this.songCurrentDurationLabel = (TextView) playTabView.findViewById(R.id.songCurrentDurationLabel);
		this.songTotalDurationLabel = (TextView) playTabView.findViewById(R.id.songTotalDurationLabel);
		return playTabView;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mp.release();
	}
	
	private Runnable mUpdateTimeTask = new Runnable() {
	   public void run() {
		   long totalDuration = mp.getDuration();
		   long currentDuration = mp.getCurrentPosition();
		  
		   // Displaying Total Duration time
		   songTotalDurationLabel.setText(""+Convert.milliSecondsToTimer(totalDuration));
		   // Displaying time completed playing
		   songCurrentDurationLabel.setText(""+Convert.milliSecondsToTimer(currentDuration));
		   
		   // Updating progress bar
		   int progress = (int)(Convert.getProgressPercentage(currentDuration, totalDuration));
		   //Log.d("Progress", ""+progress);
		   songProgressBar.setProgress(progress);
		   
		   // Running this thread after 100 milliseconds
	       mHandler.postDelayed(this, 100);
	   }
	};
	
	/**
	 * Receiving song index from playlist view and play the song
	 * */
	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == 100) {
			currentSongIndex = data.getExtras().getInt("songIndex");
			playSong(currentSongIndex);
		}
	}*/
	
	public void playSong(int songIndex) {
		try {
			mp.reset();
			mp.setDataSource(songsList.get(songIndex).get("songPath"));
			mp.prepare();
			mp.start();
			
			String songTitle = songsList.get(songIndex).get("songTitle");
			songTitleLabel.setText(songTitle);
			btnPlay.setImageResource(R.drawable.btn_pause);
			
			songProgressBar.setProgress(0);
			songProgressBar.setMax(100);
			updateProgressBar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);        
    }
	
	@Override
	public void onCompletion(MediaPlayer arg0) {
		// check for repeat is ON or OFF
		if (isRepeat) {
			// repeat is on play same song again
			playSong(currentSongIndex);
		} else if (isShuffle) {
			// shuffle is on - play a random song
			Random rand = new Random();
			currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
			playSong(currentSongIndex);
		} else {
			// no repeat or shuffle ON - play next song
			if (currentSongIndex < (songsList.size() - 1)) {
				playSong(currentSongIndex + 1);
				currentSongIndex = currentSongIndex + 1;
			} else {
				// play first song
				playSong(0);
				currentSongIndex = 0;
			}
		}
	}

	/**
	 * When user starts moving the progress handler
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = mp.getDuration();
		int currentPosition = Convert.progressToTimer(seekBar.getProgress(), totalDuration);
		
		// forward or backward to certain seconds
		mp.seekTo(currentPosition);
		
		// update timer progress again
		updateProgressBar();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {		
	}

}
