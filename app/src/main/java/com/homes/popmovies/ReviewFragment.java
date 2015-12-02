package com.homes.popmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ReviewFragment extends Fragment {
    static public final String REVIEW_PARCEL = "review";

    static public ReviewFragment newInstance(final Review review) {
        final Bundle arguments = new Bundle();
        arguments.putParcelable(REVIEW_PARCEL, review);
        final ReviewFragment fragment = new ReviewFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    // Instance definitions.

    private Optional<Review> mReview;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReview = Optional.bind(getArguments())
            .map(args -> args.getParcelable(REVIEW_PARCEL));
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View rootView = inflater.inflate(
            R.layout.fragment_review,
            container,
            false);

        mReview.map(review -> {
            ((TextView) rootView.findViewById(R.id.author)).setText(review.author);
            ((TextView) rootView.findViewById(R.id.content)).setText(review.content);
            return review;
        });

        return rootView;
    }
}
