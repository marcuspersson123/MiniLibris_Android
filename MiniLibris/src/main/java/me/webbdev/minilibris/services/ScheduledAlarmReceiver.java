package me.webbdev.minilibris.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduledAlarmReceiver extends BroadcastReceiver {

        private static final String TAG = "ScheduledAlarmReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent newIntent = new Intent(context, AlarmIntentService.class);
            context.startService(newIntent);
        }

}
