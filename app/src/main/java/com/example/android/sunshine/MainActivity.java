package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarException;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private TextView mErrorMessageTextView;
    private ProgressBar mLoadingIndicator;
    private ForecastAdapter mForecastAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        // load data from the server and display on the screen
        new WeatherForecastAsyncTask().execute();
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

        mForecastAdapter = new ForecastAdapter();
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
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // class for performing network requests
    private class WeatherForecastAsyncTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            return loadWeatherData();
        }

        @Override
        protected void onPreExecute() {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String[] weatherData) {

            // As soon as the loading is complete, hide the loading indicator
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            for(String dayforecast : weatherData) {
                Log.d("weatherData", dayforecast);
            }

            if(weatherData != null && weatherData.length != 0) {
                showJsonDataView();
                // set weather data to ForecastAdapter data source and display it via RecyclerView
                mForecastAdapter.setWeatherData(weatherData);
            } else {
                // display an error message
                showErrorMessage();
                mErrorMessageTextView.setText(R.string.error_message);
            }
        }
    }

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
}