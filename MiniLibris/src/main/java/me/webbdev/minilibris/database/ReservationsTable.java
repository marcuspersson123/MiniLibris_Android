package me.webbdev.minilibris.database;
import android.database.sqlite.SQLiteDatabase;

// Executes SQL necessary to create and update the reservations table
public class ReservationsTable {

    // Create table syntax for the reservations table.
    private static final String DATABASE_CREATE = "create table " + MiniLibrisContract.Reservations.BASE_PATH
            + "(" + MiniLibrisContract.Reservations._ID + " integer primary key not null, " +
            MiniLibrisContract.Reservations.BOOK_ID + " integer not null, " +
            MiniLibrisContract.Reservations.USER_ID + " integer not null, " +
            MiniLibrisContract.Reservations.BEGINS + " text not null, " +
            MiniLibrisContract.Reservations.ENDS + " text not null, " +
            MiniLibrisContract.Reservations.IS_LENT + " integer not null" +
            ");";

    // Creates the table (wiping any data)
    public static void create(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MiniLibrisContract.Reservations.BASE_PATH);
        db.execSQL(DATABASE_CREATE);
    }

    // Upgrades the table from version to version.
    // Here it only recreates the table, as the data gets fetched from the server, anyway.
    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        create(db);
    }


}
