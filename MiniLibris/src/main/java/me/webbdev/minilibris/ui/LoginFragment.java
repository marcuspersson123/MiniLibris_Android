package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;


import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import me.webbdev.minilibris.R;


public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
       new Mediator().initialize(view);
        return view;
    }

    private class Mediator  {
        private Button loginButton;

        private EditText usernameEditText;
        private EditText passwordEditText;

        public void initialize(View view) {
            this.loginButton = (Button) view.findViewById(R.id.loginButton);
            this.passwordEditText = (EditText) view.findViewById(R.id.passwordEditText);
            this.usernameEditText = (EditText) view.findViewById(R.id.usernameEditText);
            this.loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLoginButtonClick();
                }
            });
            this.usernameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    onUsernameChanged(editable.toString());
                }
            });
            this.passwordEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    onPasswordChanged(editable.toString());
                }
            });

            this.communicate();
        }


        private void onUsernameChanged(String s) {
            this.communicate();

        }

        private void onPasswordChanged(String s) {
            this.communicate();
        }

        private void communicate() {
            boolean hasUsername = false;
            boolean hasPassword = false;
            if (!this.usernameEditText.getText().toString().isEmpty()) {
                hasUsername = true;
            }
            if (!this.passwordEditText.getText().toString().isEmpty()) {
                hasPassword = true;
            }
            if (hasPassword && hasUsername) {
                this.loginButton.setEnabled(true);
            } else {
                this.loginButton.setEnabled(false);
            }
        }

        private void onLoginButtonClick() {
            String password = this.passwordEditText.getText().toString();
            String username = this.usernameEditText.getText().toString();
            ((LoginFragmentListener) getActivity()).startLogin(username,password);

        }



    }

    public interface LoginFragmentListener {
        public void startLogin(String username, String password);
    }



}
