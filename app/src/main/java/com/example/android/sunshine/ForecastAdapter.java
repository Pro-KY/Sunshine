package com.example.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import static android.os.Build.VERSION_CODES.M;


public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>{
    private Cursor mCursor;
    private Context mContext;
    final private ListItemClickListener mOnClickListener;

    public ForecastAdapter(ListItemClickListener listener, Context context) {
        mOnClickListener = listener;
        mContext = context;
    }

    public interface ListItemClickListener {
        void onListItemClick(long forecastDate);
    }

    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item_layout;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ForecastAdapterViewHolder(view);
    }

    // takes all of the data from a cursor and uses that to populate the views
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        // extract relevant data from the cursor and display it
        String weatherSummary = holder.displayValuesFromCursor(position);

        if(weatherSummary != null) {
            holder.mWeatherTextView.setText(weatherSummary);
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
        TextView mWeatherTextView;

        ForecastAdapterViewHolder(View itemView) {
            super(itemView);
            mWeatherTextView = (TextView) itemView.findViewById(R.id.tv_item_forecast);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // RETRIEVE TEXT FROM THE LIST ITEM TEXTVIEW

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
