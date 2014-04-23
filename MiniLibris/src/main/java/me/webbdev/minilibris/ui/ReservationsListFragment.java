package me.webbdev.minilibris.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
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
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.database.MiniLibrisContract;

public class ReservationsListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public ReservationsCursorAdapter adapter;
    private Context mContext;
    private int bookId;


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
        //ListView lv = this.getListView();
        //lv.setOnItemClickListener(this);

        bookId = getActivity().getIntent().getIntExtra("id", -1);
        fillData();
    }

    private void fillData() {
        String[] from = new String[] { MiniLibrisContract.Reservations.BEGINS };
        int[] to = new int[] { R.id.begins};

        getLoaderManager().initLoader(0, null, this);
        adapter = new ReservationsCursorAdapter(mContext,null);

        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = MiniLibrisContract.Reservations.ALL_FIELDS;
        String whereClause = MiniLibrisContract.Reservations.BOOK_ID + " = ?";
        String[] whereVariables = new String[]{String.valueOf(this.bookId)};
        CursorLoader cursorLoader = new CursorLoader( mContext,
                MiniLibrisContract.Reservations.CONTENT_URI, projection, whereClause , whereVariables, null);
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



    public class ReservationsCursorAdapter extends CursorAdapter {
        private Context context;

        LayoutInflater mInflater;

        public ReservationsCursorAdapter(Context context, Cursor c) {
            // that constructor should be used with loaders.
            super(context, c, 0);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final int reservationId = cursor.getInt(cursor.getColumnIndex(MiniLibrisContract.Reservations._ID));
            TextView begins = (TextView)view.findViewById(R.id.begins);
            begins.setText(cursor.getString(cursor.getColumnIndex(MiniLibrisContract.Reservations.BEGINS)));
            Button deleteButton = (Button) view.findViewById(R.id.deleteReservationButton);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDeleteReservation(reservationId);
                }
            });

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.list_item_reservation, parent, false);
            return v;
        }

    }

    // Tells the activity to delete through a headless fragment.

    private void onDeleteReservation(int reservationId) {
        ((BookDetailActivity) getActivity()).onStartDeleteReservationTask(reservationId);
    }
}
