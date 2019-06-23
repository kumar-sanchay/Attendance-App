package turnoutcom.d_sanchay.myapplication.Classroom;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.MapConvertor;
import turnoutcom.d_sanchay.myapplication.R;

public class ShowAttentance extends AppCompatActivity  implements ShowAttRecycler.DeleteAtt,ShowAttRecycler.ViewRecord,ShowAttRecycler.Upload {
RecyclerView recyclerView;
ShowAttRecycler showAttRecycler;
String mode;
ArrayList<ShowAttModel> arrayList;
FirebaseFirestore firestore;
FirebaseUser currentUser;
AlertDialog.Builder alert;
RelativeLayout backupRel;
int fileNum = 0;
TextView backupreltext;
AlertDialog.Builder alertUpload;
Connection connection = new Connection(this);
String[] months = new String[]{"January","February","March","April","May","June","July","August","September","October","November","December"};
String[] weeks = new String[]{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attentance);
        backupRel = findViewById(R.id.backup_show_att);
        backupreltext = findViewById(R.id.backuprel_text);
        recyclerView = findViewById(R.id.showAtttRecycle);
         mode = getIntent().getStringExtra("mode");
        final String name = getIntent().getStringExtra("name");
        final String total = getIntent().getStringExtra("total");
        alert = new AlertDialog.Builder(this);
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        arrayList = new ArrayList<>();
        showAttRecycler = new ShowAttRecycler(arrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(showAttRecycler);
        recyclerView.setHasFixedSize(true);
        showAttRecycler.setOnClick(ShowAttentance.this);
        showAttRecycler.setViewRecord(ShowAttentance.this);
        showAttRecycler.setUpload(ShowAttentance.this);
        alertUpload = new AlertDialog.Builder(this);
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+name+"/";
        final File file = new File(path);
        File[] files = file.listFiles();

        for(File f : files){
            if(mode.equals("date")){
            String date = getIntent().getStringExtra("fileName");
            if(f.getName().startsWith(date)){
                ++fileNum;
                // arrayList.add(f.getName().replace(".txt",""));
                findFile(path,f.getName(),total,name);
            }
        }else if(mode.equals("month")){
                String month = getIntent().getStringExtra("month");
                String year = getIntent().getStringExtra("year");
                if(f.getName().contains(months[Integer.valueOf(month)-1]) && f.getName().contains(year)){
                    ++fileNum;
                    findFile(path,f.getName(),total,name);
                }
            }
        }

        if(fileNum==0){
            backupRel.setVisibility(View.VISIBLE);
            final String downloadingPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+name+"/";
            if(mode.equals("month")){
                final String month = getIntent().getStringExtra("month");
                final String year = getIntent().getStringExtra("year");
                String query = "?orderBy=\"date\"";
                String url  = "https://socialapp-a4fdb.firebaseio.com/"+currentUser.getUid()+"/"+year+month+"/"+name+"/"+".json"+query;

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(response!=null){
                        try {
                            Iterator<String> iterator = response.keys();
                            while (iterator.hasNext()){
                                String key = iterator.next();
                                JSONObject jsonObject = response.getJSONObject(key);
                                String codes = jsonObject.getString("code");
                                String att = jsonObject.getString("attendance");
                                String recorded = jsonObject.getString("recorded");
                                File f = new File(path + codes + ".txt");
                                FileOutputStream fos = null;
                                try {
                                    f.createNewFile();
                                    fos = new FileOutputStream(f);
                                    fos.write(att.getBytes());
                                    Map<String,String> map = new HashMap<>();
                                    map = MapConvertor.stringToMap(att);
                                    String topics = map.get("topic");
                                    String absent = String.valueOf(map.size()-1);
                                    String present = String.valueOf(Integer.valueOf(total)-Integer.valueOf(absent));
                                    String percent = String.valueOf((Float.valueOf(present)/Float.valueOf(total))*100);
                                    ShowAttModel showAttModel = new ShowAttModel(codes,topics,total,present,absent,percent,name,recorded);
                                    arrayList.add(showAttModel);
                                    showAttRecycler.notifyDataSetChanged();
                                    backupRel.setVisibility(View.GONE);
                                } catch (IOException e1) {
                                    backupRel.setVisibility(View.GONE);
                                    e1.printStackTrace();

                                }finally {
                                    if(fos!=null){
                                        try {
                                            fos.close();
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }}

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        backupRel.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"No records Available.",Toast.LENGTH_SHORT).show();
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                requestQueue.add(jsonObjectRequest);
            }else if(mode.equals("date")){
                String date = getIntent().getStringExtra("fileName");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
                Date dates = null;
                try {
                    dates = simpleDateFormat.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dates);
               String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
                String year = String.valueOf(calendar.get(Calendar.YEAR));
                final String mainurl = "https://socialapp-a4fdb.firebaseio.com/"+currentUser.getUid()+"/"+year+month+"/"+name+"/";
                String query = "?orderBy=\"code\"&startAt=\""+date+"\"";
                String url  = mainurl+".json"+query.replace(" ","%20").replace("\"","%22");
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    Iterator<String> iterator = response.keys();
                    while (iterator.hasNext()){
                        String key = iterator.next();
                        try {
                            JSONObject jsonObject = response.getJSONObject(key);
                            String codes = jsonObject.getString("code");
                            String att = jsonObject.getString("attendance");
                            String recorded = jsonObject.getString("recorded");
                            File f = new File(path + codes + ".txt");
                            FileOutputStream fos = null;
                            try {
                                f.createNewFile();
                                fos = new FileOutputStream(f);
                                fos.write(att.getBytes());
                                Map<String,String> map = new HashMap<>();
                                map = MapConvertor.stringToMap(att);
                                String topics = map.get("topic");
                                String absent = String.valueOf(map.size()-1);
                                String present = String.valueOf(Integer.valueOf(total)-Integer.valueOf(absent));
                                String percent = String.valueOf((Float.valueOf(present)/Float.valueOf(total))*100);
                                ShowAttModel showAttModel = new ShowAttModel(codes,topics,total,present,absent,percent,name,recorded);
                                arrayList.add(showAttModel);
                                showAttRecycler.notifyDataSetChanged();
                                backupRel.setVisibility(View.GONE);
                            } catch (IOException e1) {
                                backupRel.setVisibility(View.GONE);
                                e1.printStackTrace();

                            }finally {
                                if(fos!=null){
                                    try {
                                        fos.close();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        backupRel.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"No records Available.",Toast.LENGTH_SHORT).show();
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(jsonObjectRequest);
            }
        }
    }

    @Override
    public void onClick(final int position){
        final String className = arrayList.get(position).getClassname();
       final String fileName = arrayList.get(position).getFileName();
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+className+"/"+fileName+".txt";
        alert.setTitle("Delete Record")
                .setMessage("The record for "+fileName+" will be deleted.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        if(connection.connected()){
                            String month="",year = "";
                        backupRel.setVisibility(View.VISIBLE);
                            if(mode.equals("month")){
                             month = getIntent().getStringExtra("month");
                             year = getIntent().getStringExtra("year");
                            }else {
                             String ss = getIntent().getStringExtra("fileName");
                             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
                                try {
                                    Date date = simpleDateFormat.parse(ss);
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    month = String.valueOf(calendar.get(Calendar.MONTH)+1);
                                    year = String.valueOf(calendar.get(Calendar.YEAR));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        backupreltext.setText("Deleting Record...Please Wait");
                            final String mainurl = "https://socialapp-a4fdb.firebaseio.com/"+currentUser.getUid()+"/"+year+month+"/"+className+"/";
                            String query = "?orderBy=\"code\"&equalTo=\""+fileName+"\"";
                            String url  = mainurl+".json"+query.replace(" ","%20").replace("\"","%22");
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if(response!=null){
                                        Iterator<String> iterator = response.keys();
                                        while (iterator.hasNext()){
                                            String key = iterator.next();
                                            String deleteUrl = mainurl+key+"/.json";
                                            StringRequest stringRequest = new StringRequest(Request.Method.DELETE, deleteUrl, new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                            if(response.equals("null")){
                                                File file = new File(path);
                                                if(file.exists()){
                                                    boolean delete = file.delete();
                                                    if(delete){
                                                        arrayList.remove(position);
                                                        showAttRecycler.notifyDataSetChanged();
                                                        backupRel.setVisibility(View.GONE);
                                                        message("Record deleted successfully");
                                                    }
                                                    else { backupRel.setVisibility(View.GONE);
                                                        message("Operation Unsuccessful");}
                                                }
                                            }
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    backupRel.setVisibility(View.GONE);
                                                    message("Operation Unsuccessful");
                                                }
                                            });
                                            RequestQueue requestQueue1 = Volley.newRequestQueue(getApplicationContext());
                                            requestQueue1.add(stringRequest);
                                        }
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    backupRel.setVisibility(View.GONE);
                                    message("Operation Unsuccessful");
                                }
                            });
                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                            requestQueue.add(jsonObjectRequest);
                        }else {
                            Toast.makeText(getApplicationContext(),"Network Error",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
             dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    void message(String text){
        Toast.makeText(ShowAttentance.this,text,Toast.LENGTH_SHORT).show();
    }
    void findFile(String path,String Filename,String total,String name){
        FileInputStream fileInputStream = null;
        try {
            File dir = new File(path+Filename);
            fileInputStream = new FileInputStream(dir);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder  stringBuilder = new StringBuilder();
            String topic;

            while((topic = bufferedReader.readLine())!=null){
                stringBuilder.append(topic);
            }
            Map<String,String> map = new HashMap<>();
            map = MapConvertor.stringToMap(stringBuilder.toString());
            String filename = Filename.replace(".txt","");
            String topics = map.get("topic");
            String recorded = map.get("recorded");
            String absent = String.valueOf(map.size()-1);
            String present = String.valueOf(Integer.valueOf(total)-Integer.valueOf(absent));
            String percent = String.valueOf((Float.valueOf(present)/Float.valueOf(total))*100);
            ShowAttModel showAttModel = new ShowAttModel(filename,topics,total,present,absent,percent,name,recorded);
            arrayList.add(showAttModel);
            fileInputStream.close();
            inputStreamReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void viewRecord(int position)  {
        final String className = arrayList.get(position).getClassname();
        final String fileName = arrayList.get(position).getFileName();
        String month = "";
        String year = "";
        if(mode.equals("month")){
            month = getIntent().getStringExtra("month");
            year = getIntent().getStringExtra("year");
        }else {
            String ss = getIntent().getStringExtra("fileName");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
            try {
                Date date = simpleDateFormat.parse(ss);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                month = String.valueOf(calendar.get(Calendar.MONTH)+1);
                year = String.valueOf(calendar.get(Calendar.YEAR));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+className+"/"+fileName+".txt";
        FileInputStream fis = null;
        File file = new File(path);
        try {
            fis = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String topic;

            while ((topic=bufferedReader.readLine())!=null){
                stringBuilder.append(topic);
            }
            Intent intent = new Intent(getApplicationContext(),See_Record_List.class);
            intent.putExtra("record",stringBuilder.toString());
            intent.putExtra("classname",className);
            intent.putExtra("fileName",fileName);
            intent.putExtra("month",month);
            intent.putExtra("year",year);
            startActivity(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void upload(int position) {
        final String className = arrayList.get(position).getClassname();
        final String fileName = arrayList.get(position).getFileName();
        final String recorded = arrayList.get(position).getRecorded();
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+className+"/"+fileName+".txt";
        alertUpload.setMessage("Upload the attendance record..??")
                .setTitle("Upload")
                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        backupRel.setVisibility(View.VISIBLE);
                        backupreltext.setText("Uploading Record...Please wait.");
                    if(connection.connected()){
                        String month = "", year = "";
                        if(mode.equals("month")){
                            month = getIntent().getStringExtra("month");
                            year = getIntent().getStringExtra("year");
                        }else {
                            String ss = getIntent().getStringExtra("fileName");
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
                            try {
                                Date date = simpleDateFormat.parse(ss);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);
                                month = String.valueOf(calendar.get(Calendar.MONTH)+1);
                                year = String.valueOf(calendar.get(Calendar.YEAR));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }


                        final String mainurl = "https://socialapp-a4fdb.firebaseio.com/"+currentUser.getUid()+"/"+year+month+"/"+className+"/";
                        String query = "?orderBy=\"code\"&equalTo=\""+fileName+"\"";
                        String url  = mainurl+".json"+query.replace(" ","%20").replace("\"","%22");
                        File file = new File(path);
                        FileInputStream fileInputStream = null;
                        try {
                            fileInputStream = new FileInputStream(file);
                            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            StringBuilder stringBuilder = new StringBuilder();
                            String text;

                            while((text=bufferedReader.readLine())!=null){
                                stringBuilder.append(text);
                            }
                            final Map<String,Object> map = new HashMap<>();
                            map.put("attendance",stringBuilder.toString());
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JsonObjectRequest jsonObjectRequest = null;
                                if(!response.equals("{}")){
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    Iterator<String> iterator = jsonObject.keys();
                                    while(iterator.hasNext()){
                                        String key = iterator.next();
                                        String update = mainurl+key+"/.json";
                                         jsonObjectRequest = new JsonObjectRequest(Request.Method.PATCH, update, new JSONObject(map), new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                backupRel.setVisibility(View.GONE);
                                            message("Record Uploaded.");
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                backupRel.setVisibility(View.GONE);
                                                message("Record Not Uploaded.");
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                    map.put("code",fileName);
                                    map.put("date",ServerValue.TIMESTAMP);
                                    map.put("recorded",recorded);
                                    String newUpload = mainurl+".json";
                                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, newUpload, new JSONObject(map), new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        backupRel.setVisibility(View.GONE);
                                        message("Record Uploaded.");
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        backupRel.setVisibility(View.GONE);
                                        message("Record Not Uploaded.");
                                    }
                                }){
                                    @Override
                                    public Map<String,String> getHeaders() throws AuthFailureError{
                                        Map<String,String> hash = new HashMap<String, String>();
                                        hash.put("Content-Type","application/json; charset=utf-8");
                                        return hash;
                                    }
                                };
                            }
                            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                                if (jsonObjectRequest != null) {
                                    requestQueue.add(jsonObjectRequest);
                                }else {
                                    backupRel.setVisibility(View.GONE);
                                    message("Try Again");
                                }
                                requestQueue.getCache().clear();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                backupRel.setVisibility(View.GONE);
                                message("Record Not Updated.");
                            }
                        });
                        RequestQueue requestQueue1 = Volley.newRequestQueue(getApplicationContext());
                        requestQueue1.add(stringRequest);
                        requestQueue1.getCache().clear();
                        } catch (FileNotFoundException e) {
                            backupRel.setVisibility(View.GONE);
                            message("Operation Unsuccessful.");
                            e.printStackTrace();
                        } catch (IOException e) {
                            backupRel.setVisibility(View.GONE);
                            message("Operation Unsuccessful.");
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
                    }   else {
                        backupRel.setVisibility(View.GONE);
                        message("Network Error.");
                    }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alertUpload.create();
        dialog.show();
    }
}
