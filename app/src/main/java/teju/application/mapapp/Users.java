package teju.application.mapapp;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Users extends AppCompatActivity implements OnMapReadyCallback {

    // Google Map
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Button b1, b2;
    GoogleMap googleMap;
    FirebaseAuth auth;
    Location mLastLocation;
    private Spinner spinner;
    double lat = 0, lng = 0;
    LocationRequest mLocationRequest;
    DatabaseReference databaseReference;
    GoogleApiClient googleApiClient;
    FusedLocationProviderClient mFusedLocationClient;
    String userId;
    String Tag = "Users";
    public static final int REQUEST_LOCATION = 001;
    LocationSettingsRequest.Builder builder;
    PendingResult<LocationSettingsResult> pendingResult;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_map);

        try {

            auth = FirebaseAuth.getInstance();
            DatabaseReference refu= FirebaseDatabase.getInstance().getReference();
            refu=refu.child("Users").child(auth.toString());
            if (auth == null || !refu.getKey().equals(auth.toString())) {
                Toast.makeText(this, "no auth please login", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Users.this, Login_first.class);
                startActivity(intent);
                Toast.makeText(this, "No auth please login after intent", Toast.LENGTH_SHORT).show();
                finish();
            }
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();



            checkLocationPermission();


            b1 = findViewById(R.id.button2);
            b2= findViewById(R.id.button4);

            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Toast.makeText(Users.this, "logout succesfully after intent", Toast.LENGTH_SHORT).show();
                        stopLocationUpdates();
                        auth.signOut();
                        finish();
                        //new activity loginfirst
                        Intent intent = new Intent(Users.this, Login_first.class);
                        startActivity(intent);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


            //firebase database variable
            //firebase dataabase refrernce to the tables present
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");


            initializemap();
            spinner= findViewById(R.id.spinner2);
            //spinner values
            final DatabaseReference refvalue = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("Locations");
            refvalue.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Is better to use a List, because you don't know the size
                    // of the iterator returned by dataSnapshot.getChildren() to
                    // initialize the array
                    final List<String> areas = new ArrayList<String>();

                    for (DataSnapshot areaSnapshot: dataSnapshot.getChildren()) {
                        String areaName = areaSnapshot.getValue(String.class);
                        areas.add(areaName);
                    }

                    Spinner areaSpinner = (Spinner) findViewById(R.id.spinner);
                    ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(Users.this, android.R.layout.simple_spinner_item, areas);
                    areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    areaSpinner.setAdapter(areasAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                    // TODO Auto-generated method stub
                    DatabaseReference refr=refvalue.child(spinner.getSelectedItem().toString());
                    Toast.makeText(Users.this, refr.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });




        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    protected void initializemap() {


        if (googleMap == null) {


            FragmentManager myFragmentManager = getSupportFragmentManager();
            ((SupportMapFragment) myFragmentManager.findFragmentById(R.id.map)).getMapAsync(Users.this);

        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            stopLocationUpdates();
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * function to load map. If map is not created it will create it for you
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations, this can be null.
                                    if (location != null) {
                                        mLastLocation = location;
                                        lat = mLastLocation.getLatitude();
                                        lng = mLastLocation.getLongitude();
                                        LatLng latLng = new LatLng(lat, lng);
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));


                                    }
                                }
                            });
                }
                else {
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                }
                updateLocationUI();
            }

        }

    }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (ActivityCompat.checkSelfPermission(Users.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                Toast.makeText(this, "updatelocationui if", Toast.LENGTH_SHORT).show();
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastLocation = null;
                checkLocationPermission();
                Toast.makeText(this, "Updatelocationui else", Toast.LENGTH_SHORT).show();
            }
        } catch(SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mFusedLocationClient != null) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {


        checkLocationPermission();
        if (ActivityCompat.checkSelfPermission(Users.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            onMapReady(googleMap);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
        }


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap mMap) {
        try {
            this.googleMap = mMap;
            Log.d(Tag, "Inside the onmapready");



            googleMap.setMyLocationEnabled(true);

            if (mLastLocation != null) {
                lat = mLastLocation.getLatitude();
                lng = mLastLocation.getLongitude();
            }


            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mLocationRequest.setInterval(3 * 1000);
            builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            pendingResult = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());


            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations, this can be null.
                            if (location != null) {
                                Toast.makeText(Users.this, "Hiii onsucceslistener", Toast.LENGTH_SHORT).show();
                                mLastLocation = location;
                                lat = mLastLocation.getLatitude();
                                lng = mLastLocation.getLongitude();
                                LatLng latLng = new LatLng(lat, lng);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                            }
                        }
                    });
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }




    com.google.android.gms.location.LocationCallback mLocationCallback = new com.google.android.gms.location.LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    Log.d("MapApp", "Got Location");
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


                    Toast.makeText(Users.this, "Hiii locationresultcallback", Toast.LENGTH_SHORT).show();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("New Marker"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users/Locations");
                    ref = ref.child("Locations");
                    ref.child(userId).child(Date()).child(Time()).child("Latitudeonlresult").setValue(location.getLatitude());
                    ref.child(userId).child(Date()).child(Time()).child("Longitudeonlresult").setValue(location.getLongitude());


                }

            }
        }
    };

    public String Date() {
        Date currentDate = new Date();
        return currentDate.toString();
    }

    public String Time() {
        Time currentTime = new Time();
        currentTime.setToNow();
        return currentTime.toString();
    }

    public void checkLocationPermission() {

        if(!isGPSEnabled()) enableGPS();

        if(ActivityCompat.checkSelfPermission(Users.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(Users.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);


        }
        else
        {

        }
    }


    public boolean isGPSEnabled () {

        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        return provider!=null;
    }
    private void enableGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }
}

