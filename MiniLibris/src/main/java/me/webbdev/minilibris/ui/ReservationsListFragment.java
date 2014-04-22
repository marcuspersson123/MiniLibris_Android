package me.webbdev.minilibris.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
//import android.support.v4.app.ListFragment;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.CursorLoader;
//import android.support.v4.content.Loader;
//import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.database.MiniLibrisContract;

public class ReservationsListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,AdapterView.OnItemClickListener {

    public SimpleCursorAdapter adapter;
    private Context mContext;

    public ReservationsListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reservations_list,
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
        String[] from = new String[] { MiniLibrisContract.Reservations.BEGINS };
        int[] to = new int[] { R.id.begins};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(mContext,R.layout.list_item_reservation, null, from, to, 0);

        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = MiniLibrisContract.Reservations.ALL_FIELDS;
        CursorLoader cursorLoader = new CursorLoader( mContext,
                MiniLibrisContract.Reservations.CONTENT_URI, projection, null, null, null);
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
