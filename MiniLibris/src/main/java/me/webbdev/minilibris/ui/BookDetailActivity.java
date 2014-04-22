package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import me.webbdev.minilibris.R;

public class BookDetailActivity extends Activity implements ReservationTaskFragment.TaskCallback {

    private BookDetailFragment bookDetailFragment;
    private ReservationTaskFragment mTaskFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_detail);

        this.bookDetailFragment = (BookDetailFragment) this.getFragmentManager().findFragmentById(R.id.bookDetailFragment);
        FragmentManager fm = getFragmentManager();
        mTaskFragment = (ReservationTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new ReservationTaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
    }

    public void startReservationTask(long book_id, int year, int month, int day) {

        mTaskFragment.setBook_id(book_id);
        mTaskFragment.setYear(year);
        mTaskFragment.setMonth(month);
        mTaskFragment.setDay(day);
        mTaskFragment.start();

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
    public void onPreExecute() {
        bookDetailFragment.startingReservation();
    }

    @Override
    public void onProgressUpdate(int percent) {

    }

    @Override
    public void onCancelled() {
bookDetailFragment.reservationFailed();
    }

    @Override
    public void onPostExecute() {
bookDetailFragment.reservationTaskFinished(mTaskFragment.getResult());
    }

}
