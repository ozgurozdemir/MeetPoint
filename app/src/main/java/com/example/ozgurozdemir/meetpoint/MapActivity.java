package com.example.ozgurozdemir.meetpoint;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    /*
     *  This code written with respect to Mitch Tabian Tutorial of GoogleMapsAPI and GooglePlacesAPI
     *  https://github.com/mitchtabian/Google-Maps-Google-Places
     */

    // variable initialize
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean myLocationPermissionGranted = false;
    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168),
            new LatLng(71, 136));

    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;

    private AutoCompleteTextView mSearchText;
    private Button locationOKBtn;

    private String personID, personName, hour, date, note, location, locationLatLng;
    private ArrayList<String> participants;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().hide();

        // Initializing the intent in order to return Home activity again
        Intent intent = getIntent();
        personID = intent.getStringExtra("personID");
        personName = intent.getStringExtra("personName");
        hour = intent.getStringExtra("hour");
        date = intent.getStringExtra("date");
        note = intent.getStringExtra("note");
        location = intent.getStringExtra("location");
        locationLatLng = intent.getStringExtra("locationLatLng");
        participants = intent.getStringArrayListExtra("participants");

        // Getting permissions
        getLocationPermission();

        // Creating GoogleAPI client for using GooglePlacesAPI
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null);

        // Setup search bar for locations
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        // Setup select location button and it also returns to Home
        locationOKBtn = (Button) findViewById(R.id.locationOKBtn);
        locationOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapActivity.this, HomeActivity.class);
                i.putExtra("personID",personID);
                i.putExtra("personName", personName);
                i.putExtra("hour",hour);
                i.putExtra("date", date);
                i.putExtra("note",note);
                i.putExtra("location", location);
                i.putExtra("locationLatLng",locationLatLng);
                i.putExtra("participants", participants);
                i.putExtra("locationSelected", "true");
                startActivity(i);
                finish();
            }
        });
    }

    // Creating map if permission are granted
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(mMap != googleMap) {
            mMap = googleMap;
        }
        if (myLocationPermissionGranted) {
            // Checking the permission granted or not
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // If location is already selected it moves camera to location
            if(locationLatLng != null){
                String[] selected = locationLatLng.split(",");
                moveCamera(new LatLng(Double.parseDouble(selected[0]), Double.parseDouble(selected[1])), DEFAULT_ZOOM, location);
            }
            // Disabling the location button in order to use search bar for Google Places API
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }
    }

    // Moving camera to given location and given zoom and also create a marker with respect to
    // given title in order to understand which location are chosen
    private void moveCamera(LatLng latLng, float zoom, String title){

       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(options);
        hideSoftKeyboard();

    }

    // Initializing map
    public void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    // Getting permissions
    public void getLocationPermission(){
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If all permission are granted then initialize map
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                myLocationPermissionGranted = true;
                initMap();
            } else {
                // Otherwise request permission
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            // Otherwise request permission
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Respond of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        myLocationPermissionGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length> 0){
                    for (int i = 0; i < grantResults.length; i++){
                        // if permissions are not granted return nothing
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            myLocationPermissionGranted = false;
                            return;
                        }
                    }
                    myLocationPermissionGranted = true;
                    // initialize map if all permission granted
                    initMap();
                }
            }
        }
    }

    // Hiding the keyboard after the search in order to give decent view
    public void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Google Place API
    // Selecting the object from suggestions with respect to given search bar input
    public AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();
            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    // Respond of the selected location (moving camera and getting title and location)
    public ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                // Prevent to memory leak
                places.release();
                return;
            }
            final Place place = places.get(0);
            // Get lang and long and name
            location = place.getName().toString();
            locationLatLng = place.getViewport().getCenter().latitude + "," + place.getViewport().getCenter().longitude;
            // Move camera to selecting location
            moveCamera(new LatLng(place.getViewport().getCenter().latitude,place.getViewport().getCenter().longitude),
                    DEFAULT_ZOOM, place.getName().toString());
            // Prevent to memory leak
            places.release();
        }
    };
}
