package com.example.android.spotifystreamer.app;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by sengopal on 7/30/15.
 */
public class CallbackUtil {
    public static void onItemSelected(int selectedTrack, List<Track> tracks, boolean isTwoPane, FragmentManager fragManager) {
        PlaybackActivityFragment fragment = new PlaybackActivityFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(PlaybackActivityFragment.TRACKS, convertTo(tracks));
        args.putInt(PlaybackActivityFragment.TRACK_ID, selectedTrack);
        fragment.setArguments(args);
        if (isTwoPane) {
            //getSupportFragmentManager().beginTransaction().replace(R.id.tracks_detail_container, fragment, PLAYBACKFRAG_TAG).addToBackStack(null).commit();
            fragment.show(fragManager, "dialog");
        } else {
            /*
            Intent intent = new Intent(this, PlaybackActivity.class);
            intent.putExtra(PlaybackActivityFragment.TRACK, playTrack);
            startActivity(intent);
            */
            FragmentTransaction fragmentTransaction = fragManager.beginTransaction();
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.add(R.id.detail_container, fragment).addToBackStack(null).commit();
        }
    }

    private static ArrayList<? extends Parcelable> convertTo(List<Track> tracks) {
        ArrayList<PlayTrack> list = new ArrayList<>();
        for(Track track : tracks){
            list.add(new PlayTrack(track));
        }
        return list;
    }
}
