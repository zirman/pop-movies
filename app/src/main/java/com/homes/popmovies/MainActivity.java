package com.homes.popmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    //static private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MovieGridFragment movieGridFragment = (MovieGridFragment)
            getSupportFragmentManager().findFragmentById(R.id.fragment_movie_grid);

        movieGridFragment.getItemClickObservable().subscribe(movie -> {

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
}
