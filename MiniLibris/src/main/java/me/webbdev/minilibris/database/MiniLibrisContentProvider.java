package me.webbdev.minilibris.database;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

// Makes the databaseHelper available as a content provider
public class MiniLibrisContentProvider extends ContentProvider {

    // databaseHelper
    private MiniLibrisDatabaseHelper databaseHelper;

    // used for the UriMatcher
    private static final int BOOKS = 10;
    private static final int BOOK_ID = 11;
    private static final int RESERVATIONS = 20;
    private static final int RESERVATION_ID = 21;
    private static final int USER_BOOKS_ID = 30;

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    // initialize the URI matcher.
    static {
        sURIMatcher.addURI(MiniLibrisContract.AUTHORITY,
                MiniLibrisContract.Books.BASE_PATH, BOOKS);
        sURIMatcher.addURI(MiniLibrisContract.AUTHORITY,
                MiniLibrisContract.Books.BASE_PATH + "/#", BOOK_ID);
        sURIMatcher.addURI(MiniLibrisContract.AUTHORITY,
                MiniLibrisContract.Reservations.BASE_PATH, RESERVATIONS);
        sURIMatcher.addURI(MiniLibrisContract.AUTHORITY,
                MiniLibrisContract.Reservations.BASE_PATH + "/#", RESERVATION_ID);
        // The uri does not match a table. This is just a selector to get books that are reserved by a user.
        sURIMatcher.addURI(MiniLibrisContract.AUTHORITY,
                MiniLibrisContract.UserBooks.BASE_PATH + "/#", USER_BOOKS_ID);
    }

    // Create and store a databaseHelper helper.
    @Override
    public boolean onCreate() {
        databaseHelper = new MiniLibrisDatabaseHelper(getContext());
        return false;
    }

    // Handles a query to the database. Returns a cursor
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        String groupBy = null;
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BOOKS:
                queryBuilder.setTables(MiniLibrisContract.Books.BASE_PATH);
                break;
            case BOOK_ID:
                queryBuilder.setTables(MiniLibrisContract.Books.BASE_PATH);
                // set the where clause to the book_id
                queryBuilder.appendWhere(MiniLibrisContract.Books._ID + "="
                        + uri.getLastPathSegment());
                break;
            case RESERVATIONS:
                queryBuilder.setTables(MiniLibrisContract.Reservations.BASE_PATH);
                break;
            case RESERVATION_ID:
                queryBuilder.setTables(MiniLibrisContract.Reservations.BASE_PATH);
                // set the where clause to the reservation_id
                queryBuilder.appendWhere(MiniLibrisContract.Reservations._ID + "="
                        + uri.getLastPathSegment());
                break;
            case USER_BOOKS_ID:
                // SELECT * FROM reservations INNER  JOIN books ON reservations.book_id=books.book_id where reservations.user_id=3 GROUP BY books.book_id

                String booksTableName = MiniLibrisContract.Books.BASE_PATH;
                String reservationsTableName = MiniLibrisContract.Reservations.BASE_PATH;
                groupBy = booksTableName+"."+MiniLibrisContract.Books._ID;
                String setTablesString = reservationsTableName + " inner join " + booksTableName + " on " + reservationsTableName + "." + MiniLibrisContract.Reservations.BOOK_ID + "=" + booksTableName + "." + MiniLibrisContract.Books._ID;
                queryBuilder.setTables(setTablesString);
                String whereClause = reservationsTableName + "." + MiniLibrisContract.Reservations.USER_ID + "="
                        + uri.getLastPathSegment();
                queryBuilder.appendWhere(whereClause);
                // The querybuilder will also add the "selection"-variable in the where clause below.

                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, groupBy, null, sortOrder);
        // notify potential listeners
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    // Returns null: This content provider is not exported, so the type of data is known to its users.
    @Override
    public String getType(Uri uri) {
        return null;
    }

    // Inserts a record in one of the tables. Returns the uri for the record
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case BOOKS:
                id = sqlDB.insert(MiniLibrisContract.Books.BASE_PATH, null, values);
                break;
            case RESERVATIONS:
                id = sqlDB.insert(MiniLibrisContract.Reservations.BASE_PATH, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Uri notifyUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(notifyUri, null);

            Uri notifyUserBooks = MiniLibrisContract.UserBooks.CONTENT_URI;
            getContext().getContentResolver().notifyChange(notifyUserBooks, null);

        return notifyUri;
    }

    // Deletes one (or more) record in one of the tables. Returns number of deleted rows.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case BOOKS:
                rowsDeleted = sqlDB.delete(MiniLibrisContract.Books.BASE_PATH,
                        selection, selectionArgs);
                break;
            case BOOK_ID:
                String bookId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(MiniLibrisContract.Books.BASE_PATH,
                            MiniLibrisContract.Books._ID + "=" + bookId, null);
                } else {
                    rowsDeleted = sqlDB.delete(MiniLibrisContract.Books.BASE_PATH,
                            MiniLibrisContract.Books._ID + "=" + bookId + " and "
                                    + selection, selectionArgs);
                }
                break;
            case RESERVATIONS:
                rowsDeleted = sqlDB.delete(MiniLibrisContract.Reservations.BASE_PATH,
                        selection, selectionArgs);
                break;
            case RESERVATION_ID:
                String reservationId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(MiniLibrisContract.Reservations.BASE_PATH,
                            MiniLibrisContract.Reservations._ID + "=" + reservationId, null);
                } else {
                    rowsDeleted = sqlDB.delete(MiniLibrisContract.Reservations.BASE_PATH,
                            MiniLibrisContract.Reservations._ID + "=" + reservationId + " and "
                                    + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsDeleted>0) {
            getContext().getContentResolver().notifyChange(uri, null);

                Uri notifyUserBooks = MiniLibrisContract.UserBooks.CONTENT_URI;
                getContext().getContentResolver().notifyChange(notifyUserBooks, null);

        }
        return rowsDeleted;
    }

    // Updates one (or more) rows i a table. Returns number of updated rows.
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case BOOKS:
                rowsUpdated = sqlDB.update(MiniLibrisContract.Books.BASE_PATH,
                        values, selection, selectionArgs);
                break;
            case BOOK_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(MiniLibrisContract.Books.BASE_PATH,
                            values, MiniLibrisContract.Books._ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(MiniLibrisContract.Books.BASE_PATH,
                            values, MiniLibrisContract.Books._ID + "=" + id
                            + " and " + selection, selectionArgs);
                }
                break;
            case RESERVATIONS:
                rowsUpdated = sqlDB.update(MiniLibrisContract.Reservations.BASE_PATH,
                        values, selection, selectionArgs);
                break;
            case RESERVATION_ID:
                String reservationId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(MiniLibrisContract.Reservations.BASE_PATH,
                            values, MiniLibrisContract.Reservations._ID + "=" + reservationId,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(MiniLibrisContract.Reservations.BASE_PATH,
                            values, MiniLibrisContract.Reservations._ID + "=" + reservationId
                            + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsUpdated>0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Uri notifyUserBooks = MiniLibrisContract.UserBooks.CONTENT_URI;
            getContext().getContentResolver().notifyChange(notifyUserBooks, null);

        }
        return rowsUpdated;
    }

}

