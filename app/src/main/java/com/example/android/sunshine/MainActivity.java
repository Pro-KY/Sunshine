package com.example.android.sunshine;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Context mContext = MainActivity.this;

    private RecyclerView mRecyclerView;
    private TextView mErrorMessageTextView;
    private ProgressBar mLoadingIndicator;
    private ForecastAdapter mForecastAdapter;

    // flag for preference updates, indicates whether preferences have been updated
    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;

    // loader ID
    private final static int LOADER_ID = 1;

    @Override
    protected void onStart() {
        super.onStart();

        // check whether preferences have been updated
        if(PREFERENCES_HAVE_BEEN_UPDATED) {
            Log.d(LOG_TAG, "onStart: preferences were updated");
            getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_message);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this, mContext);
        mRecyclerView.setAdapter(mForecastAdapter);

        LoaderManager.LoaderCallbacks<Cursor> callback = MainActivity.this;
        // initialize the loader when activity is created
        getSupportLoaderManager().initLoader(LOADER_ID, null, callback);

        // Register MainActivity as an OnPreferenceChangedListener to receive a callback when a
        // SharedPreference has changed
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_map:
                openLocationMap();
                return true;
            case R.id.action_refresh:
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
                Log.d("restartLoader", "load new data");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(String viewHolderData) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("dayForecast", viewHolderData);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        Log.d("onCreateLoader", "launched");
        return new AsyncTaskLoader<Cursor>(this) {

            // hold and cache our weather data
            Cursor cursorWeatherData;

            @Override
            protected void onStartLoading() {
                Log.d("onStartLoading", "launched");

                mLoadingIndicator.setVisibility(View.VISIBLE);

                // preventing queries just because the user navigated away from the app.
                if(cursorWeatherData != null) {
                    deliverResult(cursorWeatherData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                Log.d("loadInBackground", "launched");
                ContentValues[] weatherData = loadWeatherData();

                Uri uri = WeatherContract.WeatherEntry.CONTENT_URI_DIR;
                int rowsInserted = 0;

                if(weatherData != null) {
                    rowsInserted = getContentResolver().bulkInsert(uri, weatherData);
                    Log.d("rowsInserted", String.valueOf(rowsInserted));
                }

                if(rowsInserted != 0) {
                    return getContentResolver().query(uri, null, null, null, null);
                }

                return null;
            }

            @Override
            public void deliverResult(Cursor data) {
                Log.d("deliverResult", "launched");
                cursorWeatherData = data;
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursorData) {
        // As soon as the loading is complete, hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        if(cursorData != null) {
            showJsonDataView();
            // set weather data to ForecastAdapter data source and display it via RecyclerView
            mForecastAdapter.swapCursor(cursorData);
        } else {
            // display an error message
            showErrorMessage();
            mErrorMessageTextView.setText(R.string.error_message);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    // get the user's preferred location and temperature units to execute AsyncTask for
    // requesting data from the server, return response as a json string
    private ContentValues[] loadWeatherData() {

        // location from the preferences
        String locationParameter = SunshinePreferences.getPreferredWeatherLocation(this);
        Log.d("locationParameter", locationParameter);

        // temperature units from the preferences
        String unitsParameter = SunshinePreferences.getPreferredTemperatureUnits(this);
        Log.d("unitsParameter", unitsParameter);

        // build url for the forecast request
        URL url = NetworkUtils.buildUrl(locationParameter, unitsParameter);

        Log.d("url", url.toString());

        try {
            // get response from the server in json format
            String jsonString = NetworkUtils.getResponseFromHttpUrl(url);
            return OpenWeatherJsonUtils.getFullWeatherDataFromJson(this, jsonString);
        } catch(IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    // show the data and hide the error
    private void showJsonDataView() {
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // show the error and hide the data
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void openLocationMap() {
        String addressString = SunshinePreferences.getCityName(this);
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() +
                    ", no receiving apps installed!");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Set this flag to true so that when control returns to MainActivity, it can refresh the
        // data.
        PREFERENCES_HAVE_BEEN_UPDATED = true;
    }
}