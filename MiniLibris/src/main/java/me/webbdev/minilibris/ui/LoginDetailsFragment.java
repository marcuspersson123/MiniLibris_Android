package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;


import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import me.webbdev.minilibris.R;


public class LoginDetailsFragment extends Fragment {

    private Mediator mediator;

    public LoginDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_details, container, false);
        this.mediator = new Mediator();
        mediator.initialize(view);
        return view;
    }

    public Mediator getMediator() {
        return this.mediator;
    }

     class Mediator  {
        private Button logoutButton;
        private Button gotoAppButton;
private TextView usernameTextView;
        private TextView firstnameTextView;
        private TextView surnameTextView;
        private TextView addressTextView;
        private TextView phoneTextView;

        public void initialize(View view) {
            this.logoutButton = (Button) view.findViewById(R.id.logoutButton);
            this.gotoAppButton = (Button) view.findViewById(R.id.startButton);
            this.firstnameTextView = (TextView) view.findViewById(R.id.firstnameTextView);
            this.surnameTextView = (TextView) view.findViewById(R.id.surnameTextView);
            this.usernameTextView = (TextView) view.findViewById(R.id.usernameTextView);
            this.addressTextView = (TextView) view.findViewById(R.id.addressTextView);
            this.phoneTextView = (TextView) view.findViewById(R.id.phoneTextView);

            this.logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLogout();
                }
            });
            this.gotoAppButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onGotoApp();
                }
            });

            this.communicate();
        }

        private void onGotoApp() {
            ((LoginDetailsFragmentListener) getActivity()).onStartUsingApp();
        }

        private void onLogout() {
            ((LoginDetailsFragmentListener) getActivity()).onLogout();
        }

        public void communicate() {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_info", Activity.MODE_PRIVATE);
            usernameTextView.setText(sharedPreferences.getString("username",""));
firstnameTextView.setText(sharedPreferences.getString("first_name",""));
            surnameTextView.setText(sharedPreferences.getString("surname",""));
            addressTextView.setText(sharedPreferences.getString("address",""));
            phoneTextView.setText(sharedPreferences.getString("phone",""));
        }




    }

    public interface LoginDetailsFragmentListener {
        public void onStartUsingApp();
        public void onLogout();
    }



}
