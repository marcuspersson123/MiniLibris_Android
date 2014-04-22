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


class DatabaseSynchronizer {

    private String url;

    private Timestamp fetchTimestamp;
    private Context context;
    private static String TAG = "ServerDbChanges";
    private static String SHARED_PREFERENCES_NAME = "ServerDbChanges";
    private static String LAST_SYNC_KEY = "last_fetch_key";

    public DatabaseSynchronizer(Context applicationContext) {
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
                throw new RuntimeException("You need to specify a fetch timestamp!");
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
    private void setLastSuccessfulSync(Timestamp wasSynced) {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_SYNC_KEY, wasSynced.toString());
        editor.commit();
    }

    // Synchronises rows from all tables
    // Returns true if all tables where synchronized
    public boolean syncAllTables() {
        String jsonString = fetchStringFromServer();
        if (jsonString != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject != null) {
                    boolean serverSuccess = jsonObject.optBoolean("success", false);
                    if (!serverSuccess) {
                        Log.e(TAG, "no success variable");
                        return false;
                    }
                } else {
                    Log.e(TAG, "could not parse json string to object");
                    return false;
                }

                Timestamp lastServerSync = getLastSuccessfulSync();
                JSONArray books = jsonObject.optJSONArray("books");
                if (books != null) {
                    Timestamp booksServerSync = syncBooks(books);
                    if (booksServerSync == null) {
                        Log.e(TAG, "could not sync books table");
                        return false;
                    }
                    if (booksServerSync.after(lastServerSync)) {
                        lastServerSync = booksServerSync;
                    }
                } else {
                    Log.e(TAG, "no books returned from the api");
                    return false;
                }
                JSONArray reservations = jsonObject.optJSONArray("reservations");
                if (reservations != null) {
                    Timestamp reservationsServerSync = syncReservations(reservations);
                    if (reservationsServerSync == null) {
                        Log.e(TAG, "could not sync reservations table");
                        return false;
                    }
                    if (reservationsServerSync.after(lastServerSync)) {
                        lastServerSync = reservationsServerSync;
                    }
                    setLastSuccessfulSync(lastServerSync);
                } else {
                    Log.e(TAG, "no reservations returned from the api");
                    return false;
                }

                return true;


            } catch (JSONException e) {
                Log.e(TAG, "could not parse json", e);
            }
        }
        return false;
    }

    // Synchronizes the table book.
    // returns the latest timestamp from the server if successful, or null
    // If the no changes were made, returns true
    private Timestamp syncBooks(JSONArray booksArray) {

        try {
            boolean success = true;  // success also if nothing to do
            Timestamp lastServerSync = getLastSuccessfulSync();
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


    // Synchronizes the table reservations.
    // returns the latest timestamp from the server if successful, or null
    // If the no changes were made, returns true
    // Also obsolete reservations gets deleted automatically
    private Timestamp syncReservations(JSONArray reservationsArray) {

        try {
            boolean success = true;  // success also if nothing to do
            Timestamp lastServerSync = getLastSuccessfulSync();
            for (int i = 0; i < reservationsArray.length(); i++) {
                JSONObject reservation = reservationsArray.getJSONObject(i);
                int reservationId = reservation.getInt("reservation_id");
                Timestamp serverRowTimestamp = Timestamp.valueOf(reservation.getString("changed"));
                if (serverRowTimestamp.after(lastServerSync)) {
                    lastServerSync = serverRowTimestamp;
                }
                Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Reservations.CONTENT_URI, reservationId);
                Cursor cursor = this.context.getContentResolver().query(singleUri, MiniLibrisContract.Reservations.ALL_FIELDS, null, null, null);
                if (null == cursor) {
                    success = false;
                } else if (cursor.getCount() < 1) {
                    success = insertReservation(reservation);
                } else {
                    success = updateReservation(reservation);
                }
                if (!success) {
                    break;
                }

            }
            if (success) {
                Uri allUri = MiniLibrisContract.Reservations.CONTENT_URI;
                Cursor cursor = this.context.getContentResolver().query(allUri, MiniLibrisContract.Reservations.ALL_FIELDS, null, null, null);
                while (cursor.moveToNext()) {
                    String ends = cursor.getString(cursor.getColumnIndex(MiniLibrisContract.Reservations.ENDS));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date endsDate = simpleDateFormat.parse(ends);
                        Date today = Calendar.getInstance().getTime();
                        if (endsDate.before(today)) {
                            boolean is_lent = cursor.getInt(cursor.getColumnIndex(MiniLibrisContract.Reservations.IS_LENT)) > 0;
                            if (!is_lent) {
                                int reservationId = cursor.getInt(cursor.getColumnIndex(MiniLibrisContract.Reservations._ID));
                                deleteReservation(reservationId);
                            }
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "could not parse date in syncReservations", e);
                    }


                }
                return lastServerSync;
            } else {
                return null;
            }

        } catch (JSONException e) {
            Log.e(TAG, "Json error in syncReservations", e);
            return null;
        }

    }

    private void deleteReservation(int reservationId) {
    }

    private boolean updateReservation(JSONObject reservation) {
        return true;
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
    private boolean insertReservation(JSONObject jsonObject) {

        boolean success;
        try {

            int reservationId = jsonObject.getInt("reservation_id");  // OBS! reservation_id
            int bookId = jsonObject.getInt(MiniLibrisContract.Reservations.BOOK_ID);
            int userId = jsonObject.getInt(MiniLibrisContract.Reservations.USER_ID);
            String begins = jsonObject.getString(MiniLibrisContract.Reservations.BEGINS);
            String ends = jsonObject.getString((MiniLibrisContract.Reservations.ENDS));
            int isLent = jsonObject.getInt(MiniLibrisContract.Reservations.IS_LENT);

            ContentValues contentValues = new ContentValues();
            contentValues.put(MiniLibrisContract.Reservations._ID, reservationId);
            contentValues.put(MiniLibrisContract.Reservations.BOOK_ID, bookId);
            contentValues.put(MiniLibrisContract.Reservations.USER_ID, userId);
            contentValues.put(MiniLibrisContract.Reservations.BEGINS, begins);
            contentValues.put(MiniLibrisContract.Reservations.ENDS, ends);
            contentValues.put(MiniLibrisContract.Reservations.IS_LENT, isLent);

            Uri todoUri = this.context.getContentResolver().insert(MiniLibrisContract.Reservations.CONTENT_URI, contentValues);
            success = true;

        } catch (JSONException e) {
            Log.e(TAG, "insert book", e);
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

    // will make fetching take everything from beginning om time
    public void setFetchAll() {
        this.fetchTimestamp = Timestamp.valueOf("1900-01-01 00:00:00");
    }
}
