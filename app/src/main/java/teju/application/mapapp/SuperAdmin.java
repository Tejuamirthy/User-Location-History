package teju.application.mapapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import teju.application.mapapp.SuperAdminP.Createusers;
import teju.application.mapapp.SuperAdminP.Usersassign;
import teju.application.mapapp.SuperAdminP.Userstrack;

public class SuperAdmin extends AppCompatActivity{
    private FirebaseAuth auth;
    Button create,assign,saprofile,satrack,salogout;
    Fragment createfrag,assignfrag,saprofilefrag,satrackfrag;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superadmin);
        //assigning the buttons
        create=findViewById(R.id.createbutton);
        assign=findViewById(R.id.assignbutton);
        saprofile=findViewById(R.id.profilebutton);
        satrack=findViewById(R.id.trackbutton);
        salogout=findViewById(R.id.salogout);

        salogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast.makeText(SuperAdmin.this, "logout succesfully after intent", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    finishAffinity();
                    //new activity loginfirst
                    Intent intent = new Intent(SuperAdmin.this, Login_first.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SuperAdmin.this, Createusers.class));
            }
        });
        assign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SuperAdmin.this, Usersassign.class));
            }
        });
        satrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SuperAdmin.this, Userstrack.class));
            }
        });
        saprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SuperAdmin.this, Profile.class));
            }
        });
    }
}
