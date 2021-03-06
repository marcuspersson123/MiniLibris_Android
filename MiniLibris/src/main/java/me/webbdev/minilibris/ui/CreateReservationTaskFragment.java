package me.webbdev.minilibris.ui;

import android.app.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.io.IOException;
import java.io.InputStream;

import me.webbdev.minilibris.R;

// A reservation task that creates a reservation on the server.
// Extends TaskFragment
public class CreateReservationTaskFragment extends TaskFragment {

    private long book_id;
    private int year;
    private int month;
    private int day;

    private static final String TAG = "CreateReservationTaskFragment";
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/reservation";

    private String result;
    private int userId;

    // A few setters to provide the necessary data to do the task.

    public void setBookId(long book_id) {
        this.book_id = book_id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setYear(int mYear) {
        this.year = mYear;
    }

    public void setMonth(int mMonth) {
        this.month = mMonth;
    }

    public void setDay(int mDay) {
        this.day = mDay;
    }

    // A getter for the result of the task.

    public String getResult() {
        return result;
    }


    public CreateReservationTaskFragment() {
        super();
    }

    // This is an invisible fragment, though it has to return a view.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.empty,
                container, false);
    }

    // The asynchronous task:
    // Creates a JSON object that is POST:ed to the server.
    // If an error occurred the "result" variable holds the message.
    // If there is an array of errors, the first message will be in "result"
    public void doAsyncWork() {
        String result = null;
        String begins = getBegins();
        String ends = getEnds();
     //   SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_info", Activity.MODE_PRIVATE);
     //   int userId = sharedPreferences.getInt("user_id", -1);

        try {
            JSONObject json = new JSONObject();

            json.put("book_id", book_id);
            json.put("user_id", userId);
            json.put("begins",begins);
            json.put("ends", ends);
            json.put("is_lent", false);

            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("json", json.toString()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams);
            entity.setContentEncoding(HTTP.UTF_8);

            InputStream inputStream;
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
                            JSONArray errors = jsonobject.optJSONArray("errors");
                            if (errors != null && errors.length()>0) {
                                JSONObject error = errors.optJSONObject(0);
                                String summary = error.getString("summary");
                                result = summary;
                            }
                    } catch (JSONException e) {
                        result = "Serverfel";
                    }
                } else {
                    result = "Serverfel";
                }
            } else {
                result = "Non existing inputstream";
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "client protocol", e);
            result = "Client protocol error";
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
            result = "Inget Internet";
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        this.result = result;
    }

    // Returns the "ends" variable that is to be sent to the server
    private String getEnds() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        calendar.add(Calendar.DAY_OF_MONTH,20);
        String ends = simpleDateFormat.format(calendar.getTime());
        return ends;
    }

    // Returns the "begins" variable that is to be sent to the server
    private String getBegins() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String begins = simpleDateFormat.format(calendar.getTime());
        return begins;

    }

}
