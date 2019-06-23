package turnoutcom.d_sanchay.myapplication.Classroom;

import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.WithHint;
import android.view.View;
import android.widget.EditText;

import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.MapConvertor;
import turnoutcom.d_sanchay.myapplication.R;

public class AttSheet extends AppCompatActivity implements AttendanceAdapter.OnItemClickListener {
RecyclerView recyclerView;
EditText topic;
ArrayList<RollNo> list;
Map<String,String> absent;
AttendanceAdapter attendanceAdapter;
CircularProgressButton progressButton;
FirebaseUser currentUser;
FirebaseFirestore firestore;
Connection connection;
LinearLayoutManager linearLayoutManager;
HashMap<String,Integer> map;
 String path;
 Handler handler;
 String uploaded= "";
 int fcount = 0;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_att_sheet);
        topic = findViewById(R.id.topicName);
        recyclerView = findViewById(R.id.attendanceSheet);
        list = new ArrayList<>();
        absent = new HashMap<>();
        map = new HashMap<>();
        handler = new Handler();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        connection = new Connection(this);
        progressButton = findViewById(R.id.doneAtt);
        attendanceAdapter = new AttendanceAdapter(list);
        recyclerView.setAdapter(attendanceAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        attendanceAdapter.setItemOnClickListener(AttSheet.this);
        Calendar calendar = Calendar.getInstance();
        final String dates = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        final String codes = getIntent().getStringExtra("codes");
       final String name = getIntent().getStringExtra("name");
        final int from = Integer.valueOf(getIntent().getStringExtra("from"));
        final int to = Integer.valueOf(getIntent().getStringExtra("to"));
        progressButton.setText("Save");
        progressButton.setIndeterminateProgressMode(true);
        path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+name+"/"+ dates+".txt";
        for(int i = to;i<=from;i++){
            String word = "";
            if(i<=9){
                word = codes+"0"+String.valueOf(i);
            }else {
             word = codes+String.valueOf(i);}
            RollNo rollNo = new RollNo(word);
            list.add(rollNo);
            map.put(word,1);
            attendanceAdapter.notifyDataSetChanged();
        }

        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View v) {
                if(progressButton.getProgress()==-1){
                    progressButton.setProgress(0);
                    progressButton.setIdleText("Proceed Again");
                    progressButton.setTextColor(Color.WHITE);
                }
        else if(progressButton.getProgress()==0) {
        progressButton.setProgress(30);
        String topicName = topic.getText().toString();
        absent.put("topic",topicName);
        absent.put("recorded",dates);
        String att = MapConvertor.mapToString(absent);
        FileOutputStream fileOutputStream = null;
                try {
                    while (true){
                        File file = new File(path);
                        if(file.exists()){
                            path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+name+"/"+ dates+"("+String.valueOf(++fcount)+")"+".txt";
                        }else {
                            break;
                        }
                    }
                    Intent intent = new Intent(AttSheet.this,SaveAttendanceJob.class);
                    intent.putExtra("className",name);
                    intent.putExtra("attendance",att);
                    if(fcount==0)
                         intent.putExtra("code",dates);
                    else
                        intent.putExtra("code",dates+"("+String.valueOf(fcount)+")");
                    SaveAttendanceJob.enquesWork(intent,AttSheet.this);

                    fileOutputStream = new FileOutputStream(new File(path));
                    fileOutputStream.write((att).getBytes());
                } catch (FileNotFoundException e) {
                    progressButton.setProgress(-1);
                    progressButton.setErrorText("Something went wrong.");
                    progressButton.setTextColor(Color.WHITE);
                    e.printStackTrace();
                } catch (IOException e) {
                    progressButton.setProgress(-1);
                    progressButton.setErrorText("Something went wrong.");
                    progressButton.setTextColor(Color.WHITE);
                    e.printStackTrace();
                } finally {
                    try {
                        if(fileOutputStream!=null)
                              fileOutputStream.close();
                        progressButton.setProgress(100);
                        progressButton.setCompleteText("Record Saved.");
                        progressButton.setTextColor(Color.WHITE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }}
        });
    }


    @Override
    public void onItemClick(int position) {
        int getMap = map.get(list.get(position).getRollno());
        if(getMap%2!=0) {
            absent.put(list.get(position).getRollno(),list.get(position).getRollno());
            map.put(list.get(position).getRollno(),++getMap);
        }else {
            absent.remove(list.get(position).getRollno());
            map.put(list.get(position).getRollno(),++getMap);
        }
    }

}