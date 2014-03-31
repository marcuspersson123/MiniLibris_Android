package me.webbdev.minilibris;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
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


public class SyncDatabaseIntentService extends IntentService {

    private static final String TAG = "SyncDatabaseIntentService";
    private String url = "http://minilibris.webbdev.me/minilibris/api/books";

    public SyncDatabaseIntentService() {
        super("Sync database service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String jsonString = getJsonString();
        if (jsonString != null) {
            JSONArray books = getBooksFromJson(jsonString);
            if (books != null) {
                wipeLocalBooks();
                insertBooks(books);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private JSONArray getBooksFromJson(String jsonString) {

        try {
            JSONObject jsonobject = new JSONObject(jsonString);
            boolean serverSuccess = jsonobject.getBoolean("success");
            if (serverSuccess) {
                return jsonobject.getJSONArray("books");
            }
        } catch (JSONException e) {
            Log.e(TAG, "getbooksfromjson", e);
        }
        return null;
    }

    private void wipeLocalBooks() {
        this.getContentResolver().delete(MiniLibrisContract.Books.CONTENT_URI, null, null);
    }

    private boolean insertBooks(JSONArray books) {

        boolean success = true;
        try {
            for (int i = 0; i < books.length(); i++) {
                JSONObject jb = (JSONObject) books.get(i);
                //Book book = new Book();
                int book_id = jb.getInt(MiniLibrisContract.Books.BOOK_ID);
                String title = jb.getString(MiniLibrisContract.Books.TITLE);
                String author = jb.getString(MiniLibrisContract.Books.AUTHOR);
                String publisher = jb.getString(MiniLibrisContract.Books.PUBLISHER);
                int year = jb.getInt(MiniLibrisContract.Books.YEAR);
                int category_id = jb.getInt(MiniLibrisContract.Books.CATEGORY_ID);

                ContentValues values = new ContentValues();
                values.put(MiniLibrisContract.Books.BOOK_ID, book_id);
                values.put(MiniLibrisContract.Books.TITLE, title);
                values.put(MiniLibrisContract.Books.AUTHOR, author);
                values.put(MiniLibrisContract.Books.PUBLISHER, publisher);
                values.put(MiniLibrisContract.Books.YEAR, year);
                values.put(MiniLibrisContract.Books.CATEGORY_ID, category_id);
                Uri todoUri = this.getContentResolver().insert(MiniLibrisContract.Books.CONTENT_URI, values);
            }
 //           String[] projection = MiniLibrisContract.Books.ALL_FIELDS;
 //           Cursor cursor = this.getContentResolver().query(MiniLibrisContract.Books.CONTENT_URI, projection, null, null, null);

        } catch (JSONException e) {
            Log.e(TAG, "insertbooks",e);
            success = false;
        }

        return success;
    }

    private String getJsonString() {
        String result = null;
        try {
            InputStream inputStream = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, "getjsonstring",e);
        } catch (IOException e) {
            Log.e(TAG, "getjsonstring", e);
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
        if (result=="") {
            return null;
        } else {
            return result;
        }
    }
}
