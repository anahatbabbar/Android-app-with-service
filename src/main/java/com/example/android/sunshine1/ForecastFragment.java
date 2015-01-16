package com.example.android.sunshine1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

//Anahat - Make sure you are using android.support.v4.app.LoadManager and CursorLoader. This functionality is available only in HoneyComb and later OSes.
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.sunshine1.data.WeatherContract;
import com.example.android.sunshine1.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine1.data.WeatherContract.LocationEntry;
import com.example.android.sunshine1.service.SunshineService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by anahat.babbar on 7/31/2014.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView listContainer;

    //Anahat - Adding stuff for LoaderManager to work
    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    private boolean mUseTodayLayout;

    //Anahat - Constant for savedInstanceState
    private final String SELECTED_KEY = "list_selection_position";

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_TYPEID = 6;

    // Anahat - LoadManager declaring stuff Ends her ---------------------------------------------

    //Anahat - Bringing the adapter outside the scope of onCreateView so that it can be accessed and updated by onPostExecute method in AsyncTask
    //Anahat - Commenting out below line now because now we are using SimpleCursorAdaptor instead of ArrayAdaptor
//    ArrayAdapter<String> mForecastAdapter;

    // Anahat - Now declaring mForecastAdapter variable as SimpleCursorAdaptor. Make sure it is from android.support.v4.widget package. Else it wont compile with later Android OSes
    //Anahat - commenting out the line below because we are now using our custom Cursor Adapter ForecastAdapter
