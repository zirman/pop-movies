package com.homes.popmovies.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.homes.popmovies.R;
import com.homes.popmovies.fragments.DetailFragment;
import com.homes.popmovies.fragments.MovieGridFragment;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {

    private final BehaviorSubject<MovieGridFragment> mMovieGridFragments =
        BehaviorSubject.<MovieGridFragment>create();

    private final Subscription mItemClicksSubscription =
        Observable.switchOnNext(mMovieGridFragments.map(MovieGridFragment::itemClicks))
            .subscribe(movie -> {

                if (findViewById(R.id.fragment_detail) == null) {
                    Intent intent = new Intent(this, DetailActivity.class);
                    intent.putExtra(DetailFragment.MOVIE_PARCEL, movie);
                    startActivity(intent);

                } else {

                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_detail, DetailFragment.newInstance(movie))
                        .commit();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMovieGridFragments.onNext((MovieGridFragment) getSupportFragmentManager()
            .findFragmentById(R.id.fragment_movie_grid));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMovieGridFragments.onCompleted();
        mItemClicksSubscription.unsubscribe();
    }
}
