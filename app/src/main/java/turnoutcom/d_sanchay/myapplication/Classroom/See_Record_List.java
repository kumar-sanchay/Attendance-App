package turnoutcom.d_sanchay.myapplication.Classroom;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.MapConvertor;
import turnoutcom.d_sanchay.myapplication.R;

public class See_Record_List extends AppCompatActivity implements ShowAttRecordRecycler.ChangeRecord{
RecyclerView recyclerView;
ArrayList<RollNo> list,real_list;
ShowAttRecordRecycler showAttRecordRecycler;
EditText find;
Map<String,String> absent,findStu;
Spinner spinner;
AlertDialog.Builder alert;
Connection connection = new Connection(this);
FirebaseFirestore firestore;
FirebaseUser currentUser;
String classname,filename,codes;
int from,to;
RelativeLayout relativeLayoutProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see__record__list);
        recyclerView = findViewById(R.id.recycler_see_record);
        list = new ArrayList<>();
        real_list = new ArrayList<>();
        showAttRecordRecycler = new ShowAttRecordRecycler(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(showAttRecordRecycler);
        recyclerView.setHasFixedSize(true);
        showAttRecordRecycler.setChangeRecord(See_Record_List.this);
        find = findViewById(R.id.find_student);
        findStu = new HashMap<>();
        alert = new AlertDialog.Builder(this);
        spinner = findViewById(R.id.att_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,getResources().getStringArray(R.array.Spinner));
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        relativeLayoutProgress = findViewById(R.id.show_att_progress_rel);

        classname = getIntent().getStringExtra("classname");
        filename = getIntent().getStringExtra("fileName");
        String record = getIntent().getStringExtra("record");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+classname+"/"+classname+".txt";
        Map detail = null;
        absent = MapConvertor.stringToMap(record);
        File file = new File(path);
        FileInputStream fileInputStream=null;
        try {
             fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            detail = (HashMap) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if(fileInputStream!=null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

         from = Integer.valueOf(detail.get("from").toString());
         to = Integer.valueOf(detail.get("to").toString());
         codes = String.valueOf(detail.get("codes"));

        putValue(from,to,codes);

     find.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {

         }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {

         }

         @Override
         public void afterTextChanged(Editable s) {
         String text = s.toString().toUpperCase();
         if(!text.isEmpty()){
              findStudent(text.toUpperCase());}
         else {allStudent();}
         }
     });

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position){
                case 0:
                    allStudent();
                    break;
                case 1:
                    presentStu();
                    break;
                case 2:
                    absentStu();
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        allStudent();
        }
    });

    }

    public void findStudent(String text){

       ArrayList<RollNo> array = new ArrayList<>();
       for(RollNo rollNo : list){
           if(rollNo.getRollno().contains(text)){
               array.add(rollNo);
           }
       }
       showAttRecordRecycler.update(array);
}
public void putValue(int from,int to,String codes){
        list.clear();
    for(int i = from;i<=to;i++){
        String word = "";
        if(i<=9){
            word = codes+"0"+String.valueOf(i);
        }else {
            word = codes+String.valueOf(i);}
        if(absent.containsKey(word)){
            RollNo rollNo = new RollNo(word,"Absent");
            list.add(rollNo);
            real_list.add(rollNo);
            showAttRecordRecycler.notifyDataSetChanged();
        }  else{
            RollNo rollNo = new RollNo(word,"Present");
            list.add(rollNo);
            real_list.add(rollNo);
            showAttRecordRecycler.notifyDataSetChanged();
        }
    }
}
public void presentStu(){
    ArrayList<RollNo> abList = new ArrayList<>();
    for(RollNo rollNo : real_list){
        if(rollNo.getStatus().equals("Present")){
            abList.add(rollNo);
        }
    }
    list.clear();
    list.addAll(abList);
    showAttRecordRecycler.absent(list);
}
public void absentStu(){
    ArrayList<RollNo> abList = new ArrayList<>();
    for(RollNo rollNo : real_list){
        if(rollNo.getStatus().equals("Absent")){
            abList.add(rollNo);
        }
    }
    list.clear();
    list.addAll(abList);
    showAttRecordRecycler.absent(list);
}

