package com.example.android.sunshine_v2.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine_v2.MainActivity;
import com.example.android.sunshine_v2.R;
import com.example.android.sunshine_v2.utilities.SunshineDateUtils;
import com.example.android.sunshine_v2.utilities.SunshineWeatherUtils;

/**
 * Created by Lenovo on 12/8/2017.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>{

    private final Context mContext;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    /*
     * Below, we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onClick method whenever
     * an item is clicked in the list.
     */
    final private ForecastAdapterOnclickHandler mClickHandler;

    public interface ForecastAdapterOnclickHandler{
        //Refactor onClick to accept a long as its parameter rather than a String
        void onClick(long date);
    }

    private Cursor mCursor;
    private boolean mUseTodayLayout;

    public ForecastAdapter(@NonNull Context context, ForecastAdapterOnclickHandler clickHandler){
        mContext = context;
        mClickHandler = clickHandler;
        mUseTodayLayout = mContext.getResources().getBoolean(R.bool.use_today_layout);
    }
    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int layoutId;

        switch (viewType){
            case VIEW_TYPE_TODAY:{
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY:{
                layoutId = R.layout.forecast_list_item;
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        View view = LayoutInflater.from(mContext)
                .inflate(layoutId,viewGroup,false);
        view.setFocusable(true);

        return new ForecastAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position){
        // Move the cursor to the appropriate position
        mCursor.moveToPosition(position);

        /****************
         * Weather Icon *
         ****************/
        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;
        int viewType = getItemViewType(position);

        switch (viewType){
            case VIEW_TYPE_TODAY:
                weatherImageId = SunshineWeatherUtils
                        .getLargeArtResourceIdForWeatherCondition(weatherId);
                break;
            case VIEW_TYPE_FUTURE_DAY:
                weatherImageId = SunshineWeatherUtils
                        .getSmallArtResourceIdForWeatherCondition(weatherId);
                break;
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        forecastAdapterViewHolder.iconView.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
        // Generate a weather summary with the date, description, high and low
        /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);

        /* Get human readable string using our utility method */
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext,dateInMillis, false);

        forecastAdapterViewHolder.dateView.setText(dateString);
        /***********************
         * Weather Description *
         ***********************/

        /* Use the weatherId to obtain the proper description */
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);

        /* Create the accessibility (a11y) String from the weather description */
        String descriptionAlly = mContext.getString(R.string.a11y_forecast, description);

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.descriptionView.setText(description);
        forecastAdapterViewHolder.descriptionView.setContentDescription(descriptionAlly);

        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = SunshineWeatherUtils.formatTemperature(mContext,highInCelsius);
        /* Create the accessibility (a11y) String from the weather description */
        String highAlly = mContext.getString(R.string.a11y_high_temp, highString);

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.highTempView.setText(highString);
        forecastAdapterViewHolder.highTempView.setContentDescription(highAlly);

        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);

        String lowString = SunshineWeatherUtils.formatTemperature(mContext,  lowInCelsius);
        String lowAlly = mContext.getString(R.string.a11y_low_temp,lowString);

        // Display the summary that you created above
        forecastAdapterViewHolder.lowTempView.setText(lowString);
        forecastAdapterViewHolder.lowTempView.setContentDescription(lowAlly);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mCursor)return 0;
        return mCursor.getCount();
    }

    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position. This method is useful when we want to use different layouts for different items
     * depending on their position. In Sunshine, we take advantage of this method to provide a
     * different layout for the "today" layout. The "today" layout is only shown in portrait mode
     * with the first item in the list.
     *
     * @param position index within our RecyclerView and Cursor
     * @return the view type (today or future day)
     */
    @Override
    public int getItemViewType(int position){
        if (mUseTodayLayout && position ==0){
            return VIEW_TYPE_TODAY;
        }else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    //Create a new method that allows you to swap Cursors.
    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;
        final ImageView iconView;

        ForecastAdapterViewHolder(View view){
            super(view);

            iconView =(ImageView) view.findViewById(R.id.weather_icon);
            dateView = (TextView) view.findViewById(R.id.date);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            highTempView =(TextView) view.findViewById(R.id.high_temperature);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            //Instead of passing the String for the clicked item, pass the date from the cursor
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }










}
