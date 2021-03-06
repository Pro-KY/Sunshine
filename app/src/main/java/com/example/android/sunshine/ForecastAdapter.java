package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import java.util.Set;

import static android.R.attr.id;
import static android.os.Build.VERSION_CODES.M;


public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    // constant IDs for the ViewType for today and for a future day
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    private Cursor mCursor;
    private Context mContext;
    private boolean mUseTodayLayout;

    final private ListItemClickListener mOnClickListener;

    public ForecastAdapter(ListItemClickListener listener, Context context) {
        mOnClickListener = listener;
        mContext = context;
        mUseTodayLayout = mContext.getResources().getBoolean(R.bool.use_today_layout);
    }

    public interface ListItemClickListener {
        void onListItemClick(long forecastDate);
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId;

        switch(viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_layout;
                break;
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        view.setFocusable(true);

        return new ForecastAdapterViewHolder(view);
    }

    // takes all of the data from a cursor and uses that to populate the views
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        // extract relevant data from the cursor and display it
        mCursor.moveToPosition(position);

        // weather ICON
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;

        int viewType = forecastAdapterViewHolder.getItemViewType();

        switch(viewType) {
            case VIEW_TYPE_TODAY:
                weatherImageId = SunshineWeatherUtils.
                        getLargeArtResourceIdForWeatherCondition(weatherId);
                break;
            case VIEW_TYPE_FUTURE_DAY:
                weatherImageId = SunshineWeatherUtils
                        .getSmallArtResourceIdForWeatherCondition(weatherId);
                break;
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        forecastAdapterViewHolder.iconView.setImageResource(weatherImageId);

        // read DATE from the cursor and
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        Log.d("date:", dateString);
        // display date string
        forecastAdapterViewHolder.dateView.setText(dateString);

        // weather DESCRIPTION
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);
        // Create the accessibility (a11y) String from the weather description
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);
        forecastAdapterViewHolder.descriptionView.setText(description);
        forecastAdapterViewHolder.descriptionView.setContentDescription(descriptionA11y);

        // HIGH temperature
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        String highString = SunshineWeatherUtils.formatTemperature(mContext, highInCelsius);
        String highA11y = mContext.getString(R.string.a11y_high_temp, highString);
        // Set the text and content description (for accessibility purposes)
        forecastAdapterViewHolder.highTempView.setText(highString);
        forecastAdapterViewHolder.highTempView.setContentDescription(highA11y);

        // LOW temperature
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
        String lowString = SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius);
        String lowA11y = mContext.getString(R.string.a11y_low_temp, lowString);
        // Set the text and content description (for accessibility purposes)
        forecastAdapterViewHolder.lowTempView.setText(lowString);
        forecastAdapterViewHolder.lowTempView.setContentDescription(lowA11y);
    }

    @Override
    public int getItemViewType(int position) {

        if(mUseTodayLayout && position == 0) {
            return VIEW_TYPE_TODAY;
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    @Override
    public int getItemCount() {
        if(mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    // take in a new Cursor and update the value of the old Cursor
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        // After the new Cursor is set, call notifyDataSetChanged
        notifyDataSetChanged();
    }

    public class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;

        final ImageView iconView;

        ForecastAdapterViewHolder(View itemView) {
            super(itemView);
            dateView = (TextView) itemView.findViewById(R.id.date);
            descriptionView = (TextView) itemView.findViewById(R.id.weather_description);
            highTempView = (TextView) itemView.findViewById(R.id.high_temperature);
            lowTempView = (TextView) itemView.findViewById(R.id.low_temperature);
            iconView = (ImageView) itemView.findViewById(R.id.weather_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            // query cursor for the single item
            int clickedPosition = getAdapterPosition();

            if(mCursor.moveToPosition(clickedPosition)) {
                long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
                mOnClickListener.onListItemClick(dateInMillis);
            }
        }

        private String displayValuesFromCursor(int cursorPosition) {

            if(mCursor.moveToPosition(cursorPosition)) {
                // extract all the relevant data from the Cursor and display it
                long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
                String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);

                int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
                String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);

                double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
                double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);

                String highAndLowTemperature = SunshineWeatherUtils.formatHighLows(mContext, highInCelsius, lowInCelsius);

                return dateString + " - " + description + " - " + highAndLowTemperature;
            }

            return null;
        }
    }
}
