package me.webbdev.minilibris.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.sql.Timestamp;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.webbdev.minilibris.ui.MainActivity;


public class SyncDatabaseIntentService extends IntentService {

    private static final String TAG = "SyncDatabaseIntentService";
    public static final int NOTIFICATION_ID = 1;
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/databaseChanges";
    public static final String START_SYNC = "start_sync";

    public SyncDatabaseIntentService() {
        super("Sync database service");
    }

    // Synchronizes the local database.
    // Does its job if it was a valid cloud message, or a local syncAllTables request
    // Tries to synchronize only the latest changes.
    // Displays a notification and removes it
    @Override
    protected void onHandleIntent(Intent intent) {
        if (isRegularCloudMessage(intent) || isStartSyncEvent(intent)) {
            sendNotification("Minilibris syncAllTables");
            syncAllTables();
            this.removeNotification();
        }

        CloudBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Removes the notification that was set at the beginning
    private void removeNotification() {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private boolean isStartSyncEvent(Intent intent) {
        return intent.hasExtra(START_SYNC);
    }

    private boolean isRegularCloudMessage(Intent intent) {
        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(this);  // "this" is the context
        String messageType = googleCloudMessaging.getMessageType(intent);

        if (GoogleCloudMessaging.
                MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
        } else if (GoogleCloudMessaging.
                MESSAGE_TYPE_DELETED.equals(messageType)) {

        } else if (GoogleCloudMessaging.
                MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            // A regular GCM message was received
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String msg) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_popup_sync)
                        .setContentTitle("MiniLibris")
                        .setStyle(new Notification.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    // Synchronises rows from all tables
    // Returns true if all tables where synchronized
    public boolean syncAllTables() {
        DatabaseFetcher databaseSyncer = new DatabaseFetcher(this.getApplicationContext());
        databaseSyncer.setUrl(url);

        // The databaseFetcher fetches from the last successful update, unless telling it otherwise
        Timestamp lastSuccessfulSync = databaseSyncer.getLastSuccessfulSync();
        Timestamp aDayAgo = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000);

        // Very old data, books can have been old et al. So, fetch the entire database again
        if (lastSuccessfulSync.before(aDayAgo)) {
            databaseSyncer.setFetchAll();
        }

        JSONObject jsonObject = databaseSyncer.fetchFromServer();
        if (jsonObject != null) {

                JSONArray books = jsonObject.optJSONArray("books");
                JSONArray reservations = jsonObject.optJSONArray("reservations");

                if (books == null || reservations == null) {
                    Log.e(TAG, "The server responded without all tables");
                    return false;
                }

                Timestamp lastServerSync = databaseSyncer.getLastSuccessfulSync();
                BooksSynchronizer booksSynchronizer = new BooksSynchronizer(this);
                Timestamp booksServerSync = booksSynchronizer.syncBooks(books);
                if (booksServerSync == null) {
                    Log.e(TAG, "could not sync books table");
                    return false;
                }
                if (booksServerSync.after(lastServerSync)) {
                    lastServerSync = booksServerSync;
                }
                ReservationsSynchronizer reservationsSynchronizer = new ReservationsSynchronizer(this);
                Timestamp reservationsServerSync = reservationsSynchronizer.syncReservations(reservations);
                if (reservationsServerSync == null) {
                    Log.e(TAG, "could not sync reservations table");
                    return false;
                }
                if (reservationsServerSync.after(lastServerSync)) {
                    lastServerSync = reservationsServerSync;
                }
                databaseSyncer.setLastSuccessfulSync(lastServerSync);

                return true;



        }
        return false;
    }

}
