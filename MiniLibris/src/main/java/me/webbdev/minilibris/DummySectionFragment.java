package me.webbdev.minilibris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DummySectionFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>  {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    public SimpleCursorAdapter adapter;
    private Context mContext;

    public DummySectionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main_dummy,
                container, false);

        new HttpAsyncTask(this.getActivity().getApplicationContext()).execute("http://minilibris.webbdev.me/minilibris/api/books");
        return rootView;
    }
    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        mContext = this.getActivity().getApplicationContext();
        fillData();
    }

    private void fillData() {

        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[] { MiniLibrisContract.Books.TITLE };
        int[] to = new int[] { android.R.id.text1};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(mContext,R.layout.books_list_item, null, from, to, 0);

        setListAdapter(adapter);
    }

    // creates a new loader after the initLoader () call
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = MiniLibrisContract.Books.ALL_FIELDS;
        CursorLoader cursorLoader = new CursorLoader( mContext,
                MiniLibrisContract.Books.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        private Context context;

        public HttpAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... urls) {

            String url = urls[0];
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
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            // lägga som IntentService?
            
           // List<Book> books = new ArrayList<Book>();
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
                    Uri todoUri = this.context.getContentResolver().insert(MiniLibrisContract.Books.CONTENT_URI, values);
                    //booksDataSource.createBook(book);
                }
                String[] projection = MiniLibrisContract.Books.ALL_FIELDS;
                Cursor cursor = this.context.getContentResolver().query(MiniLibrisContract.Books.CONTENT_URI, projection, null, null, null);
                // books = booksDataSource.getAllBooks();
                long endTime = System.currentTimeMillis();
                int i=0;
                i++;
            }catch(Exception e)
            {
            }

        }
    }
}
