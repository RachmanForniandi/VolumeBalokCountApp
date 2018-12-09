package com.example.android.sunshine_v2.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.sunshine_v2.utilities.SunshineDateUtils;

/**
 * Created by USER on 26/01/2018.
 */

public class WeatherProvider extends ContentProvider {
    // Create static constant integer values named CODE_WEATHER & CODE_WEATHER_WITH_DATE to identify the URIs this ContentProvider can handle
    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 103;


    //Instantiate a static UriMatcher using the buildUriMatcher method
    /*
    * The URI Matcher used by this content provider. The leading "s" in this variable name
    * signifies that this UriMatcher is a static member variable of WeatherProvider and is a
    * common convention in Android programming.
    */
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Declare, but don't instantiate a WeatherDbHelper object called mOpenHelper
    private WeatherDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher(){

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In Sunshine, we use CODE_WEATHER or CODE_WEATHER_WITH_DATE.
         */

        /* This URI is content://com.example.android.sunshine/weather/ */
        matcher.addURI(authority, WeatherContract.PATH_FOR_WEATHER, CODE_WEATHER);

        /*
         * This URI would look something like content://com.example.android.sunshine/weather/1472214172
         * The "/#" signifies to the UriMatcher that if PATH_WEATHER is followed by ANY number,
         * that it should return the CODE_WEATHER_WITH_DATE code
         */
        matcher.addURI(authority,WeatherContract.PATH_FOR_WEATHER + "/#", CODE_WEATHER_WITH_DATE);

        return matcher;
    }

    /**
     * In onCreate, we initialize our content provider on startup. This method is called for all
     * registered content providers on the application main thread at application launch time.
     * It must not perform lengthy operations, or application startup will be delayed.
     *
     * Nontrivial initialization (such as opening, upgrading, and scanning
     * databases) should be deferred until the content provider is used (via {@link #query},
     * {@link #bulkInsert(Uri, ContentValues[])}, etc).
     */

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)){

            //Only perform our implementation of bulkInsert if the URI matches the CODE_WEATHER code
            case CODE_WEATHER:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    //try to insert all data
                    for(ContentValues value : values){
                        long weatherDate = value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if (!SunshineDateUtils.isDateNormalized(weatherDate)){
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }

                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1){
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    //executes after the try is complete
                    db.endTransaction();
                }

                if (rowsInserted > 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                //return to the number of rows inserted from bulkInsert implementation
                return rowsInserted;

                //If the URI does match match CODE_WEATHER, return the super implementation of bulkInsert
                default:
                    return super.bulkInsert(uri,values);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs,  String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)){
            case CODE_WEATHER_WITH_DATE:{
                /*
                 * In order to determine the date associated with this URI, we look at the last
                 * path segment. In the comment above, the last path segment is 1472214172 and
                 * represents the number of seconds since the epoch, or UTC time.
                 */
                String normalizedUtcDateString = uri.getLastPathSegment();

                /*
                 * The query method accepts a string array of arguments, as there may be more
                 * than one "?" in the selection statement. Even though in our case, we only have
                 * one "?", we have to create a string array that only contains one element
                 * because this method signature accepts a string array.
                 */
                String[] selectionArguments = new String[]{normalizedUtcDateString};

                cursor = mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        /*
                         * A projection designates the columns we want returned in our Cursor.
                         * Passing null will return all columns of data within the Cursor.
                         * However, if you don't need all the data from the table, it's best
                         * practice to limit the columns returned in the Cursor with a projection.
                         */
                        projection,
                        /*
                         * The URI that matches CODE_WEATHER_WITH_DATE contains a date at the end
                         * of it. We extract that date and use it with these next two lines to
                         * specify the row of weather we want returned in the cursor. We use a
                         * question mark here and then designate selectionArguments as the next
                         * argument for performance reasons. Whatever Strings are contained
                         * within the selectionArguments array will be inserted into the
                         * selection statement by SQLite under the hood.
                         */
                        WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            /*
             * When sUriMatcher's match method is called with a URI that looks EXACTLY like this
             *
             *      content://com.example.android.sunshine/weather/
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return all of the weather in our weather table.
             *
             * In this case, we want to return a cursor that contains every row of weather data
             * in our weather table.
             */
            case CODE_WEATHER:{
                cursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default: throw  new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //Call setNotificationUri on the cursor and then return the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not yet to implementing getType.");
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        throw new RuntimeException("Not yet to implementing the delete method. Use bulkInsert instead");
    }

    @Override
    public int delete(@NonNull Uri uri, String selection,  String[] selectionArgs) {
        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numberRowsDeleted;

        if (null == selection)selection = "1";

        switch (sUriMatcher.match(uri)){
            //Only implement the functionality, given the proper URI, to delete ALL rows in the weather table
            case CODE_WEATHER:
                numberRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                   WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,selectionArgs
                );
                break;
                default: throw new UnsupportedOperationException("Unknown Uri: "+ uri);
        }

         /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numberRowsDeleted > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        //Return the number of rows deleted
        return numberRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] selectionArgs) {
        throw new RuntimeException("Not yet to implementing update method.");
    }

    @Override
    @TargetApi(11)
    public void shutdown(){
        mOpenHelper.close();
        super.shutdown();
    }
}
