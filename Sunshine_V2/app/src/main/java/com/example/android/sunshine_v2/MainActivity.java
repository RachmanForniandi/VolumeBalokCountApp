package com.example.android.sunshine_v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.sunshine_v2.adapter.ForecastAdapter;
import com.example.android.sunshine_v2.data.SunshinePreferences;
import com.example.android.sunshine_v2.data.WeatherContract;
import com.example.android.sunshine_v2.sync.SunshineSyncUtils;
import com.example.android.sunshine_v2.utilities.FakeDataUtils;
import com.example.android.sunshine_v2.utilities.NetworkUtils;
import com.example.android.sunshine_v2.utilities.OpenWeatherJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,ForecastAdapter.ForecastAdapterOnclickHandler {

    //Create a field to store the weather display TextView and else
    private static  final String TAG = MainActivity.class.getSimpleName();

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    public static final int ID_FORECAST_LOADER = 50;

    private RecyclerView mRecyclerView;
    public ForecastAdapter mForecastAdapter;

    private ProgressBar mLoadingIndicator;
    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        getSupportActionBar().setElevation(0f);

        //Use findViewById to get a reference to the weather display TextView
        /*
         * Using findViewById, we get a reference to our TextView from xml. This allows us to
         * do things like set the text of the TextView.
         */
        mRecyclerView = (RecyclerView)findViewById(R.id.rcView_forecast);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar)findViewById(R.id.pb_loading);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        //Use setHasFixedSize(true) on mRecyclerView to designate that all items in the list will have the same size
        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        //set mForecastAdapter equal to a new ForecastAdapter
        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = new ForecastAdapter(this,this);

        //Use mRecyclerView.setAdapter and pass in mForecastAdapter
        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        // Remove the code for the AsyncTask and initialize the AsyncTaskLoader
        /*
         * This ID will uniquely identify the Loader. We can use it, for example, to get a handle
         * on our Loader at a later point in time through the support LoaderManager.
         */
        showLoading();

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(ID_FORECAST_LOADER,null, this);

        //Call SunshineSyncUtils's startImmediateSync method
        SunshineSyncUtils.initialize(this);
    }

    /**
     * This method uses the URI scheme for showing a location found on a map in conjunction with
     * an implicit Intent. This super-handy intent is detailed in the "Common Intents" page of
     * Android's developer site:
     *
     * @see "http://developer.android.com/guide/components/intents-common.html#Maps"
     * <p>
     * Protip: Hold Command on Mac or Control on Windows and click that link to automagically
     * open the Common Intents page
     */
    private void openPreferredLocationMap(){
        double[] coords = SunshinePreferences.getLocationCoordinates(this);
        String posLat = Double.toString(coords[0]);
        String posLong = Double.toString(coords[1]);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        }else {
            Log.d(TAG,"Couldn't call " + geoLocation.toString()
                    + ", no receiving apps installed");
        }
    }

    //Refactor onCreateLoader to return a Loader<Cursor>, not Loader<String[]>
    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created. This Activity only uses one loader, so we don't necessarily NEED to check the
     * loaderId, but this is certainly best practice.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, final Bundle bundle) {
        switch (loaderId){
            case ID_FORECAST_LOADER:
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnWards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder
                );

                default: throw new RuntimeException("Loader Not Implemented: " +loaderId);
        }
    }

    // When the load is finished, show either the data or an error message if there is no data
    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Call mForecastAdapter's swapCursor method and pass in the new Cursor
        mForecastAdapter.swapCursor(data);
        //If mPosition equals RecyclerView.NO_POSITION, set it to 0
        if (mPosition == RecyclerView.NO_POSITION)mPosition = 0;
        //Smooth scroll the RecyclerView to mPosition
        mRecyclerView.smoothScrollToPosition(mPosition);

        //If the Cursor's size is not equal to 0, call showWeatherDataView
        if (data.getCount() !=0)showWeatherDataView();
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Call mForecastAdapter's swapCursor method and pass in null
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mForecastAdapter.swapCursor(null);
    }


    @Override
    public void onClick(long date){
        // Remove the Toast and launch the DetailActivity using an explicit Intent
        Intent intentDetailWeather = new Intent(MainActivity.this, DetailActivity.class);

        //Refactor onClick to pass the URI for the clicked date with the Intent
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        intentDetailWeather.setData(uriForDateClicked);
        startActivity(intentDetailWeather);
    }

    //Create a method called showWeatherDataView that will hide the error message and show the weather data
    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showLoading(){
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    //Override onCreateOptionsMenu to inflate the menu for this Activity
    //Return true to display the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;

    }

    //Override onOptionsItemSelected to handle clicks on the refresh button
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        //Launch the map when the map menu item is clicked
        if(id == R.id.action_map){
            openPreferredLocationMap();
            return true;
        }

        if (id == R.id.action_settings){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