//    SimpleCursorAdapter mForecastAdapter;

    //Anahat - Now declaring mForecastAdapter variable as ForecastAdaptor
    ForecastAdapter mForecastAdapter;

    //Anahat - Declaring position variable to persist the list position value across multiple activities
    int mPosition;

    //Anahat - IMPORTANT. Creating a callback Interface that needs to be implemented by all activities that would use this fragment
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback{
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String date);
    }


    public ForecastFragment() {
    }

    //Anahat - onCreate method is called when the fragment is created. onCreateView method is called when the fragment UI is initialized, i.e. after onCreate().

    //Anahat - This method onCreate is added in Fragment code to activate Menus in fragment. setHasOptionsMenu is added so that menu related call-back methods are called on any events on menu
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Anahat - to set that the fragment has a options menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Anahat - Saving the position of list selection so that even when the orientation of device is changed, the position is persisted in the new activity
        if(mPosition != ListView.INVALID_POSITION){
            outState.putString(SELECTED_KEY,Integer.toString(mPosition));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Anahat - Commenting the code below which was added to test the app in the very beginning of building this app

//        //Array List of items to be displayed in the ListView
//        String[] forecastArray = {
//                "Today - Sunday - 88/63",
//                "Tomorrow - Foggy - 70/45",
//                "Weds - Cloudy - 72/63",
//                "Thurs - Rainy - 64/51",
//                "Fri - Foggy - 70/63",
//                "Sat - Sunny - 76/68"
//        };
//
//
//        List<String> weekForecast = new ArrayList(Arrays.asList(forecastArray));

        // Anahat Commenting stopped for the above code

        //Get the adapter to bind data to the element in the layout xml file
        //Anahat - Commenting out the ArrayAdapter initliazation because we want to initialize mForecastAdapter as SimpleCursorAdapter object
//        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast_1, R.id.list_item_forecast_textview_1, new ArrayList<String>()/*weekForecast*/);

        // Anahat - IMPORTANT - the use of SimpleCursorAdapter is commented out below because we are now using our own adapter so that we can have
        //Anahat - data bound to our custom views..
        /*

        //Anahat Initializing mForecastAdapter as SimpleCursorAdapter object
        mForecastAdapter = new SimpleCursorAdapter(getActivity(),
                                                   R.layout.list_item_forecast_1,
                                                    null,
                                                    //These DB column names
                                                    new String[]{WeatherEntry.COLUMN_DATETEXT,
                                                                 WeatherEntry.COLUMN_SHORT_DESC,
                                                                 WeatherEntry.COLUMN_MAX_TEMP,
                                                                 WeatherEntry.COLUMN_MIN_TEMP},
                                                    //To these textview ids
                                                    new int[]{R.id.list_item_date_textview,
                                                              R.id.list_item_forecast_textview,
                                                              R.id.list_item_high_textview,
                                                              R.id.list_item_low_textview});

        //Anahat - Now that the data is here in the UI, it needs to be formatted. SimpleCursor provides a method that is called just before the data is displayed in the UI
        mForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                boolean isMetric = Utility.isMetric(getActivity());
                switch (columnIndex) {
                    case COL_WEATHER_MAX_TEMP:
                    case COL_WEATHER_MIN_TEMP: {
                        // we have to do some formatting and possibly a conversion
                        ((TextView) view).setText(Utility.formatTemperature(
                                cursor.getDouble(columnIndex), isMetric));
                        return true;
                    }
                    case COL_WEATHER_DATE: {
                        String dateString = cursor.getString(columnIndex);
                        TextView dateView = (TextView) view;
                        dateView.setText(Utility.formatDate(dateString));
                        return true;
                    }
                }
                return false;
            }
        });

        */

        // Anahat - Using the custom adapter ForecastAdapter now to bind the data values to the view.
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        //Anahat - Setting the mUseTodayLayout variable in ForecastAdapter that figures out how to display today's list item, which is different
        //Anahat - for tablets and phones.
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        //Anahat - now bind the adapter to the view. This is because nowhere we have defined how this data is associated with the view
        //Anahat - First find the view in the hierarchy of layout i.e. in fragment_main xml
        listContainer = (ListView) rootView.findViewById(R.id.listview_forecast);
        //Anahat - now bind the data to the view
        listContainer.setAdapter(mForecastAdapter);

        // Anahat - Code below to take weather string from one activity and passing it on to another activity.
        listContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //Anahat - the parameters in the below methods are variables ListView listContainer, TextView, position of item in adapter , and row number ????
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //Anahat - Commenting out the code line below which was used when we were using arrayAdapter. Now that we are using SimpleCursorAdapter, we do not need this line.
//                String weatherString = (String)adapterView.getItemAtPosition(i);

                //Anahat - Getting data from the SimpleCursorAdapter. Adapters are used because they cache data in the app memory. Also, adapters fill in list items one by one.
                //Anahat Commenting out the line below because we are using custom adapter ForecastAdapter
//                SimpleCursorAdapter adapter = (SimpleCursorAdapter) adapterView.getAdapter();

                //Anahat - Getting data from the ForecastAdapter. Adapters are used because they cache data in the app memory. Also, adapters fill in list items one by one.
                ForecastAdapter adapter = (ForecastAdapter) adapterView.getAdapter();

                Cursor weatherCursor = adapter.getCursor();
                if (weatherCursor != null && weatherCursor.moveToPosition(i)){

                    //Anahat - Commenting the lines below that create a weather String that was intially passed to details screen.
//                    String weatherString = Utility.formatDate(weatherCursor.getString(COL_WEATHER_DATE)) +
//                            "-" +
//                            weatherCursor.getString(COL_WEATHER_DESC) +
//                            "-" +
//                            Utility.formatTemperature(Double.parseDouble(weatherCursor.getString(COL_WEATHER_MAX_TEMP)), Utility.isMetric(getActivity())) +
//                            "/" +
//                            Utility.formatTemperature(Double.parseDouble(weatherCursor.getString(COL_WEATHER_MIN_TEMP)), Utility.isMetric(getActivity()));


                    //Anahat - Now we are sending just the date for detail activity to get the latest data from the DB
                    String dateString = weatherCursor.getString(COL_WEATHER_DATE);

                    // Anahat - Toast used below to test passing of weather string
                    /* Commenting the TOAST for now as it was only for debugging
                     Toast toast = Toast.makeText(getActivity(),weatherString, Toast.LENGTH_SHORT);
                     toast.show();
                     */

                    // Anahat - Commenting out the code below of calling the activity using intents straight from the fragment for phone implementation.
                    //Anahat - Now using activity callback to decide what needs to be done on item selection based on device settings.
//                    //Anahat - Calling explicit intent to call DetailActivity
//                    Intent intent = new Intent(getActivity(), DetailActivity.class);
//                    intent.putExtra(DetailActivity.DATE_KEY, dateString/*weatherString*/);
//                    //Anahat - StartActivity method below calls the onCreate method of DetailActivity.
//                    startActivity(intent);

                    //Anahat - Calling callback method of the activity
                    Callback cb = (Callback)getActivity();
                    cb.onItemSelected(dateString);
                }
                //Anahat - Saving the position in class instance variable so that if the orientation of device is changed, the selection in the list is persisted.
                mPosition = i;
            }
        });

        //Anahat - Checking if the savedStateInstance has value for mPosition. If yes, then the orientation of the device was recently changed. Get the last position and set mPosition
        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = Integer.parseInt(savedInstanceState.getString(SELECTED_KEY));
        }

        return rootView;
    }


    //Anahat - The menu method below is a little different from activity menu button. Here the Menu inflater is passed as argument to the method.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    //Anahat - Method added to handle menu events.
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {

           //Anahat - Calling custom update method to fetch fresh data from the server
            updateWeather();
               return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //Anahat - Method added so that it calls Async task to fetch data from the server
    private void updateWeather(){

        // Anahat - Commented out the next lines of code as now we are calling a custom service SunshineService to refresh data

        /*
        //Anahat - getting the location details from SharedPreference
        String locationValue = Utility.getPreferredLocation(getActivity());

        //Anahat - Calling Async Task - See next comment
//       new FetchWeatherTask().execute(locationValue);

        //Anahat - Calling separated out Async task. See the extra parameters passed in constructor.
         new FetchWeatherTask(getActivity()/*,mForecastAdapter*//*).execute(locationValue);

        */


        //Anahat - The code below is to call custom service SunshineService for refreshing data.
        /* Anahat - Commenting out the code below as now we are using alarms to call the service

        //Anahat - getting the location details from SharedPreference
        String locationValue = Utility.getPreferredLocation(getActivity());
        Intent serviceIntent = new Intent(getActivity(), SunshineService.class);
        serviceIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, locationValue);
        getActivity().startService(serviceIntent);

        */

        //Anahat - the code below is call automatically call custom service using Alarm Manager.
        AlarmManager alarmMgr = (AlarmManager)getActivity().getSystemService(getActivity().ALARM_SERVICE);
        String locationVal = Utility.getPreferredLocation(getActivity());
        Intent servIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        servIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, locationVal);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(),0,servIntent,PendingIntent.FLAG_ONE_SHOT);
        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis()+5000, alarmIntent);
    }

    //Anahat - Setting the mUseTodayLayout variable which is then passed to ForecastAdapter that figures out how to display today's list item, which is different
    //Anahat - for tablets and phones.
    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
        //Anahat - Checking if mForecastAdapter is initialized or not. It may NOT be initialized yet as create method of MainActivity may have run before onCreateView method of this fragment ran completely.
        //Anahat - In that case, mForecastAdapter method is directly called from onCreateView method of this forecastFragment.
        if(mForecastAdapter != null){
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    //Anahat - adding onStart method that is called the moment the app is launched
    public void onStart(){
        super.onStart();
        //Anahat - Getting rid of the line below so that the data does not get refreshed every time onStart method is called.
        updateWeather();
    }

    //Anahat - Here making sure that when this activity resumes after preference changes, the weather location is picked up again and weather info is refreshed
    //Anahat - Thus the loader is refreshed.
    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    //Anahat - The below implemented 3 methods are overriden from LoaderManager interface

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
                );
    }

    //Anahat This callback method is called when the loading of cursor from DB is complete and the data is ready for UI to consume
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Anahat - Swapping the cursor that mForecastAdapter will now use to populate the UI textViews
        int i = data.getCount();
        mForecastAdapter.swapCursor(data);

        //Anahat - Check if this load is for a new activity that is created when the orientation of the device is changed
        if(mPosition != ListView.INVALID_POSITION){
            listContainer.setSelection(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Anahat - We do not ave any data for now for this
        mForecastAdapter.swapCursor(null);
    }

    //Anahat - Overriding the below method from Fragment abstract class
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    //Anahat - IMPORTANT. ALL THE CODE BELOW IS COMMENTED OUT

//    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{
//
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//
//        //Anahat - Updating the adapter with the result received from doInBAckground method
//        @Override
//        protected void onPostExecute(String[] result) {
//            if(result != null){
//                mForecastAdapter.clear();
//                for(String dayForecastStr : result){
//                    mForecastAdapter.add(dayForecastStr);
//                }
//            }
//
//        }
//
//        @Override
//        public String[] doInBackground(String... params){
//            //Adding code to connect ot a HTTP server
//
//            // These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//
//            String format = "json";
//            String units = "metric";
//            int days = 7;
//
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are available at OWM's forecast API page, at
//                // http://openweathermap.org/API#forecast
//
//                //Anahat - Below is the URL passed without URL builder. A constant string is just paased. But it is non-dynamic
////                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&cnt=7&units=metric&mode=json");
//
//               //Anahat - Below is the recommended method of creating the URL using the Uri.Builder class.
//                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
//                final String QUERY_PARAM = "q";
//                final String FORMAT_PARAM = "mode";
//                final String UNITS_PARAM = "units";
//                final String DAYS_PARAM = "cnt";
//
//                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM,params[0])
//                                .appendQueryParameter(FORMAT_PARAM,format)
//                                .appendQueryParameter(UNITS_PARAM,units)
//                                .appendQueryParameter(DAYS_PARAM,Integer.toString(days)).build();
//                URL url = new URL(builtUri.toString());
//
//                Log.v(LOG_TAG, "Built URI "+ url.toString());
//
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    forecastJsonStr = null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                forecastJsonStr = buffer.toString();
//
//                //Log statement verbose is added as a debug string
////                Log.v(LOG_TAG, "Forecast JSON string is: "+forecastJsonStr);
//                try {
//                    String[] forecastArray = getWeatherDataFromJson(forecastJsonStr, days);
//                    return forecastArray;
//                }
//                catch(JSONException e){
//                    Log.e(LOG_TAG, "Error", e);
//                    e.printStackTrace();
//                    return null;
//                }
//
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
//                // If the code didn't successfully get the weather data, there's no point in attemping
//                // to parse it.
//                return null;
//            }finally{
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
////            return null;
//            //end
//        }
//
//        /* The date/time conversion code is going to be moved outside the asynctask later,
//         * so for convenience we're breaking it out into its own method now.
//         */
//        private String getReadableDateString(long time){
//            // Because the API returns a unix timestamp (measured in seconds),
//            // it must be converted to milliseconds in order to be converted to valid date.
//            Date date = new Date(time * 1000);
//            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
//            return format.format(date).toString();
//        }
//
//        /**
//         * Prepare the weather high/lows for presentation.
//         */
//        private String formatHighLows(double high, double low) {
//            // For presentation, assume the user doesn't care about tenths of a degree.
//
//           //Anahat - checking for the sharedPreference of units chosen to display the values by the user
//            String tempUnit = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getResources().getString(R.string.temp_unit_key),"");
//            if(tempUnit.equals("imperial")) {
//                high = metricToImperial(high);
//                low = metricToImperial(low);
//            }
//
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }
//
//        private double metricToImperial(double temp){
//            temp = ((temp*9)/5) + 32;
//            return temp;
//        }
//
//        /**
//         * Take the String representing the complete forecast in JSON Format and
//         * pull out the data we need to construct the Strings needed for the wireframes.
//         *
//         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
//         * into an Object hierarchy for us.
//         */
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//                throws JSONException {
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DATETIME = "dt";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            String[] resultStrs = new String[numDays];
//            for(int i = 0; i < weatherArray.length(); i++) {
//                // For now, using the format "Day, description, hi/low"
//                String day;
//                String description;
//                String highAndLow;
//
//                // Get the JSON object representing the day
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                // The date/time is returned as a long.  We need to convert that
//                // into something human-readable, since most people won't read "1400356800" as
//                // "this saturday".
//                long dateTime = dayForecast.getLong(OWM_DATETIME);
//                day = getReadableDateString(dateTime);
//
//                // description is in a child array called "weather", which is 1 element long.
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                // Temperatures are in a child object called "temp".  Try not to name variables
//                // "temp" when working with temperature.  It confuses everybody.
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//
//            return resultStrs;
//        }
//    }
}
