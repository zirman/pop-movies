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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homes.popmovies.adapters.EmptyAdapter;
import com.homes.popmovies.adapters.MovieAdapter;
import com.homes.popmovies.utilities.Http;
import com.homes.popmovies.dtobjs.Movie;
import com.homes.popmovies.R;
import com.homes.popmovies.utilities.Transform;
import com.homes.popmovies.data.MovieContract.MovieEntry;
import com.homes.popmovies.data.MovieContract.FavoriteEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pcollections.TreePVector;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class MovieGridFragment extends Fragment {
    private static final String LOG_TAG = MovieGridFragment.class.getSimpleName();

    private static TreePVector<Movie> getMovieDataFromJson(final String jsonString) {

        try {
            final JSONObject movieJson = new JSONObject(jsonString);
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

    // Instance declarations

    private final BehaviorSubject<String> mSortBys = BehaviorSubject.<String>create();
    private final BehaviorSubject<MovieAdapter> mAdapters = BehaviorSubject.<MovieAdapter>create();

    private final BehaviorSubject<RecyclerView> mRecyclerViews =
        BehaviorSubject.<RecyclerView>create();

    private final Observable<Movie> mItemClicks =
        Observable.switchOnNext(mAdapters.map(MovieAdapter::itemClicks));

    private final CompositeSubscription mSubscriptions = new CompositeSubscription();

    public MovieGridFragment() {

        mSubscriptions.add(
            Observable.combineLatest(
                mRecyclerViews,
                mAdapters,
                (Func2<RecyclerView, MovieAdapter, Pair<RecyclerView, MovieAdapter>>) Pair::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> pair.first.setAdapter(pair.second)));

        mSubscriptions.add(
            mSortBys.distinctUntilChanged()
                .observeOn(Schedulers.io())
                .map(this::networkUpdate)  // update from network if connected
                .observeOn(Schedulers.computation())  // process data on computation thread
                .map(this::bulkInsertContentProvider)  // insert network data if received
                // updates the adapter with query from content provider
                .map(this::queryContentProvider)
                .map(this::cursorToTreePVector)
                .map(MovieAdapter::new)
                .subscribe(mAdapters::onNext));
    }

    private void publishSortBy() {
        mSortBys.onNext(getSortBy());
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

    private Pair<String, String> networkUpdate(final String sortBy) {

        // TODO: only fetch when time has elapsed since last successful fetch

        if (isFavorites(sortBy) || ! isConnected()) {
            return new Pair<>(sortBy, "");
        }

        return new Pair<>(sortBy, fetchMovies(sortBy));
    }

    private String bulkInsertContentProvider(final Pair<String, String> pair) {

        if (! pair.second.isEmpty()) {

            final TreePVector<ContentValues> contentValues =
                Transform.map(getMovieDataFromJson(pair.second), Movie::toContentValues);

            final ContentValues[] array = new ContentValues[contentValues.size()];
            contentValues.toArray(array);
            getContext().getContentResolver().bulkInsert(MovieEntry.buildMoviesUri(), array);
        }

        return pair.first;
    }

    private Cursor queryContentProvider(final String sortBy) {
        final boolean isFavorites = isFavorites(sortBy);

        return getContext().getContentResolver().query(
            isFavorites ?
                FavoriteEntry.buildFavoritesUri() :
                MovieEntry.buildMoviesUri(),
            null,
            null,
            null,
            isFavorites ? null : sortBy);
    }

    private boolean isFavorites(final String sortBy) {
        return sortBy.equalsIgnoreCase(getString(R.string.sort_by_favorites));
    }

    private TreePVector<Movie> cursorToTreePVector(final Cursor cursor) {

        if (! cursor.moveToFirst()) {
            return TreePVector.empty();
        }

        TreePVector<Movie> movies = TreePVector.empty();

        do {
            movies = movies.plus(new Movie(cursor));
        } while (cursor.moveToNext());

        cursor.close();
        return movies;
    }

    private String fetchMovies(final String sortBy) {
        final String BASE_URI = "http://api.themoviedb.org/3/discover/movie?sort_by=&api_key=";

        try {

            return Http.request(new URL(Uri.parse(BASE_URI)
                .buildUpon()
                .appendQueryParameter(
                    "sort_by",
                    sortBy.replace(' ', '.'))
                .appendQueryParameter(
                    "api_key",
                    getContext().getString(R.string.tmdb_api_key))
                .build()
                .toString()));

        } catch (final IOException error) {

            // TODO: display network error

            error.printStackTrace();
        }

        return "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        publishSortBy();
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_movie_grid, container, false);

        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new EmptyAdapter());
        mRecyclerViews.onNext(recyclerView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        publishSortBy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSortBys.onCompleted();
        mAdapters.onCompleted();
        mRecyclerViews.onCompleted();
        mSubscriptions.unsubscribe();
    }

    public Observable<Movie> itemClicks() {
        return mItemClicks;
    }
}
