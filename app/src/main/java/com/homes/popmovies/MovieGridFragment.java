package com.homes.popmovies;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MovieGridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MovieGridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class MovieGridFragment extends Fragment {
    private static final String LOG_TAG =
            MovieGridFragment.class.getSimpleName();

    ArrayAdapter arrayAdpater;

    private OnFragmentInteractionListener mListener;

    public static MovieGridFragment newInstance() {
        MovieGridFragment fragment = new MovieGridFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public MovieGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_movie_grid, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;

        } catch (ClassCastException e) {

            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/
     * communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnFragmentInteractionListener {

        // TODO: Update argument type and name

        public void onFragmentInteraction(Uri uri);
    }

    private void updateMovies() {
        new FetchMoviesTask().execute();
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String ... zips) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String[] moviesJsonStr;

            try {

                URL url = new URL(
                        Uri.parse("http://api.themoviedb.org/3/discover/movie?sort_by=&api_key=")
                                .buildUpon()
                                .appendQueryParameter(
                                        "sort_by",
                                        "popularity.desc")
                                .appendQueryParameter(
                                        "api_key",
                                        getString(R.string.tmdb_api_key))
                                .build()
                                .toString());

                // Create the request to OpenWeatherMap, and open the connection

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {

                    // Nothing to do.

                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {

                    // Since it's JSON, adding a newline isn't necessary (it
                    // won't affect parsing) But it does make debugging a *lot*
                    // easier if you print out the completed buffer for
                    // debugging.

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {

                    // Stream was empty.  No point in parsing.

                    return null;
                }

                try {

                    forecastJsonStr = getWeatherDataFromJson(
                            buffer.toString(),
                            7);

                } catch (JSONException exception) {
                    Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                    return null;
                }

            } catch (IOException e) {

                Log.e(LOG_TAG, "Error ", e);

                // If the code didn't successfully get the weather data, there's
                // no point in attemping to parse it.

                return null;

            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {

                    try {
                        reader.close();

                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return forecastJsonStr;
        }

        @Override
        protected void onPostExecute(String[] json) {
            arrayAdapter.clear();
            arrayAdapter.addAll(json);
        }

        private String getReadableDateString(long time){

            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to
            // valid date.

            SimpleDateFormat shortenedDateFormat =
                    new SimpleDateFormat("EEE MMM dd");

            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */

        private String formatHighLows(double high, double low) {

            // For presentation, assume the user doesn't care about tenths of a
            // degree.

            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(
                            getActivity());

//            String units = sharedPref.getString(
//                    getString(R.string.pref_temperature_units_key),
//                    getString(R.string.pref_temperature_units_default));
//
//            if (units.equals("imperial")) {
//                high = (high * 9 / 5) + 32;
//                low = (low * 9 / 5) + 32;
//            }

            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the
         * wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and
         * converts it into an Object hierarchy for us.
         */

        private String[] getWeatherDataFromJson(
                String forecastJsonStr,
                int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.

            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city
            // that is being asked for, which means that we need to know the GMT
            // offset to translate this data properly.

            // Since this data is also sent in-order and the first day is always
            // the current day, we're going to take advantage of that to get a
            // nice normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a
            // mess.

            int julianStartDay = Time.getJulianDay(
                    System.currentTimeMillis(),
                    dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];

            for(int i = 0; i < weatherArray.length(); i += 1) {

                // For now, using the format "Day, description, hi/low"

                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read
                // "1400356800" as "this saturday".

                long dateTime;

                // Cheating to convert this to UTC time, which is what we want
                // anyhow

                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1
                // element long.

                JSONObject weatherObject =
                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

                description =
                        weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to
                // name variables "temp" when working with temperature.  It
                // confuses everybody.

                JSONObject temperatureObject =
                        dayForecast.getJSONObject(OWM_TEMPERATURE);

                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;
        }
    }
}
