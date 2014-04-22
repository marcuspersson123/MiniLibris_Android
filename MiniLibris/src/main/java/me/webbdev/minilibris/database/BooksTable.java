package me.webbdev.minilibris.database;
import android.database.sqlite.SQLiteDatabase;

// Executes SQL necessary to create and update the books table
public class BooksTable {

    // Create table syntax for the books table.
    private static final String DATABASE_CREATE = "create table " + MiniLibrisContract.Books.BASE_PATH
            + "(" + MiniLibrisContract.Books._ID + " integer primary key not null, " +
            MiniLibrisContract.Books.TITLE + " text not null, " +
            MiniLibrisContract.Books.PUBLISHER + " text not null, " +
            MiniLibrisContract.Books.AUTHOR + " text not null, " +
            MiniLibrisContract.Books.YEAR + " integer not null, " +
            MiniLibrisContract.Books.CATEGORY_ID + " integer not null, " +
            MiniLibrisContract.Books.CHANGED + " text not null" +
            ");";

    // Creates the table (wiping any data)
    public static void create(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + MiniLibrisContract.Books.BASE_PATH);
        db.execSQL(DATABASE_CREATE);
    }

    // Upgrades the table from version to version.
    // Here it only recreates the table, as the data gets fetched from the server, anyway.
    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        create(db);
    }


}
