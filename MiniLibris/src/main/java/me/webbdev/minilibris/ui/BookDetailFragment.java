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

public class BookDetailFragment extends Fragment implements View.OnClickListener {
    private TextView titleTextView;
    private ImageButton reserveImageButton;
    private long book_id;
    private TextView authorTextView;
    private TextView yearTextView;
    private TextView publisherTextView;
    private ImageView bookImageView;

    public interface BookDetailFragmentListener {
        public int getBookId();
    }

    public BookDetailFragment() {

    }
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
        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        this.reserveImageButton.setOnClickListener(this);
         book_id = ((BookDetailFragmentListener) getActivity()).getBookId();

        Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, book_id);
        Cursor cursor = this.getActivity().getContentResolver().query(singleUri, MiniLibrisContract.Books.ALL_FIELDS, null, null, null);
        if (cursor.getCount()>0) {
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

    // The user clicked on the reserve-button. A Datepicker gets displayed.
    @Override
    public void onClick(View view) {
        if (view == this.reserveImageButton) {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getActivity().getFragmentManager(), "datePicker");
        }
    }

    // Event when a reservation is to be sent from headless fragment.
    public void onStartingReservation() {
        this.reserveImageButton.setEnabled(false);
    }

    // Event when a reservation failed due to IO or server error in the headless fragment.
    public void onReservationFailed() {
        this.reserveImageButton.setEnabled(true);
        Toast.makeText(getActivity(), "Fel! Gick inte att reservera.",Toast.LENGTH_LONG).show();
    }

    // Triggered when the headless fragment communicated with the server.
    // If a message, then an error occurred.
    public void onReservationTaskFinished(String errorMessage) {
        this.reserveImageButton.setEnabled(true);
        if (errorMessage != null) {
            Toast.makeText(getActivity(), errorMessage,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Boken är reserverad",Toast.LENGTH_LONG).show();
        }
    }




}
