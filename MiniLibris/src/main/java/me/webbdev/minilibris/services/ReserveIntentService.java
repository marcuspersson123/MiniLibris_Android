package me.webbdev.minilibris.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ReserveIntentService extends IntentService {

    private static final String TAG = "ReserveIntentService";
    private String url = "http://minilibris.webbdev.me/minilibris/api/reservation";

    public ReserveIntentService() {
        super("Reserve service");
    }

    private JSONObject getJsonObject(String jsonString) {

        try {
            JSONObject jsonobject = new JSONObject(jsonString);
            boolean serverSuccess = jsonobject.getBoolean("success");
            if (serverSuccess) {
                return jsonobject;
            }
        } catch (JSONException e) {
            Log.e(TAG, "could not parse json", e);
        }
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long book_id = intent.getLongExtra("id",-1);
        int year = intent.getIntExtra("year",-1);
        int month = intent.getIntExtra("month",-1);
        int day = intent.getIntExtra("day",-1);
        String jsonString = sendReservation(book_id, year, month, day);
        if (jsonString != null) {
            JSONObject jsonObject = getJsonObject(jsonString);
            if (jsonObject != null) {
            }
        }


    }

    private String sendReservation(long book_id, int year, int month, int day) {
        String result = null;
        try {
            InputStream inputStream = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(book_id)));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse httpResponse = httpclient.execute(httppost);
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "client protocol", e);
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
        } finally {
            return result;
        }
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