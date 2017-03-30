package com.example.android.sunshine;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;


public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private final static int DETAIL_LOADER_ID = 2;

    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTemperatureView;
    private TextView mLowTemperatureView;
    private TextView mHumidityView;
    private TextView mPressureView;
    private TextView mWindView;

    private String mForecastSummary;

    private Uri mUri;

    public static final String[] DETAIL_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_CONDITION_ID = 1;
    public static final int INDEX_WEATHER_MAX_TEMP = 2;
    public static final int INDEX_WEATHER_MIN_TEMP = 3;
    public static final int INDEX_WEATHER_HUMIDITY = 4;
    public static final int INDEX_WEATHER_PRESSURE = 5;
    public static final int INDEX_WEATHER_WIND_SPEED = 6;
    public static final int INDEX_WEATHER_DEGREES = 7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        mDateView = (TextView) findViewById(R.id.tv_date);
        mDescriptionView = (TextView) findViewById(R.id.tv_description);
        mHighTemperatureView = (TextView) findViewById(R.id.tv_high_temperature);
        mLowTemperatureView = (TextView) findViewById(R.id.tv_low_temperature);
        mHumidityView = (TextView) findViewById(R.id.tv_humidity);
        mPressureView = (TextView) findViewById(R.id.tv_pressure);
        mWindView = (TextView) findViewById(R.id.tv_wind);

        Intent intent = getIntent();
        mUri = intent.getData();

        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);

        //detailForecast.setText(mDetailWeatherForecast);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_share:
                //shareDayForecast(mDetailWeatherForecast);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    // utility method for sharing detail weather data via shareIntent
    private void shareDayForecast(String text) {
        String mimeType = "text/plain";
        String chooserTitle = "Choose an app...";

        ShareCompat.IntentBuilder
                .from(this)
                .setType(mimeType)
                .setChooserTitle(chooserTitle)
                .setText(text)
                .startChooser();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case DETAIL_LOADER_ID:

                // query the weather details by date
                return new CursorLoader(
                        this,
                        mUri,
                        DETAIL_FORECAST_PROJECTION,
                        null,
                        null,
                        null
                );
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Gets all the weather detail information from the cursor and displays them
        // in the appropriate views.

        // validate cursor data
        boolean cursorHasValidData = false;

        if(data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }

        if(!cursorHasValidData) {
            return;
        }

        // extract all the relevant data from the Cursor and bind the data to UI
        long dateInMillis = data.getLong(DetailActivity.INDEX_WEATHER_DATE);
        String dateString = SunshineDateUtils.getFriendlyDateString(this, dateInMillis, false);
        mDateView.setText(dateString);

        int weatherId = data.getInt(DetailActivity.INDEX_WEATHER_CONDITION_ID);
        String descriptionText = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);
        mDescriptionView.setText(descriptionText);

        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */

        float highInCelsius = data.getFloat(DetailActivity.INDEX_WEATHER_MAX_TEMP);
        String highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius);
        mLowTemperatureView.setText(String.valueOf(highString));

        float lowInCelsius = data.getFloat(DetailActivity.INDEX_WEATHER_MIN_TEMP);
        String lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius);
        mHighTemperatureView.setText(String.valueOf(lowString));

        float humidity = data.getFloat(DetailActivity.INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);
        mHumidityView.setText(String.valueOf(humidityString));

        float pressure = data.getFloat(DetailActivity.INDEX_WEATHER_PRESSURE);
        mPressureView.setText(String.valueOf(pressure));

        float windSpeed = data.getFloat(DetailActivity.INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(DetailActivity.INDEX_WEATHER_DEGREES);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);
        mWindView.setText(String.valueOf(windString));
    }
}
