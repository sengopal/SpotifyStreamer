package com.example.android.spotifystreamer.app;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private static final String SHARE_HASHTAG = " #SunshineApp";
    public static final String PLAY_NEXT = "PLAY_NEXT";
    private static final String ACTION_PREV = "ACTION_PREV";
    private static final String ACTION_PAUSE = "ACTION_PAUSE";
    private static final String ACTION_NEXT = "ACTION_NEXT";

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

    private ShareActionProvider mShareActionProvider;

    private String mNotifAction;

    public PlaybackActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        inflater.inflate(R.menu.share_menu, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mCurrentTrack != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mCurrentTrack.getPreviewUrl());
        return shareIntent;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void actForNotification(String action) {
        if(null!=mNotifAction && mNotifAction.trim().length() > 0) {
            //Act on the notification
            if (ACTION_PAUSE.equals(mNotifAction)) {
                pause();
            } else if (ACTION_NEXT.equals(mNotifAction)) {
                if (mCurrentTrackId < mTracks.size()) {
                    playNextTrack();
                }
            } else if (ACTION_PREV.equals(mNotifAction)) {
                if (mCurrentTrackId > 0) {
                    playPreviousTrack();
                }
            }
            //Reset for next action
            mNotifAction = "";
        }
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
        if (mCurrentTrackId < 0) {
            //reset to the first track
            mCurrentTrackId = 0;
        }
        mCurrentTrack = mTracks.get(mCurrentTrackId);
        stop();
        playUsingService();
        createUpdateNotification();
    }

    private void playNextTrack() {
        mCurrentTrackId++;
        if (mCurrentTrackId >= mTracks.size()) {
            //reset to the first track
            mCurrentTrackId = 0;
        }
        mCurrentTrack = mTracks.get(mCurrentTrackId);
        stop();
        playUsingService();
        createUpdateNotification();
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

        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mAlbumImg = (ImageView) rootView.findViewById(R.id.trackImg);

        mAlbumTitle = (TextView) rootView.findViewById(R.id.albumTitle);
        mTrackTitle = (TextView) rootView.findViewById(R.id.trackTitle);
        mSeekLabelStart = (TextView) rootView.findViewById(R.id.seekLabelStart);

        Intent intent = getActivity().getIntent();
        mNotifAction = intent.getAction();

        if (null != savedInstanceState) {
            mTracks = savedInstanceState.getParcelableArrayList(TRACKS);
            mCurrentTrackId = savedInstanceState.getInt(TRACK_ID);
            if (!PlaybackService.isPlaying()) {
                playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
            }
        } else if (arguments != null && null != arguments.getParcelableArrayList(TRACKS)) {
            mNewSelection = arguments.getBoolean(PLAY_NEXT);
            mTracks = arguments.getParcelableArrayList(TRACKS);
            mCurrentTrackId = arguments.getInt(TRACK_ID);
        } else if (getActivity().getIntent() != null) {
            mTracks = intent.getParcelableArrayListExtra(TRACKS);
            mCurrentTrackId = intent.getIntExtra(TRACK_ID, -1);
            mNotifAction = intent.getAction();
        }
        mCurrentTrack = extractCurrentTrack(mCurrentTrackId);
        setupAlbumArt();

        Log.v(LOG_TAG, "Playing: " + mCurrentTrack.getTrack());
        Log.v(LOG_TAG, "Is Service playing: " + PlaybackService.isPlaying());

        Intent serviceIntent = new Intent(getActivity(), PlaybackService.class);
        getActivity().startService(serviceIntent);
        getActivity().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        createUpdateNotification();

        return rootView;
    }

    private void createUpdateNotification() {

        Intent resultIntent = new Intent(getActivity(), PlaybackActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
        stackBuilder.addParentStack(PlaybackActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        resultIntent.putParcelableArrayListExtra(TRACKS, mTracks);
        resultIntent.putExtra(TRACK_ID, mCurrentTrackId);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity());
        builder.setContentIntent(resultPendingIntent);

        int requestID = (int) System.currentTimeMillis();

        Intent prevIntent = new Intent(getActivity(), PlaybackActivity.class);
        prevIntent.putExtra(TRACK_ID, mCurrentTrackId);
        prevIntent.putParcelableArrayListExtra(TRACKS, mTracks);
        prevIntent.setAction(PlaybackActivityFragment.ACTION_PREV);
        //prevIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        prevIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent piPrevIntent = PendingIntent.getActivity(getActivity(), requestID, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        requestID = (int) System.currentTimeMillis();
        Intent pauseIntent = new Intent(getActivity(), PlaybackActivity.class);
        pauseIntent.putExtra(TRACK_ID, mCurrentTrackId);
        pauseIntent.putParcelableArrayListExtra(TRACKS, mTracks);
        pauseIntent.setAction(PlaybackActivityFragment.ACTION_PAUSE);
        //pauseIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        prevIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent piPauseIntent = PendingIntent.getActivity(getActivity(), requestID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        requestID = (int) System.currentTimeMillis();
        Intent nextIntent = new Intent(getActivity(), PlaybackActivity.class);
        nextIntent.putExtra(TRACK_ID, mCurrentTrackId);
        nextIntent.putParcelableArrayListExtra(TRACKS, mTracks);
        nextIntent.setAction(PlaybackActivityFragment.ACTION_NEXT);
        //nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        prevIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent piNextIntent = PendingIntent.getActivity(getActivity(), requestID, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        setupNotification(builder);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(mCurrentTrack.getArtist() + System.getProperty("line.separator") + mCurrentTrack.getAlbum()))
                .addAction(android.R.drawable.ic_media_previous,
                        getString(R.string.previous), piPrevIntent)
                .addAction(android.R.drawable.ic_media_pause,
                        getString(R.string.pause), piPauseIntent)
                .addAction(android.R.drawable.ic_media_next,
                        getString(R.string.next), piNextIntent);
        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, builder.build());
    }

    private void setupNotification(NotificationCompat.Builder builder) {
        builder.setSmallIcon(android.R.drawable.ic_media_play);
        builder.setContentText(mCurrentTrack.getArtist() + System.getProperty("line.separator") + mCurrentTrack.getAlbum());
        builder.setContentTitle(mCurrentTrack.getTrack());
    }

    private PlayTrack extractCurrentTrack(int trackId) {
        if (null != mTracks && trackId >= 0) {
            return mTracks.get(trackId);
        }
        return null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.v(LOG_TAG, "onServiceConnected");
        mServiceMessenger = new Messenger(service);

        actForNotification(mNotifAction);

        if (mNewSelection) {
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
            msg.replyTo = mReciever;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    private void setupAlbumArt() {
        //Update Album art
        if (null != mCurrentTrack.getAlbumImg()) {
            Picasso.with(getActivity()).load(mCurrentTrack.getAlbumImg()).into(mAlbumImg);
        }
        mAlbumTitle.setText(mCurrentTrack.getArtist() + System.getProperty("line.separator") + mCurrentTrack.getAlbum());
        mTrackTitle.setText(mCurrentTrack.getTrack());
    }

    private void stop() {
        try {
            Message msg = Message.obtain(null, PlaybackService.STOP_TRACK);
            msg.replyTo = mReciever;
            mServiceMessenger.send(msg);
            mSeekBar.setProgress(0);
            setSeekLabel(0);
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    private void pause() {
        try {
            Message msg = Message.obtain(null, PlaybackService.PAUSE_TRACK);
            msg.replyTo = mReciever;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    private void resume() {
        try {
            Message msg = Message.obtain(null, PlaybackService.PLAY_TRACK);
            msg.replyTo = mReciever;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    private void seekTo(int progress) {
        try {
            Message msg = Message.obtain(null, PlaybackService.SEEK_IN_TRACK);
            msg.replyTo = mReciever;
            msg.arg1 = progress;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
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
        outState.putInt(TRACK_ID, mCurrentTrackId);
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
        } else if (v == playPauseBtn) {
            if (PlaybackService.isPlaying()) {
                playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
                pause();
            } else {
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