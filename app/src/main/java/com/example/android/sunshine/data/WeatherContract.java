package com.example.android.sunshine.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.example.android.sunshine.utilities.SunshineDateUtils;


public class WeatherContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.example.android.sunshine";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // the path for the "weather" directory
    public static final String PATH_WEATHER = "weather";

    // A single item of data. The number(#) here is meant to match a date
    public static final String PATH_WEATHER_WITH_DATE = "weather/#";

    public static final class WeatherEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final String TABLE_NAME = "weather";

        // weather ID as returned by API, used to identify the icon to be used
        public static final String COLUMN_DATE = "date";         // long
        // min temperature in °C for the day
        public static final String COLUMN_MIN_TEMP = "min";      // float
        // max temperature in °C for the day
        public static final String COLUMN_MAX_TEMP = "max";      // float
        public static final String COLUMN_PRESSURE = "pressure"; // float
        public static final String COLUMN_HUMIDITY = "humidity"; // float
        public static final String COLUMN_WEATHER_ID = "weather_id"; // int
        public static final String COLUMN_WIND_SPEED = "wind";   // float
        // meteorological degrees (e.g, 0 is north, 180 is south).
        public static final String COLUMN_DEGREES = "degrees";   // float


        // Returns just the selection part of the weather query from a normalized today value.
        public static String getSqlSelectForTodayOnwards() {
            long normalizedUtcNow = SunshineDateUtils.normalizeDate(System.currentTimeMillis());
            return WeatherContract.WeatherEntry.COLUMN_DATE + " >= " + normalizedUtcNow;
        }

        // Returns just the selection part of the weather query from a normalized today value.
        public static Uri buildWeatherUriWithDate(long date) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(date))
                    .build();
        }
    }
}
