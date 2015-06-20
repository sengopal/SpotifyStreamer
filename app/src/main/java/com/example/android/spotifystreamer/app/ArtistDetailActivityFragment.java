package com.example.android.spotifystreamer.app;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

    TopTracksAdapter listViewAdapter;

    public ArtistDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        listViewAdapter = new TopTracksAdapter(getActivity(), new ArrayList<Track>());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_tracks);
        listView.setAdapter(listViewAdapter);

        Intent intent = getActivity().getIntent();
        String artistId = intent.getStringExtra("ARTIST_ID");
        FetchTopTracks task = new FetchTopTracks();
        task.execute(artistId);

        return rootView;
    }


    public class TopTracksAdapter extends ArrayAdapter<Track> {
        public TopTracksAdapter(Context context, ArrayList<Track> tracks) {
            super(context, 0, tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Track track = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.tracks_results, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.list_item_textview);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.imageView);

            textView.setText(track.name);
            if(null!=track.album.images && !track.album.images.isEmpty() && null!=track.album.images.get(0) && null!=track.album.images.get(0).url) {
                Picasso.with(getContext()).load(track.album.images.get(0).url).into(imgView);
            }
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
            if (tracks != null) {
                listViewAdapter.clear();
                listViewAdapter.addAll(tracks);
            }
        }

        private List<Track> getSearchResults(String[] searchText) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            HashMap<String,Object> queryMap = new HashMap<>();
            queryMap.put("country","US");
            Tracks tracks = spotify.getArtistTopTrack(searchText[0], queryMap);
            return tracks.tracks;
        }
    }
}
