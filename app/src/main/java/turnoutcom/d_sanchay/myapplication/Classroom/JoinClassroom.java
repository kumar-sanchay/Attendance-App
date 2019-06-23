package turnoutcom.d_sanchay.myapplication.Classroom;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.R;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.SharedPrefManager;

public class JoinClassroom extends AppCompatActivity {
EditText username,classname,roll;
CircularProgressButton circularProgressButton;
FirebaseFirestore firestore;
FirebaseUser currentUser;
DatabaseReference databaseReference;
private int STORAGE_PERMISSION_CODE = 1;
Connection connection = new Connection(this);
    int numClass=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_classroom);
        username = findViewById(R.id.joinClassUsername);
        classname = findViewById(R.id.joinClassName);
        roll= findViewById(R.id.joinRollNo);
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        circularProgressButton = findViewById(R.id.join_classsroom_proceed);
        circularProgressButton.setTextColor(Color.WHITE);
        circularProgressButton.setText("Proceed.");
        circularProgressButton.setIndeterminateProgressMode(true);
        if(ContextCompat.checkSelfPermission(JoinClassroom.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){

        }else {requestStoragePermission();}
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(circularProgressButton.getProgress()==-1){
                    circularProgressButton.setIdleText("Proceed Again.");
                    circularProgressButton.setTextColor(Color.WHITE);
                    circularProgressButton.setProgress(0);
                }else if(circularProgressButton.getProgress()==0){
                    circularProgressButton.setProgress(30);
                    final String user = username.getText().toString();
                    final String className = classname.getText().toString();
                    final String rollNo = roll.getText().toString();

                    if(!user.isEmpty() || !className.isEmpty() || !rollNo.isEmpty()){
                     if(connection.connected()){
                         if(currentUser!=null){
                             databaseReference = FirebaseDatabase.getInstance().getReference(String.valueOf(user.charAt(0)));
                             databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                     if(dataSnapshot.hasChild(user)){
                                         String  userId = dataSnapshot.child(user).child("userId").getValue().toString();
                                         if(!userId.equals(currentUser.getUid())){
                                             firestore = FirebaseFirestore.getInstance();
                                             currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                             try {
                                                 numClass = Integer.valueOf(SharedPrefManager.getmInstances(getApplicationContext()).getClassBackup());
                                             }catch (NumberFormatException e){
                                                 numClass = 0;
                                                 final Query query = firestore.collection("Users").document(currentUser.getUid()).collection("Classroom");
                                                 query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                     @Override
                                                     public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                         if(!queryDocumentSnapshots.isEmpty()){
                                                             for(DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                                                                 if(documentChange.getType()== DocumentChange.Type.ADDED){
                                                                     numClass = numClass + 1;
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 });
                                             }
                                             CollectionReference collectionReference = firestore.collection("Users").document(userId).collection("Classroom");
                                             collectionReference.document(className).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                     if (task.isSuccessful()) {
                                                         if(task.getResult().exists()){
                                                             final String random = task.getResult().getString("random");
                                                             final Map<String, Object> map = new HashMap<>();
                                                             map.put("random", random);
                                                             map.put("from", "f");
                                                             map.put("codes", rollNo);
                                                             map.put("status", "p");
                                                             map.put("total",user);
                                                             map.put("to","");
                                                             firestore.collection("Users").document(currentUser.getUid()).collection("Classroom").document(className).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                 @Override
                                                                 public void onComplete(@NonNull Task<Void> task) {
                                                                     if (task.isSuccessful()) {
                                                                         String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+className;
                                                                         File file = new File(path);
                                                                         FileOutputStream fos = null;

                                                                         try {
                                                                             file.mkdirs();
                                                                             File fo = new File(path+"/"+className+".txt");
                                                                             fos = new FileOutputStream(fo);
                                                                             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
                                                                             objectOutputStream.writeObject(map);
                                                                             objectOutputStream.close();
                                                                             numClass = numClass + 1;
                                                                             SharedPrefManager.getmInstances(getApplicationContext()).setClassBackup(String.valueOf(numClass));
                                                                             Intent intent1 = new Intent(getApplicationContext(),Classroom.class);
                                                                             startActivity(intent1);
                                                                             finish();
                                                                         } catch (FileNotFoundException e) {
                                                                             circularProgressButton.setProgress(-1);
                                                                             circularProgressButton.setErrorText("Try Again.");
                                                                             circularProgressButton.setTextColor(Color.WHITE);
                                                                             e.printStackTrace();
                                                                         } catch (IOException e) {
                                                                             circularProgressButton.setProgress(-1);
                                                                             circularProgressButton.setErrorText("Try Again.");
                                                                             circularProgressButton.setTextColor(Color.WHITE);
                                                                             e.printStackTrace();
                                                                         }finally {
                                                                             if(fos!=null){
                                                                                 try {
                                                                                     fos.close();
                                                                                 } catch (IOException e) {
                                                                                     circularProgressButton.setProgress(-1);
                                                                                     circularProgressButton.setErrorText("Try Again.");
                                                                                     circularProgressButton.setTextColor(Color.WHITE);
                                                                                     e.printStackTrace();
                                                                                 }
                                                                             }
                                                                         }

                                                                     } else {
                                                                         circularProgressButton.setProgress(-1);
                                                                         circularProgressButton.setErrorText("Try Again.");
                                                                         circularProgressButton.setTextColor(Color.WHITE);
                                                                     }
                                                                 }
                                                             });
                                                         } else {
                                                             circularProgressButton.setProgress(-1);
                                                             circularProgressButton.setErrorText("Try Again.");
                                                             circularProgressButton.setTextColor(Color.WHITE);
                                                         }
                                                     }else {
                                                         circularProgressButton.setProgress(-1);
                                                         circularProgressButton.setErrorText("Try Again.");
                                                         circularProgressButton.setTextColor(Color.WHITE);

                                                     }}
                                             }).addOnFailureListener(new OnFailureListener() {
                                                 @Override
                                                 public void onFailure(@NonNull Exception e) {
                                                     circularProgressButton.setProgress(-1);
                                                     circularProgressButton.setErrorText("Try Again.");
                                                     circularProgressButton.setTextColor(Color.WHITE);

                                                 }
                                             });
                                         }else {
                                             circularProgressButton.setProgress(-1);
                                             circularProgressButton.setErrorText("Cannot join own class.");
                                             circularProgressButton.setTextColor(Color.WHITE);
                                         }
                                     }else {
                                         circularProgressButton.setProgress(-1);
                                         circularProgressButton.setErrorText("Username invalid.");
                                         circularProgressButton.setTextColor(Color.WHITE);
                                     }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError databaseError) {
                                     circularProgressButton.setProgress(-1);
                                     circularProgressButton.setErrorText("Try Again.");
                                     circularProgressButton.setTextColor(Color.WHITE);
                                 }
                             });
                         }
                     }
                    }else{
                        circularProgressButton.setProgress(-1);
                        circularProgressButton.setErrorText("Try Again.");
                        circularProgressButton.setTextColor(Color.WHITE);
                    }
                }
            }
        });
    }
    private void requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This will help you to save attendance offline network issues..!!")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(JoinClassroom.this,new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Never", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(this,new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(JoinClassroom.this,"Thanks for granting",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(JoinClassroom.this,"Permission Denied",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
