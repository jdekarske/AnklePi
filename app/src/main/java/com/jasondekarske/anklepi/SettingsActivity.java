package com.jasondekarske.anklepi;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;

public class SettingsActivity extends AppCompatActivity {
    public static final String defaultwalking = "defaultwalking";
    public static final String defaultsquat = "defaultsquat";
    public static final String defaultdeadlift = "defaultdeadlift";
    public static final String defaultpress = "defaultpress";
    public static final String loglift = "loglift_switch";
    public static final String logpressure = "logpressure_switch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
