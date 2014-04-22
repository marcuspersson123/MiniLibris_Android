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

    @Override
    public void onClick(View view) {
        if (view == this.reserveButton) {

            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getActivity().getFragmentManager(), "datePicker");
        }
    }


    public void startingReservation() {
        this.reserveButton.setEnabled(false);
    }

    public void reservationFailed() {
        this.reserveButton.setEnabled(true);

    }

    public void reservationTaskFinished(String message) {
        this.reserveButton.setEnabled(true);
        if (message != null) {
            Toast.makeText(getActivity(), message,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "The book is reserved",Toast.LENGTH_LONG).show();
        }
    }

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

                    activity.startReservationTask(book_id, year, month, day);
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
}
