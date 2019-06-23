package turnoutcom.d_sanchay.myapplication.Registration;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

import turnoutcom.d_sanchay.myapplication.Classroom.Classroom;
import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.R;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.SharedPrefManager;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.UserId;

public class FirstUserInfo extends AppCompatActivity {
    EditText username,name;

    final int RequestPermissionCode=0;
    CircularProgressButton progressButton;
    FirebaseFirestore firestore;
    FirebaseUser currentUser;
    DatabaseReference databaseReference;
    Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_user_info);
        progressButton = findViewById(R.id.setProfile);
        progressButton.setText("Proceed");
        progressButton.setTextColor(Color.WHITE);
        progressButton.setIndeterminateProgressMode(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        username = findViewById(R.id.username);
        name = findViewById(R.id.Name);
        connection = new Connection(this);
        firestore = FirebaseFirestore.getInstance();
        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(progressButton.getProgress() == -1){
                    progressButton.setProgress(0);
                    progressButton.setTextColor(Color.WHITE);
                    progressButton.setIdleText("Proceed Again.");
                }else if(progressButton.getProgress() == 0){
                    progressButton.setProgress(30);
                    final String userName = username.getText().toString().trim();
                    final String Name = name.getText().toString();
                    if(userName.isEmpty()  || Name.isEmpty()){
                        progressButton.setProgress(-1);
                        progressButton.setErrorText("Fill all the fields.");
                        progressButton.setTextColor(Color.WHITE);
                    }else {
                        RealtimeData(userName,Name);
                    }

                    }
                }
            });

        }


   public void RealtimeData(final String name,final String Name){
        final char firstLetter[] = name.toCharArray();
       final HashMap<String,Object> map = new HashMap<>();
        map.put("userId",currentUser.getUid());
        map.put("username",name);
        final HashMap<String,Object> collMap = new HashMap<>();
        collMap.put("username",name);
        collMap.put("Name",Name);
        collMap.put("joined",FieldValue.serverTimestamp());
        databaseReference = FirebaseDatabase.getInstance().getReference(String.valueOf(firstLetter[0]));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(name)){
                    String userId = dataSnapshot.child(name).child("userId").getValue().toString();
                    if(userId.equals(currentUser.getUid())){
                        databaseReference.child(name).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    firestore.collection("Users").document(currentUser.getUid()).set(collMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                UserId userId = new UserId(name,Name);
                                                SharedPrefManager.getmInstances(getApplicationContext()).setUser(userId);
                                                startActivity(new Intent(FirstUserInfo.this,Classroom.class));
                                                finish();
                                            }else {
                                                progressButton.setProgress(-1);
                                                progressButton.setErrorText("Please try Again!!");
                                                progressButton.setTextColor(Color.WHITE);
                                            }
                                        }
                                    });

                                }else{
                                    progressButton.setProgress(-1);
                                    progressButton.setErrorText("Please try Again!!");
                                    progressButton.setTextColor(Color.WHITE);
                                }
                            }
                        });
                    }else {
                    progressButton.setProgress(-1);
                    progressButton.setErrorText("Username Exists!!");
                    progressButton.setTextColor(Color.WHITE);}
                }else {
                    databaseReference.child(name).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                firestore.collection("Users").document(currentUser.getUid()).set(collMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            UserId userId = new UserId(name,Name);
                                            SharedPrefManager.getmInstances(getApplicationContext()).setUser(userId);
                                            startActivity(new Intent(FirstUserInfo.this,Classroom.class));
                                            finish();
                                        }else {
                                            progressButton.setProgress(-1);
                                            progressButton.setErrorText("Please try Again!!");
                                            progressButton.setTextColor(Color.WHITE);
                                        }
                                    }
                                });

                            }else{
                                progressButton.setProgress(-1);
                                progressButton.setErrorText("Please try Again!!");
                                progressButton.setTextColor(Color.WHITE);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressButton.setProgress(-1);
                progressButton.setErrorText("Operation Failed!!");
                progressButton.setTextColor(Color.WHITE);
            }
        });

   }


    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser==null){
            startActivity(new Intent(FirstUserInfo.this,Login.class));
        }
    }
}
