package me.webbdev.minilibris.ui;

import android.app.Activity;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
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


public class DeleteReservationTaskFragment extends TaskFragment {

    private static final String TAG = "DeleteReservationTaskFragment";
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/reservation";
    private String result;
    private int reservationId;

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getResult() {
        return result;
    }

    public DeleteReservationTaskFragment(String TAG) {
        super(TAG);
    }

    public void doAsyncWork() {
        //String result;

        try {
            InputStream inputStream;
            HttpClient httpclient = new DefaultHttpClient();
            HttpDelete httpDelete = new HttpDelete(url + "/" + reservationId + "?mobile=1");

            HttpResponse httpResponse = httpclient.execute(httpDelete);
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) {
                String jsonString = convertInputStreamToString(inputStream);
                if (jsonString != null) {
                    try {
                        JSONObject jsonobject = new JSONObject(jsonString);
                        //boolean serverSuccess = jsonobject.getBoolean("success");
                        if (jsonobject != null) {
                            if (!jsonobject.getBoolean("success")) {
                                JSONObject validationContainer = jsonobject.optJSONObject("validationContainer");
                                JSONArray errors = validationContainer.getJSONArray("errors");
                                if (errors != null && errors.length() > 0) {
                                    JSONObject error = errors.getJSONObject(0);
                                    result = error.getString("summary");
                                }
                            } else {
                                result = null;
                            }

                        } else {
                            result = "No JSON object";
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
        }

        //this.result = result;

    }


}
