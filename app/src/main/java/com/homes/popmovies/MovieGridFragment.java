package com.homes.popmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.homes.popmovies.data.MovieContract.*;
import com.jakewharton.rxbinding.widget.RxAdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class MovieGridFragment extends Fragment {
    private static final String LOG_TAG = MovieGridFragment.class.getSimpleName();

    private GridView mGridView;
    private Cursor mCursor;
    private BaseAdapter mMoviePosterAdapter;

    private final PublishSubject<Movie> mItemClickObservable =
        PublishSubject.<Movie>create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public Observable<Movie> getItemClickObservable() {
        return mItemClickObservable;
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

        mGridView = (GridView) rootView.findViewById(R.id.gridview_movies);

        RxAdapterView.itemClickEvents(mGridView).subscribe(adapterViewItemClickEvent -> {

            // Hacky way to get Movies from adapters.  Couldn't figure out a better way.
            // Maybe with using MatrixCursor I can have both adapters follow the cursor pattern.

            final int position = adapterViewItemClickEvent.position();

            if (mCursor != null) {

                if (mCursor.moveToPosition(position)) {
                    mItemClickObservable.onNext(new Movie(mCursor));
                }

            } else {
                mItemClickObservable
                    .onNext(((MoviePosterAdapter) mMoviePosterAdapter).getItem(position));
            }
        });

        return rootView;
    }

    private void updateMovies() {
        Log.e(LOG_TAG, "In updateMovies");

        final String sortBy = PreferenceManager.getDefaultSharedPreferences(getActivity())
            .getString(
                getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_default));

        (sortBy.equals(getString(R.string.sort_by_favorites)) ?
            getFavorites() :
            fetchMovies(sortBy).observeOn(AndroidSchedulers.mainThread()))
            .subscribe(mGridView::setAdapter);
    }

    private Observable<BaseAdapter> getFavorites() {
        final Context context = getContext();

        mCursor = context.getContentResolver().query(
            FavoriteEntry.buildFavoritesUri(),
            null,
            null,
            null,
            null);

        mMoviePosterAdapter = null;

        return Observable.just(new FavoritesAdapter(
            context,
            mCursor,
            0));
    }

    private Observable<BaseAdapter> fetchMovies(final String sortBy) {

        try {
            mCursor = null;

            Observable<BaseAdapter> o = Http.request(new URL(
                Uri.parse("http://api.themoviedb.org/3/discover/movie?sort_by=&api_key=")
                    .buildUpon()
                    .appendQueryParameter(
                        "sort_by",
                        sortBy)
                    .appendQueryParameter(
                        "api_key",
                        getString(R.string.tmdb_api_key))
                    .build()
                    .toString()))
                .map(MovieGridFragment::getMovieDataFromJson)
                .map(movies -> new MoviePosterAdapter(getContext(), movies));

            o.subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    mMoviePosterAdapter = s;
                });

            return o;

        } catch (Exception error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return Observable.empty();
        }
    }

    static private Movie[] getMovieDataFromJson(final String jsonString) {

        try {

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

        } catch (JSONException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return null;
        }
    }
}
