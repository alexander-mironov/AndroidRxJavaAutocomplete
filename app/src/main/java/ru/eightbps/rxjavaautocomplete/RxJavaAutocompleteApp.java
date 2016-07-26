package ru.eightbps.rxjavaautocomplete;

import android.app.Application;

public class RxJavaAutocompleteApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String googleMapsKey = getString(R.string.google_maps_key);
        if (googleMapsKey.isEmpty()) {
            throw new RuntimeException("You should add your Google Maps API Key!");
        }
    }
}
