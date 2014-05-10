package me.webbdev.minilibris.services;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.android.gms.gcm.GoogleCloudMessaging;

// Receives an intent when the server sends a message through Google cloud
// If the message from google is of message type,
// starts the SyncDatabaseIntentService as a wakeful service.
public class CloudBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNormalMessage(context, intent)) {
            SyncDatabaseIntentService.startWakeful(this, context, false);
            DailyAlarmIntentService.start(context);

        }
        setResultCode(Activity.RESULT_OK);
    }

    private boolean isNormalMessage(Context context, Intent intent) {
        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(context);  // "this" is the context
        String messageType = googleCloudMessaging.getMessageType(intent);

        if (GoogleCloudMessaging.
                MESSAGE_TYPE_SEND_ERROR.equals(messageType))

        {
        } else if (GoogleCloudMessaging.
                MESSAGE_TYPE_DELETED.equals(messageType))

        {

        } else if (GoogleCloudMessaging.
                MESSAGE_TYPE_MESSAGE.equals(messageType))

        {
            // A regular GCM message was received
            return true;
        }

        return false;
    }
}
