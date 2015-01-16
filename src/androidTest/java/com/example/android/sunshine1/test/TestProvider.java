package com.example.android.sunshine1.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;
import android.widget.CursorAdapter;

import com.example.android.sunshine1.data.WeatherContract.LocationEntry;
import com.example.android.sunshine1.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine1.data.WeatherDbHelper;

import junit.framework.Assert;

import java.util.Map;
import java.util.Set;

/**
 * Created by Anahat.Babbar on 9/6/2014.
 */

//Anahat - This file was initially copied from TestDB class and then modified to test the provider

public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    //Anahat - Adding some constants that will be used in the testing. These constants will be used only for TestProvider. SO these were NOT copied from TestDB
    static public String TEST_CITY_NAME = "North Pole";
    static public String TEST_LOCATION = "99705";
    static public String TEST_DATE = "20141205";


    //Anahat - Important. setUp() method below  is called first by the test runner to setup the environment before each test
    public void setUp(){
        deleteAllRecords();
    }

    //Anahat - Custom method added below to test delete content providers.
    public void deleteAllRecords(){
        //Calling content resolver to delete weatherEntry table
        mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
                                                null,
                                                null);

        //Anahat - calling content resolver to delete locationEntry table
        mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,
                null,
                null);

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    //Anahat Adding tests to test update method of WeatherProvider
    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        validateCursor(cursor, updatedValues);
    }

    // Anahat - Make sure we can delete after adding/updating stuff so taht further tests can be run
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }


    //Anahat - Important. Android tests are executed in the order the tests are declared below.

    public void testGetType(){
        // content://com.example.android.sunshine1.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine1.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine1.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine1.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine1.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine1.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine1.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine1.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine1.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine1.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadProvider(){
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createNorthPoleLocationValues();

        long locationRowId;

        //Anahat - Removing the below lines from TestDB file and adding a line to call contentResolver instead to test WeatherProvider
        //locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);
        //assertTrue(locationRowId != -1);
        //Log.d(LOG_TAG, "New row id: " + locationRowId);
        Uri locationInsertUri = getContext().getContentResolver().insert(LocationEntry.CONTENT_URI,testValues);
        locationRowId = ContentUris.parseId(locationInsertUri);
        assertTrue(locationInsertUri != null);


        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Anahat - Testing LOCATION URI
        Cursor cursor = getContext().getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, testValues);

        // Anahat - Testing LOCATION_ID URI passing in the id
        cursor = getContext().getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null
        );

        validateCursor(cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = createWeatherValues(locationRowId);

        //Anahat - Removing the below line from TestDB file and adding a line to call contentResolver instead to test WeatherProvider
        //long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        //assertTrue(weatherRowId != -1);
        Uri weatherInsertUri = getContext().getContentResolver().insert(WeatherEntry.CONTENT_URI,weatherValues);
        assertTrue(weatherInsertUri != null);

        //Anahat - This is a change from TestDB class method. This is just a way of receiving data from content provider
        Cursor weatherCursor = getContext().getContentResolver().query(
          WeatherEntry.CONTENT_URI,
          null, //  leaving columns null just returns every column in the table
          null, // cols for where clause
          null, // values for 'where' clause
          null // sort order
        );

        validateCursor(weatherCursor, weatherValues);

        //Anahat - It is a good practice to close the cursor once you are done. Else, in onStop(), you will get CursorNotClosed exception
        weatherCursor.close();

        //Anahat - This is to test WeatherEntry with location
        weatherCursor = getContext().getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                null, //  leaving columns null just returns every column in the table
                null, // cols for where clause
                null, // values for 'where' clause
                null // sort order
        );
        validateCursor(weatherCursor, weatherValues);
        weatherCursor.close();

        //Anahat - This is to test WeatherEntry with location and the START Date
        weatherCursor = getContext().getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION,TEST_DATE),
                null, //  leaving columns null just returns every column in the table
                null, // cols for where clause
                null, // values for 'where' clause
                null // sort order
        );
        validateCursor(weatherCursor, weatherValues);
        weatherCursor.close();

        //Anahat - This is to test WeatherEntry with location and JUST the Date
        weatherCursor = getContext().getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION,TEST_DATE),
                null, //  leaving columns null just returns every column in the table
                null, // cols for where clause
                null, // values for 'where' clause
                null // sort order
        );
        validateCursor(weatherCursor, weatherValues);
        weatherCursor.close();

        dbHelper.close();
    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -147.353);

        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
