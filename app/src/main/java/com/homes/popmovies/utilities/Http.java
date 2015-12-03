package com.homes.popmovies.utilities;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
    private static final String LOG_TAG = Http.class.getSimpleName();

//    public static Observable<String> requestObservable(final URL url) {
//
//        return Observable.<String>create(subscriber -> {
//
//            try {
//                subscriber.onNext(request(url));
//
//            } catch (final IOException error) {
//                subscriber.onError(error);
//
//            } finally {
//                subscriber.onCompleted();
//            }
//
//        }).subscribeOn(Schedulers.io());  // Ensure this runs off main thread
//    }

    public static String request(final URL url) throws IOException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();

            final InputStream inputStream = urlConnection.getInputStream();
            final StringBuilder buffer = new StringBuilder();

            if (inputStream == null) {
                throw new IOException("null input stream");
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            return buffer.toString();

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
    }
}
