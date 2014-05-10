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

    private static final String BOOK_ID_ARGUMENT_KEY = "BOOK_ID_ARGUMENT_KEY";
    private static final String USER_ID_ARGUMENT_KEY = "USER_ID_ARGUMENT_KEY";
    private TextView titleTextView;
    private ImageButton reserveImageButton;
    private int bookId = -1;
    private int userId = -1;
    private TextView authorTextView;
    private TextView yearTextView;
    private TextView publisherTextView;
    private TextView lentToDateTextView;
    private String title = "";
    private String author ="";
    private int year = 0;
    private String publisher = "";
    private String endsDate = "";
    private TextView lentToDateLabelTextView;

    // The interface is intended to be implemented by the containing Activity
    public interface BookDetailFragmentListener {
       // public int getBookId();
       // public int getUserId();
    }

    // Necessary empty constructor.
    // Retain the instance to avoid fetching data on orientation change
    public BookDetailFragment() {
        setRetainInstance(true);
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
        //ImageView bookImageView = (ImageView) view.findViewById(R.id.bookImageView);
        this.reserveImageButton = (ImageButton) view.findViewById(R.id.reserveImageButton);
        this.reserveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReserveButtonClick();
            }
        });
        this.lentToDateTextView = (TextView) view.findViewById(R.id.lentToDateTextView);
        this.lentToDateLabelTextView = (TextView) view.findViewById(R.id.lentToDateLabelTextView);

        // If the fragment was created dynamically in the Activity the arguments may already be present.
        // Otherwise, the Activity will come back set the arguments later.
        if (getArguments() != null) {
            useArguments(getArguments());
            updateViews();
        }

        return view;
    }

    // The user clicked on the reserve-button. A Datepicker fragment gets displayed.
    private void onReserveButtonClick() {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getActivity().getFragmentManager(), "datePicker");
    }

    public Bundle createArgumentsBundle(int bookId, int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt(BOOK_ID_ARGUMENT_KEY,bookId);
        bundle.putInt(USER_ID_ARGUMENT_KEY, userId);
        return bundle;
    }

    private void translateArguments(Bundle arguments) {
        this.bookId = arguments.getInt(BOOK_ID_ARGUMENT_KEY,-1);
        this.userId = arguments.getInt(USER_ID_ARGUMENT_KEY,-1);
    }

    // Now we can know which book to display.
    // Get and show the book information.
    public void useArguments(Bundle arguments) {
        // we retain this instance, so we don't need to fetch the data twice.
        if (this.bookId < 0) {
            translateArguments(arguments);

            Uri getBookUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, this.bookId);
            Cursor bookCursor = this.getActivity().getContentResolver().query(getBookUri, MiniLibrisContract.Books.ALL_FIELDS, null, null, null);
            if (bookCursor.getCount() > 0) {
                bookCursor.moveToFirst();
                 title = bookCursor.getString(bookCursor.getColumnIndex(MiniLibrisContract.Books.TITLE));
                author = bookCursor.getString(bookCursor.getColumnIndex(MiniLibrisContract.Books.AUTHOR));
                 year = bookCursor.getInt(bookCursor.getColumnIndex(MiniLibrisContract.Books.YEAR));
                 publisher = bookCursor.getString(bookCursor.getColumnIndex(MiniLibrisContract.Books.PUBLISHER));
            }
            bookCursor.close();
            Uri getReservationUri = MiniLibrisContract.Reservations.CONTENT_URI;
           // int userId = ((BookDetailFragmentListener) getActivity()).getUserId();
            Cursor reservationCursor = this.getActivity().getContentResolver().query(getReservationUri, MiniLibrisContract.Reservations.ALL_FIELDS, "is_lent=? and book_id=?", new String[] {"1",String.valueOf(this.bookId)}, null);
            if (reservationCursor.getCount() > 0) {
                reservationCursor.moveToFirst();
                endsDate = reservationCursor.getString(reservationCursor.getColumnIndex(MiniLibrisContract.Reservations.ENDS));
            }
            reservationCursor.close();
        }

    }

    // Updates the views. If the book wasn't found they will be empty strings.
    public void updateViews() {
        this.titleTextView.setText(title);
        this.authorTextView.setText(author);
        this.yearTextView.setText(String.valueOf(year));
        this.publisherTextView.setText(publisher);
        if (endsDate != "") {
            this.lentToDateTextView.setText(endsDate);
        } else {
            this.lentToDateTextView.setVisibility(View.GONE);
            this.lentToDateLabelTextView.setVisibility(View.GONE);
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
