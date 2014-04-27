package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.webbdev.minilibris.R;

public class LoginTaskFragment extends TaskFragment {

    private static final String TAG = "LoginReservationTaskFragment";
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/authentication";
    private String result;
    private String password;
private String username;
    //private int userId;

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    //public int getUserId() {
      //  return userId;
    //}

    public String getResult() {
        return result;
    }

    public LoginTaskFragment() {
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

    // Creates a JSON object that is POST:ed to the server.
    // If an error occurred the "result" variable holds the message.
    // If there is an array of errors, the first message will be in "result"
    public void doAsyncWork() {
        String result = null;

        try {



            InputStream inputStream;
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url+"?mobile=1&username="+this.username+"&password="+password);
            //httpGet.setEntity(entity);

            HttpResponse httpResponse = httpclient.execute(httpGet);
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) {
                String jsonString = convertInputStreamToString(inputStream);
                if (jsonString != null) {
                    try {
                        JSONObject jsonobject = new JSONObject(jsonString);
                        if (jsonobject != null) {
                            if (jsonobject.optBoolean("success",false)) {


                                    SharedPreferences.Editor sharedPreferencesEditor = getActivity().getSharedPreferences("user_info", Activity.MODE_PRIVATE).edit();
                                sharedPreferencesEditor.putInt("user_id", jsonobject.optInt("user_id",-1));
                                sharedPreferencesEditor.putString("username", jsonobject.optString("username",""));
                                sharedPreferencesEditor.putString("first_name", jsonobject.optString("first_name",""));
                                sharedPreferencesEditor.putString("surname", jsonobject.optString("surname",""));
                                sharedPreferencesEditor.putString("address", jsonobject.optString("address",""));
                                sharedPreferencesEditor.putString("phone", jsonobject.optString("phone",""));
                                sharedPreferencesEditor.commit();
                            } else {
                                JSONObject validationContainer = jsonobject.getJSONObject("validationContainer");
                                JSONArray errors = validationContainer.optJSONArray("errors");
                                if (errors != null && errors.length() > 0) {
                                    JSONObject error = errors.optJSONObject(0);
                                    String summary = error.getString("summary");
                                    result = summary;
                                } else {
                                    result = "Ett fel uppstod";
                                }
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
        }

        this.result = result;
    }


}
