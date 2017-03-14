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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mWeatherTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);

        String[] dummyWeatherData = {
                "Monday - 18/6",
                "Tuesday - 16/2",
                "Wednesday - 14/2",
                "Thursday - 13/2",
                "Friday - 11/5",
                "Saturday - 8/1",
                "Sunday - 5/1",
                "Monday - 15/5",
                "Tuesday - 14/6",
                "Wednesday - 21/8",
                "Thursday - 35/9",
                "Friday - 20/2",
                "Saturday - 5/6",
                "Sunday - 4/1"
        };

        for(String dayForecast : dummyWeatherData) {
            mWeatherTextView.append(dayForecast + "\n\n");
        }
    }
}