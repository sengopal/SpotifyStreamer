package com.example.android.spotifystreamer.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

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
    private String mSearchText;
    public MainActivityFragment() {
    }

    public interface Callback {
        public void onItemSelected(ArtistResult artistResult);
    }

    //Based on a stackoverflow snippet
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
                    mSearchText = v.getText().toString();
                    Log.d(LOG_TAG, "Search Text: "+ mSearchText);
                    if(isNetworkAvailable()) {
                        FetchArtists task = new FetchArtists();
                        task.execute(mSearchText);
                    }else{
                        Toast.makeText(getActivity(),"Please check your Internet connectivity and try again.", Toast.LENGTH_SHORT).show();
                    }
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
                ArtistResult artistResult = listViewAdapter.getItem(position);
                ((Callback) getActivity()).onItemSelected(artistResult);
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("SEARCH_TEXT", mSearchText);
        if(null!=listViewAdapter && null!=listViewAdapter.getArtistResultsForSave()) {
            outState.putParcelableArrayList("SEARCH_RESULTS", listViewAdapter.getArtistResultsForSave());
        }
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
            if (artists != null && !artists.isEmpty()) {
                listViewAdapter.clear();
                listViewAdapter.addAll(artists);
            }else{
                Toast.makeText(getActivity(),"Artist matching the text not found. Please refine your search", Toast.LENGTH_SHORT).show();
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
