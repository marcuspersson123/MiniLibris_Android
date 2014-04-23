package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.webbdev.minilibris.R;

public class BookDetailActivity extends Activity implements CreateReservationTaskFragment.TaskCallback {

    private BookDetailFragment bookDetailFragment;
    private CreateReservationTaskFragment mCreateReservationTaskFragment;
    private static final String TAG_CREATE_RESERVATION_TASK_FRAGMENT = "1";
    private static final String TAG_DELETE_RESERVATION_TASK_FRAGMENT = "2";
    private DeleteReservationTaskFragment mDeleteReservationTaskFragment;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_detail);

        this.bookDetailFragment = (BookDetailFragment) this.getFragmentManager().findFragmentById(R.id.bookDetailFragment);
        FragmentManager fm = getFragmentManager();
        mCreateReservationTaskFragment = (CreateReservationTaskFragment) fm.findFragmentByTag(TAG_CREATE_RESERVATION_TASK_FRAGMENT);
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mCreateReservationTaskFragment == null) {
            mCreateReservationTaskFragment = new CreateReservationTaskFragment(TAG_CREATE_RESERVATION_TASK_FRAGMENT);
            fm.beginTransaction().add(mCreateReservationTaskFragment, TAG_CREATE_RESERVATION_TASK_FRAGMENT).commit();
        }

        mDeleteReservationTaskFragment = (DeleteReservationTaskFragment) fm.findFragmentByTag(TAG_DELETE_RESERVATION_TASK_FRAGMENT);
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mDeleteReservationTaskFragment == null) {
            mDeleteReservationTaskFragment = new DeleteReservationTaskFragment(TAG_DELETE_RESERVATION_TASK_FRAGMENT);
            fm.beginTransaction().add(mDeleteReservationTaskFragment, TAG_DELETE_RESERVATION_TASK_FRAGMENT).commit();
        }

    }

    // When a user wants to reserve the book.
    // Tells a headless fragment to reserve.
    public void onStartReservationTask(long book_id, int year, int month, int day) {
        mCreateReservationTaskFragment.setBookId(book_id);
        mCreateReservationTaskFragment.setYear(year);
        mCreateReservationTaskFragment.setMonth(month);
        mCreateReservationTaskFragment.setDay(day);
        mCreateReservationTaskFragment.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.book_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPreExecute(String TAG) {
        if (TAG.equals(TAG_DELETE_RESERVATION_TASK_FRAGMENT)) {

        } else if (TAG.equals(TAG_CREATE_RESERVATION_TASK_FRAGMENT)) {
            bookDetailFragment.onStartingReservation();
        }

    }

    @Override
    public void onProgressUpdate(String TAG, int percent) {

    }

    @Override
    public void onCancelled(String TAG) {
        bookDetailFragment.onReservationFailed();
    }

    @Override
    public void onPostExecute(String TAG) {
        if (TAG.equals(TAG_DELETE_RESERVATION_TASK_FRAGMENT)) {
            Toast.makeText(this,mDeleteReservationTaskFragment.getResult(),Toast.LENGTH_LONG).show();
        } else if (TAG.equals(TAG_CREATE_RESERVATION_TASK_FRAGMENT)) {
            bookDetailFragment.onReservationTaskFinished(mCreateReservationTaskFragment.getResult());
        }
    }


    public void onStartDeleteReservationTask(int reservationId) {
        mDeleteReservationTaskFragment.setReservationId(reservationId);
        mDeleteReservationTaskFragment.start();
    }
}
