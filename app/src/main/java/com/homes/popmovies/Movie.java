package com.homes.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pcollections.TreePVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Immutable data transfer object for movies

public class Movie implements Parcelable {
    public final long id;
    public final boolean adult;
    public final String backdropPath;
    public final TreePVector<Integer> genreIds;
    public final String originalLanguage;
    public final String originalTitle;
    public final String overview;
    public final String releaseDate;
    public final String posterPath;
    public final double popularity;
    public final String title;
    public final boolean video;
    public final double voteAverage;
    public final int voteCount;

    public Movie(Parcel in) {
        id = in.readLong();
        adult = in.readByte() != 0;
        backdropPath = in.readString();

        final List<Integer> mutableList = new ArrayList();
        in.readList(mutableList, Integer.class.getClassLoader());
        genreIds = TreePVector.from(mutableList);

        originalLanguage = in.readString();
        originalTitle = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        posterPath = in.readString();
        popularity = in.readDouble();
        title = in.readString();
        video = in.readByte() != 0;
        voteAverage = in.readDouble();
        voteCount = in.readInt();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeByte(adult ? Byte.MAX_VALUE : 0);
        dest.writeString(backdropPath);
        dest.writeList(genreIds);
        dest.writeString(originalLanguage);
        dest.writeString(originalTitle);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeString(posterPath);
        dest.writeDouble(popularity);
        dest.writeString(title);
        dest.writeByte(video ? Byte.MAX_VALUE : 0);
        dest.writeDouble(voteAverage);
        dest.writeInt(voteCount);
    }

    public Movie(final JSONObject movie) throws JSONException {
        id = movie.getLong("id");
        adult = movie.getBoolean("adult");
        backdropPath = movie.getString("backdrop_path");

        JSONArray genreIdsJSONArray = movie.getJSONArray("genre_ids");
        Integer[] mutableArray = new Integer[genreIdsJSONArray.length()];
        for (int i = 0; i < genreIdsJSONArray.length(); i += 1)
            mutableArray[i] = genreIdsJSONArray.getInt(i);
        genreIds = TreePVector.from(Arrays.asList(mutableArray));

        originalLanguage = movie.getString("original_language");
        originalTitle = movie.getString("original_title");
        overview = movie.getString("overview");
        releaseDate = movie.getString("release_date");
        posterPath = movie.getString("poster_path");
        popularity = movie.getDouble("popularity");
        title = movie.getString("title");
        video = movie.getBoolean("video");
        voteAverage = movie.getDouble("vote_average");
        voteCount = movie.getInt("vote_count");
    }
}
