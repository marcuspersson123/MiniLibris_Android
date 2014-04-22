package me.webbdev.minilibris.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Timestamp;

import me.webbdev.minilibris.services.DatabaseFetcher;

public class MiniLibrisDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "minilibris.db";
    private static final int DATABASE_VERSION = 2;
    private Context context;

    public MiniLibrisDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // creates the tables for this database
    @Override
    public void onCreate(SQLiteDatabase database) {
        DatabaseFetcher databaseFetcher = new DatabaseFetcher(this.context);
        databaseFetcher.setLastSuccessfulSync(null);

        BooksTable.create(database);
        ReservationsTable.create(database);
    }

    // upgrades tables depending on version numbers
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        BooksTable.upgrade(database, oldVersion, newVersion);
        ReservationsTable.upgrade(database, oldVersion, newVersion);
    }


}

