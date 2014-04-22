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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.webbdev.minilibris.database.MiniLibrisContract;

/**
 * Created by marcusssd on 2014-04-22.
 */
public class ReservationsSynchronizer {

    private static String TAG = "ReservationsSynchronizer";
    private Context context;

    public ReservationsSynchronizer(Context applicationContext) {
        this.context = applicationContext;
    }


    // Synchronizes the table reservations.
    // returns the latest timestamp from the server if successful, or null
    // If the no changes were made, returns true
    // Also obsolete reservations gets deleted automatically
    Timestamp syncReservations(JSONArray reservationsArray) {

        try {
            boolean success = true;  // success also if nothing to do
            Timestamp lastServerSync = Timestamp.valueOf("1900-01-01 00:00:00");
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
                    success = updateReservation(reservationId, reservation);
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

    private boolean deleteReservation(int reservationId) {
        Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Reservations.CONTENT_URI, reservationId);
        Log.e(TAG, "delete reseveration-uri: " + singleUri);
        int deleteCount = this.context.getContentResolver().delete(singleUri, null, null);
        if (deleteCount < 1) {
            return false;
        } else {
            return true;
        }

    }

    private boolean updateReservation(int reservationId, JSONObject jsonObject) {
        ContentValues contentValues = this.getContentValues(jsonObject);

        Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Reservations.CONTENT_URI, reservationId);
        int updatedCount = this.context.getContentResolver().update(singleUri, contentValues, null, null);
        Log.e(TAG, "update reservation -uri: " + singleUri);

        return updatedCount>0;
    }

    // inserts a book in the table.
    // returns false if something went wrong
    private boolean insertReservation(JSONObject jsonObject) {

        ContentValues contentValues = this.getContentValues(jsonObject);
        if (contentValues!=null) {
            Uri todoUri = this.context.getContentResolver().insert(MiniLibrisContract.Reservations.CONTENT_URI, contentValues);
            Log.e(TAG, "insert reservation-uri: " + todoUri);
            boolean success = todoUri.getPathSegments().size() > 0;
            return success;
        }
        return false;
    }

    private ContentValues getContentValues(JSONObject jsonObject) {
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


            return contentValues;

        } catch (JSONException e) {
            Log.e(TAG, "Could not get content values from book", e);
        }

        return null;

    }

}