package com.homes.popmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.pcollections.TreePVector;

import java.util.Arrays;

public class MoviePosterAdapter extends BaseAdapter {

    private static final String BASE_PATH =
        "http://image.tmdb.org/t/p/w500";

    private final Context mContext;
    private final TreePVector<Movie> mMovies;

    public MoviePosterAdapter(final Context context, final Movie[] movies) {
        super();
        mContext = context;
        mMovies = TreePVector.from(Arrays.asList(movies));
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    public Movie getItem(final int position) {
        return mMovies.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return mMovies.get(position).id;
    }

    @Override
    public View getView(
        final int position,
        final View convertView,
        final ViewGroup parent) {

        final ImageView view = convertView == null ?
            new ImageView(mContext) :
            (ImageView) convertView;

        view.setAdjustViewBounds(true);

        Picasso.with(mContext)
            .load(BASE_PATH + getItem(position).posterPath)
            .into(view);

        return view;
    }
}
