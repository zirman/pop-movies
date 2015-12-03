package com.homes.popmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.homes.popmovies.data.MovieContract.*;

import com.homes.popmovies.utilities.Pusher;

public class MovieProvider extends ContentProvider {
    static private final UriMatcher sUriMatcher = buildUriMatcher();

    static public final int MOVIE = 100;
    static public final int MOVIE_VIDEOS = 101;
    static public final int MOVIE_REVIEWS = 102;
    static public final int FAVORITE = 103;
    static public final int FAVORITES = 104;
    static public final int MOVIES = 105;

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

            case MOVIE: {  // "movie/#"
                //fetchMovie();
                retCursor = movieCursor(uri, projection);
                break;
            }

            case MOVIES: {  // "movie"
                retCursor = moviesSortOrderCursor(projection, sortOrder);
                break;
            }

            case MOVIE_VIDEOS: {  // "movie/#/videos"
                retCursor = movieVideosCursor(uri, projection);
                break;
            }

            case MOVIE_REVIEWS: {  // "movie/#/reviews"
                retCursor = movieReviewsCursor(uri, projection);
                break;
            }

            case FAVORITE: {  // "favorites/#"
                retCursor = favoriteCursor(uri, projection);
                break;
            }

            case FAVORITES: {  // "favorites"
                retCursor = favoritesCursor(projection);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context context = getContext();

        if (context != null) {
            retCursor.setNotificationUri(context.getContentResolver(), uri);
        }

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

                if (db.insert(MovieEntry.TABLE_NAME, null, values) == -1) {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                returnUri = MovieEntry.buildMovieUri(values.getAsInteger(MovieEntry.COLUMN_ID));
                break;
            }

            case MOVIE_VIDEOS: {

                if (db.insert(VideoEntry.TABLE_NAME, null, values) == -1) {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                returnUri = MovieEntry.buildVideosUri(values.getAsInteger(MovieEntry.COLUMN_ID));
                break;
            }

            case MOVIE_REVIEWS: {

                if (db.insert(ReviewEntry.TABLE_NAME, null, values) == -1) {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                returnUri = MovieEntry.buildReviewsUri(values.getAsInteger(MovieEntry.COLUMN_ID));
                break;
            }

            case FAVORITES: {
                ContentValues favoriteValues = new ContentValues();

                favoriteValues.put(
                    FavoriteEntry.COLUMN_MOVIE_ID,
                    values.getAsInteger(MovieEntry.COLUMN_ID));

                if (db.insert(MovieEntry.TABLE_NAME, null, values) == -1 ||
                    db.insert(FavoriteEntry.TABLE_NAME, null, favoriteValues) == -1) {

                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                returnUri = FavoriteEntry.buildFavoritesUri();
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context context = getContext();

        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }

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
                    new String[] { MovieEntry.uriToMovieId(uri) });
                break;
            }

            case FAVORITE: {
                nRows = db.delete(
                    FavoriteEntry.TABLE_NAME,
                    sFavoriteMovieIdSelection,
                    new String[] { FavoriteEntry.uriToMovieId(uri) });
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (nRows > 0) {
            Context context = getContext();

            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }
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
            Context context = getContext();

            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }
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

    static private final String sVideoMovieIdSelection =
        VideoEntry.TABLE_NAME + "." + VideoEntry.COLUMN_MOVIE_ID + " = ?";

    static private final String sReviewMovieIdSelection =
        ReviewEntry.TABLE_NAME + "." + ReviewEntry.COLUMN_MOVIE_ID + " = ?";

    static private final String sFavoriteMovieIdSelection =
        FavoriteEntry.TABLE_NAME + "." + FavoriteEntry.COLUMN_MOVIE_ID + " = ?";

    private MovieDbHelper mOpenHelper;

    private Cursor movieCursor(final Uri uri, final String[] projection) {

        return sMovieQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            sMovieIdSelection,
            new String[]{
                MovieEntry.uriToMovieId(uri)
            },
            null,
            null,
            null);
    }

    @Override
    public int bulkInsert(@NonNull final Uri uri, @NonNull final ContentValues[] values)  {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case MOVIES: try {
                db.beginTransaction();
                int returnCount = 0;

                for (final ContentValues contentValue : values) {

                    if (db.insert(MovieEntry.TABLE_NAME, null, contentValue) == -1) {
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    }

                    returnCount += 1;
                }

                db.setTransactionSuccessful();
                return returnCount;

            } finally {
                db.endTransaction();
            }

            case MOVIE_VIDEOS: try {
                db.beginTransaction();
                final String movieId = MovieEntry.uriToMovieId(uri);
                int returnCount = 0;

                for (final ContentValues contentValue : values) {
                    contentValue.put(VideoEntry.COLUMN_MOVIE_ID, movieId);

                    if (db.insert(VideoEntry.TABLE_NAME, null, contentValue) == -1) {
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    }

                    returnCount += 1;
                }

                db.setTransactionSuccessful();
                return returnCount;

            } finally {
                db.endTransaction();
            }

            case MOVIE_REVIEWS: try {
                db.beginTransaction();
                final String movieId = MovieEntry.uriToMovieId(uri);
                int returnCount = 0;

                for (final ContentValues contentValue : values) {
                    contentValue.put(ReviewEntry.COLUMN_MOVIE_ID, movieId);

                    if (db.insert(ReviewEntry.TABLE_NAME, null, contentValue) == -1) {
                        throw new android.database.SQLException("Failed to insert row into " + uri);
                    }

                    returnCount += 1;
                }

                db.setTransactionSuccessful();
                return returnCount;

            } finally {
                db.endTransaction();
            }

            default:
                return super.bulkInsert(uri, values);
        }
    }

    private Cursor moviesSortOrderCursor(final String[] projection, final String sortOrder) {

        return sMovieQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            null,
            null,
            null,
            null,
            sortOrder);
    }

    private Cursor movieVideosCursor(final Uri uri, final String[] projection) {

        return sVideosQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            sVideoMovieIdSelection,
            new String[]{
                MovieEntry.uriToMovieId(uri)
            },
            null,
            null,
            null);
    }

    private Cursor movieReviewsCursor(final Uri uri, final String[] projection) {

        return sReviewsQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            sReviewMovieIdSelection,
            new String[]{
                MovieEntry.uriToMovieId(uri)
            },
            null,
            null,
            null);
    }

    private Cursor favoriteCursor(final Uri uri, final String[] projection) {

        return sFavoritesQueryBuilder.query(
            mOpenHelper.getReadableDatabase(),
            projection,
            sMovieIdSelection,
            new String[] { FavoriteEntry.uriToMovieId(uri) },
            null,
            null,
            null);
    }

    private Cursor favoritesCursor(final String[] projection) {

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
                .push("#")
                .join("/"),
            MOVIE);

        uriMatcher.addURI(
            authority,
            moviePath,
            MOVIES);

        uriMatcher.addURI(
            authority,
            Pusher.start()
                .push(moviePath)
                .push("#")
                .push(MovieContract.PATH_VIDEOS)
                .join("/"),
            MOVIE_VIDEOS);

        uriMatcher.addURI(
            authority,
            Pusher.start()
                .push(moviePath)
                .push("#")
                .push(MovieContract.PATH_REVIEWS)
                .join("/"),
            MOVIE_REVIEWS);

        uriMatcher.addURI(
            authority,
            Pusher.start()
                .push(MovieContract.PATH_FAVORITES)
                .push("#")
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

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
