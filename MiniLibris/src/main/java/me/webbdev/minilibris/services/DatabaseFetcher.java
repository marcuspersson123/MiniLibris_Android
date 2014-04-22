package me.webbdev.minilibris.services;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.webbdev.minilibris.database.MiniLibrisContract;


public class DatabaseFetcher {

    private String url;

    private Timestamp fetchTimestamp;
     Context context;
    private static String TAG = "DatabaseFetcher";
    private static String SHARED_PREFERENCES_NAME = "ServerDbChanges";
    private static String LAST_SYNC_KEY = "last_fetch_key";

    public DatabaseFetcher(Context applicationContext) {
        this.context = applicationContext;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Gets the last successful sync timestamp.
    // If this is the first time synchronizing it will return an old timestamp.
    public Timestamp getLastSuccessfulSync() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        String lastSync = sharedPreferences.getString(LAST_SYNC_KEY, "2000-01-01 00:00:00");
        if (lastSync.equals("")) {  // A bug that happened once, perhaps this if-construct can be removed
            lastSync = "2000-01-01 00:00:00";
        }
        Timestamp lastSyncTimestamp = Timestamp.valueOf(lastSync);
        return lastSyncTimestamp;
    }

    // Fetches a String from the server with raw json object
    // Returns null if not successful
    private String fetchStringFromServer() {
        String result = null;
        try {
            InputStream inputStream;
            HttpClient httpclient = new DefaultHttpClient();
            Uri.Builder builder = Uri.parse(url).buildUpon();
            if (fetchTimestamp == null) {
                //throw new RuntimeException("You need to specify a fetch timestamp!");
                fetchTimestamp = Timestamp.valueOf("1900-01-01 00:00:00");
            }
            builder.appendQueryParameter("after_timestamp", fetchTimestamp.toString());
            String url = builder.build().toString();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "client protocol", e);
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
        }
        return result;
    }

    public JSONObject fetchFromServer() {
        String jsonString = fetchStringFromServer();
        if (jsonString != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject != null) {
                    boolean serverSuccess = jsonObject.optBoolean("success", false);
                    if (!serverSuccess) {
                        Log.e(TAG, "no success variable");
                        return null;
                    }
                } else {
                    Log.e(TAG, "could not parse json string to object");
                    return null;
                }
                return jsonObject;
            } catch (JSONException e) {
                Log.e(TAG, "could not parse json", e);
                return null;
            }
        } else {
            return null;
        }
    }

    // Takes an inputstream and converts it into a String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
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

    // Persists the last successful synchronization timestamp
    public void setLastSuccessfulSync(Timestamp wasSynced) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (wasSynced != null) {
            editor.putString(LAST_SYNC_KEY, wasSynced.toString());
        } else {
            editor.remove(LAST_SYNC_KEY);
        }
        editor.commit();
    }



    // will make fetching take everything from beginning om time
    public void setFetchAll() {
        this.fetchTimestamp = Timestamp.valueOf("1900-01-01 00:00:00");
    }
}
