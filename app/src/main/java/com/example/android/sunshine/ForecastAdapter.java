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

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>{
    private Cursor mCursor;
    private Context mContext;
    final private ListItemClickListener mOnClickListener;

    public ForecastAdapter(ListItemClickListener listener, Context context) {
        mOnClickListener = listener;
        mContext = context;
    }

    public interface ListItemClickListener {
        void onListItemClick(String viewHolderData);
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
        holder.bind(position);
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

        void bind(int listIndex) {

            // extract relevant data from the cursor and display it
            String weatherSummary = displayValuesFromCursor(listIndex);

            if(weatherSummary != null) {
                mWeatherTextView.setText(weatherSummary);
            }
        }

        @Override
        public void onClick(View v) {
            // query cursor for the single item
            int clickedPosition = getAdapterPosition();

            // extract relevant data from the cursor and display it
            String weatherSummary = displayValuesFromCursor(clickedPosition);

            if(weatherSummary != null) {
                mOnClickListener.onListItemClick(weatherSummary);
            }
        }

        private String displayValuesFromCursor(int cursorPosition) {

            if(mCursor.moveToPosition(cursorPosition)) {
                // extract all the relevant data from the Cursor and display it
                int dateColIndex = mCursor.getColumnIndex(WeatherEntry.COLUMN_DATE);
                long dateInMillis = mCursor.getLong(dateColIndex);
                String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);

                int descColIndex = mCursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID);
                int weatherId = mCursor.getInt(descColIndex);
                String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);

                int minTempColIndex = mCursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP);
                double lowInCelsius = mCursor.getDouble(minTempColIndex);

                int maxTempColIndex = mCursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP);
                double highInCelsius = mCursor.getDouble(maxTempColIndex);

                String highAndLowTemperature = SunshineWeatherUtils.formatHighLows(mContext, highInCelsius, lowInCelsius);

                return dateString + " - " + description + " - " + highAndLowTemperature;
            }

            return null;
        }
    }
}
