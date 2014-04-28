package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import me.webbdev.minilibris.database.MiniLibrisContract;
import me.webbdev.minilibris.R;

import android.widget.*;

public class BooksListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    public static final String MODE_KEY = "MODE_KEY";
    public SimpleCursorAdapter adapter;
    private Context mContext;
    static final int ALL_BOOKS_MODE = 0;
    static final int RESERVED_BOOKS_MODE = 1;
    static final int LENT_BOOKS_MODE = 2;

    public interface BooksListFragmentListener {
        public void onBookSelected(int id);
    }

    public BooksListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_books_list,
                container, false);

        return rootView;
    }

    // Sets the list adapter and start the cursor loader.
    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        mContext = this.getActivity().getApplicationContext();
        ListView lv = this.getListView();
        lv.setOnItemClickListener(this);
        String[] databaseFields = new String[]{MiniLibrisContract.Books.TITLE, MiniLibrisContract.Books.AUTHOR};
        int[] databaseFieldsToIds = new int[]{R.id.title, R.id.author};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(mContext, R.layout.list_item_book, null, databaseFields, databaseFieldsToIds, 0);

        setListAdapter(adapter);
    }

    // Called when the loader is to be created.
    // Fetches the interesting fields
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // To avoid ambiguity in the sql join query, the _id field has to be set explicitly
        String[] databaseFields = new String[]{MiniLibrisContract.Books.BASE_PATH + "." + MiniLibrisContract.Books._ID, MiniLibrisContract.Books.TITLE, MiniLibrisContract.Books.AUTHOR};
        CursorLoader cursorLoader = null;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_info", Activity.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        String sortOrder = MiniLibrisContract.Books.TITLE + " ASC";
        switch (getArguments().getInt(MODE_KEY)) {
            case ALL_BOOKS_MODE:
                cursorLoader = new CursorLoader(mContext,
                        MiniLibrisContract.Books.CONTENT_URI, databaseFields, null, null, sortOrder);
                break;
            case RESERVED_BOOKS_MODE:

                Uri stttingleUri = ContentUris.withAppendedId(MiniLibrisContract.UserBooks.CONTENT_URI, userId);
                cursorLoader = new CursorLoader(mContext,
                        stttingleUri, databaseFields, "is_lent=?", new String[]{"0"}, sortOrder);
                break;
            case LENT_BOOKS_MODE:
                Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.UserBooks.CONTENT_URI, userId);
                cursorLoader = new CursorLoader(mContext,
                        singleUri, databaseFields, "is_lent=?", new String[]{"1"}, sortOrder);
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((BooksListFragmentListener) getActivity()).onBookSelected((int) id);
    }
}
