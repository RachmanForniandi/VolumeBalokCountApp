package com.example.android.sunshine_v2.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by USER on 21/03/2018.
 */

public class SunshineFirebaseJobService extends JobService{

    //Declare an ASyncTask field called mFetchWeatherTask
    private AsyncTask<Void, Void, Void> mFetchWeatherTask;

    //Override onStartJob and within it, spawn off a separate ASyncTask to sync weather data
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        mFetchWeatherTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids){
                Context ctx = getApplicationContext();
                SunshineSyncTask.syncWeather(ctx);
                return null;
            }

            @Override
            protected void onPostExecute(Void mVoid){
                //Once the weather data is sync'd, call jobFinished with the appropriate arguements
                jobFinished(jobParameters,false);
            }
        };

        mFetchWeatherTask.execute();
        return true;
    }

    //Override onStopJob, cancel the ASyncTask if it's not null and return true
          /**
          * Called when the scheduling engine has decided to interrupt the execution of a running job,
          * most likely because the runtime constraints associated with the job are no longer satisfied.
          *
          * @return whether the job should be retried
          //* @see Job.Builder#setRetryStrategy(RetryStrategy)
          //* @see RetryStrategy
          */
    @Override
    public boolean onStopJob(JobParameters job) {
        if (mFetchWeatherTask != null){
            mFetchWeatherTask.cancel(true);
        }
        return true;
    }
}
