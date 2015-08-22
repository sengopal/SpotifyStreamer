package com.example.android.spotifystreamer.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistDetailActivityFragment extends Fragment {
    private final String LOG_TAG = ArtistDetailActivityFragment.class.getSimpleName();
    public static final String ARTIST_ID = "ARTIST_ID";
    TopTracksAdapter listViewAdapter;
    private String mArtistId;
    private List<Track> mCurrentTracks;
    private int mCurrentPosition;


    public ArtistDetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    public interface Callback {
        public void onItemSelected(List<Track> tracks, int position, boolean playNext);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_artist_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_now_playing:
                openPlayer();
                break;
            case R.id.action_settings:
                //openSettings();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void openPlayer() {
        ((Callback) getActivity()).onItemSelected(mCurrentTracks, mCurrentPosition, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        listViewAdapter = new TopTracksAdapter(getActivity(), new ArrayList<Track>());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_tracks);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Track track = listViewAdapter.getItem(position);
                Log.v(LOG_TAG, "Track Selected: " + track.name);
                mCurrentPosition = position;
                ((Callback) getActivity()).onItemSelected(mCurrentTracks, position, true);
            }
        });

        if (null != savedInstanceState) {
            mArtistId = savedInstanceState.getString(ARTIST_ID);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mArtistId = arguments.getString(ARTIST_ID);
            }
        }
        FetchTopTracks task = new FetchTopTracks();
        task.execute(mArtistId);
        return rootView;
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ARTIST_ID, mArtistId);
        super.onSaveInstanceState(outState);
    }

    public class TopTracksAdapter extends ArrayAdapter<Track> {

        public TopTracksAdapter(Context context, ArrayList<Track> tracks) {
            super(context, 0, tracks);
            mCurrentTracks = tracks;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Track track = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.tracks_results, parent, false);
            }
            TextView trackTextView = (TextView) convertView.findViewById(R.id.list_item_track_textview);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.imageView);

            trackTextView.setText(track.name + System.getProperty("line.separator") + track.album.name);

            if (null != track.album.images && !track.album.images.isEmpty() && null != track.album.images.get(0) && null != track.album.images.get(0).url) {
                Picasso.with(getContext()).load(track.album.images.get(0).url).into(imgView);
            }
            imgView.setContentDescription("TO BE DONE FOR accessibility");
            return convertView;
        }
    }

    public class FetchTopTracks extends AsyncTask<String, Void, List<Track>> {

        public final String LOG_TAG = FetchTopTracks.class.getSimpleName();

        @Override
        protected List<Track> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            return getSearchResults(params);
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if (tracks != null && !tracks.isEmpty()) {
                listViewAdapter.clear();
                listViewAdapter.addAll(tracks);
                mCurrentTracks = tracks;
            } else {
                Toast.makeText(getActivity(), "Top 10 songs not available for this artist. Please try a different one", Toast.LENGTH_SHORT).show();
            }
        }

        private List<Track> getSearchResults(String[] searchText) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            HashMap<String, Object> queryMap = new HashMap<>();
            queryMap.put("country", "US");
            Tracks tracks = spotify.getArtistTopTrack(searchText[0], queryMap);
            return tracks.tracks;
        }
    }
}