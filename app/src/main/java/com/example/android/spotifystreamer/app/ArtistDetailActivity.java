package com.example.android.spotifystreamer.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


public class ArtistDetailActivity extends AppCompatActivity implements ArtistDetailActivityFragment.Callback, FragmentManager.OnBackStackChangedListener{

    private boolean mTwoPane = false;
    private static final String PLAYBACKFRAG_TAG = "PBTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(ArtistDetailActivityFragment.ARTIST_ID, getIntent().getStringExtra("ARTIST_ID"));

            ArtistDetailActivityFragment fragment = new ArtistDetailActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.detail_container, fragment).commit();
            getSupportFragmentManager().addOnBackStackChangedListener(this);
        }

        Intent intent = getIntent();
        String artistName = intent.getStringExtra("ARTIST_NAME");
        getSupportActionBar().setSubtitle(artistName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp(){
        //Enable Up button only  if there are entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void onItemSelected(List<Track> tracks, int position, boolean playNext) {
        CallbackUtil.onItemSelected(position, tracks, mTwoPane, getSupportFragmentManager(), playNext);
    }
}