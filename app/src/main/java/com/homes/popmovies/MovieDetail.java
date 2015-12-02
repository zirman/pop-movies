package com.homes.popmovies;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class MovieDetail {
    public final int id;
    public final int budget;
    public final String homepage;
    public final String imdbId;
    public final int revenue;
    public final int runtime;
    public final String status;
    public final String tagline;

    public MovieDetail(final JSONObject detail) throws JSONException, ParseException {
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
