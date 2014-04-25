package me.webbdev.minilibris.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import me.webbdev.minilibris.database.MiniLibrisContract;
import me.webbdev.minilibris.R;
import android.widget.*;

class BooksListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,AdapterView.OnItemClickListener {

    private final String whereClause;
    private final String[] whereClauseVariables;
    public SimpleCursorAdapter adapter;
    private Context mContext;


    public BooksListFragment(String whereClause, String[] whereClauseVariables) {
        this.whereClause = whereClause;
        this.whereClauseVariables = whereClauseVariables;
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
        String[] databaseFields = new String[] { MiniLibrisContract.Books.TITLE, MiniLibrisContract.Books.YEAR };
        int[] databaseFieldsToIds = new int[] { R.id.title, R.id.year};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(mContext,R.layout.list_item_book, null, databaseFields, databaseFieldsToIds, 0);

        setListAdapter(adapter);
    }

    // Called when the loader is to be created.
    // Fetches the interesting fields
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] databaseFields = new String[] { MiniLibrisContract.Books._ID, MiniLibrisContract.Books.TITLE, MiniLibrisContract.Books.YEAR };
        CursorLoader cursorLoader = new CursorLoader( mContext,
                MiniLibrisContract.Books.CONTENT_URI, databaseFields, whereClause, whereClauseVariables, null);
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
        Intent intent = new Intent(this.mContext,BookDetailActivity.class);
        intent.putExtra("id", (int) id);
        startActivity(intent);
    }
}
