/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TextView mUrlTextView;
    private TextView mResultsTextView;
    private TextView mErrorMessageTextView;
    private ProgressBar mLoadingIndicator;

    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        Button mMakeRequestButton = (Button) findViewById(R.id.search_button);
        mUrlTextView = (TextView) findViewById(R.id.tv_url);
        mResultsTextView = (TextView) findViewById(R.id.tv_result_query);
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_message);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // set click listener to the mMakeRequestButton
        mMakeRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new WeatherForecastAsyncTask().execute();
            }
        });
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
    private class WeatherForecastAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return loadWeatherData();
        }

        @Override
        protected void onPreExecute() {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String requestResult) {

            // As soon as the loading is complete, hide the loading indicator
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if(requestResult != null && !requestResult.equals("")) {
                showJsonDataView();
                // display result in the mResultTextView
                mResultsTextView.setText(requestResult);
            } else {
                // display an error message
                showErrorMessage();
                mErrorMessageTextView.setText(R.string.error_message);
            }

            // display url in the mUrlTextView
            mUrlTextView.setText(mUrl);
        }
    }

    // get the user's preferred location and temperature units to execute AsyncTask for
    // requesting data from the server, return response as a json string
    private String loadWeatherData() {

        // location from the preferences
        String locationParameter = NetworkUtils.getPreferredLocation(MainActivity.this);
        Log.d("locationParameter", locationParameter);

        // temperature units from the preferences
        String unitsParameter = NetworkUtils.getPreferredTempUnits(MainActivity.this);
        Log.d("unitsParameter", unitsParameter);

        // build url for the forecast request
        URL url = NetworkUtils.buildUrl(locationParameter, unitsParameter);
        mUrl = url.toString();

        try {
            // get response from the server
            return NetworkUtils.getResponseFromHttpUrl(url);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // COMPLETED (14) Create a method called showJsonDataView to show the data and hide the error
    private void showJsonDataView() {
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mResultsTextView.setVisibility(View.VISIBLE);
    }

    // COMPLETED (15) Create a method called showErrorMessage to show the error and hide the data
    private void showErrorMessage() {
        mResultsTextView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }
}