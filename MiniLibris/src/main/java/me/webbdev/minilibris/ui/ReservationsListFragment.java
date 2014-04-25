package me.webbdev.minilibris.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
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
        ListView lv = (ListView) rootView.findViewById(android.R.id.list);
        TextView emptyText = (TextView) rootView.findViewById(android.R.id.empty);
        lv.setEmptyView(emptyText);
        return rootView;
    }
    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        mContext = this.getActivity().getApplicationContext();
        bookId = getActivity().getIntent().getIntExtra("id", -1);
        getLoaderManager().initLoader(0, null, this);
        adapter = new ReservationsCursorAdapter(getActivity(),null);
        setListAdapter(adapter);
    }

    // Creates a loader for all reservations for this book.
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

    // The adapter that displays reservations.
    public class ReservationsCursorAdapter extends CursorAdapter {
        private Context context;

        LayoutInflater mInflater;

        public ReservationsCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final int reservationId = cursor.getInt(cursor.getColumnIndex(MiniLibrisContract.Reservations._ID));
            TextView timespanTextView = (TextView)view.findViewById(R.id.timespanTextView);
            String begins = cursor.getString(cursor.getColumnIndex(MiniLibrisContract.Reservations.BEGINS));
            String ends = cursor.getString(cursor.getColumnIndex(MiniLibrisContract.Reservations.ENDS));
            timespanTextView.setText(begins + " to " + ends);
            ImageButton deleteImageButton = (ImageButton) view.findViewById(R.id.deleteReservationImageButton);
            deleteImageButton.setOnClickListener(new View.OnClickListener() {
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
