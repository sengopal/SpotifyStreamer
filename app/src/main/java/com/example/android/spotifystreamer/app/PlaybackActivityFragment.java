package com.example.android.spotifystreamer.app;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlaybackActivityFragment extends DialogFragment implements ImageButton.OnClickListener, ServiceConnection, SeekBar.OnSeekBarChangeListener {
    public static final String TRACKS = "TRACKS";
    public static final String TRACK_ID = "TRACK_ID";

    private static final String LOG_TAG = PlaybackActivityFragment.class.getSimpleName();

    private PlayTrack mCurrentTrack;
    private ArrayList<PlayTrack> mTracks;
    private int mCurrentTrackId;

    private ServiceConnection mConnection = this;
    private Messenger mServiceMessenger = null;

    //Playback buttons
    private ImageButton prevBtn, nextBtn, playPauseBtn;
    private TextView mAlbumTitle, mTrackTitle, mSeekLabelStart;
    private SeekBar mSeekBar;
    private ImageView mAlbumImg;

    private final Messenger mReciever = new Messenger(new MessageRecieverHandler());
    private boolean mNewSelection;

    public PlaybackActivityFragment() {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private class MessageRecieverHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PlaybackService.UPDATE_PROGRESS:
                    mSeekBar.setProgress(msg.arg1);
                    setSeekLabel(msg.arg1);
                    break;
                case PlaybackService.TRACK_COMPLETED:
                    playNextTrack();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void setSeekLabel(int i) {
        mSeekLabelStart.setText("0:" + String.format("%02d", i / 1000));
    }

    private void playPreviousTrack() {
        mCurrentTrackId--;
        if(mCurrentTrackId<0){
            //reset to the first track
            mCurrentTrackId=0;
        }
        mCurrentTrack = mTracks.get(mCurrentTrackId);
        stop();
        playUsingService();
    }

    private void playNextTrack() {
        mCurrentTrackId++;
        if(mCurrentTrackId>=mTracks.size()){
            //reset to the first track
            mCurrentTrackId=0;
        }
        mCurrentTrack = mTracks.get(mCurrentTrackId);
        stop();
        playUsingService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback, container, false);
        Bundle arguments = getArguments();

        prevBtn = (ImageButton) rootView.findViewById(R.id.prevBtn);
        prevBtn.setOnClickListener(this);

        nextBtn = (ImageButton) rootView.findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(this);

        playPauseBtn = (ImageButton) rootView.findViewById(R.id.playPauseBtn);
        playPauseBtn.setOnClickListener(this);

        mSeekBar = (SeekBar)rootView.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mAlbumImg = (ImageView) rootView.findViewById(R.id.trackImg);

        mAlbumTitle = (TextView) rootView.findViewById(R.id.albumTitle);
        mTrackTitle = (TextView) rootView.findViewById(R.id.trackTitle);
        mSeekLabelStart = (TextView) rootView.findViewById(R.id.seekLabelStart);


        if(null!=savedInstanceState){
            mTracks = savedInstanceState.getParcelableArrayList(TRACKS);
            mCurrentTrackId = savedInstanceState.getInt(TRACK_ID);
            if(!PlaybackService.isPlaying()){
                playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
            }
        }else if (arguments != null) {
            mNewSelection = true;
            mTracks = arguments.getParcelableArrayList(TRACKS);
            mCurrentTrackId = arguments.getInt(TRACK_ID);
        }
        mCurrentTrack = extractCurrentTrack(mCurrentTrackId);
        setupAlbumArt();

        Log.v(LOG_TAG, "Playing: " + mCurrentTrack.getTrack());
        Log.v(LOG_TAG, "Is Service playing: " + PlaybackService.isPlaying());

        Intent intent = new Intent(getActivity(), PlaybackService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        return rootView;
    }

    private PlayTrack extractCurrentTrack(int trackId) {
        if(null!=mTracks && trackId >= 0){
            return mTracks.get(trackId);
        }
        return null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.v(LOG_TAG, "onServiceConnected");
        mServiceMessenger = new Messenger(service);
        if(mNewSelection){
            stop();
        }
        playUsingService();
    }

    private void playUsingService() {
        try {
            setupAlbumArt();

            Message msg = Message.obtain(null, PlaybackService.PLAY_TRACK);
            Bundle args = new Bundle();
            args.putString(PlaybackService.TRACK_URL, mCurrentTrack.getPreviewUrl());
            msg.setData(args);
            msg.replyTo=mReciever;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    private void setupAlbumArt() {
        //Update Album art
        if(null!=mCurrentTrack.getAlbumImg()) {
            Picasso.with(getActivity()).load(mCurrentTrack.getAlbumImg()).into(mAlbumImg);
        }
        mAlbumTitle.setText(mCurrentTrack.getArtist() + System.getProperty("line.separator") + mCurrentTrack.getAlbum());
        mTrackTitle.setText(mCurrentTrack.getTrack());
    }

    private void stop() {
        try {
            Message msg = Message.obtain(null, PlaybackService.STOP_TRACK);
            msg.replyTo=mReciever;
            mServiceMessenger.send(msg);
            mSeekBar.setProgress(0);
            setSeekLabel(0);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    private void pause() {
        try {
            Message msg = Message.obtain(null, PlaybackService.PAUSE_TRACK);
            msg.replyTo=mReciever;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    private void resume(){
        try {
            Message msg = Message.obtain(null, PlaybackService.PLAY_TRACK);
            msg.replyTo = mReciever;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    private void seekTo(int progress) {
        try {
            Message msg = Message.obtain(null, PlaybackService.SEEK_IN_TRACK);
            msg.replyTo=mReciever;
            msg.arg1=progress;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TRACKS, mTracks);
        outState.putInt(TRACK_ID,mCurrentTrackId);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v == prevBtn) {
            Log.v(LOG_TAG, "Previous: ");
            playPreviousTrack();
        } else if (v == nextBtn) {
            Log.v(LOG_TAG, "Next: ");
            playNextTrack();
        }else if(v == playPauseBtn){
            if(PlaybackService.isPlaying()){
                playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
                pause();
            }else{
                playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
                resume();
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceMessenger = null;
    }
}