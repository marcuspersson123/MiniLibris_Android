package me.webbdev.minilibris.database;

import android.content.ContentResolver;
import android.net.Uri;

public class MiniLibrisContract {

    protected static final String AUTHORITY = "me.webbdev.minilibris.contentprovider";

    // The contract that makes it safer to use the books table
    public static class Books {
        public static final String BASE_PATH = "books";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + BASE_PATH);
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String PUBLISHER = "publisher";
        public static final String AUTHOR = "author";
        public static final String YEAR = "year";
        public static final String CATEGORY_ID = "category_id";
        public static final String CHANGED = "changed";

        public static final String[] ALL_FIELDS = {_ID, TITLE, PUBLISHER, AUTHOR, YEAR, CATEGORY_ID, CHANGED};
    }

    // The contract that makes it safer to use the reservations table
    public static class Reservations {
        public static final String BASE_PATH = "reservations";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + BASE_PATH);
        public static final String _ID = "_id";
        public static final String BOOK_ID = "book_id";
        public static final String USER_ID = "user_id";
        public static final String BEGINS = "begins";
        public static final String ENDS = "ends";
        public static final String IS_LENT = "is_lent";

        public static final String[] ALL_FIELDS = {_ID, BOOK_ID, USER_ID, BEGINS, ENDS, IS_LENT};
    }
}

