package com.atominize.googlemapsgoogleplacestrial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    public static final int PERMISSIONS_GRANTED = 4321;

    public static final String WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String COURSE_LOCATION= Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String FINE_LOCATION= Manifest.permission.ACCESS_FINE_LOCATION;
    public static final float DEFAULT_ZOOM = 15f;

    private EditText mSearchText;
    private Button mDone;
    private ImageView myLocation;

    private Boolean permissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSearchText = findViewById(R.id.etSearch);
        mDone = findViewById(R.id.btDone);
        myLocation = findViewById(R.id.ivGps);


        getPermissions();

    }

    private void init() {
        Log.d(TAG, "init: Initializing");

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();

                geoLocate();
            }
        });

        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
    }

    private void hideKeyboard() {
        Log.d(TAG, "hideKeyboard: Hiding Soft Keyboard");
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(MapActivity.this.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);

    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: GeoLocating");

        String searchString = mSearchText.getText().toString();

//        Toast.makeText(this, searchString, Toast.LENGTH_SHORT).show();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> addressList = new ArrayList<>();

        try {
            addressList = geocoder.getFromLocationName(searchString, 1);

        } catch (IOException e) {
            Log.d(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (addressList.size() > 0) {
            Address address = addressList.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            moveMapCamera(new LatLng(address.getLatitude(), address.getLongitude()),
                    DEFAULT_ZOOM, address.getAddressLine(0));
        }
//        mSearchText.setText("");
    }

    private void getLocation() {
        Log.d(TAG, "getLocation: getting the current location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (permissionsGranted) {

                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            moveMapCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM,
                                    "My Location");

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this,
                                    "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveMapCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveMapCamera: moving camera to: lat " + latLng.latitude + ", lng: " +
                latLng.longitude);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(markerOptions);
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getPermissions() {
        Log.d(TAG, "getPermissions: get permissions for map");
        String[] permissions = {WRITE_STORAGE, COURSE_LOCATION, FINE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = true;
                    initMap();
                } else {
                    ActivityCompat.requestPermissions(this,
                            permissions, PERMISSIONS_GRANTED);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions, PERMISSIONS_GRANTED);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions, PERMISSIONS_GRANTED);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        permissionsGranted = false;

        switch (requestCode) {
            case PERMISSIONS_GRANTED: {
                if (grantResults.length > 0) {
                    for (int permission : grantResults) {
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            permissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permissions failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permissions granted");
                    permissionsGranted = true;
                    
                    initMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (permissionsGranted) {
            getLocation();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }

    }
}
