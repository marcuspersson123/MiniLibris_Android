package me.webbdev.minilibris.ui;

import android.app.Activity;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


// Abstract class that takes care of
// Invokes the subclass' "doAsyncWork()"
// Thread safely ells the Activity onPreExecute, onProgressUpdate, onCancelled and onPostExecute.
// The object gets started using "start()"
public abstract class TaskFragment extends Fragment {

    private String TAG;

    abstract void doAsyncWork();

    public TaskFragment(String TAG) {
        this.TAG = TAG;
    }

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    public static interface TaskFragmentCallback {
        void onPreExecute(String TAG);
        void onProgressUpdate(String TAG,int percent);
        void onCancelled(String TAG);
        void onPostExecute(String TAG);
    }

    private TaskFragmentCallback activity;


    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (TaskFragmentCallback) activity;
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

    // Starts the task
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
        this.activity = null;
    }

    /**
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class InnerTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (TaskFragment.this.activity != null) {
                TaskFragment.this.activity.onPreExecute(TAG);
            }
        }

        @Override
        protected Void doInBackground(Void... ignore) {
            doAsyncWork();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (TaskFragment.this.activity != null) {
                TaskFragment.this.activity.onProgressUpdate(TAG,percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (TaskFragment.this.activity != null) {
                TaskFragment.this.activity.onCancelled(TAG);
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (TaskFragment.this.activity != null) {
                TaskFragment.this.activity.onPostExecute(TAG);
            }
        }
    }

    // Converts an InputStream into a String.
    // Returns a String or null.
    // Stored here as it is commonly used in networking fragments.
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
}
