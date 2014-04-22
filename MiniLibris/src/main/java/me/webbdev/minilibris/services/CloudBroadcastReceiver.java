package me.webbdev.minilibris.services;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

// Receives an intent when the server sends a message through Google cloud
// Starts the SyncDatabaseIntentService as a wakeful service.
public class CloudBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName syncDatabaseComponentName = new ComponentName(context.getPackageName(),
                SyncDatabaseIntentService.class.getName());
        intent.setComponent(syncDatabaseComponentName);
        startWakefulService(context, intent);
        setResultCode(Activity.RESULT_OK);
    }
}
