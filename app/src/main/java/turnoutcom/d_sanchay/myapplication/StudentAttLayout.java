package turnoutcom.d_sanchay.myapplication;

import android.app.DatePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import turnoutcom.d_sanchay.myapplication.Classroom.DatePickerFragment;
import turnoutcom.d_sanchay.myapplication.Classroom.StudentViewModel;
import turnoutcom.d_sanchay.myapplication.Classroom.StudentViewRecycler;

public class StudentAttLayout extends AppCompatActivity {
RecyclerView recyclerView;
StudentViewRecycler studentViewRecycler;
ArrayList<StudentViewModel> list;
DatabaseReference databaseReference ;
Connection connection;
RelativeLayout relativeLayout;
ImageView monthPicker;
String userId = "",classname = "",roll = "",date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_att_layout);
        recyclerView = findViewById(R.id.student_recycle);
        monthPicker = findViewById(R.id.student_select_date);
        Calendar calendar = Calendar.getInstance();
         date = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        final String year = String.valueOf(calendar.get(Calendar.YEAR));
        final String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        list = new ArrayList<>();
        relativeLayout = findViewById(R.id.progress_student);
        connection = new Connection(this);
        studentViewRecycler = new StudentViewRecycler(list);
       recyclerView.setLayoutManager(new LinearLayoutManager(this));
       recyclerView.setHasFixedSize(true);
       recyclerView.setAdapter(studentViewRecycler);
       classname = getIntent().getStringExtra("classname");
       String username = getIntent().getStringExtra("teacher");
       roll = getIntent().getStringExtra("codes");

       databaseReference = FirebaseDatabase.getInstance().getReference(String.valueOf(username.charAt(0))).child(username);

       databaseReference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userId = dataSnapshot.child("userId").getValue().toString();
               final String url = "https://socialapp-a4fdb.firebaseio.com/"+userId+"/"+year+month+"/"+classname+"/";
               String query = "?orderBy=\"recorded\"&equalTo=\""+date+"\"";
               String finalUrl = url+".json"+query.replace(" ","%20").replace("\"","%22");
               giveOutput(finalUrl,roll);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {
               StudentViewModel studentViewModel = new StudentViewModel("0","","","","");
               list.add(studentViewModel);
               studentViewRecycler.notifyDataSetChanged();
           }
       });

       monthPicker.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               DatePickerFragment datePickerFragment = new DatePickerFragment();
               Calendar calendar = Calendar.getInstance();
               Bundle bundle = new Bundle();
               bundle.putInt("year",calendar.get(Calendar.YEAR));
               bundle.putInt("month",calendar.get(Calendar.MONTH));
               bundle.putInt("day",calendar.get(Calendar.DAY_OF_MONTH));
               datePickerFragment.setArguments(bundle);
               datePickerFragment.setCallback(ondate);
               datePickerFragment.show(getSupportFragmentManager(),"Date Picker");
           }
       });


    }
    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            String datess = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
            if(!datess.equals(date)){
            final String url = "https://socialapp-a4fdb.firebaseio.com/"+userId+"/"+String.valueOf(year)+String.valueOf(month+1)+"/"+classname+"/";
            String query = "?orderBy=\"recorded\"&equalTo=\""+datess+"\"";
            String finalUrl = url+".json"+query.replace(" ","%20").replace("\"","%22");
            giveOutput(finalUrl,roll);}
        }
    };

    public void giveOutput(String finalUrl, final String rolls){
        if(connection.connected()){
            StringRequest stringRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if(response!=null){
                        if(!response.equals("{}")){
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Iterator<String> iterator = jsonObject.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    JSONObject json = jsonObject.getJSONObject(key);
                                    String att = json.getString("attendance");
                                    String code = json.getString("recorded");
                                    long dates = json.getLong("date");
                                    String status = "";

                                    Map<String,String> map = MapConvertor.stringToMap(att);
                                    if(map.containsValue(rolls)){
                                        status = "Absent";
                                    }else {
                                        status = "Present";
                                    }
                                    StudentViewModel studentViewModel = new StudentViewModel("1",status,code,String.valueOf(dates),map.get("topic"));
                                    list.add(studentViewModel);
                                    studentViewRecycler.notifyDataSetChanged();
                                    relativeLayout.setVisibility(View.GONE);
                                }



                            } catch (JSONException e) {
                                StudentViewModel studentViewModel = new StudentViewModel("Something went wrong..please try Again..!!","","","","");
                                list.add(studentViewModel);
                                studentViewRecycler.notifyDataSetChanged();
                                relativeLayout.setVisibility(View.GONE);

                            }
                        }else{
                            StudentViewModel studentViewModel = new StudentViewModel("No records Available at this moment...!!","","","","");
                            list.add(studentViewModel);
                            studentViewRecycler.notifyDataSetChanged();
                            relativeLayout.setVisibility(View.GONE);
                        }
                    }else {
                        StudentViewModel studentViewModel = new StudentViewModel("No records Available at this moment...!!","","","","");
                        list.add(studentViewModel);
                        studentViewRecycler.notifyDataSetChanged();
                        relativeLayout.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    StudentViewModel studentViewModel = new StudentViewModel("No records Available...!!","","","","");
                    list.add(studentViewModel);
                    studentViewRecycler.notifyDataSetChanged();
                    relativeLayout.setVisibility(View.GONE);
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
            requestQueue.getCache().clear();
        }else {
            StudentViewModel studentViewModel = new StudentViewModel("Internet Connection Required...:(","","","","");
            list.add(studentViewModel);
            studentViewRecycler.notifyDataSetChanged();
            relativeLayout.setVisibility(View.GONE);
        }
    }
}
