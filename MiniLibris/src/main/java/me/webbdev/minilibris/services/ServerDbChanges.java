package me.webbdev.minilibris.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Created by marcusssd on 2014-04-04.
 */
public class ServerDbChanges {

    private String url;

    private Timestamp fetchTimestamp;
    private Context context;
    private static String TAG = "ServerDbChanges";
    private static String SHARED_PREFERENCES_NAME = "ServerDbChanges";
    private static String LAST_SYNC_KEY = "last_fetch_key";

    public ServerDbChanges(Context applicationContext) {
        this.context = applicationContext;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Timestamp getWasSynced() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        String lastSync = sharedPreferences.getString(LAST_SYNC_KEY, "2000-01-01 00:00:00");
        if (lastSync=="") {
            lastSync = "2000-01-01 00:00:00";
        }
        Timestamp lastSyncTimestamp = Timestamp.valueOf(lastSync);

        return lastSyncTimestamp;
    }

    public void setFetchFromTime(Timestamp fetchTimestamp) {
        this.fetchTimestamp = fetchTimestamp;
    }

    public JSONObject getChanges() {
        String jsonString = fetch();
        if (jsonString != null) {
            return getJsonObject(jsonString);
        }
        return null;
    }

    private String fetch() {
        String result = null;
        try {
            InputStream inputStream = null;
            HttpClient httpclient = new DefaultHttpClient();
            Uri.Builder builder = Uri.parse(url).buildUpon();
            builder.appendQueryParameter("after_timestamp", fetchTimestamp.toString());
            String url  = builder.build().toString();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
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

    public void setWasSynced(Timestamp wasSynced) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_SYNC_KEY, wasSynced.toString());
        editor.commit();
    }
}
