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
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.IOException;
import java.sql.Timestamp;

import org.json.JSONArray;
import org.json.JSONObject;

import me.webbdev.minilibris.ui.MainActivity;


public class SyncDatabaseIntentService extends IntentService {

    private static final String TAG = "SyncDatabaseIntentService";
    public static final int SYNCHRONIZING_NOTIFICATION_ID = 1;
    public static final int SERVER_FAIL_NOTIFICATION_ID = 2;
    public static final int INTERNET_FAIL_NOTIFICATION_ID = 3;
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/databaseChanges";
    public static final String SYNCHRONIZE_FROM_BEGINNING_OF_TIME_KEY = "SYNCHRONIZE_FROM_BEGINNING_OF_TIME_KEY";

    public SyncDatabaseIntentService() {
        super("Sync database service");
    }

    // Synchronizes the local database.
    // Tries to synchronize only the latest changes, unless told to synchronize from beginning of time.
    // Displays a notification and removes it
    @Override
    protected void onHandleIntent(Intent intent) {
        boolean synchronizeAll = intent.getBooleanExtra(SYNCHRONIZE_FROM_BEGINNING_OF_TIME_KEY, true);

        // Try to determine internet connectivity. It is not possible to do that reliably. An IOException will probably be the result.
        if (isNetworkConnected()) {
            this.removeNotification(INTERNET_FAIL_NOTIFICATION_ID);
            this.showNotification(SYNCHRONIZING_NOTIFICATION_ID, "Synchronizing database");
            boolean success = false;
            try {
                success = syncAllTables(synchronizeAll);
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

        // Update important notifications, such as if books are available to fetch at MiniLibris
        DailyAlarmIntentService.start(this);
        // If this service was called to be wakeful, notify the the WakefulBroadCastReceiver
        CloudBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Try to determine internet connectivity. It is not possible to do that reliably.
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] allNetworkInfo = connectivityManager.getAllNetworkInfo();
        if (allNetworkInfo == null) {
            // There are no active networks.
            return false;
        } else if (allNetworkInfo != null)
            for (int i = 0; i < allNetworkInfo.length; i++) {
                if (allNetworkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        return false;
    }

    // Removes any old notification that was set earlier
    private void removeNotification(int notificationId) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification(int notificationId, String msg) {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN) {

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
    }

    // Synchronises rows from all tables
    // Returns true if all tables where synchronized
    public boolean syncAllTables(boolean synchronizeFromBeginningOfTime) throws IOException {
        DatabaseFetcher databaseSyncer = new DatabaseFetcher(this.getApplicationContext());
        databaseSyncer.setUrl(url);

        if (!synchronizeFromBeginningOfTime) {
            // The databaseFetcher fetches from the last successful update, unless telling it otherwise
            Timestamp lastSuccessfulSync = databaseSyncer.getLastSuccessfulSync();
            Timestamp aDayAgo = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000);

            // Very old data, books can have been old et al. So, fetch the entire database again
            if (lastSuccessfulSync.before(aDayAgo)) {
                databaseSyncer.setFetchAll();
            }
        } else {
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

    public static void start(Context context, boolean synchronizeFromBeginningOfTime) {
        Intent intent = new Intent(context, SyncDatabaseIntentService.class);
        intent.putExtra(SYNCHRONIZE_FROM_BEGINNING_OF_TIME_KEY, synchronizeFromBeginningOfTime);
        context.startService(intent);
    }

    public static void startWakeful(WakefulBroadcastReceiver wakefulBroadcastReceiver, Context context, boolean synchronizeFromBeginningOfTime) {
        Intent intent = new Intent(context, SyncDatabaseIntentService.class);
        intent.putExtra(SYNCHRONIZE_FROM_BEGINNING_OF_TIME_KEY, synchronizeFromBeginningOfTime);
        wakefulBroadcastReceiver.startWakefulService(context, intent);
    }
}
