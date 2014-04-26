package me.webbdev.minilibris.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

// A date picker dialog gets its title replaced after date selection
public class PersistingTitleDatePickerDialog extends DatePickerDialog {

    private CharSequence title;

    public PersistingTitleDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    public void setPersistingTitle(CharSequence title) {
        this.title = title;
        setTitle(title);
    }

    public void setPersistingTitle(int resValue) {
        this.title = getContext().getString(resValue);
        setTitle(title);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);
        setTitle(title);
    }
}
