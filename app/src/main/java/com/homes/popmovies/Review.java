package com.homes.popmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.homes.popmovies.data.MovieContract.ReviewEntry;

import org.json.JSONException;
import org.json.JSONObject;

// Immutable data transfer object for reviews

public class Review implements Parcelable {
    public final String id;
    public final String author;
    public final String content;
    public final String url;

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(final Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(final int size) {
            return new Review[size];
        }
    };

    public Review(final JSONObject video) throws JSONException {
        id = video.getString("id");
        author = video.getString("author");
        content = video.getString("content");
        url = video.getString("url");
    }

    private Review(final Parcel in) {
        id = in.readString();
        author = in.readString();
        content = in.readString();
        url = in.readString();
    }

    public Review(final Cursor cursor) {
        id = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_ID));
        author = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR));
        content = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_CONTENT));
        url = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_URL));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(id);
        dest.writeString(author);
        dest.writeString(content);
        dest.writeString(url);
    }

    public ContentValues toContentValues() {
        final ContentValues videoValues = new ContentValues();
        videoValues.put(ReviewEntry.COLUMN_ID, id);
        videoValues.put(ReviewEntry.COLUMN_AUTHOR, author);
        videoValues.put(ReviewEntry.COLUMN_CONTENT, content);
        videoValues.put(ReviewEntry.COLUMN_URL, url);
        return videoValues;
    }
}
