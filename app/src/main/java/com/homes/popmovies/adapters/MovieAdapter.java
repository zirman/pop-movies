package com.homes.popmovies.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.homes.popmovies.R;
import com.homes.popmovies.dtobjs.Movie;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import org.pcollections.TreePVector;

import java.util.Arrays;

import rx.Observable;
import rx.subjects.PublishSubject;

public class MovieAdapter extends Adapter<MovieAdapter.ViewHolder> {

    private static final TreePVector<Integer> POSTER_SIZES =
        TreePVector.from(Arrays.asList(92, 154, 185, 342, 500, 780));

    private static final int POSTER_WIDTH = POSTER_SIZES.get(2);
    private static final double POSTER_ASPECT_RATIO = 1.5;
    private static final String BASE_PATH = "http://image.tmdb.org/t/p/w" + POSTER_WIDTH;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final Context mContext;
        public final ImageView mImageView;

        public ViewHolder(final View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            mImageView = (ImageView) itemView.findViewById(R.id.movie_poster_image);
        }
    }

    private final TreePVector<Movie> mMovies;

    private final PublishSubject<Movie> mItemClicks = PublishSubject.<Movie>create();

    public MovieAdapter(final TreePVector<Movie> movies) {
        mMovies = movies;
    }

    public Observable<Movie> itemClicks() {
        return mItemClicks;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        final ViewHolder viewHolder = new ViewHolder(
            LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.movie_poster_item, parent, false));

        final double width = ((double) parent.getWidth()) /
            ((GridLayoutManager) ((RecyclerView) parent).getLayoutManager()).getSpanCount();

        viewHolder.mImageView.setLayoutParams(new LinearLayout.LayoutParams(
            (int) Math.round(width),
            (int) Math.round(POSTER_ASPECT_RATIO * width)));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Movie movie = mMovies.get(position);
        holder.mImageView.setImageResource(android.R.color.transparent);
        holder.mImageView.setContentDescription(movie.title);

        RxView.clickEvents(holder.mImageView).subscribe(viewClickEvent -> {
            mItemClicks.onNext(movie);
        });

        Picasso.with(holder.mContext)
            .load(BASE_PATH + movie.posterPath)
            .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }
}
