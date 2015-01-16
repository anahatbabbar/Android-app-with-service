package com.example.android.sunshine1;

import android.app.LoaderManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.android.sunshine1.data.WeatherContract;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


/**
 * Created by Anahat.Babbar on 9/25/2014.
 */
public class FetchWeatherTask extends AsyncTask<String,Void,/*String[]*/ Void>{
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    // These are the names of the JSON objects that need to be extracted.
    final String OWM_LIST = "list";
    final String OWM_WEATHER = "weather";
    final String OWM_TEMPERATURE = "temp";
    final String OWM_MAX = "max";
    final String OWM_MIN = "min";
    final String OWM_DATETIME = "dt";
    final String OWM_DESCRIPTION = "main";

    final String OWM_CITY = "city";
    final String OWM_CITY_NAME = "name";
    final String OWM_COORD = "coord";
    final String OWM_LATITUDE = "lat";
    final String OWM_LONGITUDE = "lon";

    final String OWM_PRESSURE = "pressure";
    final String OWM_HUMIDITY = "humidity";
    final String OWM_WINDSPEED = "speed";
    final String OWM_WIND_DIRECTION = "deg";
    final String OWM_WEATHER_ID = "id";

    private final Context mContext;
    private ArrayAdapter<String> mForecastAdapter;

       //Anahat - Adding a constructor
      public FetchWeatherTask(Context context/*, ArrayAdapter<String> forecastAdapter*/){
        mContext = context;
        //mForecastAdapter = forecastAdapter;
      }

    //Anahat - Updating the adapter with the result received from doInBackground method
//    @Override
//    protected void onPostExecute(String[] result) {
//        if(result != null){
//            mForecastAdapter.clear();
//            for(String dayForecastStr : result){
//                mForecastAdapter.add(dayForecastStr);
//            }
//        }
//    }

    @Override
    public /*String[]*/ Void doInBackground(String... params) {
        //Adding code to connect ot a HTTP server

        //Anahat - Adding location param as a string to make the code easier to understand
        String locationQuery = params[0];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int days = 14;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            //Anahat - Below is the URL passed without URL builder. A constant string is just paased. But it is non-dynamic
//                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&cnt=7&units=metric&mode=json");

            //Anahat - Below is the recommended method of creating the URL using the Uri.Builder class.
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(days)).build();
            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                forecastJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();

            //Log statement verbose is added as a debug string
                Log.v(LOG_TAG, "Forecast JSON string is: "+forecastJsonStr);

            try {

                JSONObject forecastJson = new JSONObject(forecastJsonStr);

                /*String[] forecastArray =*/ getWeatherDataFromJson(forecastJsonStr, days, locationQuery);
                //return forecastArray;


            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error", e);
                e.printStackTrace();
                return null;
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
            return null;
        //end
    }

    //Anahat - Method to call WeatherProvider and insert location in the db if they do not already exist
    private long addLocation(String locationSetting, String cityName, double lat, double lon){

        ContentResolver resolver = mContext.getContentResolver();
        Log.v(LOG_TAG, "Content Provider Resolved");
        Cursor cursor = resolver.query(WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + "=?",
                new String[] {locationSetting},
                null
                );

        //Anahat - Check if the location already exists. If yes, the just return the id of that location.
        if(cursor.moveToFirst()){
            Log.v(LOG_TAG, "Location already exists in the DB");
            int idIndex= cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            return cursor.getLong(idIndex);
        }

        Log.v(LOG_TAG, "Entering a new Location. Location does NOT exist in the DB");
        //Anahat - Now that the location does not exist, add the location to the DB and return the new id
        ContentValues values = new ContentValues();
        values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
        values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
        values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

        Uri uri = resolver.insert(WeatherContract.LocationEntry.CONTENT_URI, values);
        return ContentUris.parseId(uri);

    }



    /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }


    //Anahat - Commenting the two methods below because they have now been moved to Utility class.
//
//    /**
//     * Prepare the weather high/lows for presentation.
//     */
//    private String formatHighLows(double high, double low) {
//        // For presentation, assume the user doesn't care about tenths of a degree.
//
//        //Anahat - checking for the sharedPreference of units chosen to display the values by the user
//        String tempUnit = PreferenceManager.getDefaultSharedPreferences(mContext).getString(mContext.getResources().getString(R.string.temp_unit_key),"");
//        if(tempUnit.equals("imperial")) {
//            high = metricToImperial(high);
//            low = metricToImperial(low);
//        }
//
//        long roundedHigh = Math.round(high);
//        long roundedLow = Math.round(low);
//
//        String highLowStr = roundedHigh + "/" + roundedLow;
//        return highLowStr;
//    }
//
//    private double metricToImperial(double temp){
//        temp = ((temp*9)/5) + 32;
//        return temp;
//    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private /*String[]*/ void getWeatherDataFromJson(String forecastJsonStr, int numDays, String locationQuery)
            throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        //Anahat - Calling addLocation method to add new locations using content providers and resolvers
        long locationId = addLocation(locationQuery, cityName, cityLatitude, cityLongitude);

        // Get and insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

          //Anahat - Commenting out resultStr string array because now we are using SimpleCursorAdaptor for reading data from DB and we do not need to return string array anymore.
//        String[] resultStrs = new String[numDays];

        for(int i = 0; i < weatherArray.length(); i++) {
            // These are the values that will be collected.

            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            dateTime = dayForecast.getLong(OWM_DATETIME);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            // Description is in a child array called "weather", which is 1 element long.
            // That element also contains a weather code.
            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);

            //Anahat - Removing the code lines below of setting the string in resultStr string array because we are NOW using SimpleCursorAdaptor. Thus, no need to return an array.
//            String highAndLow = formatHighLows(high, low);
//            String day = getReadableDateString(dateTime);
//            resultStrs[i] = day + " - " + description + " - " + highAndLow;
            //Anahat - Ending the commenting here
        }

        //Anahat - Inserting the weather values in the database using bulk insert of contentProvider
        if(cVVector.size() > 0){
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
        }
        Log.d(LOG_TAG, "FetchWeatherTask completed. " + cVVector.size() + " records inserted");

//        return resultStrs;
    }
}

