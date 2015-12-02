package com.homes.popmovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

public class VideoFragment extends Fragment {
    static public final String VIDEO_PARCEL = "video";

    static public VideoFragment newInstance(final Video video) {
        final Bundle arguments = new Bundle();
        arguments.putParcelable(VIDEO_PARCEL, video);
        final VideoFragment fragment = new VideoFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    // Instance definitions.

    private Optional<Video> mVideo;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideo = Optional.bind(getArguments())
            .map(args -> args.getParcelable(VIDEO_PARCEL));
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View rootView = inflater.inflate(
            R.layout.fragment_video,
            container,
            false);

        mVideo.flatMap(video -> {
            ((TextView) rootView.findViewById(R.id.video_description)).setText(video.name);

            RxView.clickEvents(rootView).subscribe(viewClickEvent -> {

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, video.toVndYoutubeUri()));

                } catch (final ActivityNotFoundException error) {
                    startActivity(new Intent(Intent.ACTION_VIEW, video.toYoutubeUri()));
                }
            });

            return Optional.empty();
        });

        return rootView;
    }
}
