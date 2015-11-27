package com.homes.popmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.homes.popmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pcollections.TreePVector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

// Immutable data transfer object for movies

public class Movie implements Parcelable {
    public final int id;
    public final boolean adult;
    public final String backdropPath;
    public final TreePVector genreIds;
    public final String originalLanguage;
    public final String originalTitle;
    public final String overview;
    public final long releaseDate;
    public final String posterPath;
    public final double popularity;
    public final String title;
    public final boolean video;
    public final double voteAverage;
    public final int voteCount;

    static private long stringToDate(final String date) {

        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date).getTime();

        } catch (final ParseException error) {
            return -1;
        }
    }

//    static private String dateToString(final Date date) {
//
//        try {
//            return dateToString()
//
//        } catch (final ParseException error) {
//            return "";
//        }
//    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(final Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(final int size) {
            return new Movie[size];
        }
    };

    public Movie(final JSONObject movie) throws JSONException {
        id = movie.getInt("id");
        adult = movie.getBoolean("adult");
        backdropPath = movie.getString("backdrop_path");

        final JSONArray genreIdsJSONArray = movie.getJSONArray("genre_ids");
        final Integer[] mutableArray = new Integer[genreIdsJSONArray.length()];

        for (int i = 0; i < genreIdsJSONArray.length(); i += 1)
            mutableArray[i] = genreIdsJSONArray.getInt(i);

        genreIds = TreePVector.from(Arrays.asList(mutableArray));

        originalLanguage = movie.getString("original_language");
        originalTitle = movie.getString("original_title");
        overview = movie.getString("overview");
        releaseDate = stringToDate(movie.getString("release_date"));
        posterPath = movie.getString("poster_path");
        popularity = movie.getDouble("popularity");
        title = movie.getString("title");
        video = movie.getBoolean("video");
        voteAverage = movie.getDouble("vote_average");
        voteCount = movie.getInt("vote_count");
    }

    public Movie(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_ID));
        adult = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_ADULT)) != 0;
        backdropPath = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_BACKDROP_PATH));

        // TODO: save and read in genreIds from cursors

        genreIds = TreePVector.empty();
        originalLanguage = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_LANGUAGE));
        originalTitle = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
        overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
        releaseDate = cursor.getLong(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
        posterPath = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));
        popularity = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_POPULARITY));
        title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
        video = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VIDEO)) != 0;
        voteAverage = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
        voteCount = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_COUNT));
    }


    private Movie(final Parcel in) {
        id = in.readInt();
        adult = in.readByte() != 0;
        backdropPath = in.readString();

        final ArrayList mutableList = new ArrayList();
        in.readList(mutableList, Integer.class.getClassLoader());
        genreIds = TreePVector.from(mutableList);

        originalLanguage = in.readString();
        originalTitle = in.readString();
        overview = in.readString();
        releaseDate = in.readLong();
        posterPath = in.readString();
        popularity = in.readDouble();
        title = in.readString();
        video = in.readByte() != 0;
        voteAverage = in.readDouble();
        voteCount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(id);
        dest.writeByte(adult ? Byte.MAX_VALUE : 0);
        dest.writeString(backdropPath);
        dest.writeList(genreIds);
        dest.writeString(originalLanguage);
        dest.writeString(originalTitle);
        dest.writeString(overview);
        dest.writeLong(releaseDate);
        dest.writeString(posterPath);
        dest.writeDouble(popularity);
        dest.writeString(title);
        dest.writeByte(video ? Byte.MAX_VALUE : 0);
        dest.writeDouble(voteAverage);
        dest.writeInt(voteCount);
    }

    public ContentValues getContentValues() {
        final ContentValues movieValues = new ContentValues();

        movieValues.put(MovieEntry.COLUMN_ID, id);
        movieValues.put(MovieEntry.COLUMN_ADULT, adult);
        movieValues.put(MovieEntry.COLUMN_BACKDROP_PATH, backdropPath);
        movieValues.put(MovieEntry.COLUMN_GENRE_IDS, genreIds.toString());
        movieValues.put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE, originalLanguage);
        movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
        movieValues.put(MovieEntry.COLUMN_OVERVIEW, overview);
        movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
        movieValues.put(MovieEntry.COLUMN_POSTER_PATH, posterPath);
        movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
        movieValues.put(MovieEntry.COLUMN_TITLE, title);
        movieValues.put(MovieEntry.COLUMN_VIDEO, video);
        movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
        movieValues.put(MovieEntry.COLUMN_VOTE_COUNT, voteCount);

        return movieValues;
    }
}
