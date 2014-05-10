package me.webbdev.minilibris.ui;


import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;


import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.services.DailyAlarmIntentService;
import me.webbdev.minilibris.services.DailyAlarmReceiver;
import me.webbdev.minilibris.services.SyncDatabaseIntentService;

public class MainActivity extends Activity implements BooksListFragment.BooksListFragmentListener, TaskFragment.TaskFragmentCallback {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "966357151127";  // (the project id in Google Cloud Messaging)
    GoogleCloudMessaging googleCloudMessaging;
    Context applicationContext;
    String registrationId;

    static final String TAG = "MainActivity";

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    // Overridden just to know if the user rotated the device in onCreate()
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("dummy", "dummy");
    }


    // On create
    // When the app is first started, synchronizes with the MiniLibris database.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int userId = getUserId();
        if (userId<0) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }
        if (savedInstanceState == null) {
            // App is just started.
            // Synchronize the database from beginning of time
            SyncDatabaseIntentService.start(this,true);
            DailyAlarmIntentService.start(this);


            // Make sure the daily alarm is setup
            setupAlarm();
        }

        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        applicationContext = getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
            registrationId = getRegistrationId(applicationContext);
            if (registrationId.isEmpty()) {
                registerInBackground();
            }
        } else {
            Toast.makeText(this,"Hittade inte Google play services",Toast.LENGTH_LONG).show();
        }
    }

    // Setup the scheduling of alarming the user of important messages.
    // It is OK to set the same Alarm several times if the PendingIntent is the same.
    private void setupAlarm() {
        Calendar updateTime = Calendar.getInstance();
        //updateTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        updateTime.set(Calendar.HOUR_OF_DAY, 8);
        updateTime.set(Calendar.MINUTE, 0);

        Intent downloader = new Intent(this, DailyAlarmReceiver.class);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(this,
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) getSystemService(
                Context.ALARM_SERVICE);
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                updateTime.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, recurringDownload);
    }

    private int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Activity.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        return userId;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * If result is empty, the app needs to register.
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            // The app version has been changed.
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (googleCloudMessaging == null) {
                        googleCloudMessaging = GoogleCloudMessaging.getInstance(applicationContext);
                    }
                    registrationId = googleCloudMessaging.register(SENDER_ID);
                    msg = "Telefonen är registrerad";

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(applicationContext, registrationId);
                } catch (IOException ex) {
                    msg = "Det gick inte att registrera";
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.e("Registrering:", msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        String registrationId = getRegistrationId(this);
        GoogleCloudMessagingTaskFragment googleCloudMessagingTaskFragment = (GoogleCloudMessagingTaskFragment) getFragmentManager().findFragmentById(R.id.googleCloudMessagingTaskFragment);
        googleCloudMessagingTaskFragment.setRegistrationId(registrationId);
        googleCloudMessagingTaskFragment.setUserId(getUserId());
        googleCloudMessagingTaskFragment.start();
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's applicationContext.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    // Inflate the menu; this adds items to the action bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // An a menu item was selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.action_synchronize:
                onSynchronizeAll();

                break;
            case R.id.action_contact:
                onShowContactActivity();
                break;
        }

        return true;
    }

    // The user issues a synchronize all event.
    // So, start synchronizing from beginning of time
    private void onSynchronizeAll() {
        SyncDatabaseIntentService.start(this, true);
    }

    // Show the Contact Activity
    private void onShowContactActivity() {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }


    // Event from BooksListFragment
    @Override
    public void onBookSelected(int id) {
        Intent intent = BookDetailActivity.createStartIntent(this, id, getUserId());
        startActivity(intent);
    }

    @Override
    public void onPreExecute(int fragmentId) {

    }

    @Override
    public void onProgressUpdate(int fragmentId, int percent) {

    }

    @Override
    public void onCancelled(int fragmentId) {

    }

    @Override
    public void onPostExecute(int fragmentId) {

    }

    /**
     * An inner class adapter that returns a fragment corresponding to
     * one of the sections.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // Returns a BooksListFragment that is set in a "mode" to display e.g. only lent books
        @Override
        public ListFragment getItem(int position) {
            BooksListFragment fragment = new BooksListFragment();
            Bundle bundle = null;
            switch (position) {
                case 0:
                    // show all books
                    bundle = BooksListFragment.createArgumentsBundle(BooksListFragment.ALL_BOOKS_MODE);
                    break;
                case 1:
                    // show reserved books
                    bundle = BooksListFragment.createArgumentsBundle(BooksListFragment.RESERVED_BOOKS_MODE);
                    break;
                case 2:
                    // books to fetch mode
                    bundle = BooksListFragment.createArgumentsBundle(BooksListFragment.BOOKS_TO_FETCH_MODE);
                    break;
                case 3:
                    // lent books mode
                    bundle = BooksListFragment.createArgumentsBundle(BooksListFragment.LENT_BOOKS_MODE);
                    break;
                case 4:
                    // books to return mode
                    bundle = BooksListFragment.createArgumentsBundle(BooksListFragment.BOOKS_TO_RETURN_MODE);
                    break;

            }
            fragment.setArguments(bundle);
            return (ListFragment) fragment;
        }

        // How many pages are there?
        @Override
        public int getCount() {
            return 5;
        }

        // Return the title of the page at the position
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.all_books).toUpperCase(l);
                case 1:
                    return getString(R.string.reserved_books).toUpperCase(l);
                case 2:
                    return getString(R.string.books_to_fetch).toUpperCase(l);
                case 3:
                    return getString(R.string.lent_books).toUpperCase(l);
                case 4:
                    return getString(R.string.to_return).toUpperCase(l);
            }
            return null;
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.e("minilibris", "play finns inte och skulle behöva hämtas");
                finish();
            } else {
                // device is not supported
                Log.e("minilibris", "play stöds inte på enheten)");
                finish();
            }
            return false;
        }
        return true;
    }

}

