package com.example.android.spotifystreamer.app;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlaybackActivityFragment extends DialogFragment {
    public static final String TRACK = "TRACK";
    private static final String LOG_TAG = PlaybackActivityFragment.class.getSimpleName();
    private PlayTrack mTrack;

    public PlaybackActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback, container, false);
        Bundle arguments = getArguments();
        if(null!=savedInstanceState){
            mTrack = savedInstanceState.getParcelable(TRACK);
        }else if (arguments != null) {
            mTrack = arguments.getParcelable(TRACK);
        }
        Log.v(LOG_TAG, "Playing: " + mTrack.getTrack());
        return rootView;
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
        outState.putParcelable(TRACK, mTrack);
        super.onSaveInstanceState(outState);
    }
}