package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import me.webbdev.minilibris.R;

public class LoginActivity extends Activity implements TaskFragment.TaskFragmentCallback, LoginFragment.LoginFragmentListener, LoginDetailsFragment.LoginDetailsFragmentListener {

    private LoginTaskFragment loginTaskFragment;
    private LoginFragment loginFragment;
    private LoginDetailsFragment loginDetailsFragment;


    // Decide which fragment to show
    // If logged in, show the loginDetails fragment.
    // If not logged in show the login fragment.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.loginTaskFragment = (LoginTaskFragment) getFragmentManager().findFragmentById(R.id.loginTaskFragment);
        this.loginFragment = (LoginFragment) getFragmentManager().findFragmentById(R.id.loginFragment);
        this.loginDetailsFragment = (LoginDetailsFragment) getFragmentManager().findFragmentById(R.id.loginDetailsFragment);
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Activity.MODE_PRIVATE);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId >= 0) {
            transaction.hide(loginFragment);
        } else {
            transaction.hide(loginDetailsFragment);
        }
        transaction.commit();
    }

    // A login task was finished.
    @Override
    public void onPostExecute(int fragmentId) {
        String fragmentMessage;

        switch (fragmentId) {
            case R.id.loginTaskFragment:
                fragmentMessage = this.loginTaskFragment.getResult();
                if (fragmentMessage != null) {
                    // Failed
                    Toast.makeText(LoginActivity.this, fragmentMessage, Toast.LENGTH_LONG).show();
                } else {
                    // Successfully gotten user information from the server.
                    // Switch to the login details fragment.
                    loginDetailsFragment.getMediator().communicate();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.hide(loginFragment);
                    transaction.show(loginDetailsFragment);
                    transaction.commit();
                    Toast.makeText(LoginActivity.this, "Inloggad", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onPreExecute(int fragmentId) {
    }

    @Override
    public void onProgressUpdate(int fragmentId, int percent) {
    }

    // A login in task was cancelled.
    @Override
    public void onCancelled(int fragmentId) {
        switch (fragmentId) {
            case R.id.loginTaskFragment:
                Toast.makeText(LoginActivity.this, "Inloggningen avbröts", Toast.LENGTH_LONG).show();
                break;

        }
    }


    // Event from login fragment.
    // Start off a login task.
    @Override
    public void onStartLogin(String username, String password) {
        this.loginTaskFragment.setPassword(password);
        this.loginTaskFragment.setUsername(username);
        this.loginTaskFragment.start();
    }

    // Event from login details fragment.
    // The user has the rights and wants to go to the app.
    @Override
    public void onStartUsingApp() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    // Event from login details fragment.
    // Remove the stored user_id in shared preferences.
    // Show the login fragment.
    @Override
    public void onLogout() {
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences("user_info", Activity.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putInt("user_id", -1);
        sharedPreferencesEditor.commit();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.show(loginFragment);
        transaction.hide(loginDetailsFragment);
        transaction.commit();
    }
}
