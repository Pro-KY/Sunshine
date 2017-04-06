package com.example.android.sunshine.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class SunshineSyncTask {

    // send http request, parse json data and insert it into ContentProvider
    synchronized public static void syncWeather(Context context) {
        try {
            // build a URL based on sharedPreferences values
            URL weatherRequestUrl = NetworkUtils.buildUrl(context);

            // set http connection and use the URL to retrieve the JSON
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

            // Parse the JSON into a list of weather values
            ContentValues[] weatherValues = OpenWeatherJsonUtils.
                    getWeatherContentValuesFromJson(jsonWeatherResponse);

            if(weatherValues != null && weatherValues.length != 0) {
                ContentResolver sunshineContentResolver = context.getContentResolver();

                // Delete old weather data
                sunshineContentResolver.delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);

                // insert our new weather data into Sunshine's ContentProvider
                sunshineContentResolver.bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        weatherValues);
            }
        } catch(Exception e) {
            // Server probably invalid
            e.printStackTrace();
        }

    }
}
