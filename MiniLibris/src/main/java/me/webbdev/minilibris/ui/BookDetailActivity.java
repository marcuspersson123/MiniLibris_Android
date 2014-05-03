package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.services.SyncDatabaseIntentService;

public class BookDetailActivity extends Activity implements TaskFragment.TaskFragmentCallback, ReservationsListFragment.ReservationsListFragmentListener, BookDetailFragment.BookDetailFragmentListener, DatePickerFragment.DatePickerFragmentListener {

    // Three fragments defined in xml
    private BookDetailFragment bookDetailFragment;
    private CreateReservationTaskFragment mCreateReservationTaskFragment;
    private DeleteReservationTaskFragment mDeleteReservationTaskFragment;

    // Display the Activity.
    // Save references to the fragments.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        FragmentManager fragmentManager = getFragmentManager();
        this.bookDetailFragment = (BookDetailFragment) fragmentManager.findFragmentById(R.id.bookDetailFragment);
        mCreateReservationTaskFragment = (CreateReservationTaskFragment) fragmentManager.findFragmentById(R.id.createReservationTaskFragment);
        mDeleteReservationTaskFragment = (DeleteReservationTaskFragment) fragmentManager.findFragmentById(R.id.deleteReservationTaskFragment);
    }

    @Override
    public void onPreExecute(int fragmentId) {
        switch (fragmentId) {
            case R.id.deleteReservationTaskFragment:
                break;
            case R.id.createReservationTaskFragment:
                bookDetailFragment.onStartingReservation();
                break;
        }
    }

    @Override
    public void onProgressUpdate(int fragmentId, int percent) {

    }

    @Override
    public void onCancelled(int fragmentId) {
        switch (fragmentId) {
            case R.id.deleteReservationTaskFragment:
                break;
            case R.id.createReservationTaskFragment:
                bookDetailFragment.onReservationFailed();
                break;
        }

    }

    @Override
    public void onPostExecute(int fragmentId) {
        String fragmentMessage;

        switch (fragmentId) {
            case R.id.deleteReservationTaskFragment:
                fragmentMessage = mDeleteReservationTaskFragment.getResult();
                if (fragmentMessage != null) {
                    // Failed
                    Toast.makeText(this, fragmentMessage, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getString(R.string.reservation_deleted), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.createReservationTaskFragment:
                fragmentMessage = mCreateReservationTaskFragment.getResult();
                if (fragmentMessage == null) {
                    // successfully created a reservation
                    Toast.makeText(this, getString(R.string.was_reserved), Toast.LENGTH_LONG).show();
                }
                bookDetailFragment.onReservationTaskFinished(fragmentMessage);
                break;
        }
    }

    // Message from fragment.
    // Starts a TaskFragment to delete a reservation.
    public void onStartDeleteReservationTask(int reservationId) {
        mDeleteReservationTaskFragment.setReservationId(reservationId);
        mDeleteReservationTaskFragment.start();
    }

    @Override
    public int getUserId() {
        return getIntent().getIntExtra("user_id", -1);
    }

    @Override
    public int getBookId() {
        return getIntent().getIntExtra("id", -1);
    }

    // Message from a fragment
    // Tells a TaskFragment to create a new reservation.
    @Override
    public void onReserveDateSelected(int year, int month, int day) {
        mCreateReservationTaskFragment.setBookId(getBookId());
        mCreateReservationTaskFragment.setUserId(getUserId());
        mCreateReservationTaskFragment.setYear(year);
        mCreateReservationTaskFragment.setMonth(month);
        mCreateReservationTaskFragment.setDay(day);
        mCreateReservationTaskFragment.start();
    }
}
