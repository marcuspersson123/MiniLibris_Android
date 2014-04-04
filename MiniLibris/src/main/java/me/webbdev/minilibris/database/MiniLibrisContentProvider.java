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

public class MiniLibrisContentProvider extends ContentProvider {

    // database
    private MiniLibrisDatabaseHelper database;

    // used for the UriMacher
    private static final int BOOKS = 10;
    private static final int BOOK_ID = 20;

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(MiniLibrisContract.AUTHORITY,
                MiniLibrisContract.Books.BASE_PATH, BOOKS);
        sURIMatcher.addURI(MiniLibrisContract.AUTHORITY,
                MiniLibrisContract.Books.BASE_PATH + "/#", BOOK_ID);
    }

    @Override
    public boolean onCreate() {
        database = new MiniLibrisDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case BOOKS:
                queryBuilder.setTables(MiniLibrisContract.Books.BASE_PATH);
                break;
            case BOOK_ID:
                queryBuilder.setTables(MiniLibrisContract.Books.BASE_PATH);
                // adding the ID to the original query
                queryBuilder.appendWhere(MiniLibrisContract.Books._ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case BOOKS:
                id = sqlDB.insert(MiniLibrisContract.Books.BASE_PATH, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Uri itemUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(itemUri, null);
        return itemUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case BOOKS:
                rowsDeleted = sqlDB.delete(MiniLibrisContract.Books.BASE_PATH,
                        selection, selectionArgs);
                break;
            case BOOK_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(MiniLibrisContract.Books.BASE_PATH,
                            MiniLibrisContract.Books._ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(MiniLibrisContract.Books.BASE_PATH,
                            MiniLibrisContract.Books._ID + "=" + id + " and "
                                    + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
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
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}

