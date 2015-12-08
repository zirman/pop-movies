package com.homes.popmovies;

import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;

public class PopMovies extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Picasso.setSingletonInstance(new Picasso.Builder(this)
            .executor(Executors.newSingleThreadScheduledExecutor())
            //.memoryCache(Cache.NONE)
            .indicatorsEnabled(true)
            //.loggingEnabled(true)
            .downloader(new OkHttpDownloader(this, Integer.MAX_VALUE))
            .build());
    }
}
