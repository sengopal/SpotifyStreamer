package com.example.android.spotifystreamer.app;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class PlaybackActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(PlaybackActivityFragment.TRACKS, getIntent().getParcelableExtra(PlaybackActivityFragment.TRACKS));
            arguments.putParcelable(PlaybackActivityFragment.TRACK_ID, getIntent().getParcelableExtra(PlaybackActivityFragment.TRACK_ID));

            PlaybackActivityFragment fragment = new PlaybackActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.playback_container, fragment).addToBackStack(null).commit();
            getSupportFragmentManager().addOnBackStackChangedListener(this);
        }

        shouldDisplayHomeUp();
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
}
