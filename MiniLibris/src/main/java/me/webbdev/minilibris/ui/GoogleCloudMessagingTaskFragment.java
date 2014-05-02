package me.webbdev.minilibris.ui;

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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.webbdev.minilibris.R;


// Sends the Google Cloud Messaging registration id that is connected to this device to server
public class GoogleCloudMessagingTaskFragment extends TaskFragment {

    private static final String TAG = GoogleCloudMessagingTaskFragment.class.getSimpleName();
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/authentication?mobile=1";
    private String registrationId;
    private int userId;
    String result = null;

    public GoogleCloudMessagingTaskFragment() {
        super();
    }

    // An invisible fragment, but necessary to return an empty LinearLayout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.empty,
                container, false);
    }


    // POST a json with data for registration id and user id.
    // If there is an array of errors, the first message will be in "result"
    public void doAsyncWork() {


        try {
            JSONObject json = new JSONObject();

            json.put("registration_id", this.registrationId);
            json.put("user_id", this.userId);

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
                        if (jsonobject != null) {
                            JSONArray errors = jsonobject.optJSONArray("errors");
                            if (errors != null && errors.length()>0) {
                                JSONObject error = errors.optJSONObject(0);
                                String summary = error.getString("summary");
                                result = summary;
                            }

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
        }        this.result = result;
    }



    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    }
}
