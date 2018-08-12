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
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenMap extends AppCompatActivity implements OnMapReadyCallback {

    // Google Map
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Button b1, b2;
    GoogleMap googleMap;
    FirebaseAuth auth;
    Location mLastLocation;
    double lat = 0, lng = 0;
    Spinner spinner;
    LocationRequest mLocationRequest;
    DatabaseReference databaseReference;
    GoogleApiClient googleApiClient;
    FusedLocationProviderClient mFusedLocationClient;
    String userId,time=null;
    LatLng inside=null;
    ArrayAdapter<String> adapter;
    String Tag = "OpenMap";
    public static final int REQUEST_LOCATION = 001;
    LocationSettingsRequest.Builder builder;
    PendingResult<LocationSettingsResult> pendingResult;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_map);

        try {

            auth = FirebaseAuth.getInstance();
            if (auth == null) {
                Toast.makeText(this, "no auth please login", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OpenMap.this, Login_first.class);
                startActivity(intent);
                Toast.makeText(this, "No auth please login after intent", Toast.LENGTH_SHORT).show();
            }
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();




            if(!checkLocationPermission()) requestpermission();


            b1 = findViewById(R.id.button2);
            b2= findViewById(R.id.button4);

            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Toast.makeText(OpenMap.this, "logout succesfully after intent", Toast.LENGTH_SHORT).show();
                        stopLocationUpdates();
                        auth.signOut();
                        finish();
                        //new activity loginfirst
                        Intent intent = new Intent(OpenMap.this, Login_first.class);
                        startActivity(intent);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            b2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Toast.makeText(OpenMap.this, "MyProfileOpened", Toast.LENGTH_SHORT).show();
                        //new activity loginfirst
                        Intent intent = new Intent(OpenMap.this, Profile.class);
                        startActivity(intent);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


            spinner= findViewById(R.id.spinner2);

            //spinner values
            final DatabaseReference refvalue = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("Locations");

            final ArrayList<String> newlist= new ArrayList<>();
            refvalue.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        Log.i(Tag, child.getKey());
                        newlist.add(child.getKey());
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e(Tag, "onCancelled", firebaseError.toException());
                }
            });

            adapter = new ArrayAdapter<>(getBaseContext(),android.R.layout.simple_list_item_1,newlist);
            adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            Log.d(Tag,"before  setonitem");
            /*
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                    // TODO Auto-generated method stub
                    String date;
                    date = arg0.getSelectedItem().toString();
                    Log.d(Tag," insideonitemselected");
                    Toast.makeText(OpenMap.this, date, Toast.LENGTH_SHORT).show();


                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                    Toast.makeText(OpenMap.this, "nothingisselectedspinner", Toast.LENGTH_SHORT).show();
                }

            });

*/
            Log.d(Tag,"after itemset");
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                    googleMap.clear();
                    // TODO Auto-generated method stub
                    final DatabaseReference today = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("Locations").child(spinner.getSelectedItem().toString());

                    final PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);


                    today.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            googleMap.clear();
                            int i=0;
                            Log.d(Tag,"inside ondatachange before for");
                            final PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                            for (DataSnapshot childr : dataSnapshot.getChildren()) {
                                try {
                                    if (inside == null) {
                                        inside = new LatLng(Double.valueOf(childr.child("Latitude").getValue().toString()), Double.valueOf(childr.child("Longitude").getValue().toString()));
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(inside));
                                        googleMap.addMarker(new MarkerOptions().position(inside).title(i+"st Marker"));
                                        i++;
                                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                                        options.add(inside);
                                    } else {
                                        i++;
                                        Log.d(Tag, inside.toString()+"inside for loop assigning options");
                                        inside = new LatLng(Double.valueOf(childr.child("Latitude").getValue().toString()), Double.valueOf(childr.child("Longitude").getValue().toString()));
                                        googleMap.addMarker(new MarkerOptions().position(inside).title(i+" Marker"));

                                        options.add(inside);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            inside=null;
                            Log.d(Tag,"assigining options to googlemap outside for");
                            googleMap.addPolyline(options);
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            Log.e(Tag, "onCancelled", firebaseError.toException());
                        }
                    });


                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

            //firebase database variable
            //firebase dataabase refrernce to the tables present
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");


            if (googleMap == null) {


                FragmentManager myFragmentManager = getSupportFragmentManager();
                Toast.makeText(this, "fragmentmanagersupportinitializemap", Toast.LENGTH_SHORT).show();
                ((SupportMapFragment) myFragmentManager.findFragmentById(R.id.map)).getMapAsync(OpenMap.this);

            }
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }


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
                    Log.d("Onrequestper","Inside the response method");
                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations, this can be null.
                                    if (location != null) {
                                        Toast.makeText(OpenMap.this, "Hiii onsucceslistener", Toast.LENGTH_SHORT).show();
                                        mLastLocation = location;
                                        lat = mLastLocation.getLatitude();
                                        lng = mLastLocation.getLongitude();
                                        LatLng latLng = new LatLng(lat, lng);
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                                        onMapReady(googleMap);


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
            if (checkLocationPermission()) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                Toast.makeText(this, "updatelocationui if", Toast.LENGTH_SHORT).show();
            } else {
                requestpermission();
                onMapReady(googleMap);
            }
        } catch (SecurityException e) {
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


        if (checkLocationPermission()) {

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
            mLocationRequest.setInterval(60 * 1000);
            mLocationRequest.setFastestInterval(1000);
            builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            pendingResult = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());


            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            try{
                            if (location ==null) {
                                Toast.makeText(OpenMap.this, "Hiii onsucceslistener", Toast.LENGTH_SHORT).show();
                                mLastLocation = location;
                                lat = mLastLocation.getLatitude();
                                lng = mLastLocation.getLongitude();
                                LatLng latLng = new LatLng(lat, lng);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                            }
                            // Got last known location. In some rare situations, this can be null.
                            else if (location.distanceTo(mLastLocation)>25) {
                                Toast.makeText(OpenMap.this, "Hiii onsucceslistener", Toast.LENGTH_SHORT).show();
                                mLastLocation = location;
                                lat = mLastLocation.getLatitude();
                                lng = mLastLocation.getLongitude();
                                LatLng latLng = new LatLng(lat, lng);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                            }
                            }catch (Exception e){
                                e.printStackTrace();
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
                if(mLastLocation==null){
                    Log.d("MapApp", "Got Location");
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


                    Toast.makeText(OpenMap.this, "Hiii locationresultcallback", Toast.LENGTH_SHORT).show();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("New Marker"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users/"+userId+"/Locations");
                    if(time==null || !time.equals(Time())) {
                        time = Time();
                        ref.child(Date()).child(Time()).child("Latitude").setValue(location.getLatitude());
                        ref.child(Date()).child(Time()).child("Longitude").setValue(location.getLongitude());
                    }

                }
                else if (getApplicationContext() != null && location.distanceTo(mLastLocation)>25) {
                    Log.d("MapApp", "Got Location");
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


                    Toast.makeText(OpenMap.this, "Hiii locationresultcallback", Toast.LENGTH_SHORT).show();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("New Marker"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users/"+userId+"/Locations");
                    if(time==null || !time.equals(Time())) {
                        time = Time();
                        ref.child(Date()).child(Time()).child("Latitude").setValue(location.getLatitude());
                        ref.child(Date()).child(Time()).child("Longitude").setValue(location.getLongitude());
                    }

                }

            }
        }
    };

    public String Date() {

         return new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());

    }

    public String Time() {
        return (new SimpleDateFormat("HH:mm").format(new Date()));

    }

    public void requestpermission(){
        if(!isGPSEnabled(this)){ enableGPS();}

        if(ActivityCompat.checkSelfPermission(OpenMap.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(OpenMap.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);


        }
    }
    public boolean checkLocationPermission() {
        if(!isGPSEnabled(this) || ActivityCompat.checkSelfPermission(OpenMap.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
                return false;

        return  true;
    }


    public boolean isGPSEnabled (Context mContext) {
        String locationProviders = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(locationProviders != null && !locationProviders.equals("")){
            Log.d(Tag,"isGPSEnabled method 'if'so enabled");
            return true;
        }
        return false;
    }

    private void enableGPS() {
        Intent gpsOptionsIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }


}

