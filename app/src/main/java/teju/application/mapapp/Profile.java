package teju.application.mapapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Profile extends AppCompatActivity implements View.OnClickListener{
    EditText name,email,username,companyid,phone;
    String userId,usertype;
    Button editprofile,updatephoto,save;
    private ImageView imageView;
    SharedPreferences sharedPreferences;
    String Tag="Profile";

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        userId =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent= getIntent();
        usertype=intent.getStringExtra("usertype");
        SharedPreferences sharedPreferences = getSharedPreferences("mapapp", Context.MODE_PRIVATE);
        usertype = sharedPreferences.getString("usertype","");
        Log.d(Tag,usertype+" this is the usertype");
        final DatabaseReference refvalue = FirebaseDatabase.getInstance().getReference().child(usertype).child(userId);

        final Map<String,String> map= new HashMap<>();
        refvalue.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap:dataSnapshot.getChildren()){
                if(snap.getKey().equals("username"))    username.setText(snap.getValue().toString());
                }
                username.setEnabled(false);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference kr= refvalue.child("Profile");
        kr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    if(child.getValue() != null) map.put(child.getKey(),child.getValue().toString());
                    else map.put(child.getKey(),"");
                    Log.d(Tag,child.getKey() +" key and value "+child.getValue().toString());
                    switch (child.getKey()){
                        case "name":
                            Log.d(Tag,map.get("name")+" assigning name");
                            name.setText(map.get("name"));
                            name.setEnabled(false);
                            break;
                        case "email":
                            Log.d(Tag,map.get("email")+" assigning email");
                            email.setText(map.get("email"));
                            email.setEnabled(false);
                            break;
                        case "companyid":
                            Log.d(Tag,map.get("companyid")+" assigning companyid");
                            companyid.setText(map.get("companyid"));
                            companyid.setEnabled(false);
                            break;
                        case "phone":
                            Log.d(Tag,map.get("phone")+" assigning phone");
                            phone.setText(map.get("phone"));
                            phone.setEnabled(false);
                            break;


                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.d("Profile", "onCancelled", firebaseError.toException());
            }
        });
        //storage references
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        //name of that person
        name=findViewById(R.id.editText3);

        //username he/she uses
        username=findViewById(R.id.editText4);

        //emailid he/she uses
        email=findViewById(R.id.editText5);

        //comapny id of that person
        companyid=findViewById(R.id.editText6);

        //phone number he uses
        phone=findViewById(R.id.editText7);

        //imageid
        imageView=findViewById(R.id.imageView);
        editprofile=findViewById(R.id.button5);
        editprofile.setOnClickListener(this);
        //for the updating photo
        updatephoto=findViewById(R.id.button6);
        updatephoto.setBackground(null);
        updatephoto.setVisibility(View.GONE);
        updatephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        photoupdate();
        //save button initialisation
        save=findViewById(R.id.savebutton);
        save.setVisibility(View.GONE);


    }
    void photoupdate(){
        StorageReference ref = storageReference.child("images").child(usertype).child(userId+".jpg");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Toast.makeText(getApplicationContext(),"updated photo from firebase",Toast.LENGTH_SHORT).show();
                Picasso.with(Profile.this).load(uri.toString()).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // File not found
                Toast.makeText(getApplicationContext(),"Please upload a photo",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    public void onClick(View view) {
        final String Name,Username,Email,CompanyId,Phone;
        Name = name.getText().toString();
        Username =username.getText().toString();
        Email=email.getText().toString();
        CompanyId=companyid.getText().toString();
        Phone=phone.getText().toString();
        name.setEnabled(true);
        phone.setEnabled(true);
        updatephoto.setVisibility(View.VISIBLE);
        editprofile.setVisibility(View.GONE);
        save.setVisibility(View.VISIBLE);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference refvalue = FirebaseDatabase.getInstance().getReference().child(usertype).child(userId).child("Profile");

                if(!Name.equals(name.getText().toString())) refvalue.child("name").setValue(name.getText().toString());
                if(!Username.equals(username.getText().toString())) {
                    final DatabaseReference rootpRef = FirebaseDatabase.getInstance().getReference().child(usertype);
                    Log.d(Tag,rootpRef.toString());
                    Query filterQuery = rootpRef.orderByChild("username").equalTo(username.getText().toString());
                    Log.d(Tag,filterQuery.toString()+" the query used inside onclicklistener");
                    filterQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.child(username.getText().toString()).exists()){
                                rootpRef.child(userId).child("username").setValue(username.getText().toString());
                                Log.d(Tag,"username updated as no matching username");
                            }else{
                                Log.d(Tag,"username tried to duplicate shown alert dialog");
                                AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
                                alertDialog.setTitle("Alert");
                                alertDialog.setMessage("This username exists, use other username");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
                if(!Email.equals(email.getText().toString())) refvalue.child("email").setValue(email.getText().toString());
                if(!CompanyId.equals(companyid.getText().toString())) refvalue.child("companyid").setValue(companyid.getText().toString());
                if(!Phone.equals(phone.getText().toString())) refvalue.child("phone").setValue(phone.getText().toString());

                Intent intentsave= new Intent(Profile.this,Profile.class);
                startActivity(intentsave);
                finish();

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                if(filePath != null)
                {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();
                    StorageMetadata metadata = new StorageMetadata.Builder()
                            .setContentType("image/jpg")
                            .build();
                    final StorageReference ref = storageReference.child("images/"+usertype+"/"+userId+".jpg");
                    ref.putFile(filePath,metadata)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Profile.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                    photoupdate();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Profile.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded "+(int)progress+"%");
                                }
                            });
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
