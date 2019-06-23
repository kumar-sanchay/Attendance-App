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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import turnoutcom.d_sanchay.myapplication.Manifest;
import turnoutcom.d_sanchay.myapplication.R;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.SharedPrefManager;

public class Class_detaits extends AppCompatActivity {
CircularProgressButton circularProgressButton;
EditText className,no_of,no_from,no_to,classCode;
FirebaseUser firebaseUser;
FirebaseFirestore firestore;
TextView textView ;
RelativeLayout relativeLayout;
Handler handler = new Handler();
private int STORAGE_PERMISSION_CODE = 1;
ArrayList<String> list;
int random = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detaits);
        className = findViewById(R.id.className);
        no_of = findViewById(R.id.total_students);
        no_from = findViewById(R.id.students_from);
        no_to = findViewById(R.id.students_to);
        classCode = findViewById(R.id.classCode);
        circularProgressButton = findViewById(R.id.create_classsroom_proceed);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        textView = findViewById(R.id.textCreateClassroom);
        circularProgressButton.setProgress(0);
        circularProgressButton.setTextColor(Color.WHITE);
        circularProgressButton.setText("Proceed");
        circularProgressButton.setIndeterminateProgressMode(true);
        relativeLayout = findViewById(R.id.class_detail_progress);
        relativeLayout.setVisibility(View.VISIBLE);
        list = new ArrayList<>();
        final Random random1 = new Random();
        random = random1.nextInt((12-1)+1)+1;
        final Query query = firestore.collection("Users").document(firebaseUser.getUid()).collection("Classroom");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
            if(!queryDocumentSnapshots.isEmpty()){
                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                    if(documentChange.getType()== DocumentChange.Type.ADDED){
                        String docId = documentChange.getDocument().getId();
                        list.add(docId);
                    }
                }
                relativeLayout.setVisibility(View.GONE);
            }else {
                relativeLayout.setVisibility(View.GONE);
            }
            }
        });
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(circularProgressButton.getProgress()==-1){
                    circularProgressButton.setProgress(0);
                    circularProgressButton.setIdleText("Proceed Again");
                    circularProgressButton.setTextColor(Color.WHITE);
                }else if(circularProgressButton.getProgress()==0){
                    circularProgressButton.setProgress(30);
                MyReceiver myReceiver = new MyReceiver(null);
                String name,total,from,to,codes;
                name = className.getText().toString();
                total = no_of.getText().toString().trim();
                from = no_from.getText().toString().trim();
                to = no_to.getText().toString().trim();
                codes = classCode.getText().toString().toUpperCase();
                if(list.contains(name)){
                    circularProgressButton.setProgress(-1);
                    circularProgressButton.setErrorText("Class name exits!!");
                    circularProgressButton.setTextColor(Color.WHITE);
                }else {
                if(firebaseUser!=null){
                    if(!name.equals("") || !total.equals("") || !from.equals("") || !to.equals("")){
                      CreateFile(name,total,from,to,codes,String.valueOf(random));
                      Intent intent = new Intent(Class_detaits.this,CreateClassJob.class);
                      intent.putExtra("name",name);
                      intent.putExtra("total",total);
                      intent.putExtra("from",from);
                      intent.putExtra("to",to);
                      intent.putExtra("codes",codes);
                      intent.putExtra("random",String.valueOf(random));
                      intent.putExtra("receiver",myReceiver);
                      CreateClassJob.enque(Class_detaits.this,intent);
                    }else {
                        circularProgressButton.setProgress(-1);
                        circularProgressButton.setErrorText("Fill all feilds.");
                        circularProgressButton.setTextColor(Color.WHITE);
                    }
                }}
            }}
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
                 ActivityCompat.requestPermissions(Class_detaits.this,new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
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

    public class MyReceiver extends ResultReceiver{

        public MyReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode==10 && resultData!=null){
            final String dataReceive = resultData.getString("data");
            handler.post(new Runnable() {
                @Override
                public void run() {
                     if(dataReceive.equals("true")){
                         SharedPrefManager.getmInstances(getApplicationContext()).setClassBackup(String.valueOf(list.size()));
                         Intent i = new Intent(Class_detaits.this,Classroom.class);
                         startActivity(i);
                         finish();
                     }else {
                         circularProgressButton.setProgress(-1);
                         circularProgressButton.setErrorText("Try Again!");
                         circularProgressButton.setTextColor(Color.WHITE);
                     }
                }
            });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Class_detaits.this,"Thanks for granting",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(Class_detaits.this,"Permission Denied",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(Class_detaits.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){

        }else {requestStoragePermission();}
    }

    public void CreateFile(String name,String total,String from,String to,String codes,String random)  {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+name+"/";
        HashMap<String,String> map = new HashMap<>();
        map.put("total",total);
        map.put("from",from);
        map.put("to",to);
        map.put("codes",codes);
        map.put("status","b");
        map.put("random",random);
        FileOutputStream fileOutputStream = null;
        File rootFile = new File(path);
        if(!rootFile.exists()){
            rootFile.mkdirs();
        }
        File file = new File (path+name+".txt");
        if(file.exists()){
            try {
                fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(map);
                objectOutputStream.close();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            try {
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(map);
                objectOutputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
