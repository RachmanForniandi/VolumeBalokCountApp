package com.example.android.sunshine_v2.utilities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.example.android.sunshine_v2.DetailActivity;
import com.example.android.sunshine_v2.R;
import com.example.android.sunshine_v2.data.SunshinePreferences;
import com.example.android.sunshine_v2.data.WeatherContract;

import java.security.PublicKey;

/**
 * Created by USER on 21/03/2018.
 */

public class NotificationUtils {

    /*
     * The columns of data that we are interested in displaying within our notification to let
     * the user know there is new weather data available.
     */
    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 3004 is in no way significant.
     */
    //Create a constant int value to identify the notification
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    /**
     * Constructs and displays a notification for the newly updated weather for today.
     *
     * @param ctx Context used to query our ContentProvider and use various Utility methods
     */
    public static void notifyUserOfNewWeather(Context ctx) {
        Uri todaysWeatherUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(
                SunshineDateUtils.normalizeDate(System.currentTimeMillis()));

        /*
         * The MAIN_FORECAST_PROJECTION array passed in as the second parameter is defined in our WeatherContract
         * class and is used to limit the columns returned in our cursor.
         */
        Cursor todayWeatherCursor = ctx.getContentResolver().query(
                todaysWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null);

        /*
         * If todayWeatherCursor is empty, moveToFirst will return false. If our cursor is not
         * empty, we want to show the notification.
         */
        if (todayWeatherCursor.moveToFirst()) {

            /* Weather ID as returned by API, used to identify the icon to be used */
            int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);

            Resources resources = ctx.getResources();
            int largeArtResourceId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(resources, largeArtResourceId);

            String notificationTitle = ctx.getString(R.string.app_name);
            String notificationText = getNotificationTitle(ctx, weatherId, high, low);


            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            int smallArtResourceId = SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx)
                    .setColor(ContextCompat.getColor(ctx, R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true);

            // Create an Intent with the proper URI to start the DetailActivity
            /*
             * This Intent will be triggered when the user clicks the notification. In our case,
             * we want to open Sunshine to the DetailActivity to display the newly updated weather.
             */
            Intent detailIntentForToday = new Intent(ctx, DetailActivity.class);
            detailIntentForToday.setData(todaysWeatherUri);

            //Use TaskStackBuilder to create the proper PendingIntent
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(ctx);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            //Set the content Intent of the NotificationBuilder
            notificationBuilder.setContentIntent(resultPendingIntent);

            //Get a reference to the NotificationManager
            NotificationManager notificationManager = (NotificationManager) ctx
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            //Notify the user with the ID WEATHER_NOTIFICATION_ID
            /* WEATHER_NOTIFICATION_ID allows you to update or cancel the notification later on */
            notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());

            //Save the time at which the notification occurred using SunshinePreferences
            /*
             * Since we just showed a notification, save the current time. That way, we can check
             * next time the weather is refreshed if we should show another notification.
             */
            SunshinePreferences.saveLastNotificationTime(ctx, System.currentTimeMillis());
        }
        todayWeatherCursor.close();
    }

        private static String getNotificationTitle(Context context, int weatherId, double high, double low) {
            String shortDescription = SunshineWeatherUtils.getStringForWeatherCondition(context, weatherId);

            String notificationFormat = context.getString(R.string.format_notification);

            String notificationText = String.format(notificationFormat,
                    shortDescription,
                    SunshineWeatherUtils.formatTemperature(context,high),
                    SunshineWeatherUtils.formatTemperature(context,low));
            return notificationText;
        }
}
