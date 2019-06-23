package turnoutcom.d_sanchay.myapplication.Registration;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import turnoutcom.d_sanchay.myapplication.Classroom.Classroom;
import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.R;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.SharedPrefManager;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.UserId;

public class Login extends AppCompatActivity {
EditText pass;
TextView email_l,forgetPass;
CircularProgressButton circularProgressButton;
AlertDialog.Builder alert;
FirebaseAuth firebaseAuth;
Connection connection;
DatabaseReference databaseReference;
FirebaseUser firebaseUser;
FirebaseFirestore firestore;
    private int STORAGE_PERMISSION_CODE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email_l = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        email_l.setText(getUser());
        connection = new Connection(this);
        forgetPass = findViewById(R.id.forget_pass);
        firebaseAuth = FirebaseAuth.getInstance();
       circularProgressButton = findViewById(R.id.login_btn);
       circularProgressButton.setText(R.string.get_in);
       circularProgressButton.setTextColor(Color.WHITE);
       circularProgressButton.setIndeterminateProgressMode(true);
       //databaseReference = FirebaseDatabase.getInstance().getReference("Username");
       firestore = FirebaseFirestore.getInstance();
        if(ContextCompat.checkSelfPermission(Login.this, Manifest.permission.GET_ACCOUNTS)== PackageManager.PERMISSION_GRANTED){

        }else {requestStoragePermission();}
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(circularProgressButton.getProgress() == -1){
                IdleButton();
            }else if(circularProgressButton.getProgress() == 0){
                circularProgressButton.setProgress(30);
                final String email = email_l.getText().toString();
                final String password = pass.getText().toString();
                if(password.isEmpty()){
                    circularProgressButton.setErrorText("Enter the password.");
                    circularProgressButton.setTextColor(Color.WHITE);
                    circularProgressButton.setProgress(-1);
                }else {
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    firestore.collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task1) {
                       if(task1.isSuccessful()){
                           String username = task1.getResult().getString("username");
                           String name = task1.getResult().getString("Name");
                           UserId userId = new UserId(username,name);
                           SharedPrefManager.getmInstances(getApplicationContext()).setUser(userId);
                           startActivity(new Intent(getApplicationContext(),Classroom.class));
                           finish();
                       }
                        }
                    });

                    }else{
                        if(connection.connected()){
                            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                               if(task.isSuccessful()){
                                   startActivity(new Intent(Login.this,FirstUserInfo.class));
                                   finish();
                               }else {
                                   ErrorButton();
                               }
                                }
                            });
                        }else{
                         ErrorButton();
                        }
                    }
                    }
                });
                }
            }
            }
        });
        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            alert = new AlertDialog.Builder(Login.this);
            alert.setIcon(R.drawable.ic_lock_black_24dp)
                 .setTitle("Check your Email!!")
                 .setMessage("A link will be  provided to your email which will allow you to change your password after clicking email button below.")
                 .setPositiveButton("Email", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                 String url = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/getOobConfirmationCode";
                 StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                     @Override
                     public void onResponse(String response){

                 PackageManager manager = getPackageManager();
                 Intent i = manager.getLaunchIntentForPackage("com.google.android.gmail");
                         if (i != null) {
                             i.addCategory(Intent.CATEGORY_LAUNCHER);
                             startActivity(i);
                         }else {
                             Snackbar snackbar = Snackbar.make(findViewById(R.id.LoginAct),"Email sent.",Snackbar.LENGTH_LONG);
                             snackbar.show();
                         }

                     }
                 }, new Response.ErrorListener() {
                     @Override
                     public void onErrorResponse(VolleyError error) {
                         Snackbar snackbar = Snackbar.make(findViewById(R.id.LoginAct),"Your email not found.If you are a first time user enter the password of your choice in ht password field to get registered.",Snackbar.LENGTH_LONG);
                         snackbar.show();
                     }
                 }){
                     @Override
                     protected Map<String,String> getParams() {
                         Map<String,String> params = new HashMap<>();
                         params.put("key","AIzaSyCSlNtuVLpbSTfhxjj5Lf95XkFq4CTRhic");
                         params.put("requestType","PASSWORD_RESET");
                         params.put("email",getUser());
                         return params;
                     }
                 };
                 RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                 requestQueue.add(stringRequest);
                 requestQueue.getCache().clear();
                         dialog.cancel();
                     }
                 });
            AlertDialog dia = alert.create();
            dia.show();
            }
        });
    }

    private void requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)){
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This permission is needed for putting a profile pic...!!")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Login.this,new String[] {Manifest.permission.GET_ACCOUNTS},STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Never", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.GET_ACCOUNTS},STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null){
            if(SharedPrefManager.getmInstances(getApplicationContext()).getUser().getUid()!=null){
                startActivity(new Intent(Login.this,Classroom.class));
                finish();
            }else {
                startActivity(new Intent(Login.this,FirstUserInfo.class));
                finish();
            }
        }
        }
        public  String getUser(){
            Pattern pattern = Patterns.EMAIL_ADDRESS;
            Account[] accounts = AccountManager.get(Login.this).getAccounts();
            for(Account account : accounts){
                if(pattern.matcher(account.name).matches()){
                    return account.name;
                }
            }
            return "";
        }
        public void IdleButton(){
        circularProgressButton.setIdleText("Get In");
        circularProgressButton.setProgress(0);
        circularProgressButton.setTextColor(Color.WHITE);
        }
        public void ErrorButton(){
        circularProgressButton.setErrorText("Operation Failed");
        circularProgressButton.setProgress(-1);
        circularProgressButton.setTextColor(Color.WHITE);
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Login.this,"Thanks for granting",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(Login.this,"Permission Denied",Toast.LENGTH_LONG).show();
            }
        }
    }
    }

