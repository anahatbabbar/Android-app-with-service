package com.example.android.sunshine1;

import android.content.Intent;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
//import android.os.Build;
//import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.example.android.sunshine1.R;
import com.example.android.sunshine1.data.WeatherContract;

public class DetailActivity extends ActionBarActivity {

    public static final String DATE_KEY = "date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Anahat - Getting the weather/date string from Main Activity using the Intent passed to the activity.
        Intent intent = this.getIntent();

        //Anahat - Search for the date Bundle corresponding to EXTRA_TEXT added by calling activity
        String dateStr = intent.getStringExtra(DATE_KEY);

        //Anahat - Now putting this intent data in a bundle argument that will be passed to DetailFragment
        Bundle args = new Bundle();
        args.putString(DATE_KEY, dateStr);

        //Anahat - Setting the args on the DetailFragment object
        DetailFragment df = new DetailFragment();
        df.setArguments(args);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, df)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
//
//        private String forecastStr;
//        private String dateStr;
//        private String strLocation;
//
//        private static String LOG_TAG = PlaceholderFragment.class.getName();
//
//        public PlaceholderFragment() {
//            //Anahat - Set so that menu callback methods can be called
//            //setHasOptionsMenu(true);
//        }
//
//        @Override
//        public void onCreate(Bundle savedInstanceState){
//            super.onCreate(savedInstanceState);
//            //Anahat - to set that the fragment has a options menu
//            setHasOptionsMenu(true);
//
//        }
//
//        //Anahat - Method overriden from Fragment Abstract class
//        @Override
//        public void onActivityCreated(Bundle savedInstanceState) {
//
//            super.onActivityCreated(savedInstanceState);
//
//           //Anahat - Grabbing the strLocation if it is already there, as saved in onsavedInstance() method i.e. if the fragment is already started once
//            if (null != savedInstanceState){
//                strLocation = savedInstanceState.getString(LOCATION_KEY);
//            }
//
//            //Anahat - Initializing the loader, which will call the loader background thread
//            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
//        }
//
//        //Anahat - Here making sure that when this activity resumes after preference changes, the weather location is picked up again and weather info is refreshed
//        //Anahat - Thus the loader is refreshed.
//        @Override
//        public void onResume() {
//            super.onResume();
//            if (strLocation != null && !strLocation.equals(Utility.getPreferredLocation(getActivity()))) {
//                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
//            }
//        }
//
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
//
//            return rootView;
//        }
//
//        @Override
//        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
//            // Inflate the menu; this adds items to the action bar if it is present.
//            inflater.inflate(R.menu.detailfragment, menu);
//
//            //Anahat - Adding ShareActionProvider to detail fragment
//            MenuItem item = menu.findItem(R.id.action_detail_share);
//
//            //Anahat - Very important. The line below from the document did not work for me and threw java.lang.ClassCastException.This is because SharedActionProvider is in 2 separate packages
//            //mShareActionProvider = (ShareActionProvider)item.getActionProvider();
//
//            ShareActionProvider mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(item);
//
//            if(mShareActionProvider != null){
//                mShareActionProvider.setShareIntent(createIntentForShareAction());
//            }
//            else{
//                Log.d(LOG_TAG, "Shared Action Provider is null");
//            }
//        }
//
//        //Anahat - Method added to create ShareAction intent.
//        private Intent createIntentForShareAction() {
//                //Anahat - Creating forecast string in the intent and setting it in ShareActionProvider and calling it
//                String shareStr = forecastStr + " #SunshineApp";
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                //The line below is important. It tells the OS not to place the sharing app on the activity stack. Else, if we restart the app, we will be in the sharing app instead of sunshine app
//                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//                shareIntent.setType("text/plain");
//                shareIntent.putExtra(Intent.EXTRA_TEXT, shareStr);
//                return shareIntent;
//        }
//
//        //Anahat - Overriding this method from Fragment abstract class. This is to save the instance variable states. When we rotate the phone, the variables are lost
//        // Anahat - Thus we save those variables in bundles
//        @Override
//        public void onSaveInstanceState(Bundle outState) {
//            super.onSaveInstanceState(outState);
//            outState.putString(LOCATION_KEY, strLocation);
//        }
//
//        //Anahat - Now defining loader callback functions
//
//        @Override
//        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//            // This is called when a new Loader needs to be created.  This
//            // fragment only uses one loader, so we don't care about checking the id.
//
//            //Anahat - Getting the weather/date string from Main Activity using the Intent passed to the activity.
//            Intent intent = getActivity().getIntent();
//
//            //Anahat - Search for the date Bundle corresponding to EXTRA_TEXT added by calling activity
//            dateStr = intent.getStringExtra(DATE_KEY);
//
//           String[] FORECAST_COLUMNS = {
//                    // In this case the id needs to be fully qualified with a table name, since
//                    // the content provider joins the location & weather tables in the background
//                    // (both have an _id column)
//                    // On the one hand, that's annoying.  On the other, you can search the weather table
//                    // using the location set by the user, which is only in the Location table.
//                    // So the convenience is worth it.
//                    WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
//                    WeatherContract.WeatherEntry.COLUMN_DATETEXT,
//                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
//                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
//                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
//                    WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
//                    WeatherContract.WeatherEntry.COLUMN_PRESSURE,
//                    WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
//                    WeatherContract.WeatherEntry.COLUMN_DEGREES,
//                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
//            };
//
//            strLocation = Utility.getPreferredLocation(getActivity());
//
//            Uri weatherForLocationAndDateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(strLocation, dateStr);
//
//            // Now create and return a CursorLoader that will take care of
//            // creating a Cursor for the data being displayed.
//            return new CursorLoader(
//                    getActivity(),
//                    weatherForLocationAndDateUri,
//                    FORECAST_COLUMNS,
//                    null,
//                    null,
//                    null
//            );
//        }
//
//        @Override
//        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//
//            if (data != null && data.moveToFirst()) {
//
//                //Anahat - Commenting the lines below that create a weather String that was intially passed to details screen.
//                String dateText = Utility.formatDate(data.getString(COL_WEATHER_DATE));
//                String descText = data.getString(COL_WEATHER_DESC);
//                String maxText = Utility.formatTemperature(getActivity(),Double.parseDouble(data.getString(COL_WEATHER_MAX_TEMP)), Utility.isMetric(getActivity()));
//                String minText = Utility.formatTemperature(getActivity(),Double.parseDouble(data.getString(COL_WEATHER_MIN_TEMP)), Utility.isMetric(getActivity()));
//
//                //Anahat - find the text view to set value to
//                TextView dateView = (TextView) getView().findViewById(R.id.detail_date_textview);
//                dateView.setText(dateText);
//                TextView descView = (TextView) getView().findViewById(R.id.detail_forecast_textview);
//                descView.setText(descText);
//                TextView maxView = (TextView) getView().findViewById(R.id.detail_high_textview);
//                maxView.setText(maxText);
//                TextView minView = (TextView) getView().findViewById(R.id.detail_low_textview);
//                minView.setText(minText);
//
//                forecastStr = String.format("%s - %s - %s/%s", dateText, descText, maxText, minText);
//            }
//        }
//
//        @Override
//        public void onLoaderReset(Loader<Cursor> loader) {
//
//        }
//    }
}
