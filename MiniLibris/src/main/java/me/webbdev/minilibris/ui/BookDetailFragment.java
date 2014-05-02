package me.webbdev.minilibris.ui;

import android.app.Fragment;
import android.view.*;
import android.os.*;

import me.webbdev.minilibris.R;

import android.net.*;
import android.content.*;
import android.database.*;
import android.widget.*;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.Dialog;

import me.webbdev.minilibris.database.*;

public class BookDetailFragment extends Fragment {

    private TextView titleTextView;
    private ImageButton reserveImageButton;
    private long book_id = -1;
    private TextView authorTextView;
    private TextView yearTextView;
    private TextView publisherTextView;
    private ImageView bookImageView;

    // The interface is intended to be implemented by the containing Activity
    public interface BookDetailFragmentListener {
        public int getBookId();
    }

    // Necessary empty constructor.
    public BookDetailFragment() {
    }

    // Create the views from xml.
    // Save references to the views.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);
        this.titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        this.authorTextView = (TextView) view.findViewById(R.id.authorTextView);
        this.yearTextView = (TextView) view.findViewById(R.id.yearTextView);
        this.publisherTextView = (TextView) view.findViewById(R.id.publisherTextView);
        this.bookImageView = (ImageView) view.findViewById(R.id.bookImageView);
        this.reserveImageButton = (ImageButton) view.findViewById(R.id.reserveImageButton);
        this.reserveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReserveButtonClick();
            }
        });

        return view;
    }

    // The user clicked on the reserve-button. A Datepicker fragment gets displayed.
    private void onReserveButtonClick() {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getActivity().getFragmentManager(), "datePicker");
    }

    // The activity is ready.
    // Now we know which book to display.
    // Get and show the book information.
    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        if (this.book_id < 0) {
            this.book_id = ((BookDetailFragmentListener) getActivity()).getBookId();

            Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, this.book_id);
            Cursor cursor = this.getActivity().getContentResolver().query(singleUri, MiniLibrisContract.Books.ALL_FIELDS, null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String title = cursor.getString(cursor.getColumnIndex(MiniLibrisContract.Books.TITLE));
                String author = cursor.getString(cursor.getColumnIndex(MiniLibrisContract.Books.AUTHOR));
                int year = cursor.getInt(cursor.getColumnIndex(MiniLibrisContract.Books.YEAR));
                String publisher = cursor.getString(cursor.getColumnIndex(MiniLibrisContract.Books.PUBLISHER));
                this.titleTextView.setText(title);
                this.authorTextView.setText(author);
                this.yearTextView.setText(String.valueOf(year));
                this.publisherTextView.setText(publisher);
            }
        }
    }

    // Event from the Activity before a starting a create reservation task.
    public void onStartingReservation() {
        this.reserveImageButton.setEnabled(false);
    }

    // Event from the Activity when creating a reservation failed.
    public void onReservationFailed() {
        this.reserveImageButton.setEnabled(true);
        Toast.makeText(getActivity(), getString(R.string.failed_to_reserve), Toast.LENGTH_LONG).show();
    }

    // Event from the Activity.
    // A create reservation task was finished.
    // The errorMessage is null if it was a success to create a reservation.
    public void onReservationTaskFinished(String errorMessage) {
        this.reserveImageButton.setEnabled(true);
        if (errorMessage != null) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Boken är reserverad", Toast.LENGTH_LONG).show();
        }
    }


}
