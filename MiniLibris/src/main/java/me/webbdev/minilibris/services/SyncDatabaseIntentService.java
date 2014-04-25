package me.webbdev.minilibris.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.sql.Timestamp;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import me.webbdev.minilibris.ui.MainActivity;


public class SyncDatabaseIntentService extends IntentService {

    private static final String TAG = "SyncDatabaseIntentService";
    public static final int SYNCHRONIZING_NOTIFICATION_ID = 1;
    public static final int SERVER_FAIL_NOTIFICATION_ID = 2;
    public static final int INTERNET_FAIL_NOTIFICATION_ID = 3;
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/databaseChanges";
    public static final String START_SYNC = "start_sync";

    public SyncDatabaseIntentService() {
        super("Sync database service");
    }

    // Synchronizes the local database.
    // Does its job if it was a valid cloud message, or if the intent has the extra "START_SYNC".
    // Tries to synchronize only the latest changes.
    // Displays a notification and removes it
    @Override
    protected void onHandleIntent(Intent intent) {
        if (isRegularCloudMessage(intent) || isStartSyncEvent(intent)) {
            // Try to determine internet connectivity. It is not possible to do that reliably. An IOException will probably be the result.
            if (isNetworkConnected()) {
                this.removeNotification(INTERNET_FAIL_NOTIFICATION_ID);
                this.showNotification(SYNCHRONIZING_NOTIFICATION_ID, "Synchronizing database");
                boolean success = false;
                try {
                    success = syncAllTables();
                    if (success) {
                        this.removeNotification(SERVER_FAIL_NOTIFICATION_ID);
                    } else {
                        this.showNotification(SERVER_FAIL_NOTIFICATION_ID, "Failed to synchronize");
                    }
                } catch (IOException e) {
                    this.showNotification(INTERNET_FAIL_NOTIFICATION_ID, "No connection to MiniLibris server");
                }

                this.removeNotification(SYNCHRONIZING_NOTIFICATION_ID);
            } else {
                this.showNotification(INTERNET_FAIL_NOTIFICATION_ID, "Not connected to the Internet");
            }
        }

        CloudBroadcastReceiver.completeWakefulIntent(intent);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = cm.getAllNetworkInfo();
        if (info == null) {
            // There are no active networks.
            return false;
        } else if (info != null)
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        return false;
    }

    // Removes the notification that was set at the beginning
    private void removeNotification(int notificationId) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
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
    private void showNotification(int notificationId, String msg) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setContentTitle("MiniLibris")
                        .setStyle(new Notification.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        switch (notificationId) {
            case SYNCHRONIZING_NOTIFICATION_ID:
                mBuilder.setSmallIcon(android.R.drawable.ic_popup_sync);
                break;
            default:
                mBuilder.setSmallIcon(android.R.drawable.stat_sys_warning);
                break;
        }
        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    // Synchronises rows from all tables
    // Returns true if all tables where synchronized
    public boolean syncAllTables() throws IOException {
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
