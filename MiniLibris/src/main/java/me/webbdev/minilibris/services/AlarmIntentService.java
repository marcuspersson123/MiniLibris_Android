package me.webbdev.minilibris.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.database.MiniLibrisContract;
import me.webbdev.minilibris.ui.MainActivity;


public class AlarmIntentService extends IntentService {


    private static final int BOOK_WAITING_NOTIFICATION_ID = 1;
    private static final int BOOK_FORGOTTEN_NOTIFICATION_ID = 2;

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, AlarmIntentService.class);
        context.startService(intent);
    }

    // Removes the notification that was set at the beginning
    private void removeNotification(int notificationId) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    private int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Activity.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        return userId;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int waitingBooks = this.getWaitingBooks();
        if (waitingBooks == 0) {
            removeNotification(BOOK_WAITING_NOTIFICATION_ID);
        } else if (waitingBooks == 1) {
            showNotification(BOOK_WAITING_NOTIFICATION_ID, "Du har en bok att hämta!");
        } else {
            showNotification(BOOK_WAITING_NOTIFICATION_ID, "Du har " + waitingBooks + " böcker att hämta!");
        }

        int forgottenBooks = this.getForgottenBooks();
        if (forgottenBooks == 0) {
            removeNotification(BOOK_FORGOTTEN_NOTIFICATION_ID);
        } else if (forgottenBooks == 1) {
            showNotification(BOOK_FORGOTTEN_NOTIFICATION_ID, "En bok måste lämnas!");
        } else {
            showNotification(BOOK_FORGOTTEN_NOTIFICATION_ID, forgottenBooks + " böcker att lämna tillbaka!");
        }
    }

    // get reserved books that are waiting to get picked up by this user.
    private int getWaitingBooks() {
        Date date = new Date();
        String todayString = new SimpleDateFormat("yyyy-MM-dd").format(date);
        Uri allUri = MiniLibrisContract.Reservations.CONTENT_URI;
        Cursor waitingBooksCursor = this.getApplicationContext().getContentResolver().query(allUri, new String[]{MiniLibrisContract.Reservations.BOOK_ID}, MiniLibrisContract.Reservations.BEGINS + "<=? and " + MiniLibrisContract.Reservations.ENDS + ">=? and " + MiniLibrisContract.Reservations.IS_LENT + "=? and " + MiniLibrisContract.Reservations.USER_ID + "=?", new String[]{todayString, todayString, "0", String.valueOf(getUserId())}, null);
        int waitingBooks = waitingBooksCursor.getCount();
        waitingBooksCursor.close();
        return waitingBooks;
    }

    // get reserved books that are waiting to get picked up by this user.
    private int getForgottenBooks() {
        Date date = new Date();
        String todayString = new SimpleDateFormat("yyyy-MM-dd").format(date);
        Uri allUri = MiniLibrisContract.Reservations.CONTENT_URI;
        Cursor cursor = this.getApplicationContext().getContentResolver().query(allUri, new String[]{MiniLibrisContract.Reservations.BOOK_ID}, MiniLibrisContract.Reservations.ENDS + "<? and " + MiniLibrisContract.Reservations.IS_LENT + "=? and " + MiniLibrisContract.Reservations.USER_ID + "=?", new String[]{todayString, "1", String.valueOf(getUserId())}, null);
        int booksCount = cursor.getCount();
        cursor.close();
        return booksCount;
    }

    private void showNotification(int notificationId, String msg) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext())
                        .setContentTitle("MiniLibris")
                        .setContentText(msg)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_stat_book)
                        .setSmallIcon(R.drawable.ic_stat_book)
                        .setContentIntent(contentIntent);


        Notification notification = mBuilder.build();

        notificationManager.notify(notificationId, notification);

    }

}
