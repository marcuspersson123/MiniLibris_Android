package me.webbdev.minilibris;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by marcusssd on 2014-03-31.
 */
public class SyncDatabaseIntentService extends IntentService {

    private String url = "http://minilibris.webbdev.me/minilibris/api/books";

    public SyncDatabaseIntentService() {
        super("Sync database service");
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        InputStream inputStream = null;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.e("InputStream", e.getLocalizedMessage());
        }




        this.getContentResolver().delete(MiniLibrisContract.Books.CONTENT_URI, null, null);




        try
        {
            long startTime = System.currentTimeMillis();
            JSONObject jsonobject = new JSONObject(result);
            JSONArray jarray = jsonobject.getJSONArray("books");
            for(int i=0;i<jarray.length();i++)
            {
                JSONObject jb = (JSONObject)jarray.get(i);
                //Book book = new Book();
                int book_id = jb.getInt(MiniLibrisContract.Books.BOOK_ID);
                String title = jb.getString(MiniLibrisContract.Books.TITLE);
                String author = jb.getString(MiniLibrisContract.Books.AUTHOR);
                String publisher = jb.getString(MiniLibrisContract.Books.PUBLISHER);
                int year = jb.getInt(MiniLibrisContract.Books.YEAR);
                int category_id = jb.getInt(MiniLibrisContract.Books.CATEGORY_ID);

                ContentValues values = new ContentValues();
                values.put(MiniLibrisContract.Books.BOOK_ID, book_id);
                values.put(MiniLibrisContract.Books.TITLE, title);
                values.put(MiniLibrisContract.Books.AUTHOR, author);
                values.put(MiniLibrisContract.Books.PUBLISHER, publisher);
                values.put(MiniLibrisContract.Books.YEAR, year);
                values.put(MiniLibrisContract.Books.CATEGORY_ID, category_id);
                Uri todoUri = this.getContentResolver().insert(MiniLibrisContract.Books.CONTENT_URI, values);
                //booksDataSource.createBook(book);
            }
            String[] projection = MiniLibrisContract.Books.ALL_FIELDS;
            Cursor cursor = this.getContentResolver().query(MiniLibrisContract.Books.CONTENT_URI, projection, null, null, null);
            // books = booksDataSource.getAllBooks();
            long endTime = System.currentTimeMillis();
            int i=0;
            i++;
        }catch(Exception e)
        {
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
