package me.webbdev.minilibris.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.Toast;

import me.webbdev.minilibris.database.MiniLibrisContract;
import me.webbdev.minilibris.R;
import android.widget.*;

public class BooksListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,AdapterView.OnItemClickListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    public SimpleCursorAdapter adapter;
    private Context mContext;

    public BooksListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main_dummy,
                container, false);

        return rootView;
    }
    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        mContext = this.getActivity().getApplicationContext();
        ListView lv = (ListView) this.getListView();
        lv.setOnItemClickListener(this);
        fillData();
    }

    private void fillData() {
        String[] from = new String[] { MiniLibrisContract.Books.TITLE };
        int[] to = new int[] { android.R.id.text1};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(mContext,R.layout.books_list_item, null, from, to, 0);

        setListAdapter(adapter);
    }

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
        adapter.swapCursor(null);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this.mContext,BookDetailActivity.class);
        intent.putExtra("id", (int) id);
        startActivity(intent);
    }
}
