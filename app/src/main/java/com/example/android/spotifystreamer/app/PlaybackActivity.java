package com.example.android.spotifystreamer.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class PlaybackActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    private static final String PLAY_FRAG_TAG = "PFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        if (savedInstanceState == null) {
            checkAndAddNewFragment();
        }
        String action = getIntent().getAction();
        shouldDisplayHomeUp((null!=action && action.trim().length() > 0));
    }

    private void checkAndAddNewFragment() {
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(PlaybackActivityFragment.TRACKS, getIntent().getParcelableArrayListExtra(PlaybackActivityFragment.TRACKS));
        arguments.putInt(PlaybackActivityFragment.TRACK_ID, getIntent().getIntExtra(PlaybackActivityFragment.TRACK_ID, -1));

        PlaybackActivityFragment fragment = (PlaybackActivityFragment) getSupportFragmentManager().findFragmentByTag(PLAY_FRAG_TAG);
        if(fragment==null) {
            fragment = new PlaybackActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.playback_container, fragment, PLAY_FRAG_TAG).addToBackStack("playback").commit();
            getSupportFragmentManager().addOnBackStackChangedListener(this);
        }else{
            fragment.actForNotification(getIntent().getAction());
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
    public void onBackStackChanged() {
        shouldDisplayHomeUp(false);
    }

    public void shouldDisplayHomeUp(boolean notifBased){
        //Enable Up button only  if there are entries in the back stack
        boolean canback = !notifBased && getSupportFragmentManager().getBackStackEntryCount()>0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkAndAddNewFragment();
        super.onNewIntent(intent);
    }
}
