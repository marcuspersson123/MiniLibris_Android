package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.services.SyncDatabaseIntentService;

public class BookDetailActivity extends Activity implements TaskFragment.TaskFragmentCallback, ReservationsListFragment.ReservationsListFragmentListener, BookDetailFragment.BookDetailFragmentListener, DatePickerFragment.DatePickerFragmentListener {

    // Three fragments defined in xml
    private BookDetailFragment bookDetailFragment;
    private CreateReservationTaskFragment mCreateReservationTaskFragment;
    private DeleteReservationTaskFragment mDeleteReservationTaskFragment;
    private int userId;
    private int bookId;

    // Display the Activity.
    // Save references to the fragments.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        translateIntent(getIntent());
        setContentView(R.layout.activity_book_detail);
        FragmentManager fragmentManager = getFragmentManager();
        this.bookDetailFragment = (BookDetailFragment) fragmentManager.findFragmentById(R.id.bookDetailFragment);
        mCreateReservationTaskFragment = (CreateReservationTaskFragment) fragmentManager.findFragmentById(R.id.createReservationTaskFragment);
        mDeleteReservationTaskFragment = (DeleteReservationTaskFragment) fragmentManager.findFragmentById(R.id.deleteReservationTaskFragment);

        Bundle bundle = this.bookDetailFragment.createArgumentsBundle(this.bookId, this.userId);
        this.bookDetailFragment.useArguments(bundle);
        this.bookDetailFragment.updateViews();
    }

    // Inflate the menu; this adds items to the action bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_detail, menu);
        return true;
    }

    // An a menu item was selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_login:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.action_contact:
                intent = new Intent(this, ContactActivity.class);
                startActivity(intent);
                break;
        }

        return true;
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
        return this.userId;
    }

    @Override
    public int getBookId() {
        return this.bookId;
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

    private void translateIntent(Intent intent) {
        this.userId = intent.getIntExtra("user_id",-1);
        this.bookId = intent.getIntExtra("book_id",-1);
    }

    public static Intent createStartIntent(Context context, int bookId, int userId) {
        Intent intent = new Intent(context, BookDetailActivity.class);
        intent.putExtra("book_id", bookId);
        intent.putExtra("user_id", userId);
        return intent;
    }


}
