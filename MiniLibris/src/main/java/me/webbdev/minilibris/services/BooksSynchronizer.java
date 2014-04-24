package me.webbdev.minilibris.services;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

import me.webbdev.minilibris.database.MiniLibrisContract;

/**
 * Created by marcusssd on 2014-04-22.
 */
public class BooksSynchronizer {

    Context context;

    private static String TAG = "BooksSynchronizer";

    public BooksSynchronizer(Context applicationContext) {
        this.context = applicationContext;
    }

    // Synchronizes the table book.
    // returns the latest timestamp from the server if successful, or null
    // Also, if the no changes were made, returns the time stamp of the last server sync.
    Timestamp syncBooks(JSONArray booksArray) {

        try {
            boolean success = true;  // success also if nothing to do
            Timestamp lastServerSync = Timestamp.valueOf("1900-01-01 00:00:00");
            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject book = booksArray.getJSONObject(i);
                int book_id = book.getInt("book_id");
                Timestamp serverRowTimestamp = Timestamp.valueOf(book.getString("changed"));
                if (serverRowTimestamp.after(lastServerSync)) {
                    lastServerSync = serverRowTimestamp;
                }
                boolean old = (book.getInt("old") > 0);
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
            Log.e(TAG, "Json error in syncBooks", e);
            return null;
        }

    }


    // Deletes a book
    // Returns true if successfully deleted
    private boolean deleteBook(JSONObject jsonObject) {
        boolean success;
        try {
            int book_id = jsonObject.getInt("book_id");  // OBS! book_id
            Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, book_id);
            int deleteCount = this.context.getContentResolver().delete(singleUri, null, null);
            if (deleteCount < 1) {
                success = false;
            } else {
                success = true;
            }

        } catch (JSONException e) {
            Log.e(TAG, "deletebook", e);
            success = false;
        }

        return success;
    }

    // Updates a book.
    // Returns true if successfully updated, otherwise returns false.
    private boolean updateBook(JSONObject jsonObject) {
        boolean success;
        try {

            int book_id = jsonObject.getInt("book_id");  // OBS! book_id
            String title = jsonObject.getString(MiniLibrisContract.Books.TITLE);
            String author = jsonObject.getString(MiniLibrisContract.Books.AUTHOR);
            String publisher = jsonObject.getString(MiniLibrisContract.Books.PUBLISHER);
            int year = jsonObject.getInt(MiniLibrisContract.Books.YEAR);
            int category_id = jsonObject.getInt(MiniLibrisContract.Books.CATEGORY_ID);
            String changed = jsonObject.getString(MiniLibrisContract.Books.CHANGED);

            ContentValues contentValues = new ContentValues();
            contentValues.put(MiniLibrisContract.Books.TITLE, title);
            contentValues.put(MiniLibrisContract.Books.AUTHOR, author);
            contentValues.put(MiniLibrisContract.Books.PUBLISHER, publisher);
            contentValues.put(MiniLibrisContract.Books.YEAR, year);
            contentValues.put(MiniLibrisContract.Books.CATEGORY_ID, category_id);
            contentValues.put(MiniLibrisContract.Books.CHANGED, changed);

            Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, book_id);
            int updatedCount = this.context.getContentResolver().update(singleUri, contentValues, null, null);
            if (updatedCount < 1) {
                success = false;
            } else {
                success = true;
            }

        } catch (JSONException e) {
            Log.e(TAG, "update book", e);
            success = false;
        }

        return success;
    }


    // inserts a book in the table.
    // returns false if something went wrong
    private boolean insertBook(JSONObject jsonObject) {

        boolean success;
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
            success = true;

        } catch (JSONException e) {
            Log.e(TAG, "insert book", e);
            success = false;
        }

        return success;
    }

}
