package com.homes.popmovies.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.homes.popmovies.utilities.Http;
import com.homes.popmovies.dtobjs.Movie;
import com.homes.popmovies.MovieAdapter;
import com.homes.popmovies.R;
import com.homes.popmovies.utilities.Transform;
import com.homes.popmovies.data.MovieContract.MovieEntry;
import com.homes.popmovies.data.MovieContract.FavoriteEntry;
import com.jakewharton.rxbinding.widget.RxAdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pcollections.TreePVector;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class MovieGridFragment extends Fragment {
    static private final String LOG_TAG = MovieGridFragment.class.getSimpleName();

    static private TreePVector<Movie> getMovieDataFromJson(final String jsonString) {

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

            return TreePVector.from(Arrays.asList(movies));

        } catch (JSONException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return null;
        }
    }

    private final BehaviorSubject<GridView> mGridView = BehaviorSubject.<GridView>create();
    private final BehaviorSubject<Movie> mItemClickObservable = BehaviorSubject.<Movie>create();
    private final BehaviorSubject<String> mSortByObservable = BehaviorSubject.<String>create();

    private Subscription mAdapterSubscription = null;
    private Subscription mItemClickSubscription = null;

    private void checkSortByPref() {
        mSortByObservable.onNext(getSortBy());
    }

    private String getSortBy() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
            .getString(
                getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_default));
    }

    private boolean isConnected() {
        final NetworkInfo networkInfo =
            ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    private Cursor sortByToCursor(final String sortBy) {

        // Favorite movies

        if (sortBy.equalsIgnoreCase(getString(R.string.sort_by_favorites))) {

            return getContext().getContentResolver().query(
                FavoriteEntry.buildFavoritesUri(),
                null,
                null,
                null,
                null);
        }

        // Performs synchronous fetch of movies and saves them in content provider

        if (isConnected()) {

            // TODO: only fetch when time has elapsed since last successful fetch

            final TreePVector<ContentValues> contentValues = fetchMovies(sortBy);
            final ContentValues[] array = new ContentValues[contentValues.size()];
            contentValues.toArray(array);

            getContext().getContentResolver().bulkInsert(MovieEntry.buildMoviesUri(), array);
        }

        // Return movies sorted from content provider

        return getContext().getContentResolver().query(
            MovieEntry.buildMoviesUri(),
            null,//new String[]{ MovieEntry.COLUMN_POSTER_PATH },
            null,
            null,
            sortBy);
    }

    private MovieAdapter cursorToAdapter(final Cursor cursor) {
        return new MovieAdapter(getContext(), cursor, 0);
    }

    private TreePVector<ContentValues> fetchMovies(final String sortBy) {
        final String BASE_URI = "http://api.themoviedb.org/3/discover/movie?sort_by=&api_key=";

        try {

            return Transform.map(getMovieDataFromJson(Http.request(new URL(Uri.parse(BASE_URI)
                    .buildUpon()
                    .appendQueryParameter(
                        "sort_by",
                        sortBy.replace(' ', '.'))
                    .appendQueryParameter(
                        "api_key",
                        getContext().getString(R.string.tmdb_api_key))
                    .build()
                    .toString()))),
                Movie::toContentValues);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return TreePVector.empty();
    }

    private void setAdapterSubscription(final Subscription adapterSubscription) {

        if (mAdapterSubscription != null) {
            mAdapterSubscription.unsubscribe();
        }

        mAdapterSubscription = adapterSubscription;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only changes adapter when sort by preference has changed or mGridView has changed

        setAdapterSubscription(Observable.combineLatest(
            mGridView,
            mSortByObservable.distinctUntilChanged()
                // sortByToCursor may perform network io and will query ContentProvider
                .observeOn(Schedulers.io())
                .map(this::sortByToCursor)
                .map(this::cursorToAdapter),
            (Func2<GridView, MovieAdapter, Pair<GridView, MovieAdapter>>) Pair::new)
            // Update UI on main thread
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(pair -> {
                final GridView gridView = pair.first;
                final MovieAdapter adapter = pair.second;
                gridView.setAdapter(adapter);

                mItemClickSubscription =
                    RxAdapterView.itemClickEvents(gridView).subscribe(adapterViewItemClickEvent -> {
                        final Cursor cursor = adapter.getCursor();
                        cursor.moveToPosition(adapterViewItemClickEvent.position());
                        mItemClickObservable.onNext(new Movie(cursor));
                    });
            }));
    }

    @Override
    public void onStart() {
        super.onStart();
        checkSortByPref();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);
        mGridView.onNext((GridView) rootView.findViewById(R.id.gridview_movies));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSortByPref();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItemClickObservable.onCompleted();
        setAdapterSubscription(null);

        if (mItemClickSubscription != null) {
            mItemClickSubscription.unsubscribe();
        }
    }

    public Observable<Movie> getItemClickObservable() {
        return mItemClickObservable;
    }
}
