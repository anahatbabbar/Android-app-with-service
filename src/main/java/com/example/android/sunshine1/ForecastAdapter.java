package com.example.android.sunshine1;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Anahat.Babbar on 10/16/2014.
 */


//Anahat - Declaring a separate class extended from CursorAdapter to fill in different views for different data records
public class ForecastAdapter extends CursorAdapter{

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;

    private boolean mUseTodayLayout;

    // Anahat - Implementing View Holder pattern to cache the textview resources id for quick access. Since these textview resources are needed for ALL records in the list, we stored them here.
    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }


    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    //Anahat - The  2 methods below have to be overridden

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Anahat - Commenting out the default line as we have more than one view now to work on
//        return LayoutInflater.from(context).inflate(R.layout.list_item_forecast_1, parent, false);

        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int p = cursor.getPosition();
        int layoutId = -1;
        if(viewType == VIEW_TYPE_TODAY)
            layoutId = R.layout.list_item_forecast_today;
        else if(viewType == VIEW_TYPE_FUTURE_DAY)
            layoutId = R.layout.list_item_forecast_1;

        View listView = LayoutInflater.from(context).inflate(layoutId, parent, false);

        //Anahat - Creating an object of ViewHolder and initializing its variables
        ViewHolder holder = new ViewHolder(listView);

        //Anahat - Now setting the object in the view object. This is an important method, and the tag can be used to store ANY object. Do not abused this method though.
        listView.setTag(holder);

        return listView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Anahat - IMPORTANT. This binding method will work for both layout resources list_item_forecast_today and list_item_forecast_1 because the textview ids are the same in both the layouts.

        // Anahat - getting the Viewholder object from the view object now that was set in view tag in newView() method. Using viewHolder object then in the code below to set the values.
        ViewHolder holder = (ViewHolder) view.getTag();

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_TYPEID);
        // Use placeholder image for now
//        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
//        iconView.setImageResource(R.drawable.ic_launcher);
//        holder.iconView.setImageResource(R.drawable.ic_launcher);
        int viewType = getItemViewType(cursor.getPosition());
        int weatherIconResource = R.drawable.ic_launcher; // Setting to a default value

        //check if we are setting weather icon for today's weather
        if (viewType == VIEW_TYPE_TODAY){
            weatherIconResource = Utility.getArtResourceForWeatherCondition(weatherId);
            holder.iconView.setImageResource(weatherIconResource);
        }
        else if (viewType == VIEW_TYPE_FUTURE_DAY){
            weatherIconResource = Utility.getIconResourceForWeatherCondition(weatherId);
            holder.iconView.setImageResource(weatherIconResource);
        }

        // Read date from cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
//        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
//        dateView.setText(Utility.getFriendlyDayString(context, dateString));
        holder.dateView.setText(Utility.getFriendlyDayString(context, dateString));

        // Read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
//        TextView descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
//        descriptionView.setText(description);
        holder.descriptionView.setText(description);

        // Read user preference for metric or imperial temperature units
//        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
//        TextView highView = (TextView)view.findViewById(R.id.list_item_high_textview);
//        highView.setText(Utility.formatTemperature(
//                high,Utility.isMetric(context)));
        holder.highTempView.setText(Utility.formatTemperature(
                context,high,Utility.isMetric(context)));

        // Read low temperature from cursor
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
//        TextView lowView = (TextView)view.findViewById(R.id.list_item_low_textview);
//        lowView.setText(Utility.formatTemperature(
//                low,Utility.isMetric(context)));
        holder.lowTempView.setText(Utility.formatTemperature(
                context,low,Utility.isMetric(context)));
    }

    //Anahat - IMPORTANT. Overriding the method below because we have 2 different views. By default, this method returns 1 for one view. Return the number if views in your app
    @Override
    public int getViewTypeCount() {
        //Anahat - Commenting the default return of 1 view
        //return 1;
        return VIEW_TYPE_COUNT;
    }

    //Anahat - This method is used in forecastfragment to set the mUseTodayLayout variable which dictates how todays layout will be as tablet and phone layouts are different for today in the list.
    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
    }

    //Anahat - Overriding the method below that returns the view type id.
    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

}
