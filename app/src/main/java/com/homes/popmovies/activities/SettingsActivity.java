package com.homes.popmovies.activities;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.homes.popmovies.R;

public class SettingsActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(
                R.string.pref_sort_by_key)));
    }

    private void bindPreferenceSummaryToValue(final Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(
            preference,
            PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object value) {
        final String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            final ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);

            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }

        } else {
            preference.setSummary(stringValue);
        }

        return true;
    }
}
