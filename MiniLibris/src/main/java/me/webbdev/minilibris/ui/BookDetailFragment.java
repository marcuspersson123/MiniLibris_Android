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
    private Button reserveButton;
    private long book_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);
        this.titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        this.reserveButton = (Button) view.findViewById(R.id.reserveButton);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        this.reserveButton.setOnClickListener(this);
         book_id = getActivity().getIntent().getIntExtra("id", -1);

        Uri singleUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, book_id);
        Cursor cursor = this.getActivity().getContentResolver().query(singleUri, MiniLibrisContract.Books.ALL_FIELDS, null, null, null);
        if (cursor.getCount()>0) {
            cursor.moveToFirst();
            String title = cursor.getString(cursor.getColumnIndex(MiniLibrisContract.Books.TITLE));
            this.titleTextView.setText(title);
        }
    }

    // The user clicked on the reserve-button. A Datepicker gets displayed.
    @Override
    public void onClick(View view) {
        if (view == this.reserveButton) {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getActivity().getFragmentManager(), "datePicker");
        }
    }

    // Event when a reservation is to be sent from headless fragment.
    public void onStartingReservation() {
        this.reserveButton.setEnabled(false);
    }

    // Event when a reservation failed due to IO or server error in the headless fragment.
    public void onReservationFailed() {
        this.reserveButton.setEnabled(true);
        Toast.makeText(getActivity(), "Error trying to reserve!",Toast.LENGTH_LONG).show();
    }

    // Triggered when the headless fragment communicated with the server.
    // If a message, then an error occurred.
    public void onReservationTaskFinished(String errorMessage) {
        this.reserveButton.setEnabled(true);
        if (errorMessage != null) {
            Toast.makeText(getActivity(), errorMessage,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "The book is reserved",Toast.LENGTH_LONG).show();
        }
    }

    // The date picker fragment.
    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        int mYear, mMonth, mDay;


        public DatePickerFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            mDay = day;
            mMonth = month;
            mYear = year;

            final DatePickerDialog dpDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            dpDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            dpDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Reserve", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    BookDetailActivity activity = (BookDetailActivity) getActivity();
                    int year = dpDialog.getDatePicker().getYear();
                    int month = dpDialog.getDatePicker().getMonth();
                    int day = dpDialog.getDatePicker().getDayOfMonth();
                    onReserveDateSelected(book_id, year, month, day);
                    dialog.dismiss();
                }
            });
            return dpDialog;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            mDay = day;
            mMonth = month;
            mYear = year;
        }
    }

    // Triggered when a date was selected in the date picker.
    // Passes the message to the Activity, which in turn will start a headless fragment.
    private void onReserveDateSelected(long book_id, int year, int month, int day) {
        ((BookDetailActivity) getActivity()).onStartReservationTask(book_id, year, month, day);
    }
}
