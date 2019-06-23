package turnoutcom.d_sanchay.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import turnoutcom.d_sanchay.myapplication.Registration.Login;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.SharedPrefManager;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.UserId;

public class EditProfile extends AppCompatActivity {
EditText username,name;
CircularProgressButton progressButton;
Button password,logout;
String usernames = "", namess = "";
DatabaseReference databaseReference,reference;
FirebaseFirestore firestore;
FirebaseUser currentUser;
FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        username = findViewById(R.id.changeUsername);
        name = findViewById(R.id.changeName);
        progressButton = findViewById(R.id.changeProfile);
        password = findViewById(R.id.changePassword);
        logout = findViewById(R.id.logout);
        progressButton.setTextColor(Color.WHITE);
        progressButton.setText("Edit Profile");
        progressButton.setIndeterminateProgressMode(true);
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();

        try {
           usernames = SharedPrefManager.getmInstances(this).getUser().getUid();
            namess = SharedPrefManager.getmInstances(this).getUser().getName();
            username.setText(usernames);
            name.setText(namess);
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(progressButton.getProgress()==-1){
                    progressButton.setProgress(0);
                    progressButton.setIdleText("Edit Again");
                    progressButton.setTextColor(Color.WHITE);
                }else if(progressButton.getProgress()==0){
                    progressButton.setProgress(40);
                    final String user = username.getText().toString();
                    final String names = name.getText().toString();
                    final Map<String,Object> map = new HashMap<>();
                    map.put("username",user);
                    map.put("userId",currentUser.getUid());
                    if(!user.isEmpty() || !names.isEmpty()){
                        databaseReference = FirebaseDatabase.getInstance().getReference(String.valueOf(usernames.charAt(0))).child(usernames);
                        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    reference = FirebaseDatabase.getInstance().getReference(String.valueOf(user.charAt(0))).child(user);
                                    reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                        Map<String,Object> collMap = new HashMap<>();
                                        collMap.put("username",user);
                                        collMap.put("Name",names);
                                        firestore.collection("Users").document(currentUser.getUid()).update(collMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                UserId userId = new UserId(user,names);
                                                SharedPrefManager.getmInstances(getApplicationContext()).setUser(userId);
                                                progressButton.setProgress(100);
                                                progressButton.setCompleteText("Profile Updated");
                                                progressButton.setTextColor(Color.WHITE);
                                            }
                                            }
                                        });
                                        }
                                    });
                                }else {
                                    progressButton.setProgress(-1);
                                    progressButton.setErrorText("Operation Unsuccessful.");
                                    progressButton.setTextColor(Color.WHITE);
                                }
                            }
                        });

                    }else {
                        progressButton.setProgress(-1);
                        progressButton.setErrorText("Fill the fields.");
                        progressButton.setTextColor(Color.WHITE);
                    }
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                SharedPrefManager.getmInstances(getApplicationContext()).LogOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });
    }
}
