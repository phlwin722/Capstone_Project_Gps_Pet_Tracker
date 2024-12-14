package com.example.gps_pet_tracker;

import android.content.Context; // Import the Context class to access application resources and services

import com.mapbox.navigation.core.MapboxNavigation; // Import MapboxNavigation to manage navigation sessions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp; // Import MapboxNavigationApp for setting up navigation
import com.mapbox.navigation.base.options.NavigationOptions; // Import NavigationOptions for configuring navigation options

public class NavigationManager {
    private static MapboxNavigation mapboxNavigation; // Static variable to hold the single instance of MapboxNavigation

    // Method to get the instance of MapboxNavigation, or create it if it doesn't exist
    public static MapboxNavigation getInstance(Context context) {
        // Check if the instance is null (i.e., not created yet)
        if (mapboxNavigation == null) {
            // Create NavigationOptions with the access token from resources
            NavigationOptions navigationOptions = new NavigationOptions.Builder(context)
                    .accessToken(context.getString(R.string.mapbox_access_token)) // Set the Mapbox access token
                    .build(); // Build the NavigationOptions object

            // Set up MapboxNavigationApp with the provided options
            MapboxNavigationApp.setup(navigationOptions);
            // Create a new instance of MapboxNavigation with the configured options
            mapboxNavigation = new MapboxNavigation(navigationOptions);
        }
        // Return the singleton instance of MapboxNavigation
        return mapboxNavigation;
    }

    // Method to destroy the MapboxNavigation instance
    public static void destroyInstance() {
        // Check if the instance is not null (i.e., it exists)
        if (mapboxNavigation != null) {
            // Call onDestroy to clean up resources and stop navigation
            mapboxNavigation.onDestroy();
            // Set the instance to null to indicate it has been destroyed
            mapboxNavigation = null;
        }
    }
}
