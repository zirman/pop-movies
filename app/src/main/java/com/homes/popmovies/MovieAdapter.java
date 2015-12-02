package com.homes.popmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.homes.popmovies.data.MovieContract.MovieEntry;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends CursorAdapter {
    private static final String BASE_PATH = "http://image.tmdb.org/t/p/w500";

    public MovieAdapter(final Context context, final Cursor cursor, final int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final ImageView view = new ImageView(mContext);
        view.setAdjustViewBounds(true);
        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final String posterPath =
            cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));

        Picasso.with(mContext)
            .load(BASE_PATH + posterPath)
            .into((ImageView) view);
    }
}
