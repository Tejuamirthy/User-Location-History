
package teju.application.mapapp;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.google.android.gms.internal.zzbco.NULL;

/**
 * Created by Teju on 04-04-2018.
 */

public class Login_first extends AppCompatActivity implements View.OnClickListener{


    private FirebaseAuth auth;
    private EditText email,password;
    private Button b1;
    private Spinner sp3;
    String usertype;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginfirst);

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("mapapp",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (auth.getCurrentUser() != null) {
            switch (sharedPreferences.getString("usertype","")){
                case "Users":
                    startActivity(new Intent(Login_first.this, OpenMap.class));
                    finish();
                    break;
                case "NormalAdmin":
                    startActivity(new Intent(Login_first.this, NormalAdmin.class));
                    finish();
                    break;
                case "SuperAdmin":
                    startActivity(new Intent(Login_first.this, SuperAdmin.class));
                    finish();
                    break;

            }
        }
        else {
            editor.putString("usertype", null);
            editor.apply();
        }

        email=findViewById(R.id.editText);
        password=findViewById(R.id.editText2);
        b1=findViewById(R.id.button3);
        b1.setOnClickListener(this);
        sp3 = findViewById(R.id.spinner);
        //spinner values
        ArrayAdapter<CharSequence> adp3 = ArrayAdapter.createFromResource(this,
                R.array.str2, android.R.layout.simple_list_item_1);

        adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp3.setAdapter(adp3);
        sp3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                usertype = sp3.getSelectedItem().toString();
                Toast.makeText(getApplicationContext(), usertype, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    @Override
    public void onClick(View view) {
      try {
          if (view == b1) {

              String emailid = email.getText().toString();
              final String passwordid = password.getText().toString();



              if (TextUtils.isEmpty(emailid) || TextUtils.isEmpty(passwordid) || usertype.equals(NULL)) {
                  Toast.makeText(getApplicationContext(), "Enter valid credentials", Toast.LENGTH_SHORT).show();
                  return;
              }



              //authenticate user
              auth.signInWithEmailAndPassword(emailid, passwordid)
                      .addOnCompleteListener(Login_first.this, new OnCompleteListener<AuthResult>() {
                          @Override
                          public void onComplete(@NonNull Task<AuthResult> task) {
                              // If sign in fails, display a message to the user. If sign in succeeds
                              // the auth state listener will be notified and logic to handle the
                              // signed in user can be handled in the listener.
                              if (!task.isSuccessful()) {
                                  // there was an error
                                  if (password.length() < 6) {
                                      password.setError("Enter password longer than 6 characteres");
                                  } else {
                                      Toast.makeText(Login_first.this, "Authentication failed", Toast.LENGTH_LONG).show();
                                  }
                              } else {
                                  DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                  rootRef=rootRef.child(usertype);
                                  ValueEventListener valueEventListener = new ValueEventListener() {
                                      @Override
                                      public void onDataChange(DataSnapshot dataSnapshot) {
                                          Toast.makeText(Login_first.this, "Databaseeventlistener", Toast.LENGTH_LONG).show();

                                              if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
                                                  //Do something
                                                  Toast.makeText(Login_first.this, "inside for", Toast.LENGTH_LONG).show();
                                                  editor.putString("usertype", dataSnapshot.getKey());
                                                  editor.putString("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                  editor.apply();

                                                  Log.d("Login",dataSnapshot.getKey());
                                                  switch (dataSnapshot.getKey()){
                                                      case "Users":
                                                          Toast.makeText(Login_first.this, "insideuserscase", Toast.LENGTH_LONG).show();
                                                          startActivity(new Intent(Login_first.this,OpenMap.class));
                                                          finish();
                                                          break;
                                                      case "NormalAdmin":
                                                          startActivity(new Intent(Login_first.this, NormalAdmin.class));
                                                          finish();
                                                          break;

                                                      case "SuperAdmin":
                                                          startActivity(new Intent(Login_first.this, SuperAdmin.class));
                                                          finish();
                                                          break;

                                                  }
                                              }

                                      }

                                      @Override
                                      public void onCancelled(DatabaseError databaseError) {}
                                  };
                                  rootRef.addListenerForSingleValueEvent(valueEventListener);
                              }
                          }
                      });

          }
      }catch(Exception e){
          e.printStackTrace();
      }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}

