package com.example.meriniguanaccounting.Preferences;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.meriniguanaccounting.R;
import com.example.meriniguanaccounting.Utils.Util;

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.accounting_preferences);

        setThemeFromSharedPreferencesAndThemeListPreferenceLabel();

//        setThemeListPreferenceLabel();
    }

    private void setThemeListPreferenceLabel() {
        ListPreference themeListPreference = (ListPreference) findPreference(Util.themeKey);
        String value = themeListPreference.getValue();
        themeListPreference.setSummary(value);
    }

    private void setThemeFromSharedPreferencesAndThemeListPreferenceLabel() {
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

        ListPreference themeListPreference = (ListPreference) findPreference(Util.themeKey);

        switch (sharedPreferences.getString(Util.themeKey, "Light")) {
            case "Light":
                themeListPreference.setSummary(getString(R.string.light));
                ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                themeListPreference.setSummary(getString(R.string.dark));
                ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Set by Battery Saver":
                themeListPreference.setSummary(getString(R.string.set_by_battery_saver));
                ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Util.themeKey)) {
            setThemeFromSharedPreferencesAndThemeListPreferenceLabel();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
