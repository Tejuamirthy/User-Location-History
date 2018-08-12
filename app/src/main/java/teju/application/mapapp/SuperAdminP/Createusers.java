package teju.application.mapapp.SuperAdminP;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import teju.application.mapapp.R;

public class Createusers extends AppCompatActivity {

    Spinner spin;
    Boolean isexistsboolean;
    Button button;
    String usertype,Tag="CreateUsers";
    EditText username,companyid,email,password;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createusers);
        username=findViewById(R.id.createusername);
        companyid=findViewById(R.id.createcompanyId);
        email=findViewById(R.id.createemail);
        password=findViewById(R.id.createpassword);
        spin = findViewById(R.id.createspinner);
        //spinner values
        ArrayAdapter<CharSequence> adpapter = ArrayAdapter.createFromResource(Createusers.this,
                R.array.str2, android.R.layout.simple_list_item_1);

        adpapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adpapter);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                usertype =spin.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), "Creating a "+usertype , Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        button=findViewById(R.id.createbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(username!=null && email !=null && companyid != null && password != null){

                    final DatabaseReference forexists= FirebaseDatabase.getInstance().getReference().child(usertype);
                    forexists.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds:dataSnapshot.getChildren()){
                                if((ds.child("username").getValue())==(username) || (ds.child("Profile").child("companyid").getValue())==(companyid)) {
                                    isexistsboolean=false;
                                    break;
                                }
                                if(isexistsboolean == null) isexistsboolean=true;
                            }
                            if(isexistsboolean){
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                        .addOnCompleteListener(Createusers.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    Log.d(Tag, "createUserWithEmail:success");
                                                    FirebaseUser user = task.getResult().getUser();
                                                    final DatabaseReference creatingindb = FirebaseDatabase.getInstance().getReference().child(spin.getSelectedItem().toString()).child(user.getUid());
                                                    creatingindb.child("username").setValue(username.getText().toString());
                                                    creatingindb.child("Profile").child("companyid").setValue(companyid.getText().toString());
                                                    creatingindb.child("Profile").child("email").setValue(email.getText().toString());
                                                    Toast.makeText(Createusers.this, "Created successfully",
                                                            Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(Createusers.this,Createusers.class));
                                                    finish();

                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Log.w(Tag, "createUserWithEmail:failure", task.getException());
                                                    Toast.makeText(Createusers.this, "Authentication failed.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });


                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }else{
                    Toast.makeText(Createusers.this,"Please enter valid details",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}