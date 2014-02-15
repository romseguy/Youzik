package com.youzik.app.fragments;

import java.util.Timer;
import java.util.TimerTask;

import com.youzik.app.R;
import com.youzik.app.entities.Download;
import com.youzik.app.helpers.Convert;
import com.youzik.app.services.DownloadManagerService;
import com.youzik.app.services.MediaPlayerService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.SeekBar;
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
    private SeekBar trackProgressBar;

    // private ImageButton btnForward;
    // private ImageButton btnBackward;
    // private ImageButton btnNext;
    // private ImageButton btnPrevious;
    // private ImageButton btnPlaylist;
    // private ImageButton btnRepeat;
    // private ImageButton btnShuffle;

    // private boolean isShuffle = false;
    // private boolean isRepeat = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View playTabView = inflater.inflate(R.layout.play_tab, container, false);
        this.btnPlay = (ImageButton) playTabView.findViewById(R.id.btnPlay);
        this.trackNameLabel = (TextView) playTabView.findViewById(R.id.trackName);
        this.trackCurrentDurationLabel = (TextView) playTabView.findViewById(R.id.trackCurrentDurationLabel);
        this.trackTotalDurationLabel = (TextView) playTabView.findViewById(R.id.trackTotalDurationLabel);
        this.trackProgressBar = (SeekBar) playTabView.findViewById(R.id.trackProgressBar);
        // this.btnForward = (ImageButton) playTabView.findViewById(R.id.btnForward);
        // this.btnBackward = (ImageButton) playTabView.findViewById(R.id.btnBackward);
        // this.btnNext = (ImageButton) playTabView.findViewById(R.id.btnNext);
        // this.btnPrevious = (ImageButton) playTabView.findViewById(R.id.btnPrevious);
        // this.btnPlaylist = (ImageButton) playTabView.findViewById(R.id.btnPlaylist);
        // this.btnRepeat = (ImageButton) playTabView.findViewById(R.id.btnRepeat);
        // this.btnShuffle = (ImageButton) playTabView.findViewById(R.id.btnShuffle);
        return playTabView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.trackProgressBar.setOnSeekBarChangeListener(new ProgressBarChangeListener());

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
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.v(TAG, "onResume() called: bind to MediaPlayerService and handle UpdateCurrentTrackTask");
        mediaPlayerIntent = new Intent(this.getActivity(), MediaPlayerService.class);
        this.getActivity().bindService(mediaPlayerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayerService.ACTION_PLAY_COMPLETED);
        intentFilter.addAction(MediaPlayerService.ACTION_PLAY_STARTED);
        this.getActivity().registerReceiver(this.playReceiver, intentFilter);

        if (mediaPlayerService == null)
            scheduleRefreshTask();
        else
            updateCurrentTrack();
    }

    @Override
    public void onPause() {
        Log.v(TAG, "onPause() called: unbind from MediaPlayerService and handle UpdateCurrentTrackTask");
        updateCurrentTrackTask.stop();
        updateCurrentTrackTask = null;

        this.getActivity().unregisterReceiver(this.playReceiver);
        this.getActivity().unbindService(serviceConnection);

        super.onPause();
    }

    public void playDownload(Download d) {
        Intent intent = new Intent(MediaPlayerService.ACTION_PLAY_TRACK);
        intent.putExtra(DownloadManagerService.DATA, d);
        this.getActivity().sendBroadcast(intent);
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
                            updateCurrentTrack();
                        }
                    });
                }
            }
        }, 10, UPDATE_INTERVAL); // delay, period
    }

    private void updateCurrentTrack() {
        Download currentTrack = mediaPlayerService.getCurrentTrack();
        Log.d(TAG, "currentTrack: " + currentTrack);
        updatePlayPauseButtonState();

        if (updateCurrentTrackTask == null) {
            updateCurrentTrackTask = new UpdateCurrentTrackTask();
            updateCurrentTrackTask.execute();
        }
        else {
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
                trackProgressBar.setMax(mediaPlayerService.getDuration());
                trackProgressBar.setProgress(currentPosition);
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

            trackTotalDurationLabel.setText("" + Convert.milliSecondsToTimer(mediaPlayerService.getDuration()));
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
                }
                catch (InterruptedException e) {
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

        public void setPaused(Boolean paused) {
            this.paused = paused;
        }
    }

    private class ProgressBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private Timer delayedSeekTimer;

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;

            Log.d(TAG, "ProgressBarChangeListener progress received from user: " + progress);
            scheduleSeek(progress);
        }

        private void scheduleSeek(final int progress) {
            if (delayedSeekTimer != null) {
                delayedSeekTimer.cancel();
            }

            delayedSeekTimer = new Timer();
            delayedSeekTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Log.d(TAG, "Delayed Seek Timer run");
                    mediaPlayerService.seek(progress);
                    updatePlayPanel(mediaPlayerService.getCurrentTrack());
                }
            }, 170);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "ProgressBarChangeListener started tracking touch");
            updateCurrentTrackTask.setPaused(true);
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "ProgressBarChangeListener stopped tracking touch");
            updateCurrentTrackTask.setPaused(false);
        }

    }

    private BroadcastReceiver playReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == MediaPlayerService.ACTION_PLAY_STARTED) {
                final Download d = (Download) intent.getParcelableExtra(DownloadManagerService.DATA);
                trackNameLabel.setText(d.getName());
                btnPlay.setImageResource(R.drawable.btn_pause);
                trackProgressBar.setProgress(0);
                trackProgressBar.setMax(mediaPlayerService.getDuration());
            }
            else if (intent.getAction() == MediaPlayerService.ACTION_PLAY_COMPLETED) {
                btnPlay.setImageResource(R.drawable.btn_play);
            }
        }
    };

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
