package me.webbdev.minilibris.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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


public class ReserveIntentService extends IntentService {


    private static final String TAG = "ReserveIntentService";
    public static final String RESERVE_INTENT_SERVICE_READY = "reserve_intent_service_ready";
    private String url = "http://minilibris.webbdev.me/minilibris/api/reservation";

    public ReserveIntentService(String name) {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        long book_id = intent.getLongExtra("id",-1);
        int year = intent.getIntExtra("year",-1);
        int month = intent.getIntExtra("month",-1);
        int day = intent.getIntExtra("day",-1);
        String result = sendReservation(book_id, year, month, day);
        Intent broadcastIntent = new Intent(RESERVE_INTENT_SERVICE_READY);
        boolean success = (result=="");
        broadcastIntent.putExtra("success", success);
        if (!success) {
            broadcastIntent.putExtra("message", result);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

    }

    public String sendReservation(long book_id, int year, int month, int day) {
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
                            if (jsonobject.has("errors")) {
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