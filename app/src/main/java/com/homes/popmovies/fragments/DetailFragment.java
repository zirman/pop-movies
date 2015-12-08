package com.homes.popmovies.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.homes.popmovies.utilities.Http;
import com.homes.popmovies.dtobjs.Movie;
import com.homes.popmovies.dtobjs.MovieDetail;
import com.homes.popmovies.R;
import com.homes.popmovies.dtobjs.Review;
import com.homes.popmovies.utilities.Transform;
import com.homes.popmovies.dtobjs.Video;
import com.homes.popmovies.data.MovieContract.*;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pcollections.TreePVector;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class DetailFragment extends Fragment {
    public static final String MOVIE_PARCEL = "movie";

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String BASE_PATH = "http://image.tmdb.org/t/p/w500";

    public static DetailFragment newInstance(final Movie movie) {
        final Bundle arguments = new Bundle();
        arguments.putParcelable(MOVIE_PARCEL, movie);
        final DetailFragment fragment = new DetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    private static MovieDetail getDetailDataFromJson(final String jsonString) {

        try {
            return new MovieDetail(new JSONObject(jsonString));

        } catch (final JSONException | ParseException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return null;
        }
    }

    private static TreePVector<Video> getVideoDataFromJson(final String jsonString) {

        try {
            final JSONArray resultsArray = new JSONObject(jsonString).getJSONArray("results");
            TreePVector<Video> videos = TreePVector.empty();

            for (int i = 0; i < resultsArray.length(); i += 1) {
                videos = videos.plus(new Video(resultsArray.getJSONObject(i)));
            }

            return videos;

        } catch (final JSONException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return TreePVector.empty();
        }
    }

    private static TreePVector<Review> getReviewDataFromJson(final String jsonString) {

        try {
            final JSONArray resultsArray = new JSONObject(jsonString).getJSONArray("results");
            TreePVector<Review> reviews = TreePVector.empty();

            for (int i = 0; i < resultsArray.length(); i += 1) {
                reviews = reviews.plus(new Review(resultsArray.getJSONObject(i)));
            }

            return reviews;

        } catch (final JSONException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return TreePVector.empty();
        }
    }

    // Instance declarations

    private final BehaviorSubject<Movie> mMovie = BehaviorSubject.<Movie>create();
    private final BehaviorSubject<View> mRootView = BehaviorSubject.<View>create();

    private Subscription mRenderSubscription;
    private Subscription mMovieDetailSubscription;
    private Subscription mVideoSubscription;
    private Subscription mReviewSubscription;
    private Subscription mShareActionProviderSubscription;

    public DetailFragment() {
        super();
        setHasOptionsMenu(true);
    }

    private void setRenderSubscription(final Subscription renderSubscription) {

        if (mRenderSubscription != null) {
            mRenderSubscription.unsubscribe();
        }

        mRenderSubscription = renderSubscription;
    }

    private void setMovieDetailSubscription(final Subscription movieDetailSubscription) {

        if (mMovieDetailSubscription != null) {
            mMovieDetailSubscription.unsubscribe();
        }

        mMovieDetailSubscription = movieDetailSubscription;
    }

    private void setVideoSubscription(final Subscription videoSubscription) {

        if (mVideoSubscription != null) {
            mVideoSubscription.unsubscribe();
        }

        mVideoSubscription = videoSubscription;
    }

    private void setReviewSubscription(final Subscription reviewSubscription) {

        if (mReviewSubscription != null) {
            mReviewSubscription.unsubscribe();
        }

        mReviewSubscription = reviewSubscription;
    }

    private void setShareActionProviderSubscription(
        final Subscription shareActionProviderSubscription) {

        if (mShareActionProviderSubscription != null) {
            mShareActionProviderSubscription.unsubscribe();
        }

        mShareActionProviderSubscription = shareActionProviderSubscription;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovie.onNext(getArguments().getParcelable(MOVIE_PARCEL));

        setRenderSubscription(Observable.combineLatest(
            mMovie,
            mRootView,
            (Func2<Movie, View, Pair<Movie, View>>) Pair::new).subscribe(pair -> {

            final Movie movie = pair.first;
            final View rootView = pair.second;
            final String addFavoritesText = getString(R.string.add_favorites_text);
            final String removeFavoritesText = getString(R.string.remove_favorites_text);
            final ContentResolver contentResolver = getContext().getContentResolver();
            final Button button = (Button) rootView.findViewById(R.id.favorite_button);

            RxView.clickEvents(button).subscribe(viewClickEvent -> {

                if (button.getText() == addFavoritesText) {

                    contentResolver.insert(
                        FavoriteEntry.buildFavoritesUri(),
                        movie.toContentValues());

                    button.setText(removeFavoritesText);

                } else {

                    contentResolver.delete(
                        FavoriteEntry.buildFavoriteUri(movie.id),
                        null,
                        null);

                    button.setText(addFavoritesText);
                }
            });

            ((TextView) rootView.findViewById(R.id.title)).setText(movie.title);

            Picasso.with(getActivity())
                .load(BASE_PATH + movie.posterPath)
                .into(((ImageView) rootView.findViewById(R.id.poster_image)));

            ((TextView) rootView.findViewById(R.id.year_text))
                .setText(formattedDate(movie.releaseDate));

            ((TextView) rootView.findViewById(R.id.rating_text))
                .setText(String.valueOf(movie.voteAverage) + "/10");

            ((TextView) rootView.findViewById(R.id.overview_text))
                .setText(movie.overview);

            setMovieDetailSubscription(Observable.<MovieDetail>create(subscriber -> {
                final MovieDetail movieDetail = fetchMovieDetails(movie.id);

                if (movieDetail != null) {
                    subscriber.onNext(movieDetail);
                }

                subscriber.onCompleted();
            })
                //.compose(Common.applySchedulers())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movieDetail -> {
                    ((TextView) rootView.findViewById(R.id.runtime_text))
                        .setText(String.format(
                            getContext().getString(R.string.format_runtime),
                            movieDetail.runtime));
                }));

            Observable<Cursor> videoObservable = Observable.<Cursor>create(subscriber -> {
                subscriber.onNext(fetchVideos(movie.id));
                subscriber.onCompleted();
            }).subscribeOn(Schedulers.io());

            setVideoSubscription(videoObservable.observeOn(Schedulers.computation())
                .subscribe(cursor -> {

                    final FragmentTransaction transaction =
                        getChildFragmentManager().beginTransaction();

                    for (int i = 0; cursor.moveToPosition(i); i += 1) {
                        final Video video = new Video(cursor);

                        transaction.add(
                            R.id.layout_videos,
                            VideoFragment.newInstance(video));
                    }

                    transaction.commit();
                    cursor.close();
                }));

            setShareActionProviderSubscription(Observable.combineLatest(
                videoObservable,
                mShareActionProvider,
                (Func2<Cursor, ShareActionProvider, Pair<Cursor, ShareActionProvider>>) Pair::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair2 -> {

                    final Cursor cursor = pair2.first;
                    final ShareActionProvider shareActionProvider = pair2.second;

                    if (cursor.moveToFirst()) {
                        shareActionProvider.setShareIntent(createShareVideoIntent(
                            new Video(cursor)));
                    }
                }));

            setReviewSubscription(Observable.<Cursor>create(subscriber -> {
                subscriber.onNext(fetchReviews(movie.id));
                subscriber.onCompleted();
            }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(cursor -> {

                    final FragmentTransaction transaction =
                        getChildFragmentManager().beginTransaction();

                    for (int i = 0; cursor.moveToPosition(i); i += 1) {

                        transaction.add(
                            R.id.layout_reviews,
                            ReviewFragment.newInstance(new Review(cursor)));
                    }

                    return transaction;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(FragmentTransaction::commit));

            final Cursor cursor = contentResolver.query(
                FavoriteEntry.buildFavoriteUri(movie.id),
                null,
                null,
                null,
                null);

            button.setText(
                (cursor != null && cursor.moveToFirst()) ?
                    removeFavoritesText :
                    addFavoritesText);
        }));
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View rootView = inflater.inflate(
            R.layout.fragment_detail,
            container,
            false);

        mRootView.onNext(rootView);
        return rootView;
    }

    private BehaviorSubject<ShareActionProvider> mShareActionProvider =
        BehaviorSubject.<ShareActionProvider>create();

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider.onNext(
            (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem));
    }

    private Intent createShareVideoIntent(final Video video) {
        final Intent intent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, video.toYoutubeUri().toString());
        return intent;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mMovie.subscribe(movie -> outState.putParcelable(MOVIE_PARCEL, movie));
    }

    private MovieDetail fetchMovieDetails(final int id) {

        if (isConnected()) {

            try {

                final String json = Http.request(new URL(
                    Uri.parse("http://api.themoviedb.org/3/movie/")
                        .buildUpon()
                        .appendPath(String.valueOf(id))
                        .appendQueryParameter(
                            "api_key",
                            getString(R.string.tmdb_api_key))
                        .build()
                        .toString()));

                return getDetailDataFromJson(json);

            } catch (final IOException error) {
                Log.e(LOG_TAG, error.getMessage(), error);
                error.printStackTrace();
            }
        }

        return null;
    }

    private Cursor fetchVideos(final int id) {

        if (isConnected()) {

            try {

                final String json = Http.request(new URL(
                    Uri.parse("http://api.themoviedb.org/3/movie/")
                        .buildUpon()
                        .appendPath(String.valueOf(id))
                        .appendPath("videos")
                        .appendQueryParameter(
                            "api_key",
                            getString(R.string.tmdb_api_key))
                        .build()
                        .toString()));

                final TreePVector<ContentValues> videos =
                    Transform.map(getVideoDataFromJson(json), Video::toContentValues);

                final ContentValues[] contentValues = new ContentValues[videos.size()];
                videos.toArray(contentValues);

                getContext().getContentResolver().bulkInsert(
                    MovieEntry.buildVideosUri(id),
                    contentValues);

            } catch (final IOException error) {
                error.printStackTrace();
            }
        }

        return getContext().getContentResolver().query(
            MovieEntry.buildVideosUri(id),
            null,
            null,
            null,
            null);
    }

    private Cursor fetchReviews(final int id) {

        if (isConnected()) {

            try {

                final String json = Http.request(new URL(
                    Uri.parse("http://api.themoviedb.org/3/movie/")
                        .buildUpon()
                        .appendPath(String.valueOf(id))
                        .appendPath("reviews")
                        .appendQueryParameter(
                            "api_key",
                            getString(R.string.tmdb_api_key))
                        .build()
                        .toString()));

                final TreePVector<ContentValues> reviews =
                    Transform.map(getReviewDataFromJson(json), Review::toContentValues);

                Transform.filter(getReviewDataFromJson(json), foo -> true);

                final ContentValues[] contentValues = new ContentValues[reviews.size()];
                reviews.toArray(contentValues);

                getContext().getContentResolver().bulkInsert(
                    MovieEntry.buildReviewsUri(id),
                    contentValues);

            } catch (final IOException error) {
                error.printStackTrace();
            }
        }

        return getContext().getContentResolver().query(
            MovieEntry.buildReviewsUri(id),
            null,
            null,
            null,
            null);
    }

    private String formattedDate(long releaseDate) {

        // Show abbreviated date when release date was this year.
        // Otherwise show release date as year.

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.get(Calendar.YEAR);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);

        return (cal.getTimeInMillis() < releaseDate) ?
            DateUtils.formatDateTime(
                getContext(),
                releaseDate,
                DateUtils.FORMAT_ABBREV_ALL) :
            String.valueOf(cal.get(Calendar.YEAR));
    }

    private boolean isConnected() {

        final NetworkInfo networkInfo =
            ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setRenderSubscription(null);
        setMovieDetailSubscription(null);
        setReviewSubscription(null);
        setVideoSubscription(null);
        setShareActionProviderSubscription(null);
    }
}
