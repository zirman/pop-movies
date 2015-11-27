package com.homes.popmovies;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.homes.popmovies.data.MovieContract.*;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class DetailFragment extends Fragment {
    static public final String MOVIE_PARCEL = "movie";

    static private final String LOG_TAG = DetailFragment.class.getSimpleName();
    static private final String BASE_PATH = "http://image.tmdb.org/t/p/w500";

    static public DetailFragment newInstance(final Movie movie) {
        final Bundle arguments = new Bundle();
        arguments.putParcelable(MOVIE_PARCEL, movie);
        final DetailFragment fragment = new DetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    static private class Detail {
        public final int id;
        public final int budget;
        public final String homepage;
        public final String imdbId;
        public final int revenue;
        public final int runtime;
        public final String status;
        public final String tagline;

        public Detail(final JSONObject detail) throws JSONException, ParseException {
            id = detail.getInt("id");
            budget = detail.getInt("budget");
            homepage = detail.getString("homepage");
            imdbId = detail.getString("imdb_id");
            revenue = detail.getInt("revenue");
            runtime = detail.getInt("runtime");
            status = detail.getString("status");
            tagline = detail.getString("tagline");
        }
    }

    static private Detail getDetailDataFromJson(final String jsonString) {

        try {
            return new Detail(new JSONObject(jsonString));

        } catch (final JSONException | ParseException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return null;
        }
    }

    static private Video[] getVideoDataFromJson(final String jsonString) {

        try {
            final JSONObject movieJson = new JSONObject(jsonString);
            final JSONArray resultsArray = movieJson.getJSONArray("results");
            final Video[] videos = new Video[resultsArray.length()];

            for (int i = 0; i < resultsArray.length(); i += 1) {
                videos[i] = new Video(resultsArray.getJSONObject(i));
            }

            return videos;

        } catch (final JSONException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return null;
        }
    }

    static private Review[] getReviewDataFromJson(final String jsonString) {

        try {
            final JSONObject movieJson = new JSONObject(jsonString);
            final JSONArray resultsArray = movieJson.getJSONArray("results");
            final Review[] reviews = new Review[resultsArray.length()];

            for (int i = 0; i < resultsArray.length(); i += 1) {
                reviews[i] = new Review(resultsArray.getJSONObject(i));
            }

            return reviews;

        } catch (final JSONException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return null;
        }
    }

    // Instance definitions.

    private Optional<Movie> mMovie = Optional.empty();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovie = Optional.unit(getArguments())
            .map(args -> args.getParcelable(MOVIE_PARCEL));
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

        mMovie.map(movie -> {
            final String addFavoritesText = getString(R.string.add_favorites_text);
            final String removeFavoritesText = getString(R.string.remove_favorites_text);
            final ContentResolver mContentResolver = getContext().getContentResolver();

            Button button = (Button) rootView.findViewById(R.id.favorite_button);

            RxView.clickEvents(button).subscribe(viewClickEvent -> {

                if (button.getText() == addFavoritesText) {

                    mContentResolver.insert(
                        FavoriteEntry.buildFavoritesUri(),
                        movie.getContentValues());

                    button.setText(removeFavoritesText);

                } else {

                    mContentResolver.delete(
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

            // Show abbreviated date when release was this year.
            // Otherwise show year.

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.get(Calendar.YEAR);
            cal.set(Calendar.MONTH, 0);
            cal.set(Calendar.DAY_OF_MONTH, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            ((TextView) rootView.findViewById(R.id.year_text))
                .setText((cal.getTimeInMillis() < movie.releaseDate) ?
                    DateUtils.formatDateTime(
                        getContext(),
                        movie.releaseDate,
                        DateUtils.FORMAT_ABBREV_ALL) :
                    String.valueOf(cal.get(Calendar.YEAR)));

            ((TextView) rootView.findViewById(R.id.rating_text))
                .setText(String.valueOf(movie.voteAverage) + "/10");

            ((TextView) rootView.findViewById(R.id.overview_text))
                .setText(movie.overview);

            fetchDetails(movie.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(detail -> {
                    ((TextView) rootView.findViewById(R.id.runtime_text))
                        .setText(String.format(
                            getContext().getString(R.string.format_runtime),
                            detail.runtime));
                });

            fetchVideos(movie.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(videos -> {
                    FragmentTransaction transaction = getChildFragmentManager()
                        .beginTransaction();

                    for (final Video video : videos) {
                        transaction.add(
                            R.id.layout_videos,
                            VideoFragment.newInstance(video));
                    }

                    transaction.commit();
                });

            fetchReviews(movie.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((final Review[] reviews) -> {
                    FragmentTransaction transaction = getChildFragmentManager()
                        .beginTransaction();

                    for (final Review review : reviews) {
                        transaction.add(
                            R.id.layout_reviews,
                            ReviewFragment.newInstance(review));
                    }

                    transaction.commit();
                });


            Cursor cursor = mContentResolver.query(
                FavoriteEntry.buildFavoriteUri(movie.id),
                null,
                null,
                null,
                null);

            if (cursor != null) {
                button.setText(
                    cursor.moveToFirst() ?
                        removeFavoritesText :
                        addFavoritesText);
            }

            return movie;
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mMovie.map_(movie -> outState.putParcelable(MOVIE_PARCEL, movie));
    }

    private Observable<Detail> fetchDetails(final int id) {

        try {

            return Http.request(new URL(
                Uri.parse("http://api.themoviedb.org/3/movie/")
                    .buildUpon()
                    .appendPath(String.valueOf(id))
                    .appendQueryParameter(
                        "api_key",
                        getString(R.string.tmdb_api_key))
                    .build()
                    .toString()))
                .map(DetailFragment::getDetailDataFromJson);

        } catch (final MalformedURLException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return Observable.empty();
        }
    }

    private Observable<Video[]> fetchVideos(final int id) {

        try {

            return Http.request(new URL(
                Uri.parse("http://api.themoviedb.org/3/movie/")
                    .buildUpon()
                    .appendPath(String.valueOf(id))
                    .appendPath("videos")
                    .appendQueryParameter(
                        "api_key",
                        getString(R.string.tmdb_api_key))
                    .build()
                    .toString()))
                .map(DetailFragment::getVideoDataFromJson);

        } catch (final MalformedURLException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return Observable.empty();
        }
    }

    private Observable<Review[]> fetchReviews(final int id) {

        try {

            return Http.request(new URL(
                Uri.parse("http://api.themoviedb.org/3/movie/")
                    .buildUpon()
                    .appendPath(String.valueOf(id))
                    .appendPath("reviews")
                    .appendQueryParameter(
                        "api_key",
                        getString(R.string.tmdb_api_key))
                    .build()
                    .toString()))
                .map(DetailFragment::getReviewDataFromJson);

        } catch (final MalformedURLException error) {
            Log.e(LOG_TAG, error.getMessage(), error);
            return Observable.empty();
        }
    }
}
