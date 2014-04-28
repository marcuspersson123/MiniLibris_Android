package me.webbdev.minilibris.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.ui.BookDetailActivity;
import me.webbdev.minilibris.ui.PersistingTitleDatePickerDialog;

// The date picker fragment.
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    int mYear, mMonth, mDay;

    public interface DatePickerFragmentListener {
        public void onReserveDateSelected(int year, int month, int day);
    }

    public DatePickerFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mDay = day;
        mMonth = month;
        mYear = year;

        final PersistingTitleDatePickerDialog dpDialog = new PersistingTitleDatePickerDialog(getActivity(), this, year, month, day);

        dpDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        calendar.add(Calendar.YEAR,1);
        dpDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        dpDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Reservera", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                BookDetailActivity activity = (BookDetailActivity) getActivity();
                int year = dpDialog.getDatePicker().getYear();
                int month = dpDialog.getDatePicker().getMonth();
                int day = dpDialog.getDatePicker().getDayOfMonth();
                ((DatePickerFragmentListener) getActivity()).onReserveDateSelected(year, month, day);
                dialog.dismiss();
            }
        });
        dpDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Avbryt", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        dpDialog.setPersistingTitle(R.string.select_reservation_date_title);
        return dpDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        mDay = day;
        mMonth = month;
        mYear = year;
    }
}