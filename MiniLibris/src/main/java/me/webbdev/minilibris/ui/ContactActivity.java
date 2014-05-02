package me.webbdev.minilibris.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import me.webbdev.minilibris.R;

public class ContactActivity extends Activity {

    private static final String TAG_MAP_FRAGMENT = "TAG_MAP_FRAGMENT";
    private MapFragment mapFragment;
    private LatLng latLng;
    private boolean mapFragmentUnitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        FragmentManager fragmentManager = this.getFragmentManager();
        this.mapFragment = (MapFragment) fragmentManager.findFragmentByTag(TAG_MAP_FRAGMENT);
        this.latLng = new LatLng(55.600263, 13.004637);
        this.mapFragmentUnitialized =  (this.mapFragment == null);

        if (this.mapFragmentUnitialized) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(13)
                    .build();
            GoogleMapOptions googleMapOptions = new GoogleMapOptions();
            googleMapOptions.zoomGesturesEnabled(true);
            googleMapOptions.camera(cameraPosition);
            this.mapFragment = MapFragment.newInstance(googleMapOptions);
            this.mapFragment.setRetainInstance(true);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.mapFragmentPlaceHolder, mapFragment, TAG_MAP_FRAGMENT);
            fragmentTransaction.commit();
        } else {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.mapFragmentPlaceHolder, mapFragment, TAG_MAP_FRAGMENT);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mapFragmentUnitialized) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("MiniLibris");
            this.mapFragment.getMap().addMarker(markerOptions);
        }
    }
}
