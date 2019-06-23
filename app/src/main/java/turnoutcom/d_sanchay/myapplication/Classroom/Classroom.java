package turnoutcom.d_sanchay.myapplication.Classroom;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import turnoutcom.d_sanchay.myapplication.Bottom_Change_Roll;
import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.R;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.SharedPrefManager;
import turnoutcom.d_sanchay.myapplication.StudentAttLayout;

public class Classroom extends AppCompatActivity implements ClassRecycler.OnClickListener,ClassRecycler.DeleteClass,ClassRecycler.ChangeRoll {

CardView createClass,joinClass;
FirebaseUser currentUser;
RecyclerView recyclerView;
FirebaseFirestore firestore;
long listOfFiles = 0;
long backupFile = 0;
int bottomSheetInt = 1;
ClassRecycler classRecycler;
ArrayList<ClassModel> list,real_list;
ArrayList<String> many;
AlertDialog.Builder alertDialog;
RelativeLayout relativeLayoutProgress;
BottomSheetBehavior bottomSheetBehavior;
ImageView bottom;
EditText search;
DatabaseReference databaseReference;
Connection connection = new Connection(this);
private String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom);
        many = new ArrayList<>();
        search = findViewById(R.id.classroom_search);
        bottom = findViewById(R.id.bottomOpen);
        final View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        relativeLayoutProgress = findViewById(R.id.classroomProgress);
        createClass = findViewById(R.id.create_classsroom);
        joinClass = findViewById(R.id.classroom_join);
        alertDialog  = new AlertDialog.Builder(this);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        String classValue = SharedPrefManager.getmInstances(this).getClassBackup();
        recyclerView = findViewById(R.id.classRecycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        real_list = new ArrayList<>();
        classRecycler = new ClassRecycler(list,1);
        recyclerView.setAdapter(classRecycler);
        recyclerView.setHasFixedSize(true);
        classRecycler.setItemOnClickListener(this);
        classRecycler.setOnDelete(this);
        classRecycler.setChangeRoll(this);
        relativeLayoutProgress.setVisibility(View.VISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            if(!s.toString().equals("")){
                searchClass(s.toString());
            }else {
                list.clear();
                list.addAll(real_list);
                classRecycler.update(real_list);
            }
            }
        });

        try{
        File file = new File(path);
        File[] files = file.listFiles();
        for(File inFile :  files){
            if(inFile.isDirectory()){
                listOfFiles = listOfFiles + 1;
            }
        }}catch (Exception e){
            listOfFiles = 0;
          e.printStackTrace();
        }
        try {
                if (classValue.equals(String.valueOf(listOfFiles))) {
                        checkFile();
                        relativeLayoutProgress.setVisibility(View.GONE);

                }else if(classValue.equals("0")){
                    relativeLayoutProgress.setVisibility(View.GONE);
                } else {
                    backUp();

                }

        }catch (NullPointerException e){
            backUp();
        }
        if(currentUser!=null){
            createClass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(),Class_detaits.class);
                    startActivity(i);
                    finish();
                }
            });
            joinClass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
             Intent i = new Intent(getApplicationContext(),JoinClassroom.class);
             startActivity(i);
                }
            });
        }

        bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetInt%2==0){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetInt++;
                }
                else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    bottomSheetInt++;
                }
            }
        });
    }

    private void searchClass(String s) {
        list.clear();
        for(ClassModel classModel : real_list){
            if(classModel.getClassName().contains(s)){
                list.add(classModel);
            }
        }
        classRecycler.update(list);
    }

    public void backUp(){
        firestore = FirebaseFirestore.getInstance();
        Query query = firestore.collection("Users").document(currentUser.getUid()).collection("Classroom");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful()){
                for(QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()) {
                    if (queryDocumentSnapshot.exists()) {
                        String direcName = queryDocumentSnapshot.getId();
                        String total = queryDocumentSnapshot.getString("total");
                        String from = queryDocumentSnapshot.getString("from");
                        String to = queryDocumentSnapshot.getString("to");
                        String codes = queryDocumentSnapshot.getString("codes");
                        String random = queryDocumentSnapshot.getString("random");
                        String status = queryDocumentSnapshot.getString("status");
                        SharedPrefManager.getmInstances(getApplicationContext()).setClassBackup(String.valueOf(++backupFile));
                        CreateFiles(direcName, total, from, to, codes, random, status);

                    }else {
                        SharedPrefManager.getmInstances(getApplicationContext()).setClassBackup(String.valueOf(0));
                    }
                }
                    }
                }
            } ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                SharedPrefManager.getmInstances(getApplicationContext()).setClassBackup(String.valueOf(0));
            }
        });
    }




    public void CreateFiles(String name,String total,String from,String to,String codes,String random,String status)  {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+name+"/";
        HashMap<String,String> map = new HashMap<>();
        map.put("total",total);
        map.put("from",from);
        map.put("to",to);
        map.put("codes",codes);
        map.put("random",random);
        map.put("status",status);
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
                checkFile();
                relativeLayoutProgress.setVisibility(View.GONE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                relativeLayoutProgress.setVisibility(View.GONE);
            } catch (IOException e) {
                relativeLayoutProgress.setVisibility(View.GONE);
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
                checkFile();
                relativeLayoutProgress.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                relativeLayoutProgress.setVisibility(View.GONE);
            }

        }

    }

    public void checkFile(){
        File file = new File(path);
        File[] files = file.listFiles();
        for(File inFile : files){
            if(inFile.isDirectory()){
                String fileName = inFile.getName();
                String paths = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+fileName+"/";
                File direc = new File(paths+fileName+".txt");
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(direc);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ObjectInputStream objectInputStream = null;
                try {
                    objectInputStream = new ObjectInputStream(fileInputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Map details = null;
                try {
                    if (objectInputStream != null) {
                        details = (HashMap) objectInputStream.readObject();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (objectInputStream != null) {
                        objectInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert details != null;
                if(!many.contains(fileName)){
                    many.add(fileName);
                ClassModel classModel = new ClassModel(fileName,details.get("total").toString(),details.get("to").toString(),details.get("from").toString(),details.get("codes").toString(),details.get("status").toString(),details.get("random").toString());
                real_list.add(classModel);
                list.add(classModel);
                classRecycler.notifyDataSetChanged();}
            }
        }relativeLayoutProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void OnClick(int position) {
         String classname = list.get(position).getClassName();
         String total = list.get(position).getTotal();
        String status = list.get(position).getStatus();
         String from = list.get(position).getFrom();
         String to = list.get(position).getTo();
         String codes = list.get(position).getCodes();
         if(status.equals("b")){
             classRecycler.notifyDataSetChanged();
        Bundle bundle = new Bundle();
        bundle.putString("classname",classname);
        bundle.putString("total",total);
        bundle.putString("from",from);
        bundle.putString("to",to);
        bundle.putString("codes",codes);
        Bottom_Sheet_Class bottom_sheet_class = new Bottom_Sheet_Class();
        bottom_sheet_class.setArguments(bundle);
        bottom_sheet_class.show(getSupportFragmentManager(),"ClassBottom");
         }else if(status.equals("p")){
             classRecycler.notifyDataSetChanged();
           Intent intent = new Intent(getApplicationContext(), StudentAttLayout.class);
           intent.putExtra("classname",classname);
           intent.putExtra("codes",codes);
           intent.putExtra("teacher",total);
           startActivity(intent);
         }
    }


    @Override
    public void OnDelete(final int position) {
       final String className = list.get(position).getClassName();
        alertDialog.setTitle("Delete Classroom")
                .setMessage("Deleting the classroom will too delete your all attendance record.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        relativeLayoutProgress.setVisibility(View.VISIBLE);
                        if(connection.connected()){
                            final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+className+"/";
                            final String pre = SharedPrefManager.getmInstances(getApplicationContext()).getClassBackup();
                            CollectionReference collectionReference = firestore.collection("Users").document(currentUser.getUid()).collection("Classroom");
                            collectionReference.document(className).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        deleteRest(pre,path,position);
                                    }else {
                                    Toast.makeText(getApplicationContext(),"Try Again!",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }else {
                            Toast.makeText(getApplicationContext(),"Internet Connection Required.",Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.show();
    }

    public boolean delete(File file){
        if(file.isDirectory()){
            for(File child:file.listFiles()){
                delete(child);
            }
        }
        return file.delete();
    }
    private void deleteRest(final String pre, final String path, final int pos){
        String url = "https://socialapp-a4fdb.firebaseio.com/"+currentUser.getUid()+"/.json";
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                File file = new File(path);
                if(delete(file)){
                    int value = Integer.valueOf(pre);
                    SharedPrefManager.getmInstances(getApplicationContext()).setClassBackup(String.valueOf(value-1));
                    list.remove(pos);
                    real_list.remove(pos);
                    classRecycler.notifyDataSetChanged();
                    relativeLayoutProgress.setVisibility(View.GONE);
                }else {
                    Log.d("Deletion", "onClick: Not Deleted from phone.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                relativeLayoutProgress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Opperation Unsuccessful",Toast.LENGTH_LONG).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    @Override
    public void Change(int position) {
        String classname = list.get(position).getClassName();
        Bundle bundle = new Bundle();
        bundle.putString("classname",classname);
        Bottom_Change_Roll bottom_change_roll = new Bottom_Change_Roll();
        bottom_change_roll.setArguments(bundle);
        bottom_change_roll.show(getSupportFragmentManager(),"ChangeRoll");
    }
}
