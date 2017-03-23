package com.example.android.sunshine;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class DetailActivity extends AppCompatActivity{
    // weather forecast data for sharing via shareIntent
    private String mDetailWeatherForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        Intent intent = getIntent();
        mDetailWeatherForecast =  intent.getStringExtra("dayForecast");

        TextView detailForecast = (TextView) findViewById(R.id.tv_detail_forecast);
        detailForecast.setText(mDetailWeatherForecast);
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
                shareDayForecast(mDetailWeatherForecast);
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
}