public void allStudent(){
    if(real_list!=null){
        list.clear();
        list.addAll(real_list);
    showAttRecordRecycler.absent(list);
    }

}

    @Override
    public void onClickRecord(final int position) {
        String status = list.get(position).getStatus();
        final String rollNo = list.get(position).getRollno();
        final String month = getIntent().getStringExtra("month");
        final String year = getIntent().getStringExtra("year");
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+classname+"/"+filename+".txt";

        if(status.equals("Absent")){
            alert.setTitle("Change to Present")
                 .setMessage("Changes made will be reflected to your students record.")
                 .setPositiveButton("Make Present", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                         relativeLayoutProgress.setVisibility(View.VISIBLE);
                         absent.remove(rollNo);
                         String att = MapConvertor.mapToString(absent);
                         send(att,year,month,filename,path);
                         /*
                        final CollectionReference collectionReference =  firestore.collection("Users").document(currentUser.getUid()).collection("Classroom").document(classname).collection("Attendance");
                         Query query = collectionReference.whereEqualTo("code",filename);
                         query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                             @Override
                             public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                             if(!queryDocumentSnapshots.isEmpty()){
                                 for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){
                                     if(documentChange.getType()== DocumentChange.Type.ADDED){
                                         String docId = documentChange.getDocument().getId();
                                        final String rec = MapConvertor.mapToString(absent);
                                         Map<String,Object> map = new HashMap<>();
                                         map.put("attendance",rec);
                                         collectionReference.document(docId).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                  if(task.isSuccessful()){
                                                      File file = new File(path);
                                                      FileOutputStream fileOutputStream = null;
                                                      try {
                                                          fileOutputStream = new FileOutputStream(file);
                                                          fileOutputStream.write(rec.getBytes());
                                                          list.clear();
                                                          putValue(from,to,codes);
                                                          showAttRecordRecycler.notifyDataSetChanged();
                                                          relativeLayoutProgress.setVisibility(View.GONE);
                                                          Toast.makeText(getApplicationContext(),"Changes made successfully.",Toast.LENGTH_SHORT).show();
                                                      } catch (FileNotFoundException e1) {
                                                          relativeLayoutProgress.setVisibility(View.GONE);
                                                          Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                                          e1.printStackTrace();
                                                      } catch (IOException e1) {
                                                          relativeLayoutProgress.setVisibility(View.GONE);
                                                          Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                                          e1.printStackTrace();
                                                      }finally {
                                                          if(fileOutputStream!=null){
                                                          try {
                                                              fileOutputStream.close();
                                                          } catch (IOException e1) {
                                                              e1.printStackTrace();
                                                          }}
                                                      }
                                                  }else {
                                                      relativeLayoutProgress.setVisibility(View.GONE);
                                                      Toast.makeText(getApplicationContext(),"Operation Unsuccessful",Toast.LENGTH_SHORT).show();
                                                  }
                                             }
                                         });
                                     }
                                 }
                             }
                             }
                         });*/

                     }
                 }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();
        }else if(status.equals("Present")){
                alert.setTitle("Change to Absent")
                        .setMessage("Changes made will be reflected to your students record.")
                        .setPositiveButton("Make Absent", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    relativeLayoutProgress.setVisibility(View.VISIBLE);
                                    absent.put(rollNo,rollNo);
                                    String att = MapConvertor.mapToString(absent);
                                    send(att,year,month,filename,path);
                                /*    final CollectionReference collectionReference =  firestore.collection("Users").document(currentUser.getUid()).collection("Classroom").document(classname).collection("Attendance");
                                    Query query = collectionReference.whereEqualTo("code",filename);
                                    query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            if(!queryDocumentSnapshots.isEmpty()){
                                                for(DocumentChange documentChange:queryDocumentSnapshots.getDocumentChanges()){
                                                    if(documentChange.getType()== DocumentChange.Type.ADDED){
                                                        String docId = documentChange.getDocument().getId();
                                                        final String rec = MapConvertor.mapToString(absent);
                                                        Map<String,Object> map = new HashMap<>();
                                                        map.put("attendance",rec);
                                                        collectionReference.document(docId).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    File file = new File(path);
                                                                    FileOutputStream fileOutputStream = null;
                                                                    try {
                                                                        fileOutputStream = new FileOutputStream(file);
                                                                        fileOutputStream.write(rec.getBytes());
                                                                        list.clear();
                                                                        putValue(from,to,codes);
                                                                        showAttRecordRecycler.notifyDataSetChanged();
                                                                        relativeLayoutProgress.setVisibility(View.GONE);
                                                                        fileOutputStream.close();
                                                                        Toast.makeText(getApplicationContext(),"Changes made successfully.",Toast.LENGTH_SHORT).show();

                                                                    } catch (FileNotFoundException e1) {
                                                                        relativeLayoutProgress.setVisibility(View.GONE);
                                                                        Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                                                        e1.printStackTrace();
                                                                    } catch (IOException e1) {
                                                                        relativeLayoutProgress.setVisibility(View.GONE);
                                                                        Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                                                        e1.printStackTrace();
                                                                    }
                                                                }else {
                                                                    relativeLayoutProgress.setVisibility(View.GONE);
                                                                    Toast.makeText(getApplicationContext(),"Operation Unsuccessful",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        }
                                    });*/
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = alert.create();
                dialog.show();
        }
    }
    private void send(final String att,String year,String month,String filename,final String path){
        String query = "?orderBy=\"code\"&equalTo=\""+filename+"\"";
        final String url = "https://socialapp-a4fdb.firebaseio.com/"+currentUser.getUid()+"/"+year+month+"/"+classname+"/";
        final String finalUrl = url+".json"+query.replace(" ","%20").replace("\"","%22");
        final Map<String,Object> map = new HashMap<>();
        map.put("attendance",att);
        map.put("code",filename);
        if(connection.connected()){
            StringRequest stringRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response != null) {
                        if(!response.equals("{}")){
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Iterator<String> iterator = jsonObject.keys();
                                while (iterator.hasNext()){
                                    String key = iterator.next();
                                    String update = url+key+"/.json";
                                   JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, update, new JSONObject(map), new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            File file = new File(path);
                                            FileOutputStream fileOutputStream = null;
                                            try {
                                                fileOutputStream = new FileOutputStream(file);
                                                fileOutputStream.write(att.getBytes());
                                                list.clear();
                                                putValue(from,to,codes);
                                                showAttRecordRecycler.notifyDataSetChanged();
                                                relativeLayoutProgress.setVisibility(View.GONE);
                                                fileOutputStream.close();
                                                Toast.makeText(getApplicationContext(),"Changes made successfully.",Toast.LENGTH_SHORT).show();
                                            } catch (FileNotFoundException e1) {
                                                relativeLayoutProgress.setVisibility(View.GONE);
                                                Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                                e1.printStackTrace();
                                            } catch (IOException e1) {
                                                relativeLayoutProgress.setVisibility(View.GONE);
                                                Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                                e1.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            relativeLayoutProgress.setVisibility(View.GONE);
                                            Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    requestQueue.add(jsonObjectRequest);

                                }
                            } catch (JSONException e) {
                                relativeLayoutProgress.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                        }else {
                            String post = finalUrl+".json";
                            map.put("date", ServerValue.TIMESTAMP);
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, post, new JSONObject(map), new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    File file = new File(path);
                                    FileOutputStream fileOutputStream = null;
                                    try {
                                        fileOutputStream = new FileOutputStream(file);
                                        fileOutputStream.write(att.getBytes());
                                        list.clear();
                                        putValue(from,to,codes);
                                        showAttRecordRecycler.notifyDataSetChanged();
                                        relativeLayoutProgress.setVisibility(View.GONE);
                                        fileOutputStream.close();
                                        Toast.makeText(getApplicationContext(),"Changes made successfully.",Toast.LENGTH_SHORT).show();
                                    } catch (FileNotFoundException e1) {
                                        relativeLayoutProgress.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                        e1.printStackTrace();
                                    } catch (IOException e1) {
                                        relativeLayoutProgress.setVisibility(View.GONE);
                                        Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                        e1.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    relativeLayoutProgress.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                }
                            }){
                                @Override
                                public Map<String,String> getHeaders() throws AuthFailureError{
                                    Map<String,String> hash = new HashMap<String, String>();
                                    hash.put("Content-Type","application/json; charset=utf-8");
                                    return hash;
                                }
                            };
                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                            requestQueue.add(jsonObjectRequest);
                        }

                    }else {
                        String post = finalUrl+".json";
                        map.put("date", ServerValue.TIMESTAMP);
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, post, new JSONObject(map), new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                File file = new File(path);
                                FileOutputStream fileOutputStream = null;
                                try {
                                    fileOutputStream = new FileOutputStream(file);
                                    fileOutputStream.write(att.getBytes());
                                    list.clear();
                                    putValue(from,to,codes);
                                    showAttRecordRecycler.notifyDataSetChanged();
                                    relativeLayoutProgress.setVisibility(View.GONE);
                                    fileOutputStream.close();
                                    Toast.makeText(getApplicationContext(),"Changes made successfully.",Toast.LENGTH_SHORT).show();
                                } catch (FileNotFoundException e1) {
                                    relativeLayoutProgress.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                    e1.printStackTrace();
                                } catch (IOException e1) {
                                    relativeLayoutProgress.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                                    e1.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                relativeLayoutProgress.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                            }
                        }){
                            @Override
                            public Map<String,String> getHeaders() throws AuthFailureError{
                                Map<String,String> hash = new HashMap<String, String>();
                                hash.put("Content-Type","application/json; charset=utf-8");
                                return hash;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                        requestQueue.add(jsonObjectRequest);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    relativeLayoutProgress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }else {
            File file = new File(path);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(att.getBytes());
                list.clear();
                putValue(from,to,codes);
                showAttRecordRecycler.notifyDataSetChanged();
                relativeLayoutProgress.setVisibility(View.GONE);
                fileOutputStream.close();
                Toast.makeText(getApplicationContext(),"Changes made successfully.",Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e1) {
                relativeLayoutProgress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            } catch (IOException e1) {
                relativeLayoutProgress.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Try Again.",Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            }

        }

       }
    }
