package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarException;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<String[]>{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private TextView mErrorMessageTextView;
    private ProgressBar mLoadingIndicator;
    private ForecastAdapter mForecastAdapter;

    // loader ID
    private final static int LOADER_ID = 1;

    @Override
    protected void onStart() {
        super.onStart();

        // whenever the loaderManager has something to notify us of, it will do so through this callback.
        LoaderManager.LoaderCallbacks<String[]> callback = MainActivity.this;

        // Initialize the loader, load data from the server and display on the screen
        getSupportLoaderManager().initLoader(LOADER_ID, null, callback);
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

        mForecastAdapter = new ForecastAdapter(this);
        mRecyclerView.setAdapter(mForecastAdapter);
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
    public Loader<String[]> onCreateLoader(int id, final Bundle args) {
        Log.d("onCreateLoader", "launched");
        return new AsyncTaskLoader<String[]>(this) {

            // hold and cache our weather data
            String[] weatherDataJson;

            @Override
            protected void onStartLoading() {
                Log.d("onStartLoading", "launched");

                mLoadingIndicator.setVisibility(View.VISIBLE);

                // preventing queries just because the user navigated away from the app.
                if(weatherDataJson != null) {
                    deliverResult(weatherDataJson);
                } else {
                    forceLoad();
                }
            }

            @Override
            public String[] loadInBackground() {
                Log.d("loadInBackground", "launched");
                return loadWeatherData();
            }

            @Override
            public void deliverResult(String[] data) {
                Log.d("deliverResult", "launched");
                weatherDataJson = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        Log.d("onLoadFinished", "launched");
        Log.d("arrayLength", String.valueOf(data.length));
        // As soon as the loading is complete, hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        if(data.length != 0) {
            showJsonDataView();
            // set weather data to ForecastAdapter data source and display it via RecyclerView
            mForecastAdapter.setWeatherData(data);
        } else {
            // display an error message
            showErrorMessage();
            mErrorMessageTextView.setText(R.string.error_message);
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {}

    // get the user's preferred location and temperature units to execute AsyncTask for
    // requesting data from the server, return response as a json string
    private String[] loadWeatherData() {

        // location from the preferences
        String locationParameter = NetworkUtils.getPreferredLocation(MainActivity.this);
        Log.d("locationParameter", locationParameter);

        // temperature units from the preferences
        String unitsParameter = NetworkUtils.getPreferredTempUnits(MainActivity.this);
        Log.d("unitsParameter", unitsParameter);

        // build url for the forecast request
        URL url = NetworkUtils.buildUrl(locationParameter, unitsParameter);

        Log.d("url", url.toString());

        try {
            // get response from the server in json format
            String jsonString = NetworkUtils.getResponseFromHttpUrl(url);
            return OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(this, jsonString);
        } catch(IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    // COMPLETED (14) Create a method called showJsonDataView to show the data and hide the error
    private void showJsonDataView() {
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // COMPLETED (15) Create a method called showErrorMessage to show the error and hide the data
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void openLocationMap() {
        String addressString = "1600 Ampitheatre Parkway, CA";
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


}