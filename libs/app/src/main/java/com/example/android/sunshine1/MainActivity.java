package com.example.android.sunshine1;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback{

    private static String LOG_TAG = MainActivity.class.toString();

    boolean mTwoPane;

    //Anahat - Defining the method of the custom interface Callback of ForecastFragment
    @Override
    public void onItemSelected(String date) {
        if(mTwoPane){
            //Anahat - Here the Activity already exists. Just detail fragment is being replaced or added running in the Main Activity for tablets.
            Bundle args = new Bundle();
            args.putString(DetailActivity.DATE_KEY, date);
            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            //Anahat - Replacing the detail fragment now
            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, df).commit();

        }
        else{
            //Anahat - Here detail ACTIVITY is being called from Main ACTIVITY running on a Phone
             Intent intent = new Intent(this, DetailActivity.class);
             intent.putExtra(DetailActivity.DATE_KEY, date/*weatherString*/);
             //Anahat - StartActivity method below calls the onCreate method of DetailActivity.
             startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Anahat - Commenting out the below line now that we are using 2 pane view and statically attaching forecastFragment to the layout
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new ForecastFragment())
//                    .commit();
//        }

        //Anahat - Now check if this onCreate activity method is called for 2 pane layouts (sw-600dp) or a single pane layout
        if(findViewById(R.id.weather_detail_container) != null){
            //Anahat - this means the onCreate is called for 2 pane device
            mTwoPane = true;

            //Anahat In two pane mode add or replace the detail view in the activity using fragment transaction
            if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, new DetailFragment())
                    .commit();
            }
        }
        else{
            mTwoPane = false;
        }

        //Anahat - Telling the forecast fragment whether it is one pane or 2 pane mode. This will dictate
        // how today's whether in the list will be displayed (different between phone and tablet)
        //Anahat - Now using getSupportFragmentManager to retrieve the already created forecastFragment
        ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);


        Log.v(LOG_TAG, "This is from onCreate() method");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //Anahat - Calling Explicit Intent Settings when the user selects settings from Menu
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        //Anahat - Calling Implicit Intent Android Maps when the user selects Maps from the menu
        if(id == R.id.action_map){
            openPreferredLocationInMap();
        }
        return super.onOptionsItemSelected(item);
    }

    //Anahat - Method created to call Maps intent
    private void openPreferredLocationInMap(){
        //Anahat - Declaring constant string for building the Maps intent URI
        final String MAPS_INTENT_BASE_STRING = "geo:0,0";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String locationValue = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.pref_location_key),getResources().getString(R.string.pref_location_default));
        Uri mapsIntentUri = Uri.parse(MAPS_INTENT_BASE_STRING).buildUpon().appendQueryParameter("q",locationValue).build();
        Log.v(this.getClass().toString(),"Maps intent URI: "+mapsIntentUri.toString());
        intent.setData(mapsIntentUri);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }
        else{
            Log.d(this.getClass().toString(),"No Maps app found on the device");
        }
    }

    //Anahat - Below is the experiment on application lifecycle. Added debug statements on the activity lifecycle methods to understand the sequence of their calls.
    @Override
    public void onStart(){
        super.onStart();
        Log.v(LOG_TAG, "This is from onStart() method");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.v(LOG_TAG, "This is from onStop() method");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.v(LOG_TAG, "This is from onPause() method");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(LOG_TAG, "This is from onResume() method");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(LOG_TAG, "This is from onDestroy() method");
    }
}
