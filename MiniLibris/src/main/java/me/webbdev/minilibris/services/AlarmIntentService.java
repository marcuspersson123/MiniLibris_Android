package me.webbdev.minilibris.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import me.webbdev.minilibris.R;
import me.webbdev.minilibris.database.MiniLibrisContract;
import me.webbdev.minilibris.ui.MainActivity;


public class AlarmIntentService extends IntentService {


    private static final int BOOK_WAITING_NOTIFICATION_ID = 100;
    private static final int BOOK_FORGOTTEN_NOTIFICATION_ID = 200;

    public AlarmIntentService() {
        super("AlarmIntentService");
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, AlarmIntentService.class);
        context.startService(intent);
    }

    // Removes the notifications of a notification type
    private void removeNotifications(int notificationId) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        for (int notificationAdder = 0;notificationAdder<10;notificationAdder++) {
            notificationManager.cancel(notificationId+notificationAdder);
        }
    }

    private int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Activity.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);
        return userId;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        removeNotifications(BOOK_WAITING_NOTIFICATION_ID);
        removeNotifications(BOOK_FORGOTTEN_NOTIFICATION_ID);
        ArrayList<Integer> waitingBooks = this.getWaitingBooks();
        int notificationIdAdder;

        if (waitingBooks.isEmpty()) {

        } else {
            notificationIdAdder = 0;
            for (Iterator<Integer> waitingBooksIterator = waitingBooks.iterator(); waitingBooksIterator.hasNext();) {
                String title = getBookTitle(waitingBooksIterator.next());
                showNotification(BOOK_WAITING_NOTIFICATION_ID+notificationIdAdder, "Hämta bok", "\"" + title + "\"");
                notificationIdAdder++;
            }
        }

        ArrayList<Integer> forgottenBooks = this.getForgottenBooks();
        if (forgottenBooks.isEmpty()) {

        } else {
            notificationIdAdder = 0;
            for (Iterator<Integer> forgottenBooksIterator = forgottenBooks.iterator(); forgottenBooksIterator.hasNext();) {
                String title = getBookTitle(forgottenBooksIterator.next());
                showNotification(BOOK_FORGOTTEN_NOTIFICATION_ID+notificationIdAdder, "Försenad bok", "\"" + title + "\"");
                notificationIdAdder++;
            }

        }
    }

    private String getBookTitle(int bookId) {
        String title = "";
        Uri getBookUri = ContentUris.withAppendedId(MiniLibrisContract.Books.CONTENT_URI, bookId);
        Cursor bookCursor = getApplicationContext().getContentResolver().query(getBookUri, MiniLibrisContract.Books.ALL_FIELDS, null, null, null);
        if (bookCursor.getCount() > 0) {
            bookCursor.moveToFirst();
             title = bookCursor.getString(bookCursor.getColumnIndex(MiniLibrisContract.Books.TITLE));
        }
        bookCursor.close();
        return title;
    }

    // get reserved books that are waiting to get picked up by this user.
    private ArrayList<Integer> getWaitingBooks() {
        ArrayList<Integer> books = new ArrayList<Integer>();
        Date date = new Date();
        String todayString = new SimpleDateFormat("yyyy-MM-dd").format(date);
        Uri allUri = MiniLibrisContract.Reservations.CONTENT_URI;
        Cursor waitingBooksCursor = this.getApplicationContext().getContentResolver().query(allUri, new String[]{MiniLibrisContract.Reservations.BOOK_ID}, MiniLibrisContract.Reservations.BEGINS + "<=? and " + MiniLibrisContract.Reservations.ENDS + ">=? and " + MiniLibrisContract.Reservations.IS_LENT + "=? and " + MiniLibrisContract.Reservations.USER_ID + "=?", new String[]{todayString, todayString, "0", String.valueOf(getUserId())}, null);
        while (waitingBooksCursor.moveToNext()) {
            int bookId = waitingBooksCursor.getInt(waitingBooksCursor.getColumnIndex(MiniLibrisContract.Reservations.BOOK_ID));
            books.add(bookId);
        }
        //  int waitingBooks = waitingBooksCursor.getCount();
        waitingBooksCursor.close();
        return books;
    }

    // get reserved books that are waiting to get picked up by this user.
    private ArrayList<Integer> getForgottenBooks() {
        ArrayList<Integer> books = new ArrayList<Integer>();
        Date date = new Date();
        String todayString = new SimpleDateFormat("yyyy-MM-dd").format(date);
        Uri allUri = MiniLibrisContract.Reservations.CONTENT_URI;
        Cursor cursor = this.getApplicationContext().getContentResolver().query(allUri, new String[]{MiniLibrisContract.Reservations.BOOK_ID}, MiniLibrisContract.Reservations.ENDS + "<? and " + MiniLibrisContract.Reservations.IS_LENT + "=? and " + MiniLibrisContract.Reservations.USER_ID + "=?", new String[]{todayString, "1", String.valueOf(getUserId())}, null);
        //int booksCount = cursor.getCount();
        while (cursor.moveToNext()) {
            int bookId = cursor.getInt(cursor.getColumnIndex(MiniLibrisContract.Reservations.BOOK_ID));
            books.add(bookId);
        }
        cursor.close();
        return books;
    }

    private void showNotification(int notificationId, String title, String smallText) {
        NotificationManager notificationManager;
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext())
                        .setContentTitle(title)
                        .setContentText(smallText)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_stat_book)

                        .setContentIntent(contentIntent);

        Notification notification = mBuilder.build();

        notificationManager.notify(notificationId, notification);

    }

}
