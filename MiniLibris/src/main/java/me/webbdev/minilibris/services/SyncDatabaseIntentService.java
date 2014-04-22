package me.webbdev.minilibris.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.sql.Timestamp;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import me.webbdev.minilibris.ui.MainActivity;


public class SyncDatabaseIntentService extends IntentService {

    private static final String TAG = "SyncDatabaseIntentService";
    public static final int NOTIFICATION_ID = 1;
    private static final String url = "http://minilibris.webbdev.me/minilibris/api/books";
    private static final String START_SYNC = "start_sync";

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
            DatabaseSynchronizer databaseSyncer = new DatabaseSynchronizer(this.getApplicationContext());
            databaseSyncer.setUrl(url);
            Timestamp lastSuccessfulSync = databaseSyncer.getLastSuccessfulSync();
            Timestamp aDayAgo = new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000);

            if (lastSuccessfulSync.before(aDayAgo)) {
                databaseSyncer.setFetchAll();
            }
            databaseSyncer.syncAllTables();
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
}
