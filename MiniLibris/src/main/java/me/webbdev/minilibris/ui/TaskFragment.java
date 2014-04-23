package me.webbdev.minilibris.ui;

import android.app.Activity;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public abstract class TaskFragment extends Fragment {


    private String TAG;

    public TaskFragment(String TAG) {
        this.TAG = TAG;
    }

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    static interface TaskCallback {
        void onPreExecute(String TAG);
        void onProgressUpdate(String TAG,int percent);
        void onCancelled(String TAG);
        void onPostExecute(String TAG);
    }

    private TaskCallback mTaskCallback;


    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mTaskCallback = (TaskCallback) activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

    }

    public void start() {
        // Create and execute the background task.
        InnerTask innerTask = new InnerTask();
        innerTask.execute();
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mTaskCallback = null;
    }

    /**
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class InnerTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (mTaskCallback != null) {
                mTaskCallback.onPreExecute(TAG);
            }
        }

        @Override
        protected Void doInBackground(Void... ignore) {
            doAsyncWork();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mTaskCallback != null) {
                mTaskCallback.onProgressUpdate(TAG,percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mTaskCallback != null) {
                mTaskCallback.onCancelled(TAG);
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mTaskCallback != null) {
                mTaskCallback.onPostExecute(TAG);
            }
        }
    }

    protected static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();
        if (result.equals("")) {
            return null;
        } else {
            return result;
        }
    }

    abstract void doAsyncWork();

}
