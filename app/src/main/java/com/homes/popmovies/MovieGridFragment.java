package com.homes.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.jakewharton.rxbinding.widget.RxAdapterView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pcollections.TreePVector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class MovieGridFragment extends Fragment {

    private static final String LOG_TAG =
            MovieGridFragment.class.getSimpleName();

    private MoviePosterAdapter moviePosterAdapter;

    public static MovieGridFragment newInstance() {
        final MovieGridFragment fragment = new MovieGridFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public MovieGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        final View rootView = inflater.inflate(
                R.layout.fragment_movie_grid,
                container,
                false);

        final GridView gridView = (GridView)
                rootView.findViewById(R.id.gridview_movies);

        moviePosterAdapter = new MoviePosterAdapter(getActivity());

        gridView.setAdapter(moviePosterAdapter);

        RxAdapterView.itemClickEvents(gridView).subscribe(event -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);

            intent.putExtra(
                    DetailActivity.MOVIE_PARCEL,
                    moviePosterAdapter.getItem(event.position()));

            startActivity(intent);
        });

        return rootView;
    }

    private void updateMovies() {

        final SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(
                        getActivity());

        new FetchMoviesTask().execute(sharedPref.getString(
                getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_default)));
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private static final String BASE_URI =
                "http://api.themoviedb.org/3/discover/movie?sort_by=&api_key=";

        @Override
        protected Movie[] doInBackground(final String ... sortBy) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Movie[] movies;
            try {

                final URL url = new URL(
                        Uri.parse(BASE_URI)
                                .buildUpon()
                                .appendQueryParameter(
                                        "sort_by",
                                        sortBy[0])
                                .appendQueryParameter(
                                        "api_key",
                                        getString(R.string.tmdb_api_key))
                                .build()
                                .toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                final InputStream inputStream = urlConnection.getInputStream();
                final StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    throw new IOException("null input stream");
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                try {
                    movies = getMovieDataFromJson(buffer.toString());

                } catch (JSONException exception) {
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                    movies = null;
                }

            } catch (IOException exception) {
                Log.e(LOG_TAG, "Error ", exception);
                movies = null;

            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {

                    try {
                        reader.close();

                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return movies;
        }

        @Override
        protected void onPostExecute(final Movie[] movies) {
            moviePosterAdapter.replace(movies);
        }

        private Movie[] getMovieDataFromJson(final String jsonString)
                throws JSONException {

            final JSONObject movieJson = new JSONObject(jsonString);

            // Unused properties.
            //int page = movieJson.getInt("page");
            //int totalPages = movieJson.getInt("total_pages");
            //int totalResults = movieJson.getInt("total_results";

            final JSONArray resultsArray = movieJson.getJSONArray("results");
            final Movie[] movies = new Movie[resultsArray.length()];

            for (int i = 0; i < resultsArray.length(); i += 1) {
                movies[i] = new Movie(resultsArray.getJSONObject(i));
            }

            return movies;
        }
    }

    private class MoviePosterAdapter extends BaseAdapter {

        private static final String BASE_PATH =
                "http://image.tmdb.org/t/p/w500";

        private final Context context;
        private TreePVector<Movie> movies;

        public MoviePosterAdapter(final Context newContext) {
            context = newContext;
            movies = TreePVector.empty();
        }

        public void replace(final Movie[] newMovies) {
            movies = newMovies != null ?
                    TreePVector.from(Arrays.asList(newMovies)) :
                    TreePVector.<Movie>empty();
            notifyDataSetChanged();
        }

        public int getCount() {
            return movies.size();
        }

        public Movie getItem(final int position) {
            return movies.get(position);
        }

        public long getItemId(final int position) {
            return movies.get(position).id;
        }

        public View getView(
                final int position,
                final View convertView,
                final ViewGroup parent) {

            final ImageView view = convertView == null ?
                    new ImageView(context) :
                    (ImageView) convertView;

            view.setAdjustViewBounds(true);

            Picasso.with(context)
                    .load(BASE_PATH + getItem(position).posterPath)
                    .into(view);

            return view;
        }
    }
}
