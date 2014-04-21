package me.webbdev.minilibris.ui;

import android.app.Activity;

import android.app.Fragment;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

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


public class ReservationTaskFragment extends Fragment {

    private static final String TAG = "ReserveFragment";
    public static final String RESERVE_INTENT_SERVICE_READY = "reserve_intent_service_ready";
    private String url = "http://minilibris.webbdev.me/minilibris/api/reservation";
    private String result;

    public void setBook_id(long book_id) {
        this.book_id = book_id;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getResult() {
        return result;
    }

    private long book_id;
    private int year;
    private int month;
    private int day;

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    static interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute();
    }

    private TaskCallbacks mCallbacks;
    private DummyTask mTask;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
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
        mTask = new DummyTask();
        mTask.execute();
    }
    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /**
     * A dummy task that performs some (dumb) background work and
     * proxies progress updates and results back to the Activity.
     *
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class DummyTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                result = null;
                mCallbacks.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Void... ignore) {
            result = sendReservation();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute();
            }
        }
    }





    public String sendReservation() {
        String result = null;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month,day);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String begins = simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH,20);
        String ends = simpleDateFormat.format(calendar.getTime());
        try {
            JSONObject json = new JSONObject();

            json.put("book_id", book_id);
            json.put("user_id", 3);
            json.put("begins",begins);
            json.put("ends", ends);
            json.put("is_lent", false);

            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("json", json.toString()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams);
            entity.setContentEncoding(HTTP.UTF_8);

            InputStream inputStream = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(entity);

            HttpResponse httpResponse = httpclient.execute(httppost);
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) {
                String jsonString = convertInputStreamToString(inputStream);
                if (jsonString != null) {
                    try {
                        JSONObject jsonobject = new JSONObject(jsonString);
                        //boolean serverSuccess = jsonobject.getBoolean("success");
                        if (jsonobject != null) {
                            if (jsonobject.optJSONArray("errors").length()>0) {
                                result = "Not allowed";
                            }

                        }
                    } catch (JSONException e) {
                        result = "Malformed JSON";
                    }
                }
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "client protocol", e);
            result = "Client protocol error";
            //Toast.makeText(this,"Client protocol error",Toast.LENGTH_LONG);
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
            result = "IO Exception";
            //Toast.makeText(this,"Client/server error",Toast.LENGTH_LONG);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();
        if (result == "") {
            return null;
        } else {
            return result;
        }
    }


}
