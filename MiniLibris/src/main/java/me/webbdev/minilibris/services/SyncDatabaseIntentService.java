package me.webbdev.minilibris.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentUris;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import android.database.Cursor;

import me.webbdev.minilibris.database.BooksTable;
import me.webbdev.minilibris.database.MiniLibrisContract;


public class SyncDatabaseIntentService extends IntentService {

    private static final String TAG = "SyncDatabaseIntentService";
    private String url = "http://minilibris.webbdev.me/minilibris/api/books";

    public SyncDatabaseIntentService() {
        super("Sync database service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseSyncer databaseSyncer = new DatabaseSyncer(this.getApplicationContext());
        databaseSyncer.setUrl(url);
        Timestamp lastSync = databaseSyncer.getWasSynced();
        Timestamp aDayAgo = new Timestamp(System.currentTimeMillis()-24*60*60*1000);

        if (lastSync.after(aDayAgo)) {
            databaseSyncer.setFetchFromTime(databaseSyncer.getWasSynced());
        }
        databaseSyncer.sync();


        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


}
