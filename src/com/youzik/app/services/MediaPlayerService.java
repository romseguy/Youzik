package com.youzik.app.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.youzik.app.entities.Download;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MediaPlayerService extends Service implements OnCompletionListener {

    public static final String INTENT_BASE_NAME      = "com.youzik.app.MediaPlayerService";
    public static final String ACTION_QUEUE_TRACK    = INTENT_BASE_NAME + ".ACTION_QUEUE_TRACK";
    public static final String ACTION_PLAY_TRACK     = INTENT_BASE_NAME + ".ACTION_PLAY_TRACK";
    public static final String ACTION_PLAY_COMPLETED = INTENT_BASE_NAME + ".ACTION_PLAY_COMPLETED";
    public static final String ACTION_PLAY_STARTED   = INTENT_BASE_NAME + ".ACTION_PLAY_STARTED";

    private List<Download>     queuedTracks          = new ArrayList<Download>();
    private MediaPlayer        mediaPlayer;
    private boolean            paused                = false;

    private final IBinder      mediaPlayerBinder     = new MediaPlayerBinder();

    public class MediaPlayerBinder extends Binder {

        public MediaPlayerService getService() {
            Log.v("MediaPlayerService", "MediaPlayerBinder getService() called");
            return MediaPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v("MediaPlayerService", "onBind() called");
        return this.mediaPlayerBinder;
    }

    private BroadcastReceiver queueTrackReceiver = new BroadcastReceiver() {

                                                     @Override
                                                     public void onReceive(Context context, Intent intent) {
                                                         Log.v("MediaPlayerService", "QUEUE_TRACK received");
                                                         queuedTracks.add((Download) intent.getParcelableExtra(DownloadManagerService.DATA));
                                                     }
                                                 };

    private BroadcastReceiver playTrackReceiver  = new BroadcastReceiver() {

                                                     @Override
                                                     public void onReceive(Context context, Intent intent) {
                                                         Log.v("MediaPlayerService", "PLAY_TRACK received");
                                                         Download d = (Download) intent.getParcelableExtra(DownloadManagerService.DATA);
                                                         stop();
                                                         queuedTracks.clear();
                                                         queuedTracks.add(d);
                                                         play();
                                                     }
                                                 };

    @Override
    public void onCreate() {
        Log.v("MediaPlayerService", "onCreate() called");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_QUEUE_TRACK);
        this.registerReceiver(queueTrackReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY_TRACK);
        this.registerReceiver(playTrackReceiver, intentFilter);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i("MediaPlayerService", "onStart() called, instance=" + this.hashCode());
    }

    @Override
    public void onDestroy() {
        Log.i("MediaPlayerService", "onDestroy() called");
        this.release();
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        this.release();

        Intent intent = new Intent();
        intent.setAction(ACTION_PLAY_COMPLETED);
        sendBroadcast(intent);
    }

    private void release() {
        if (this.mediaPlayer == null) {
            return;
        }

        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.stop();
        }

        this.mediaPlayer.release();
        this.mediaPlayer = null;
    }

    public boolean isPlaying() {
        if (this.queuedTracks.isEmpty() || this.mediaPlayer == null) {
            return false;
        }
        return this.mediaPlayer.isPlaying();
    }

    public void play() {
        if (this.queuedTracks.size() == 0)
            return;

        Download d = queuedTracks.get(0);

        // the media player is already instanciated and paused so we can play
        // the track right away
        if (this.mediaPlayer != null && this.paused) {
            this.mediaPlayer.start();
            this.paused = false;
            return;
        }
        else if (mediaPlayer != null) {
            this.release();
        }

        try {
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setDataSource(d.getUrl());
            this.mediaPlayer.prepare();
            this.mediaPlayer.start();
            this.mediaPlayer.setOnCompletionListener(this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction(ACTION_PLAY_STARTED);
        intent.putExtra(DownloadManagerService.DATA, d);
        sendBroadcast(intent);
    }

    public void seek(int timeInMillis) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(timeInMillis);
        }
    }

    public void stop() {
        this.release();
    }

    public void pause() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.pause();
            this.paused = true;
        }
    }

    public Download getCurrentTrack() {
        if (queuedTracks.isEmpty())
            return null;

        return queuedTracks.get(0);
    }

    public int getCurrentPosition() {
        if (this.mediaPlayer == null)
            return 0;

        return this.mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        if (this.mediaPlayer == null)
            return 0;

        return this.mediaPlayer.getDuration();
    }

}
