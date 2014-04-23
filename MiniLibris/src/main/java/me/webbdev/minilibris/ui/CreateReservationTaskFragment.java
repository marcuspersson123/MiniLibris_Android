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


public class CreateReservationTaskFragment extends TaskFragment {

    private static final String TAG = "ReserveFragment";
    public static final String RESERVE_INTENT_SERVICE_READY = "reserve_intent_service_ready";
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/reservation";
    private String result;

    public void setBookId(long book_id) {
        this.book_id = book_id;
    }

    public void setYear(int mYear) {
        this.mYear = mYear;
    }

    public void setMonth(int mMonth) {
        this.mMonth = mMonth;
    }

    public void setDay(int mDay) {
        this.mDay = mDay;
    }

    public String getResult() {
        return result;
    }

    private long book_id;
    private int mYear;
    private int mMonth;
    private int mDay;

    public CreateReservationTaskFragment(String TAG) {
        super(TAG);
    }

    public void doAsyncWork() {
        String result = null;
        String begins = getBegins();
        String ends = getEnds();

      /*  Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String begins = simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH,20);
        String ends = simpleDateFormat.format(calendar.getTime());*/
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
                        //boolean serverSuccess = jsonobject.getBoolean("success");
                        if (jsonobject != null) {
                            JSONArray errors = jsonobject.optJSONArray("errors");
                            if (errors != null && errors.length()>0) {
                                result = "Not allowed";
                            }

                        }
                    } catch (JSONException e) {
                        result = "Malformed JSON object";
                    }
                } else {
                    result = "Malformed json string";
                }
            } else {
                result = "Non existing inputstream";
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "client protocol", e);
            result = "Client protocol error";
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
            result = "IO Exception";
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        this.result = result;

    }

    private String getEnds() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        calendar.add(Calendar.DAY_OF_MONTH,20);
        String ends = simpleDateFormat.format(calendar.getTime());
        return ends;
    }

    private String getBegins() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String begins = simpleDateFormat.format(calendar.getTime());
        return begins;

    }

}
