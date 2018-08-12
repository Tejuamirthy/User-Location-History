package teju.application.mapapp.SuperAdminP;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import teju.application.mapapp.Login_first;
import teju.application.mapapp.Profile;
import teju.application.mapapp.R;

public class Userstrack extends AppCompatActivity implements OnMapReadyCallback{
    private FirebaseAuth auth;
    String userId,Tag="UsersTrack",uid;
    Spinner spinner,spinnernames;
    Button b1,b2;
    LatLng inside;
    Polyline polyline;
    DatabaseReference refvalue;
    ArrayAdapter<String> adapter,adapternames;
    GoogleApiClient googleApiClient;
    GoogleMap googleMap=null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normaladmin);

        auth = FirebaseAuth.getInstance();
        if (auth == null) {
            Toast.makeText(this, "no auth please login", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Userstrack.this, Login_first.class);
            startActivity(intent);
            Toast.makeText(this, "Not authorised please login again", Toast.LENGTH_SHORT).show();
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        b1=findViewById(R.id.naprofile);
        b2=findViewById(R.id.nasignout);
        b1.setVisibility(View.GONE);
        b2.setVisibility(View.GONE);
        if (googleMap == null) {
            FragmentManager myFragmentManager = getSupportFragmentManager();
            Toast.makeText(this, "fragmentmanagersupportinitializemap", Toast.LENGTH_SHORT).show();
            ((SupportMapFragment) myFragmentManager.findFragmentById(R.id.mapna)).getMapAsync(this);
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        spinnernames=findViewById(R.id.spinnernames);

        //spinner values
        final DatabaseReference refnamesvalue = FirebaseDatabase.getInstance().getReference().child("Users");

        final ArrayList<String> newnameslist = new ArrayList<>();
        refnamesvalue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d(Tag, child.getKey()+" spinnernames ondatachange");
                    newnameslist.add(child.child("username").getValue().toString());
                }
                adapternames.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d(Tag, "onCancelled spinnernames", firebaseError.toException());
            }
        });

        adapternames = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, newnameslist);
        adapternames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnernames.setAdapter(adapternames);
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
        spinnernames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                spinner = findViewById(R.id.spinnerdate);

                Log.d(Tag,userId);
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Users");
                Log.d(Tag,rootRef.toString());
                Query filterQuery = rootRef.orderByChild("username").equalTo(spinnernames.getSelectedItem().toString());
                Log.d(Tag,spinnernames.getSelectedItem().toString()+" "+" String into method");
                Log.d(Tag,filterQuery.toString()+" the query used");
                uid=null;
                filterQuery.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            Toast.makeText(Userstrack.this, "Databaseeventlistener na", Toast.LENGTH_LONG).show();

                            Log.d(Tag, dataSnapshot.getKey() + " " + dataSnapshot.getValue().toString() + "found key,value inside onDataChange findusername");
                            for(DataSnapshot datasnapchild: dataSnapshot.getChildren()){
                                if(uid==null) uid=datasnapchild.getKey();
                                Log.d(Tag,uid+" this is tracking users'suid");
                            }

                            refvalue = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Locations");
                            final ArrayList<String> newlist = new ArrayList<>();
                            refvalue.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        Log.d(Tag, child.getKey());
                                        newlist.add(child.getKey());
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError firebaseError) {
                                    Log.d(Tag, "onCancelled", firebaseError.toException());
                                }
                            });
                            adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, newlist);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                            if(!newlist.contains("No loc dates")){
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                                    // TODO Auto-generated method stub
                                    Log.d(Tag,uid+" before reference uid");
                                    final DatabaseReference today = refvalue.child(spinner.getSelectedItem().toString());
                                    Log.d(Tag,uid+" after reference uid");
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
                                            polyline=googleMap.addPolyline(options);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError firebaseError) {
                                            Log.d(Tag, "onCancelled", firebaseError.toException());
                                        }
                                    });
                                    Log.d(Tag,uid+" after null uid");

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> arg0) {
                                    // TODO Auto-generated method stub
                                }
                            });
                            }

                        }
                        else Log.d(Tag, "not found key,value inside onDataChange findusername");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(Tag, " inside oncancelled database error findusername");

                    }
                });

                //spinner values



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

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });






        //spinner values
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap =googleMap;
    }



}
