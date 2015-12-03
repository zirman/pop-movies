package com.homes.popmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.homes.popmovies.utilities.Pusher;
import com.homes.popmovies.data.MovieContract.*;

public class MovieDbHelper extends SQLiteOpenHelper {
    static private final int DATABASE_VERSION = 1;
    static public final String DATABASE_NAME = "movie.db";

    private static String statement(final String s) {
        return s + ';';
    }

    private static String parens(final String s) {
        return '(' + s + ')';
    }

    private static String createTable(final String name, final String columns) {
        return Pusher.start()
            .push("CREATE TABLE")
            .push(name)
            .push(parens(columns))
            .join(" ");
    }

    private static String foreignKey(final String column, final String table, final String index) {
        return Pusher.start()
            .push("FOREIGN KEY")
            .push(parens(column))
            .push("REFERENCES")
            .push(table)
            .push(parens(index))
            .join(" ");
    }

    private static String unique(final String columns) {
        return Pusher.start()
            .push("UNIQUE")
            .push(parens(columns))
            .push("ON CONFLICT REPLACE")
            .join(" ");
    }

    public MovieDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIES_TABLE = statement(createTable(
            MovieEntry.TABLE_NAME,
            Pusher.start()
                .push(MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT")
                .push(MovieEntry.COLUMN_ID + " INTEGER NOT NULL")
                .push(MovieEntry.COLUMN_ADULT + " INTEGER NOT NULL")
                .push(MovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_GENRE_IDS + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL")
                .push(MovieEntry.COLUMN_TITLE + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_VIDEO + " TEXT NOT NULL")
                .push(MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL")
                .push(MovieEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL")
                .push(unique(MovieEntry.COLUMN_ID))
                .join(", ")));

        final String SQL_CREATE_VIDEOS_TABLE = statement(createTable(
            VideoEntry.TABLE_NAME,
            Pusher.start()
                .push(VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT")
                .push(VideoEntry.COLUMN_ID + " TEXT NOT NULL")
                .push(VideoEntry.COLUMN_ISO_639_1 + " TEXT NOT NULL")
                .push(VideoEntry.COLUMN_KEY + " TEXT NOT NULL")
                .push(VideoEntry.COLUMN_NAME + " TEXT NOT NULL")
                .push(VideoEntry.COLUMN_SITE + " TEXT NOT NULL")
                .push(VideoEntry.COLUMN_SIZE + " INTEGER NOT NULL")
                .push(VideoEntry.COLUMN_TYPE + " TEXT NOT NULL")
                .push(VideoEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL")
                .push(foreignKey(
                    VideoEntry.COLUMN_MOVIE_ID,
                    MovieEntry.TABLE_NAME,
                    MovieEntry.COLUMN_ID))
                .push(unique(VideoEntry.COLUMN_ID))
                .join(", ")));

        final String SQL_CREATE_REVIEWS_TABLE = statement(createTable(
            ReviewEntry.TABLE_NAME,
            Pusher.start()
                .push(ReviewEntry._ID + " INTEGER PRIMARY KEY")
                .push(ReviewEntry.COLUMN_ID + " TEXT NOT NULL")
                .push(ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL")
                .push(ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL")
                .push(ReviewEntry.COLUMN_URL + " TEXT NOT NULL")
                .push(ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL")
                .push(foreignKey(
                    ReviewEntry.COLUMN_MOVIE_ID,
                    MovieEntry.TABLE_NAME,
                    MovieEntry.COLUMN_ID))
                .push(unique(ReviewEntry.COLUMN_ID))
                .join(", ")));

        final String SQL_CREATE_FAVORITES_TABLE = statement(createTable(
            FavoriteEntry.TABLE_NAME,
            Pusher.start()
                .push(FavoriteEntry._ID + " INTEGER PRIMARY KEY")
                .push(FavoriteEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL")
                .push(foreignKey(
                    FavoriteEntry.COLUMN_MOVIE_ID,
                    MovieEntry.TABLE_NAME,
                    MovieEntry.COLUMN_ID))
                .push(unique(FavoriteEntry.COLUMN_MOVIE_ID))
                .join(", ")));

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_VIDEOS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }
}
