package com.homes.popmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.homes.popmovies.data.MovieContract.*;

import com.homes.popmovies.Optional;
import com.homes.popmovies.Pusher;

public class MovieProvider extends ContentProvider {
    static private final UriMatcher sUriMatcher = buildUriMatcher();

    static public final int MOVIE = 100;
    static public final int MOVIE_VIDEOS = 101;
    static public final int MOVIE_REVIEWS = 102;
    static public final int FAVORITE = 103;
    static public final int FAVORITES = 104;

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(
        @NonNull final Uri uri,
        final String[] projection,
        @Nullable final String selection,
        @Nullable final String[] selectionArgs,
        @Nullable final String sortOrder) {

        final Cursor retCursor;

        switch (sUriMatcher.match(uri)) {

            case MOVIE: {  // "movie/*"
                retCursor = getMovie(uri, projection);
                break;
            }

            case MOVIE_VIDEOS: {  // "movie/*/videos"
                retCursor = getVideos(uri, projection);
                break;
            }

            case MOVIE_REVIEWS: {  // "movie/*/reviews"
                retCursor = getReviews(uri, projection);
                break;
            }

            case FAVORITE: {  // "favorites/*"
                retCursor = getFavorite(uri, projection);
                break;
            }

            case FAVORITES: {  // "favorites"
                retCursor = getFavorites(projection);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Optional.unit(getContext()).map_(context ->
            retCursor.setNotificationUri(context.getContentResolver(), uri));

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull final Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {

            case MOVIE:
                return MovieEntry.CONTENT_ITEM_TYPE;

            case MOVIE_VIDEOS:
                return VideoEntry.CONTENT_DIR_TYPE;

            case MOVIE_REVIEWS:
                return ReviewEntry.CONTENT_DIR_TYPE;

            case FAVORITE:
                return FavoriteEntry.CONTENT_ITEM_TYPE;

            case FAVORITES:
                return FavoriteEntry.CONTENT_DIR_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        final Uri returnUri;

        switch (match) {

            case MOVIE: {
                final long _id = db.insert(MovieEntry.TABLE_NAME, null, values);

                if (_id > 0) {
                    returnUri = MovieEntry
                        .buildMovieUri(values.getAsInteger(MovieEntry.COLUMN_ID));

                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case MOVIE_VIDEOS: {
                final long _id = db.insert(VideoEntry.TABLE_NAME, null, values);

                if (_id > 0) {
                    returnUri = MovieEntry
                        .buildVideosUri(values.getAsInteger(MovieEntry.COLUMN_ID));

                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case MOVIE_REVIEWS: {
                final long _id = db.insert(ReviewEntry.TABLE_NAME, null, values);

                if (_id > 0) {
                    returnUri = MovieEntry
                        .buildReviewsUri(values.getAsInteger(MovieEntry.COLUMN_ID));

                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            case FAVORITES: {
                ContentValues favoriteValues = new ContentValues();

                favoriteValues.put(
                    FavoriteEntry.COLUMN_MOVIE_ID,
                    values.getAsInteger(MovieEntry.COLUMN_ID));

                final long movies_id = db.insert(MovieEntry.TABLE_NAME, null, values);
                final long favorites_id = db.insert(FavoriteEntry.TABLE_NAME, null, favoriteValues);

                if (movies_id > 0 && favorites_id > 0) {
                    returnUri = FavoriteEntry.buildFavoritesUri();

                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Optional.unit(getContext()).map_(context ->
            context.getContentResolver().notifyChange(uri, null));

        return returnUri;
    }

    @Override
    public int delete(
        @NonNull final Uri uri,
        final String selectionString,
        final String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        final int nRows;

        switch (match) {

            case MOVIE: {
                nRows = db.delete(
                    MovieEntry.TABLE_NAME,
                    sMovieIdSelection,
                    new String[] { MovieEntry.getMovieIdFromUri(uri) });
                break;
            }

            case FAVORITE: {
                nRows = db.delete(
                    FavoriteEntry.TABLE_NAME,
                    sFavoriteMovieIdSelection,
                    new String[] { FavoriteEntry.getMovieIdFromUri(uri) });
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (nRows > 0) {
            Optional.unit(getContext()).map_(context ->
                context.getContentResolver().notifyChange(uri, null));
        }

        return nRows;
    }

    @Override
    public int update(
        @NonNull final Uri uri,
        final ContentValues values,
        final String selection,
        final String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int nRows;

        switch (match) {

            case MOVIE: {
                nRows = db.update(
                    MovieEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (nRows > 0) {
            Optional.unit(getContext()).map_(context ->
                context.getContentResolver().notifyChange(uri, null));
        }

        return nRows;
    }

    static private String innerJoin(
        final String table1,
        final String table2,
        final String column1,
        final String column2) {

        return Pusher.start()
            .push(table1)
            .push("INNER JOIN")
            .push(table2)
            .push("ON")
            .push(table1 + "." + column1)
            .push("=")
            .push(table2 + "." + column2)
            .join(" ");
    }

    static private final SQLiteQueryBuilder sMovieQueryBuilder;
    static private final SQLiteQueryBuilder sVideosQueryBuilder;
    static private final SQLiteQueryBuilder sReviewsQueryBuilder;
    static private final SQLiteQueryBuilder sFavoritesQueryBuilder;

    static {
        sMovieQueryBuilder = new SQLiteQueryBuilder();
        sMovieQueryBuilder.setTables(MovieEntry.TABLE_NAME);

        sVideosQueryBuilder = new SQLiteQueryBuilder();
        sVideosQueryBuilder.setTables(VideoEntry.TABLE_NAME);

        sReviewsQueryBuilder = new SQLiteQueryBuilder();
        sReviewsQueryBuilder.setTables(ReviewEntry.TABLE_NAME);

        sFavoritesQueryBuilder = new SQLiteQueryBuilder();

        sFavoritesQueryBuilder.setTables(innerJoin(
            FavoriteEntry.TABLE_NAME,
            MovieEntry.TABLE_NAME,
            FavoriteEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_ID));

    }

    static private final String sMovieIdSelection =
        MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_ID + " = ?";

    static private final String sFavoriteMovieIdSelection =
        FavoriteEntry.TABLE_NAME + "." + FavoriteEntry.COLUMN_MOVIE_ID + " = ?";

    private MovieDbHelper mOpenHelper;

    private Cursor getMovie(final Uri uri, final String[] projection) {

        return sMovieQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            sMovieIdSelection,
            new String[]{
                MovieEntry.getMovieIdFromUri(uri)
            },
            null,
            null,
            null);
    }

    private Cursor getVideos(final Uri uri, final String[] projection) {

        return sVideosQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            sMovieIdSelection,
            new String[]{
                MovieEntry.getMovieIdFromUri(uri)
            },
            null,
            null,
            null);
    }

    private Cursor getReviews(final Uri uri, final String[] projection) {

        return sReviewsQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            sMovieIdSelection,
            new String[]{
                MovieEntry.getMovieIdFromUri(uri)
            },
            null,
            null,
            null);
    }

    private Cursor getFavorite(final Uri uri, final String[] projection) {

        return sFavoritesQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            sMovieIdSelection,
            new String[] { FavoriteEntry.getMovieIdFromUri(uri) },
            null,
            null,
            null);
    }

    private Cursor getFavorites(final String[] projection) {

        return sFavoritesQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            null,
            null,
            null,
            null,
            null);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        final String moviePath = MovieContract.PATH_MOVIE;

        uriMatcher.addURI(
            authority,
            Pusher.start()
                .push(moviePath)
                .push("*")
                .join("/"),
            MOVIE);

        uriMatcher.addURI(
            authority,
            Pusher.start()
                .push(moviePath)
                .push("*")
                .push(MovieContract.PATH_VIDEOS)
                .join("/"),
            MOVIE_VIDEOS);

        uriMatcher.addURI(
            authority,
            Pusher.start()
                .push(moviePath)
                .push("*")
                .push(MovieContract.PATH_REVIEWS)
                .join("/"),
            MOVIE_REVIEWS);

        uriMatcher.addURI(
            authority,
            Pusher.start()
                .push(MovieContract.PATH_FAVORITES)
                .push("*")
                .join("/"),
            FAVORITE);

        uriMatcher.addURI(
            authority,
            Pusher.start()
                .push(MovieContract.PATH_FAVORITES)
                .join("/"),
            FAVORITES);

        return uriMatcher;
    }

//    @Override
//    public int bulkInsert(final Uri uri, final ContentValues[] values) {
//        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
//        final int match = sUriMatcher.match(uri);
//
//        switch (match) {
//            case WEATHER:
//                db.beginTransaction();
//                int returnCount = 0;
//                try {
//                    for (ContentValues value : values) {
//                        normalizeDate(value);
//
//                        final long _id = db.insert(
//                            WeatherContract.WeatherEntry.TABLE_NAME, null, value);
//
//                        if (_id != -1) {
//                            returnCount++;
//                        }
//                    }
//                    db.setTransactionSuccessful();
//                } finally {
//                    db.endTransaction();
//                }
//                getContext().getContentResolver().notifyChange(uri, null);
//                return returnCount;
//            default:
//                return super.bulkInsert(uri, values);
//        }
//    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
