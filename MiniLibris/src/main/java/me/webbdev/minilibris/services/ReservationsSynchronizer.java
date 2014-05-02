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
                boolean old = (reservation.getInt("old") > 0);
                Timestamp serverRowTimestamp = Timestamp.valueOf(reservation.getString("changed"));
                if (serverRowTimestamp.after(lastServerSync)) {
                    lastServerSync = serverRowTimestamp;
                }
                Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Reservations.CONTENT_URI, reservationId);
                Cursor cursor = this.context.getContentResolver().query(singleUri, MiniLibrisContract.Reservations.ALL_FIELDS, null, null, null);
                if (null == cursor) {
                    success = false;
                } else if (cursor.getCount() < 1) {
                    if (!old) {
                        success = insertReservation(reservation);
                    }
                } else {
                    if (!old) {
                        success = updateReservation(reservationId, reservation);
                    } else {
                        deleteReservation(reservationId);
                    }
                }
                if (cursor != null) {
                    cursor.close();
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
            Log.e(TAG, "Json error in syncReservations", e);
            return null;
        }

    }

    // Deletes a reservation locally
    private boolean deleteReservation(int reservationId) {
        Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Reservations.CONTENT_URI, reservationId);
        int deleteCount = this.context.getContentResolver().delete(singleUri, null, null);
        return deleteCount >= 1;
    }

    // Updates a reservation locally
    private boolean updateReservation(int reservationId, JSONObject jsonObject) {
        ContentValues contentValues = this.getContentValues(jsonObject);
        Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Reservations.CONTENT_URI, reservationId);
        int updatedCount = this.context.getContentResolver().update(singleUri, contentValues, null, null);
        return updatedCount>0;
    }

    // Inserts a reservation locally
    private boolean insertReservation(JSONObject jsonObject) {

        ContentValues contentValues = this.getContentValues(jsonObject);
        if (contentValues!=null) {
            Uri todoUri = this.context.getContentResolver().insert(MiniLibrisContract.Reservations.CONTENT_URI, contentValues);
            boolean success = todoUri.getPathSegments().size() > 0;
            return success;
        }
        return false;
    }

    // Copies a reservation in JSON format to ContentValues format.
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
