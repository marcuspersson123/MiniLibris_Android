package me.webbdev.minilibris.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentUris;

import java.sql.Timestamp;

import android.database.Cursor;

import me.webbdev.minilibris.database.BooksTable;
import me.webbdev.minilibris.database.MiniLibrisContract;


public class SyncDatabaseIntentService extends IntentService {

    private static final String TAG = "SyncDatabaseIntentService";
    private String url = "http://minilibris.webbdev.me/minilibris/api/books";

    public SyncDatabaseIntentService() {
        super("Sync database service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ServerDbChanges serverDbChanges = new ServerDbChanges(this.getApplicationContext());
        serverDbChanges.setUrl(url);
        serverDbChanges.setFetchFromTime(serverDbChanges.getWasSynced());
        JSONObject jsonObject = serverDbChanges.getChanges();
        if (jsonObject != null) {
            try {
                boolean success = false;
                Timestamp lastServerSync = serverDbChanges.getWasSynced();
                JSONArray books = jsonObject.getJSONArray("books");
                for (int i = 0; i < books.length(); i++) {
                    JSONObject book = books.getJSONObject(i);
                    int book_id = book.getInt("book_id");
                    Timestamp serverRowTimestamp = Timestamp.valueOf(book.getString("changed"));
                    if (serverRowTimestamp.after(lastServerSync)) {
                        lastServerSync = serverRowTimestamp;
                    }
                    Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, book_id);
                    Cursor cursor = this.getContentResolver().query(singleUri, MiniLibrisContract.Books.ALL_FIELDS, null, null, null);


                    if (null == cursor) {
                    } else if (cursor.getCount() < 1) {
                        success = insertBook(book);
                    } else {
                        success = updateBook(book);
                    }
//                    Date changed = Date.parse(book.getString("changed"));
//                    if (changed>Date.parse(lastChange)) {
//lastChange = changed.toString();
//                    }
                    if (!success) {
                        break;
                    }

                }
                // JSONArray categories = jsonObject.getJSONArray("categories");
                if (books != null) {
                    // wipeLocalBooks();
                    // BooksTable.
                    //insertBooks(books);

                }
                if (success) {
                serverDbChanges.setWasSynced(lastServerSync);
                }

            } catch (JSONException e) {
                Log.e(TAG, "could not get array from json", e);
            }

        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    private void wipeLocalBooks() {
        this.getContentResolver().delete(MiniLibrisContract.Books.CONTENT_URI, null, null);
    }

    private boolean updateBook(JSONObject jsonObject) {

        return false;
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

            Uri todoUri = this.getContentResolver().insert(MiniLibrisContract.Books.CONTENT_URI, contentValues);

        } catch (JSONException e) {
            Log.e(TAG, "insertbook", e);
            success = false;
        }

        return success;
    }

}
