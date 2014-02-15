package com.youzik.app.fragments;

import java.util.Timer;
import java.util.TimerTask;

import com.youzik.app.R;
import com.youzik.app.entities.Download;
import com.youzik.app.helpers.Convert;
import com.youzik.app.services.DownloadManagerService;
import com.youzik.app.services.MediaPlayerService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class PlayTabFragment extends Fragment {
	
	private String TAG = "PlayTabFragment";
	private static final int UPDATE_INTERVAL = 250;
	
	private ServiceConnection serviceConnection = new MediaPlayerServiceConnection();
	private MediaPlayerService mediaPlayerService;
    private Intent mediaPlayerIntent;
    
	private Timer waitForAudioPlayertimer = new Timer();
	private Handler handler = new Handler();
	private UpdateCurrentTrackTask updateCurrentTrackTask;
	
	private ImageButton btnPlay;
	private TextView trackNameLabel;
	private TextView trackCurrentDurationLabel;
	private TextView trackTotalDurationLabel;
	//private ImageButton btnForward;
	//private ImageButton btnBackward;
	//private ImageButton btnNext;
	//private ImageButton btnPrevious;
	//private ImageButton btnPlaylist;
	//private ImageButton btnRepeat;
	//private ImageButton btnShuffle;
	//private SeekBar songProgressBar;
	
	//private boolean isShuffle = false;
	//private boolean isRepeat = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		View playTabView = inflater.inflate(R.layout.play_tab, container, false);
		this.btnPlay = (ImageButton) playTabView.findViewById(R.id.btnPlay);
		this.trackNameLabel = (TextView) playTabView.findViewById(R.id.trackName);
		this.trackCurrentDurationLabel = (TextView) playTabView.findViewById(R.id.trackCurrentDurationLabel);
		this.trackTotalDurationLabel = (TextView) playTabView.findViewById(R.id.trackTotalDurationLabel);
		//this.btnForward = (ImageButton) playTabView.findViewById(R.id.btnForward);
		//this.btnBackward = (ImageButton) playTabView.findViewById(R.id.btnBackward);
		//this.btnNext = (ImageButton) playTabView.findViewById(R.id.btnNext);
		//this.btnPrevious = (ImageButton) playTabView.findViewById(R.id.btnPrevious);
		//this.btnPlaylist = (ImageButton) playTabView.findViewById(R.id.btnPlaylist);
		//this.btnRepeat = (ImageButton) playTabView.findViewById(R.id.btnRepeat);
		//this.btnShuffle = (ImageButton) playTabView.findViewById(R.id.btnShuffle);
		//this.songProgressBar = (SeekBar) playTabView.findViewById(R.id.songProgressBar);
		//this.songProgressBar.setOnSeekBarChangeListener(new TimeLineChangeListener());
		return playTabView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//this.songProgressBar.setOnSeekBarChangeListener(this);
		
		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mediaPlayerService.isPlaying())
					mediaPlayerService.pause();
				else
					mediaPlayerService.play();
				
				updatePlayPauseButtonState();
			}
		});
		
		/*btnForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mediaPlayer == null)
					return;
				
				int currentPosition = mediaPlayerService.getCurrentPosition();
				
				// check if seekForward time is lesser than song duration
				if (currentPosition + seekForwardTime <= mediaPlayerService.getDuration()) {
					// forward song
					mediaPlayer.seekTo(currentPosition + seekForwardTime);
				} else {
					// forward to end position
					mediaPlayer.seekTo(mediaPlayerService.getDuration());
				}
			}
		});
		
		btnBackward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mediaPlayer == null)
					return;
				
				int currentPosition = mediaPlayerService.getCurrentPosition();
				// check if seekBackward time is greater than 0 sec
				if (currentPosition - seekBackwardTime >= 0) {
					// forward song
					mediaPlayer.seekTo(currentPosition - seekBackwardTime);
				} else {
					// backward to starting position
					mediaPlayer.seekTo(0);
				}
			}
		});*/
		
		/*btnNext.setOnClickListener(new View.OnClickListener() {
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
		});*/
		
		/*btnRepeat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isRepeat) {
					isRepeat = false;
					Toast.makeText(getActivity().getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				} else {
					isRepeat = true;
					Toast.makeText(getActivity().getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
					//isShuffle = false;
					btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
					//btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}
			}
		});*/
		
		/*btnShuffle.setOnClickListener(new View.OnClickListener() {
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
		});*/
		
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

	/**
	 * Receiving song index from playlist view and play the song
	 */
	/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == 100) {
			currentSongIndex = data.getExtras().getInt("songIndex");
			playSong(currentSongIndex);
		}
	}*/
	
	@Override
    public void onResume() {
    	super.onResume();
    	
    	Log.v(TAG, "onResume() called: bind to MediaPlayerService and handle UpdateCurrentTrackTask");
    	mediaPlayerIntent = new Intent(this.getActivity(), MediaPlayerService.class);
		this.getActivity().bindService(mediaPlayerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		
		if (mediaPlayerService == null)
			scheduleRefreshTask();
		else
			updateCurrentlyPlaying();
    }
	
    @Override
    public void onPause() {
		Log.v(TAG, "onPause() called: unbind from MediaPlayerService and handle UpdateCurrentTrackTask");
		updateCurrentTrackTask.stop();
		updateCurrentTrackTask = null;
		this.getActivity().unbindService(serviceConnection);
		
		super.onPause();
    }
	
	public void playDownload(Download d) {
		Intent intent = new Intent(MediaPlayerService.ACTION_PLAY_TRACK);
		intent.putExtra(DownloadManagerService.DATA, d);
		this.getActivity().sendBroadcast(intent);
		
		trackNameLabel.setText(d.getName());
		btnPlay.setImageResource(R.drawable.btn_pause);
	}

	private void scheduleRefreshTask() {
		waitForAudioPlayertimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Log.d(TAG, "updateScreenAsync running timer");

				if (mediaPlayerService != null) {
					waitForAudioPlayertimer.cancel();
					
					handler.post(new Runnable() {
						@Override
						public void run() {
							updateCurrentlyPlaying();
						}
					});
				}
			}
		}, 10, UPDATE_INTERVAL); // delay, period
	}

	private void updateCurrentlyPlaying() {
		Download currentTrack = mediaPlayerService.getCurrentTrack();
		Log.d(TAG, "currentTrack: " + currentTrack);
		updatePlayPauseButtonState();

		if (updateCurrentTrackTask == null) {
			updateCurrentTrackTask = new UpdateCurrentTrackTask();
			updateCurrentTrackTask.execute();
		} else {
			Log.e(TAG, "updateCurrentTrackTask is not null");
		}
	}
	
	private void updatePlayPauseButtonState() {
		if (mediaPlayerService.isPlaying())
			btnPlay.setImageResource(R.drawable.btn_pause);
		else
			btnPlay.setImageResource(R.drawable.btn_play);
	}
	
	private void updatePlayPanel(final Download track) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentPosition = mediaPlayerService.getCurrentPosition();
                //songProgressBar.setMax(track.getDuration());
                //songProgressBar.setProgress(currentPosition);
                PlayTabFragment.this.trackCurrentDurationLabel.setText(Convert.milliSecondsToTimer(currentPosition));
            }
        });
    }
	
	private class UpdateCurrentTrackTask extends AsyncTask<Void, Download, Void> {

		public boolean stopped = false;
		public boolean paused = false;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Download currentTrack = mediaPlayerService.getCurrentTrack();
			
			if (currentTrack == null)
				return;
			
			if ("Unknown track".equals(trackNameLabel.getText()))
				trackNameLabel.setText(currentTrack.getName());
				
			trackTotalDurationLabel.setText("" +Convert.milliSecondsToTimer(mediaPlayerService.getDuration()));	
		}

		@Override
		protected Void doInBackground(Void... params) {
			while (!stopped) {
				if (!paused) {
					Download currentTrack = mediaPlayerService.getCurrentTrack();
					
					if (currentTrack != null)
						publishProgress(currentTrack);
				}
				
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Log.d(TAG, "UpdateCurrentTrackTask stopped");
			return null;
		}

		@Override
		protected void onProgressUpdate(Download... track) {
			if (stopped || paused) {
				return;
			}

			updatePlayPanel(track[0]);
		}

		public void stop() {
			stopped = true;
		}

		public void pause() {
			this.paused = true;
		}

		public void unPause() {
			this.paused = false;
		}
	}

	private final class MediaPlayerServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName className, IBinder baBinder) {
			Log.d(TAG, "MediaPlayerServiceConnection Service connected");
			mediaPlayerService = ((MediaPlayerService.MediaPlayerBinder) baBinder).getService();
			getActivity().startService(mediaPlayerIntent);
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "MediaPlayerServiceConnection Service disconnected");
			mediaPlayerService = null;
		}

	}

}
