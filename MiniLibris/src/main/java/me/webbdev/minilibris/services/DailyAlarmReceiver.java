package me.webbdev.minilibris.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DailyAlarmReceiver extends BroadcastReceiver {

        private static final String TAG = "DailyAlarmReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent newIntent = new Intent(context, DailyAlarmIntentService.class);
            context.startService(newIntent);
        }

}
