package com.example.android.sunshine;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


public class DetailActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        Intent intent = getIntent();
        String dayForecast = intent.getStringExtra("dayForecast");

        TextView detailForecast = (TextView) findViewById(R.id.tv_detail_forecast);
        detailForecast.setText(dayForecast);
    }
}
