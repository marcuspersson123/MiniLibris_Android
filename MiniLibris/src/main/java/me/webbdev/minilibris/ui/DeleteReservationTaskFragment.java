package me.webbdev.minilibris.ui;

import android.app.Activity;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import me.webbdev.minilibris.R;


public class DeleteReservationTaskFragment extends TaskFragment {

    private static final String TAG = "DeleteReservationTaskFragment";
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/reservation";
    private String result;
    private int reservationId;

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getResult() {
        return this.result;
    }

    public DeleteReservationTaskFragment() {
        super();
    }

    // This is an invisible fragment. However, to be able to instantiate it in xml it has to return a view.
    // Returns an empty LinearLayout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.empty,
                container, false);
    }

    // Sends a registration id to be deleted to the server.
    // The variables sent are in the URL (reservation id and mobile)
    // After finishing, the "result" variable holds any error message or is null.
    public void doAsyncWork() {
        this.result = null;
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
                        if (jsonobject != null) {
                            if (!jsonobject.getBoolean("success")) {
                                JSONObject validationContainer = jsonobject.optJSONObject("validationContainer");
                                JSONArray errors = validationContainer.getJSONArray("errors");
                                if (errors != null && errors.length() > 0) {
                                    JSONObject error = errors.getJSONObject(0);
                                    this.result = error.getString("summary");
                                }
                            } else {
                                this.result = null;
                            }

                        } else {
                            this.result = "No JSON object";
                        }
                    } catch (JSONException e) {
                        this.result = "Malformed JSON object";
                    }
                } else {
                    this.result = "Malformed json string";
                }
            } else {
                this.result = "Non existing inputstream";
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "client protocol", e);
            this.result = "Client protocol error";
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
            this.result = "IO Exception";
        }

    }


}
