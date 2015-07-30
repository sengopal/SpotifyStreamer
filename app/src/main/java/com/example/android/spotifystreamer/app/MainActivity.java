package com.example.android.spotifystreamer.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import kaaes.spotify.webapi.android.models.Track;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback, ArtistDetailActivityFragment.Callback{

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String RESULTSFRAG_TAG = "RESTAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.tracks_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.tracks_detail_container, new MainActivityFragment(), RESULTSFRAG_TAG).commit();
            }
        }else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemSelected(ArtistResult artist) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(ArtistDetailActivityFragment.ARTIST_ID, artist.getId());

            ArtistDetailActivityFragment fragment = new ArtistDetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tracks_detail_container, fragment, RESULTSFRAG_TAG).addToBackStack(null)
                    .commit();
        } else {
            Intent intent = new Intent(this, ArtistDetailActivity.class);
            intent.putExtra("ARTIST_ID", artist.getId());
            intent.putExtra("ARTIST_NAME", artist.getName());
            startActivity(intent);
        }
    }


    @Override
    public void onItemSelected(Track track) {
        PlaybackActivityFragment fragment = new PlaybackActivityFragment();
        PlayTrack playTrack = new PlayTrack(track);
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(PlaybackActivityFragment.TRACK, playTrack);
            fragment.setArguments(args);

            //getSupportFragmentManager().beginTransaction().replace(R.id.tracks_detail_container, fragment, PLAYBACKFRAG_TAG).addToBackStack(null).commit();
            fragment.show(getSupportFragmentManager(), "dialog");
        } else {
            Intent intent = new Intent(this, PlaybackActivity.class);
            intent.putExtra(PlaybackActivityFragment.TRACK, playTrack);
            startActivity(intent);
        }
    }
}
