package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.services.SyncDatabaseIntentService;

public class BookDetailActivity extends Activity implements TaskFragment.TaskFragmentCallback {

    private BookDetailFragment bookDetailFragment;
    private CreateReservationTaskFragment mCreateReservationTaskFragment;
//    private static final String TAG_CREATE_RESERVATION_TASK_FRAGMENT = "1";
//    private static final String TAG_DELETE_RESERVATION_TASK_FRAGMENT = "2";
    private DeleteReservationTaskFragment mDeleteReservationTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_detail);

        FragmentManager fragmentManager = getFragmentManager();
        this.bookDetailFragment = (BookDetailFragment) fragmentManager.findFragmentById(R.id.bookDetailFragment);
        mCreateReservationTaskFragment = (CreateReservationTaskFragment) fragmentManager.findFragmentById(R.id.createReservationTaskFragment);
        mDeleteReservationTaskFragment = (DeleteReservationTaskFragment) fragmentManager.findFragmentById(R.id.deleteReservationTaskFragment);

       /* mCreateReservationTaskFragment = (CreateReservationTaskFragment) fragmentManager.findFragmentByTag(TAG_CREATE_RESERVATION_TASK_FRAGMENT);
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mCreateReservationTaskFragment == null) {
            mCreateReservationTaskFragment = new CreateReservationTaskFragment(TAG_CREATE_RESERVATION_TASK_FRAGMENT);
            fragmentManager.beginTransaction().add(mCreateReservationTaskFragment, TAG_CREATE_RESERVATION_TASK_FRAGMENT).commit();
        }*/

/*        mDeleteReservationTaskFragment = (DeleteReservationTaskFragment) fragmentManager.findFragmentByTag(TAG_DELETE_RESERVATION_TASK_FRAGMENT);
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mDeleteReservationTaskFragment == null) {
            mDeleteReservationTaskFragment = new DeleteReservationTaskFragment(TAG_DELETE_RESERVATION_TASK_FRAGMENT);
            fragmentManager.beginTransaction().add(mDeleteReservationTaskFragment, TAG_DELETE_RESERVATION_TASK_FRAGMENT).commit();
        }*/

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

    private void startServerSynchronizing() {
        // When testing on Shared network GCM rarely works. Update immediately.
        Intent syncDatabaseIntent = new Intent(this, SyncDatabaseIntentService.class);
        syncDatabaseIntent.putExtra(SyncDatabaseIntentService.START_SYNC, 1);
        startService(syncDatabaseIntent);
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
                    // When testing on Shared network GCM rarely works. Update immediately.
                    startServerSynchronizing();

                }
                break;
            case R.id.createReservationTaskFragment:
                // When testing on Shared network GCM rarely works. Update immediately.
                Intent syncDatabaseIntent = new Intent(this, SyncDatabaseIntentService.class);
                syncDatabaseIntent.putExtra(SyncDatabaseIntentService.START_SYNC, 1);
                startService(syncDatabaseIntent);
                fragmentMessage = mCreateReservationTaskFragment.getResult();
                if (fragmentMessage == null) {
                    // successfully created a reservation
                    // When testing on Shared network GCM rarely works. Update immediately.
                    startServerSynchronizing();
                }
                bookDetailFragment.onReservationTaskFinished(fragmentMessage);
                break;
        }
    }


    public void onStartDeleteReservationTask(int reservationId) {
        mDeleteReservationTaskFragment.setReservationId(reservationId);
        mDeleteReservationTaskFragment.start();
    }
}
