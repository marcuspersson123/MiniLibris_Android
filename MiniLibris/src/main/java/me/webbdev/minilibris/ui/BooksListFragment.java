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
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    public SimpleCursorAdapter adapter;
    private Context mContext;

    public BooksListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_books_list,
                container, false);

        return rootView;
    }
    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        mContext = this.getActivity().getApplicationContext();
        ListView lv = this.getListView();
        lv.setOnItemClickListener(this);
        fillData();
    }

    private void fillData() {
        String[] from = new String[] { MiniLibrisContract.Books.TITLE };
        int[] to = new int[] { android.R.id.text1};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(mContext,R.layout.list_item_book, null, from, to, 0);

        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = MiniLibrisContract.Books.ALL_FIELDS;
        CursorLoader cursorLoader = new CursorLoader( mContext,
                MiniLibrisContract.Books.CONTENT_URI, projection, null, null, null);
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
