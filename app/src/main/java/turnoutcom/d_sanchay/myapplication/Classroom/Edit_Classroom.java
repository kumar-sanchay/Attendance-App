package turnoutcom.d_sanchay.myapplication.Classroom;

import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.MapConvertor;
import turnoutcom.d_sanchay.myapplication.R;

public class Edit_Classroom extends AppCompatActivity {
EditText total,from,to,initial;
FirebaseUser currentUser;
FirebaseFirestore firestore;
CircularProgressButton circularProgressButton;
Map add = null;
Connection connection=new Connection(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__classroom);
        total = findViewById(R.id.edit_total_stu);
        from = findViewById(R.id.edit_from_stu);
        to = findViewById(R.id.edit_to_stu);
        initial = findViewById(R.id.edit_codes);
        firestore = FirebaseFirestore.getInstance();
        circularProgressButton = findViewById(R.id.edit_class);
        circularProgressButton.setIndeterminateProgressMode(true);
        circularProgressButton.setTextColor(Color.WHITE);
        circularProgressButton.setText("Proceed");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
       final String className = getIntent().getStringExtra("name");
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+className+"/"+className+".txt";

        File f = new File(path);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(f);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            add = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
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

        total.setText(add.get("total").toString());
        from.setText(add.get("from").toString());
        to.setText(add.get("to").toString());
        initial.setText(add.get("codes").toString());
        final String perviousCode = add.get("codes").toString();
        circularProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(circularProgressButton.getProgress()==-1){
                    circularProgressButton.setProgress(0);
                    circularProgressButton.setIdleText("Proceed Again");
                    circularProgressButton.setTextColor(Color.WHITE);
                }else if(circularProgressButton.getProgress()==0){
                    circularProgressButton.setProgress(30);
                    if(connection.connected()){
                        String totals = total.getText().toString();
                        String froms = from.getText().toString();
                        String tos = to.getText().toString();
                        final String initials = initial.getText().toString();
                        if(!totals.isEmpty() || !froms.isEmpty() || !tos.isEmpty()){
                            final Map<String,Object> map = new HashMap<>();
                            map.put("total",totals);
                            map.put("from",froms);
                            map.put("to",tos);
                            map.put("codes",initials);
                            firestore.collection("Users").document(currentUser.getUid()).collection("Classroom").document(className).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        File file = new File(path);
                                        FileOutputStream fos = null;

                                        try {
                                            String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+className+"/";
                                            File files = new File(path);
                                            File[] f = files.listFiles();
                                            for(File fs : f){
                                                    if(!fs.getName().equals(className+".txt")){
                                                     FileInputStream fois = new FileInputStream(fs);
                                                        InputStreamReader inputStreamReader = new InputStreamReader(fois);
                                                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                                        StringBuilder stringBuilder = new StringBuilder();
                                                        String text;
                                                        while((text=bufferedReader.readLine())!=null){
                                                            stringBuilder.append(text);
                                                        }
                                                        Map<String,String> absent= new HashMap<>();
                                                        Map<String,String> changed = new HashMap<>();
                                                        absent = MapConvertor.stringToMap(stringBuilder.toString());
                                                        for(Map.Entry ab: absent.entrySet()){
                                                            String key = "";
                                                            if(!ab.getKey().equals("topic")){
                                                                key = ab.getKey().toString().replace(perviousCode,"");
                                                                changed.put(initials+key,initials+key);
                                                            }else {
                                                                changed.put(ab.getKey().toString(),ab.getValue().toString());
                                                            }

                                                        }
                                                        String finalized = MapConvertor.mapToString(changed);
                                                        File newFile = new File(path+fs.getName());
                                                        FileOutputStream fstream = new FileOutputStream(newFile);
                                                        fstream.write(finalized.getBytes());
                                                        fstream.close();
                                                    }else {
                                                        fos = new FileOutputStream(file);
                                                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos);
                                                        map.put("status","b");
                                                        map.put("random",add.get("random"));
                                                        objectOutputStream.writeObject(map);
                                                        objectOutputStream.close();
                                                        fos.close();
                                                    }
                                            }
                                            Intent intent = new Intent(getApplicationContext(),Classroom.class);
                                            startActivity(intent);
                                            finish();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                            circularProgressButton.setProgress(-1);
                                            circularProgressButton.setErrorText("Error.Try Again.");
                                            circularProgressButton.setTextColor(Color.WHITE);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            circularProgressButton.setProgress(-1);
                                            circularProgressButton.setErrorText("Error.Try Again.");
                                            circularProgressButton.setTextColor(Color.WHITE);
                                        }
                                    }   else {
                                        circularProgressButton.setProgress(-1);
                                        circularProgressButton.setErrorText("Error.Try Again.");
                                        circularProgressButton.setTextColor(Color.WHITE);

                                    }
                                }
                            });
                        }
                    }else {
                        circularProgressButton.setProgress(-1);
                        circularProgressButton.setErrorText("Needs Internet Connection");
                        circularProgressButton.setTextColor(Color.WHITE);
                    }
                }
            }
        });

    }
}
