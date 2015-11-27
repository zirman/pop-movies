package com.homes.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

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
}
