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

import me.webbdev.minilibris.database.MiniLibrisContract;


public class DatabaseSyncer {

    private String url;

    private Timestamp fetchTimestamp;
    private Context context;
    private static String TAG = "ServerDbChanges";
    private static String SHARED_PREFERENCES_NAME = "ServerDbChanges";
    private static String LAST_SYNC_KEY = "last_fetch_key";

    public DatabaseSyncer(Context applicationContext) {
        this.context = applicationContext;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Timestamp getWasSynced() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        String lastSync = sharedPreferences.getString(LAST_SYNC_KEY, "2000-01-01 00:00:00");
        if (lastSync == "") {  // fallback again
            lastSync = "2000-01-01 00:00:00";
        }

        Timestamp lastSyncTimestamp = Timestamp.valueOf(lastSync);

        return lastSyncTimestamp;
    }

    public void setFetchFromTime(Timestamp fetchTimestamp) {
        this.fetchTimestamp = fetchTimestamp;
    }


    private String fetchStringFromServer() {
        String result = null;
        try {
            InputStream inputStream = null;
            HttpClient httpclient = new DefaultHttpClient();
            Uri.Builder builder = Uri.parse(url).buildUpon();
            if (fetchTimestamp == null) {
                fetchTimestamp = getWasSynced();
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

    public void sync() {
        String jsonString = fetchStringFromServer();
        if (jsonString != null) {
            JSONObject jsonObject = getJsonObject(jsonString);
            if (jsonObject != null) {
                boolean success = true;
                Timestamp booksServerSync = syncBooks(jsonObject.optJSONArray("books"));
                if (booksServerSync == null) {
                    success = false;
                }
                if (success) {
                    Timestamp lastServerSync = getWasSynced();
                    if (booksServerSync.after(lastServerSync)) {
                        lastServerSync = booksServerSync;
                    }
                    setWasSynced(lastServerSync);
                }
            }
        }
    }

    private Timestamp syncBooks(JSONArray books) {

        try {
            boolean success = true;  // success if nothing to do
            Timestamp lastServerSync = getWasSynced();
            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                int book_id = book.getInt("book_id");
                Timestamp serverRowTimestamp = Timestamp.valueOf(book.getString("changed"));
                if (serverRowTimestamp.after(lastServerSync)) {
                    lastServerSync = serverRowTimestamp;
                }
                boolean old = (book.getInt("old")>0);
                Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, book_id);
                Cursor cursor = this.context.getContentResolver().query(singleUri, MiniLibrisContract.Books.ALL_FIELDS, null, null, null);
                if (null == cursor) {
                    success = false;
                } else if (cursor.getCount() < 1) {
                    if (!old) {
                    success = insertBook(book);
                    }
                } else {
                    if (!old) {
                    success = updateBook(book);
                    } else {
                        success = deleteBook(book);
                    }
                }
                if (!success) {
                    break;
                }

            }
            if (success) {
                return lastServerSync;
            } else {
                return null;
            }

        } catch (JSONException e) {
            Log.e(TAG, "could not get array from json", e);
            return null;
        }

    }

    private void wipeLocalBooks() {
        this.context.getContentResolver().delete(MiniLibrisContract.Books.CONTENT_URI, null, null);
    }

    private boolean deleteBook(JSONObject jsonObject) {
        boolean success = true;
        try {

            int book_id = jsonObject.getInt("book_id");  // OBS! book_id

            Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, book_id);
            int deleteCount = this.context.getContentResolver().delete(singleUri, null,null);
            if (deleteCount<1) {
                success = false;
            }

        } catch (JSONException e) {
            Log.e(TAG, "deletebook", e);
            success = false;
        }

        return success;
    }

    private boolean updateBook(JSONObject jsonObject) {
        boolean success = true;
        try {

            int book_id = jsonObject.getInt("book_id");  // OBS! book_id
            String title = jsonObject.getString(MiniLibrisContract.Books.TITLE);
            String author = jsonObject.getString(MiniLibrisContract.Books.AUTHOR);
            String publisher = jsonObject.getString(MiniLibrisContract.Books.PUBLISHER);
            int year = jsonObject.getInt(MiniLibrisContract.Books.YEAR);
            int category_id = jsonObject.getInt(MiniLibrisContract.Books.CATEGORY_ID);
            String changed = jsonObject.getString(MiniLibrisContract.Books.CHANGED);

            ContentValues contentValues = new ContentValues();
           // contentValues.put(MiniLibrisContract.Books._ID, book_id);
            contentValues.put(MiniLibrisContract.Books.TITLE, title);
            contentValues.put(MiniLibrisContract.Books.AUTHOR, author);
            contentValues.put(MiniLibrisContract.Books.PUBLISHER, publisher);
            contentValues.put(MiniLibrisContract.Books.YEAR, year);
            contentValues.put(MiniLibrisContract.Books.CATEGORY_ID, category_id);
            contentValues.put(MiniLibrisContract.Books.CHANGED, changed);

            Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, book_id);
            int updatedCount = this.context.getContentResolver().update(singleUri, contentValues,null,null);
            if (updatedCount<1) {
                success = false;
            }

        } catch (JSONException e) {
            Log.e(TAG, "insertbook", e);
            success = false;
        }

        return success;
    }

    private boolean insertBook(JSONObject jsonObject) {

        boolean success = true;
        try {

            int book_id = jsonObject.getInt("book_id");  // OBS! book_id
            String title = jsonObject.getString(MiniLibrisContract.Books.TITLE);
            String author = jsonObject.getString(MiniLibrisContract.Books.AUTHOR);
            String publisher = jsonObject.getString(MiniLibrisContract.Books.PUBLISHER);
            int year = jsonObject.getInt(MiniLibrisContract.Books.YEAR);
            int category_id = jsonObject.getInt(MiniLibrisContract.Books.CATEGORY_ID);
            String changed = jsonObject.getString(MiniLibrisContract.Books.CHANGED);

            ContentValues contentValues = new ContentValues();
            contentValues.put(MiniLibrisContract.Books._ID, book_id);
            contentValues.put(MiniLibrisContract.Books.TITLE, title);
            contentValues.put(MiniLibrisContract.Books.AUTHOR, author);
            contentValues.put(MiniLibrisContract.Books.PUBLISHER, publisher);
            contentValues.put(MiniLibrisContract.Books.YEAR, year);
            contentValues.put(MiniLibrisContract.Books.CATEGORY_ID, category_id);
            contentValues.put(MiniLibrisContract.Books.CHANGED, changed);

            Uri todoUri = this.context.getContentResolver().insert(MiniLibrisContract.Books.CONTENT_URI, contentValues);

        } catch (JSONException e) {
            Log.e(TAG, "insertbook", e);
            success = false;
        }

        return success;
    }

}
