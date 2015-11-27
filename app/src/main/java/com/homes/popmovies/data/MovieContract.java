package com.homes.popmovies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.homes.popmovies.Pusher;

public class MovieContract {
    static public final String CONTENT_AUTHORITY = "com.homes.popmovies";
    static public final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static public final String PATH_MOVIE = "movie";
    static public final String PATH_VIDEOS = "videos";
    static public final String PATH_REVIEWS = "reviews";
    static public final String PATH_FAVORITES = "favorites";

    static public final class MovieEntry implements BaseColumns {
        static public final String TABLE_NAME = "movies";
        static public final String COLUMN_ID = "id";
        static public final String COLUMN_ADULT = "adult";
        static public final String COLUMN_BACKDROP_PATH = "backdrop_path";
        static public final String COLUMN_GENRE_IDS = "genre_ids";
        static public final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        static public final String COLUMN_ORIGINAL_TITLE = "original_title";
        static public final String COLUMN_OVERVIEW = "overview";
        static public final String COLUMN_RELEASE_DATE = "release_date";
        static public final String COLUMN_POSTER_PATH = "poster_path";
        static public final String COLUMN_POPULARITY = "popularity";
        static public final String COLUMN_TITLE = "title";
        static public final String COLUMN_VIDEO = "video";
        static public final String COLUMN_VOTE_AVERAGE = "vote_average";
        static public final String COLUMN_VOTE_COUNT = "vote_count";

        static public final Uri CONTENT_URI = BASE_CONTENT_URI
            .buildUpon()
            .appendPath(PATH_MOVIE)
            .build();

        static public final String CONTENT_ITEM_TYPE = Pusher.start()
            .push(ContentResolver.CURSOR_ITEM_BASE_TYPE)
            .push(CONTENT_AUTHORITY)
            .push(PATH_MOVIE)
            .join("/");

        static public Uri buildMovieUri(final int id) {
            return CONTENT_URI
                .buildUpon()
                .appendPath(String.valueOf(id))
                .build();
        }

        static public Uri buildVideosUri(final int id) {
            return buildMovieUri(id)
                .buildUpon()
                .appendPath(PATH_VIDEOS)
                .build();
        }

        static public Uri buildReviewsUri(final int id) {
            return buildMovieUri(id)
                .buildUpon()
                .appendPath(PATH_REVIEWS)
                .build();
        }

        static public String getMovieIdFromUri(final Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    static public final class VideoEntry implements BaseColumns {
        static public final String TABLE_NAME = "videos";
        static public final String COLUMN_ID = "id";
        static public final String COLUMN_ISO_639_1 = "iso_639_1";
        static public final String COLUMN_KEY = "key";
        static public final String COLUMN_NAME = "name";
        static public final String COLUMN_SITE = "site";
        static public final String COLUMN_SIZE = "size";
        static public final String COLUMN_TYPE = "type";

        // foreign key

        static public final String COLUMN_MOVIE_ID = "movie_id";

        static public final String CONTENT_DIR_TYPE = Pusher.start()
            .push(ContentResolver.CURSOR_DIR_BASE_TYPE)
            .push(CONTENT_AUTHORITY)
            .push(PATH_VIDEOS)
            .join("/");
    }

    static public final class ReviewEntry implements BaseColumns {
        static public final String TABLE_NAME = "reviews";
        static public final String COLUMN_ID = "id";
        static public final String COLUMN_AUTHOR = "author";
        static public final String COLUMN_CONTENT = "content";
        static public final String COLUMN_URL = "url";

        // foreign key

        static public final String COLUMN_MOVIE_ID = "movie_id";

        static public final String CONTENT_DIR_TYPE = Pusher.start()
            .push(ContentResolver.CURSOR_DIR_BASE_TYPE)
            .push(CONTENT_AUTHORITY)
            .push(PATH_REVIEWS)
            .join("/");
    }

    static public final class FavoriteEntry implements BaseColumns {
        static public final String TABLE_NAME = "favorites";

        // foreign key

        static public final String COLUMN_MOVIE_ID = "movie_id";

        static public final Uri CONTENT_URI = BASE_CONTENT_URI
            .buildUpon()
            .appendPath(PATH_FAVORITES)
            .build();

        static public final String CONTENT_ITEM_TYPE = Pusher.start()
            .push(ContentResolver.CURSOR_ITEM_BASE_TYPE)
            .push(CONTENT_AUTHORITY)
            .push(PATH_FAVORITES)
            .join("/");

        static public final String CONTENT_DIR_TYPE = Pusher.start()
            .push(ContentResolver.CURSOR_DIR_BASE_TYPE)
            .push(CONTENT_AUTHORITY)
            .push(PATH_FAVORITES)
            .join("/");

        static public Uri buildFavoritesUri() {
            return CONTENT_URI;
        }

        static public Uri buildFavoriteUri(final int id) {
            return CONTENT_URI
                .buildUpon()
                .appendPath(String.valueOf(id))
                .build();
        }

        static public String getMovieIdFromUri(final Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}

