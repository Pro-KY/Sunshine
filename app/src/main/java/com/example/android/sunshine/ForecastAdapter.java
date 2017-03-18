package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>{
    private String[] mWeatherData;
    final private ListItemClickListener mOnClickListener;

    public ForecastAdapter(ListItemClickListener listener) {
        mOnClickListener = listener;
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

    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if(mWeatherData != null) {
            return mWeatherData.length;
        }
        return 0;
    }

    void setWeatherData(String[] weatherData) {
        mWeatherData = weatherData;
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
            mWeatherTextView.setText(mWeatherData[listIndex]);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Log.d("clickedPosition", String.valueOf(clickedPosition));
            String dayForecast = mWeatherData[clickedPosition];
            mOnClickListener.onListItemClick(dayForecast);
        }
    }
}
