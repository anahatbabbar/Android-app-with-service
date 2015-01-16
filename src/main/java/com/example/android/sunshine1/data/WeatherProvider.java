package com.example.android.sunshine1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.android.sunshine1.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine1.data.WeatherContract.LocationEntry;

/**
 * Created by Anahat.Babbar on 9/10/2014.
 */
public class WeatherProvider extends ContentProvider{

    //Anahat - Adding a class variable to create a JOIN query
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    //Anahat - Adding a static constructor block to initialize the static variable and create the join query
    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(WeatherEntry.TABLE_NAME + " INNER JOIN " +
                                                            LocationEntry.TABLE_NAME + " ON " +
                                                            WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_LOC_KEY  +
                                                            "=" + LocationEntry.TABLE_NAME + "." + LocationEntry._ID);
    }

    //Anahat - Adding the query parameters now for the join query
    //Anahat - Creating parameters in the query when both location setting and start date are given. The location settings code query parameter with start date parameter will be added later in the query
    private static final String sLocationSettingSelectionWithStartDateSelection =
            LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_DATETEXT + ">= ?";

    //Anahat - Creating parameters in the query when only location setting is given
    private static final String sLocationSettingSelection =
            LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ?" ;

    //Anahat - Creating parameter in the query when location and a specific weather date is given (NOT a start date)
    private static final String sLocationSettingWithDaySelection =
            LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_DATETEXT + " = ?";

    // Anahat - Defining a custom method to create a JOIN query and return a cursor of result for location and START date
    private Cursor getWeatherByLocationSetting (Uri uri, String[] projection, String sortOrder){
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null){
            selection = sLocationSettingSelection;
            selectionArgs = new String[] {locationSetting};
        }
        else {
            selection = sLocationSettingSelectionWithStartDateSelection;
            selectionArgs = new String[] {locationSetting, startDate};
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                                                           projection,
                                                           selection,
                                                           selectionArgs,
                                                           null,
                                                           null,
                                                           sortOrder);
    }

    //Anahat - Defining a custom method to create a JOIN query and return a cursor of result for location and DATE
    private Cursor getWeatherByLocationSettingWithDate(Uri uri, String[] projection, String sortOrder){
        String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        String day = WeatherEntry.getDateFromUri(uri);
        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingWithDaySelection,
                new String[] {locationSetting, day},
                null,
                null,
                sortOrder);
    }

    //Anahat - Instance variable for our database helper
    private WeatherDbHelper mOpenHelper;

    //Anahat - Uri matcher constants
    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    //Anahat - This uriMatcher will be used by content resolver to match the uris with content provider and table names
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    //Anahat - Adding a new method to the class to build the Uri matcher object. Here use '*' to match string values passed and '#' to match integer values passed
    private static UriMatcher buildUriMatcher(){

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER,100);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*", 101);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*/*",102);

        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION,300);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION + "/#",301);

        return matcher;
    }

    //Anahat - Creating the database helper class object. This is the first method you would implement in this class
    @Override
    public boolean onCreate() {

        //Anahat - Create a WeatherDBHelper object and getting a handle on the database/data storage
        mOpenHelper = new WeatherDbHelper(getContext());

        //Anahat - Returning true when this method gets processed successfully
        return true;
    }

    //Anahat - The third method you would implement in this class.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        final int match = sUriMatcher.match(uri);
        switch(match){
            case WEATHER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case WEATHER_WITH_LOCATION:{
                retCursor = getWeatherByLocationSetting(uri,projection,sortOrder);
                break;
            }

            case WEATHER_WITH_LOCATION_AND_DATE:{
                retCursor = getWeatherByLocationSettingWithDate(uri, projection, sortOrder);
                break;
            }

            case LOCATION:{
                retCursor = mOpenHelper.getReadableDatabase().query(
                  LocationEntry.TABLE_NAME,
                  projection,
                  selection,
                  selectionArgs,
                  null,
                  null,
                  sortOrder
                );
                break;
            }

            case LOCATION_ID:{
                long id = ContentUris.parseId(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                    LocationEntry.TABLE_NAME,
                    projection,
                    LocationEntry._ID + "='" + Long.toString(id) +"'",
                    null,
                    null,
                    null,
                    sortOrder
                );
               break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: "+ uri);
        }

       //Anahat - The below line registers a ContentObserver on the cursor. This cursor will then be notified for any changes in the uri or its descendents. By descendents, we mean any uri that shares the base uri.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }


    //Anahat - Returns the mime type associated with a given URI. This is the second method you should implement in content provider
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: "+ uri);
        }
        //return null;
    }

    //Anahat - Override this method to insert records in the tables
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        Uri retUri;
        final int match = sUriMatcher.match(uri);

        //Anahat - Very important. We are matching ONLY the base URIs in insert method. This is because when we insert into databse, we want to notify every content observer that might be observing any data change.
        //Anahat - The cursors register themselves for notify for descendents, i.e. notifying the root uri will also notify the content observers of the descendants uris. If we were to notify on a descendant's uri
        //Anahat - straight away, then the root uri, which will certainly get impacted with any change in descendants uri will not get notified, which is wrong.
        switch (match) {
            case WEATHER: {
                long _id = mOpenHelper.getWritableDatabase().insert(WeatherEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    retUri = WeatherEntry.buildWeatherUri(_id);
                } else {
                    throw new SQLException("Failed to insert new row in uri: " + uri);
                }
                break;
            }

            case LOCATION: {
                long _id = mOpenHelper.getWritableDatabase().insert(LocationEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    retUri = LocationEntry.buildLocationUri(_id);
                } else {
                    throw new SQLException("Failed to insert new row in uri: " + uri);
                }
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //Anahat - the line below will notify all content observer of the change in the data corresponding to the uri
        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    //Anahat - Overriding delete method of content provider. It returns the number of records affected
    //Anahat - VERY IMPORTANT. PASSING NULL SELECTION DELETES ALL RECORDS IN THE TABLE
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int intNumOfRecords;
        final int match = sUriMatcher.match(uri);

        //Anahat - Very important. We are matching ONLY the base URIs in insert method. This is because when we insert into databse, we want to notify every content observer that might be observing any data change.
        //Anahat - The cursors register themselves for notify for descendents, i.e. notifying the root uri will also notify the content observers of the descendants uris. If we were to notify on a descendant's uri
        //Anahat - straight away, then the root uri, which will certainly get impacted with any change in descendants uri will not get notified, which is wrong.
        switch (match) {
            case WEATHER: {
                intNumOfRecords = mOpenHelper.getWritableDatabase().delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case LOCATION: {
                intNumOfRecords = mOpenHelper.getWritableDatabase().delete(LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //Anahat - the line below will notify all content observer of the change in the data corresponding to the uri. Notify only if there is a delete actually.
        if(null == selection || intNumOfRecords != 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return intNumOfRecords;
    }

    //Anahat - Overriding update method of content provider
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int intNumOfRecords;
        final int match = sUriMatcher.match(uri);

        //Anahat - Very important. We are matching ONLY the base URIs in insert method. This is because when we insert into databse, we want to notify every content observer that might be observing any data change.
        //Anahat - The cursors register themselves for notify for descendents, i.e. notifying the root uri will also notify the content observers of the descendants uris. If we were to notify on a descendant's uri
        //Anahat - straight away, then the root uri, which will certainly get impacted with any change in descendants uri will not get notified, which is wrong.
        switch (match) {
            case WEATHER: {
                intNumOfRecords = mOpenHelper.getWritableDatabase().update(WeatherEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            }

            case LOCATION: {
                intNumOfRecords = mOpenHelper.getWritableDatabase().update(LocationEntry.TABLE_NAME,contentValues, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //Anahat - the line below will notify all content observer of the change in the data corresponding to the uri
        if(intNumOfRecords != 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return intNumOfRecords;
    }

    //Anahat - This is an Optional method added. This is added to do quick bulk insertions.
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction(); //Anahat - Starting db transaction
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful(); // Anahat - Commiting db transaction
                } finally {
                    db.endTransaction(); // Anahat - Ending transaction
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
