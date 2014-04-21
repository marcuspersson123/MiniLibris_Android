package me.webbdev.minilibris.database;

import android.content.ContentResolver;
import android.net.Uri;

public class MiniLibrisContract {

    protected static final String AUTHORITY = "me.webbdev.minilibris.contentprovider";

    public static class Books {

        public static final String BASE_PATH = "books";
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + BASE_PATH);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/books";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/book";
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String PUBLISHER = "publisher";
        public static final String AUTHOR = "author";
        public static final String YEAR = "year";
        public static final String CATEGORY_ID = "category_id";
        public static final String CHANGED = "changed";

        public static final String[] ALL_FIELDS = {_ID, TITLE, PUBLISHER, AUTHOR, YEAR, CATEGORY_ID, CHANGED};
    }
}

