package com.example.android.spotifystreamer.app;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private ArtistsListsAdapter listViewAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        EditText searchText = (EditText) rootView.findViewById(R.id.search_text);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(LOG_TAG, "Editor Action called");
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String searchText = v.getText().toString();
                    Log.d(LOG_TAG, "Search Text: "+ searchText);
                    FetchArtists task = new FetchArtists();
                    task.execute(searchText);
                }
                return false;
            }
        });

        listViewAdapter = new ArtistsListsAdapter(getActivity(), new ArrayList<ArtistResult>());

        if(savedInstanceState!=null){
            searchText.setText(savedInstanceState.getString("SEARCH_TEXT"));
            ArrayList<ArtistResult> searchResults = savedInstanceState.getParcelableArrayList("SEARCH_RESULTS");
            listViewAdapter.addAll(searchResults);
        }

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forcast);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArtistResult artist = listViewAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
                intent.putExtra("ARTIST_ID", artist.getId());
                intent.putExtra("ARTIST_NAME", artist.getName());
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        EditText searchText = (EditText) getView().findViewById(R.id.search_text);
        outState.putString("SEARCH_TEXT", searchText.getText().toString());
        outState.putParcelableArrayList("SEARCH_RESULTS",listViewAdapter.getArtistResultsForSave());
    }

    public class ArtistsListsAdapter extends ArrayAdapter<ArtistResult>{
        private ArrayList<ArtistResult> artistResults;
        public ArtistsListsAdapter(Context context, ArrayList<ArtistResult> artists) {
            super(context, 0, artists);
            this.artistResults = artists;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ArtistResult artist = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_results, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.list_item_textview);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.imageView);

            textView.setText(artist.getName());
            String imageUrl = artist.getAlbumUrl();
            if(null!=imageUrl){
                Picasso.with(getContext()).load(imageUrl).into(imgView);
            }
            return convertView;
        }

        public ArrayList<ArtistResult> getArtistResultsForSave() {
            return artistResults;
        }
    }


    public class FetchArtists extends AsyncTask<String, Void, List<ArtistResult>> {

        public final String LOG_TAG = FetchArtists.class.getSimpleName();

        @Override
        protected List<ArtistResult> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            return getSearchResults(params);
        }

        @Override
        protected void onPostExecute(List<ArtistResult> artists) {
            if (artists != null) {
                listViewAdapter.clear();
                listViewAdapter.addAll(artists);
            }
        }

        private List<ArtistResult> getSearchResults(String[] searchText) {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artistsPager = spotify.searchArtists(searchText[0]);

            List<String> results = new ArrayList<>();
            return convertToArtistResults(artistsPager.artists.items);
        }

        private ArrayList<ArtistResult> convertToArtistResults(List<Artist> artists) {
            ArrayList<ArtistResult> artistResults = new ArrayList<>();

            for(Artist artist : artists){
                ArtistResult artistResult = new ArtistResult(artist.id, artist.name, getImageUrl(artist));
                artistResults.add(artistResult);
            }
            return artistResults;
        }

        private String getImageUrl(Artist artist) {
            if(null!=artist.images && !artist.images.isEmpty() && null!=artist.images.get(0) && null!=artist.images.get(0).url) {
                return artist.images.get(0).url;
            }
            return null;
        }
    }
}
