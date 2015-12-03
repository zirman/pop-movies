package com.homes.popmovies.dtobjs;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.homes.popmovies.data.MovieContract.VideoEntry;

import org.json.JSONException;
import org.json.JSONObject;

// Immutable data transfer object for videos

public class Video implements Parcelable {
    public final String id;
    public final String iso_639_1;
    public final String key;
    public final String name;
    public final String site;
    public final int size;
    public final String type;

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(final Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(final int size) {
            return new Video[size];
        }
    };

    public Video(final JSONObject video) throws JSONException {
        id = video.getString("id");
        iso_639_1 = video.getString("iso_639_1");
        key = video.getString("key");
        name = video.getString("name");
        site = video.getString("site");
        size = video.getInt("size");
        type = video.getString("type");
    }

    private Video(final Parcel in) {
        id = in.readString();
        iso_639_1 = in.readString();
        key = in.readString();
        name = in.readString();
        site = in.readString();
        size = in.readInt();
        type = in.readString();
    }

    public Video(final Cursor cursor) {
        id = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_ID));
        iso_639_1 = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_ISO_639_1));
        key = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_KEY));
        name = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_NAME));
        site = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_SITE));
        size = cursor.getInt(cursor.getColumnIndex(VideoEntry.COLUMN_SIZE));
        type = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_TYPE));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(id);
        dest.writeString(iso_639_1);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
        dest.writeInt(size);
        dest.writeString(type);
    }

    public ContentValues toContentValues() {
        final ContentValues videoValues = new ContentValues();
        videoValues.put(VideoEntry.COLUMN_ID, id);
        videoValues.put(VideoEntry.COLUMN_ISO_639_1, iso_639_1);
        videoValues.put(VideoEntry.COLUMN_KEY, key);
        videoValues.put(VideoEntry.COLUMN_NAME, name);
        videoValues.put(VideoEntry.COLUMN_SITE, site);
        videoValues.put(VideoEntry.COLUMN_SIZE, size);
        videoValues.put(VideoEntry.COLUMN_TYPE, type);
        return videoValues;
    }

    public Uri toVndYoutubeUri() {
        return Uri.parse("vnd.youtube:" + key);
    }

    public Uri toYoutubeUri() {
        return Uri.parse("https://www.youtube.com/watch")
            .buildUpon()
            .appendQueryParameter("v", key)
            .build();
    }
}
