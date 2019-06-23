package turnoutcom.d_sanchay.myapplication.Classroom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import turnoutcom.d_sanchay.myapplication.Connection;

public class SaveAttendanceJob extends JobIntentService {
    FirebaseFirestore firestore;
    FirebaseUser currentUser;
    Handler handler = new Handler();
    public static void enquesWork(Intent intent, Context context){
        enqueueWork(context,SaveAttendanceJob.class,300,intent);
    }
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if(!isStopped()) {
            Calendar calendar = Calendar.getInstance();
            String year = String.valueOf(calendar.get(Calendar.YEAR));
            String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
            final String dates = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
            String attendance = intent.getStringExtra("attendance");
            String name = intent.getStringExtra("className");
            String code = intent.getStringExtra("code");
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            firestore = FirebaseFirestore.getInstance();
            Map<String,Object> map = new HashMap<>();
            map.put("attendance",attendance);
            map.put("date", ServerValue.TIMESTAMP);
            map.put("code",code);
            map.put("recorded",dates);

            if(currentUser!=null){
                   String url = "https://socialapp-a4fdb.firebaseio.com/"+currentUser.getUid()+"/"+year+month+"/"+name+"/.json";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(map), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Record Uploaded",Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Record not Uploaded",Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    public Map<String,String> getHeaders() throws AuthFailureError{
                        HashMap<String,String> hashMap = new HashMap<String, String>();
                        hashMap.put("Content-Type","application/json; charset=utf-8");
                        return hashMap;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                requestQueue.add(jsonObjectRequest);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onStopCurrentWork() {
        return super.onStopCurrentWork();
    }

}
