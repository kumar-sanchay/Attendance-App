package turnoutcom.d_sanchay.myapplication.Classroom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
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
import java.util.Random;

import javax.annotation.Nullable;

import turnoutcom.d_sanchay.myapplication.SharedPrefManager.SharedPrefManager;

public class CreateClassJob extends JobIntentService {
    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;
    int numClass;
    static void enque(Context context,Intent intent){
        enqueueWork(context,CreateClassJob.class,1000,intent);
    }
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final Bundle bundle = new Bundle();
        final ResultReceiver resultReceiver = intent.getParcelableExtra("receiver");
        if(!isStopped()){
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String name = intent.getStringExtra("name");
        final String total= intent.getStringExtra("total");
        final String from =  intent.getStringExtra("from");
        final String to = intent.getStringExtra("to");
        final String codes = intent.getStringExtra("codes");
        final String random = intent.getStringExtra("random");
        try {
            numClass = Integer.valueOf(SharedPrefManager.getmInstances(this).getClassBackup());
        }catch (NumberFormatException e){
            numClass = 0;
            final Query query = firestore.collection("Users").document(firebaseUser.getUid()).collection("Classroom");
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


        Map<String,String> map = new HashMap<>();
        map.put("total",total);
        map.put("from",from);
        map.put("to",to);
        map.put("codes",codes);
        map.put("random",random);
        map.put("att","0");
        map.put("status","b");
        firestore.collection("Users").document(firebaseUser.getUid()).collection("Classroom").document(name)
                .set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
           if(task.isSuccessful()){
               numClass +=1;
               SharedPrefManager.getmInstances(getApplicationContext()).setClassBackup(String.valueOf(numClass));
               bundle.putString("data","true");
               resultReceiver.send(10,bundle);
           }     else {
               bundle.putString("data","false");
               resultReceiver.send(10,bundle);
           }
            }
        });}else {
            bundle.putString("data","false");
            resultReceiver.send(10,bundle);
        }
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
