package com.homes.popmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment {
    private static final String MOVIE_PARCEL = "movie";

    private static final String BASE_PATH =
            "http://image.tmdb.org/t/p/w500";

    private Optional<Movie> movie;

    public static DetailFragment newInstance(final Movie movie) {
        final Bundle arguments = new Bundle();
        arguments.putParcelable(MOVIE_PARCEL, movie);
        final DetailFragment fragment = new DetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public DetailFragment() {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        movie = Optional.unit(getArguments())
                .map(args -> args.getParcelable(MOVIE_PARCEL));
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.fragment_detail,
                container,
                false);

        movie.ifPresent(movie -> {
            ((TextView) rootView.findViewById(R.id.title)).setText(movie.title);

            Picasso.with(getActivity())
                    .load(BASE_PATH + movie.posterPath)
                    .into(((ImageView) rootView.findViewById(R.id.poster_image)));

            ((TextView) rootView.findViewById(R.id.year_text))
                    .setText(movie.releaseDate.split("-")[0]);

            ((TextView) rootView.findViewById(R.id.runtime_text))
                    .setText("time");

            ((TextView) rootView.findViewById(R.id.rating_text))
                    .setText(String.valueOf(movie.voteAverage) + "/10");

            ((TextView) rootView.findViewById(R.id.overview_text))
                    .setText(movie.overview);
        });

        return rootView;
    }

//    @Override
//    public void onActivityCreated(final Bundle inState) {
//        super.onActivityCreated(inState);
//        Bundle foo = inState.getBundle(MOVIE_BUNDLE);
//        if (foo != null)
//        movie = new Movie(foo);
//    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MOVIE_PARCEL, movie.get());
    }
}
