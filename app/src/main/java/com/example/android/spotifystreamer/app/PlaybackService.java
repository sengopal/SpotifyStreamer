package com.example.android.spotifystreamer.app;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sengopal on 7/30/15.
 */
public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    public static final int PLAY_TRACK = 1;
    public static final int PAUSE_TRACK = 2;
    public static final int SEEK_IN_TRACK = 3;
    public static final int STOP_TRACK = 4;

    public static final String TRACK_URL = "TRACK_URL";
    private static final String LOG_TAG = PlaybackService.class.getSimpleName();
    public static final int UPDATE_PROGRESS = 1;
    public static final int TRACK_COMPLETED = 2;

    MediaPlayer mMediaPlayer = null;

    private Messenger mClient = null;
    private final Messenger mMessenger = new Messenger(new PlaybackServiceHandler());
    private static boolean isRunning;
    private MediaObserver observer;

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        if(null!=mMediaPlayer) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
        /*
        String url = intent.getStringExtra(TRACK_URL);
        play(url);
        return super.onStartCommand(intent,flags,startId);
        */
    }

    private void play(String url) {
        try {
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
        observer = new MediaObserver();
        new Thread(observer).start();
        isRunning = true;
        Log.v(LOG_TAG, "Starting to play: ");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(null!=mMediaPlayer) {
            mMediaPlayer.reset();
        }
        return true;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        stopPlayback();
        try {
            Message msg = Message.obtain(null, PlaybackService.TRACK_COMPLETED);
            mClient.send(msg);
        }catch(RemoteException e){

        }
    }

    private void stopPlayback() {
        mMediaPlayer.stop();
        if(null!= observer) {
            observer.stop();
        }
        isRunning = false;
    }

    private class PlaybackServiceHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOG_TAG, "handleMessage: " + msg.what);
            if(null!=msg.replyTo) {
                mClient = msg.replyTo;
            }
            switch (msg.what) {
                case PLAY_TRACK:
                    if(isRunning){
                        mMediaPlayer.start();
                    }else{
                        play(msg.getData().getString(PlaybackService.TRACK_URL));
                    }
                    break;
                case PAUSE_TRACK:
                    mMediaPlayer.pause();
                    break;
                case SEEK_IN_TRACK:
                    mMediaPlayer.seekTo(msg.arg1);
                    break;
                case STOP_TRACK:
                    stopPlayback();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private class MediaObserver implements Runnable {
        private AtomicBoolean stop = new AtomicBoolean(false);

        public void stop() {
            stop.set(true);
        }

        @Override
        public void run() {
            while (!stop.get()) {
                try {
                    Message msg = Message.obtain(null, PlaybackService.UPDATE_PROGRESS);
                    msg.arg1=mMediaPlayer.getCurrentPosition();
                    //Log.v(LOG_TAG," Update Seekbar: "+ msg.arg1);
                    mClient.send(msg);
                    Thread.sleep(200);
                } catch (Exception e) {
                    Log.v(LOG_TAG, "Exception");
                }
            }
        }
    }


}
