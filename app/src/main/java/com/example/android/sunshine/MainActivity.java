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
import android.widget.TextView;

import com.example.android.sunshine.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private TextView mUrlTextView;
    private TextView mResultTextView;

    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        Button mMakeRequestButton = (Button) findViewById(R.id.search_button);
        mUrlTextView = (TextView) findViewById(R.id.tv_url);
        mResultTextView = (TextView) findViewById(R.id.tv_result_query);

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
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String requestResult) {
            // display result in the mResultTextView
            mResultTextView.setText(requestResult);
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
}