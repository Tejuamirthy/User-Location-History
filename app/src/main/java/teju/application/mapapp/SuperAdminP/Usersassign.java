package teju.application.mapapp.SuperAdminP;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import teju.application.mapapp.R;

public class Usersassign extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    Spinner spinneradmin;
    String Tag="Usersassign",normaladminuid=null;
    ListView lv;
    ArrayAdapter<String> adapternames;
    List<User> userArrayList;
    InViewTtems usersAdapter;
    List<String> news;
    DatabaseReference refnamesvalue,assignedreference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usersassign);
        lv=findViewById(R.id.listview);
        spinneradmin=findViewById(R.id.adminassign);
        refnamesvalue = FirebaseDatabase.getInstance().getReference().child("NormalAdmin");
    try {
    final ArrayList<String> newnameslist = new ArrayList<>();
    refnamesvalue.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot){
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                Log.d(Tag, child.getKey() + " spinnernames ondatachange");
                try {
                    newnameslist.add((child.child("username")).getValue().toString());
                }catch (Exception e){e.printStackTrace();}
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
    spinneradmin.setAdapter(adapternames);

    spinneradmin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
           Query q= refnamesvalue.orderByChild("username").equalTo(spinneradmin.getSelectedItem().toString());
           q.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    normaladminuid=null;
                    for(DataSnapshot datasnapchild: dataSnapshot.getChildren()){
                        if(normaladminuid==null) {
                            normaladminuid = datasnapchild.getKey();
                            Log.d(Tag, spinneradmin.getSelectedItem().toString() + " NormalAdmin's uid is " + datasnapchild.getKey());
                            assignedreference = refnamesvalue.child(normaladminuid).child("assignedusers");
                        }
                    }
                    lv.setAdapter(null);
                    displayUserList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }

    });
    }catch (Exception e){e.printStackTrace();}

    }

    private void displayUserList() {



        refnamesvalue.orderByChild("username").equalTo(spinneradmin.getSelectedItem().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshotinsidefortrue) {
                userArrayList=new ArrayList<>();
                news=new ArrayList<>();
                lv.setAdapter(null);
                for(DataSnapshot d:dataSnapshotinsidefortrue.getChildren()) {
                    d = d.child("assignedusers");
                    for (DataSnapshot childds : d.getChildren()) {
                        userArrayList.add(new User(childds.getKey(), true));
                        news.add(childds.getKey());
                        Log.d(Tag, childds.getKey());
                    }
                    DatabaseReference userlistref = FirebaseDatabase.getInstance().getReference().child("Users");
                    userlistref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                final String areaId = ds.child("username").getValue(String.class);
                                Log.d(Tag,"before if "+news);
                                if (!news.contains(areaId)){
                                    userArrayList.add(new User(areaId));
                                    news.add(areaId);
                                    //and so on
                                    Log.d(Tag,"inside if "+news);
                                    Log.d(Tag, areaId + " / inside creating true ticks ");
                                }
                            }
                            usersAdapter = new InViewTtems(userArrayList, Usersassign.this);
                            lv.setAdapter(usersAdapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(Tag," oncancelled bring userarraylist here remaining");

            }
        });


        //retrieve data from the database and assign

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int pos=lv.getPositionForView(compoundButton);

        if(pos!= ListView.INVALID_POSITION){
            User u=userArrayList.get(pos);
            u.setSelected(b);
            if(b){
                assignedreference.child(u.getUsername()).setValue("");
                Log.d(Tag,u.getUsername()+" added to the assigned to the "+normaladminuid);
            }else{
                assignedreference.child(u.getUsername()).removeValue();
                Log.d(Tag,u.getUsername()+" is removed from assign  "+normaladminuid);
            }
        }
    }
}
