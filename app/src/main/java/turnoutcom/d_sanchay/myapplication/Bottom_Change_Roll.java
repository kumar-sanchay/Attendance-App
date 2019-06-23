package turnoutcom.d_sanchay.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import turnoutcom.d_sanchay.myapplication.Classroom.Classroom;
import turnoutcom.d_sanchay.myapplication.Classroom.JoinClassroom;

public class Bottom_Change_Roll extends BottomSheetDialogFragment {
EditText change;
CircularProgressButton progressButton;
FirebaseFirestore firestore;
FirebaseUser currentUser;
Map<String,Object> details = null;
int STORAGE_PERMISSION_CODE = 1;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View  view = inflater.inflate(R.layout.change_roll_no,container,false);
        change = view.findViewById(R.id.changeRoll);
        firestore = FirebaseFirestore.getInstance();
        final String classname = getArguments().getString("classname");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        progressButton = view.findViewById(R.id.changeRollButton);
        progressButton.setText("Change Roll no.");
        progressButton.setTextColor(Color.WHITE);
        progressButton.setIndeterminateProgressMode(true);
        if(ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){

        }else {requestStoragePermission();}


        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Turnout/"+classname+"/"+classname+".txt";
        File file = new File(path);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            details = (HashMap) objectInputStream.readObject();
            change.setText(details.get("codes").toString());
            objectInputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            progressButton.setProgress(-1);
            progressButton.setErrorText("Operation Failed.");
            progressButton.setTextColor(Color.WHITE);
            e.printStackTrace();
        } catch (IOException e) {
            progressButton.setProgress(-1);
            progressButton.setErrorText("Operation Failed.");
            progressButton.setTextColor(Color.WHITE);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            progressButton.setProgress(-1);
            progressButton.setErrorText("Operation Failed.");
            progressButton.setTextColor(Color.WHITE);
            e.printStackTrace();
        }


        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(progressButton.getProgress()==-1){
                    progressButton.setProgress(0);
                    progressButton.setIdleText("Proceed Again.");
                    progressButton.setTextColor(Color.WHITE);
                }else if(progressButton.getProgress()==0){
                    progressButton.setProgress(40);
                    final String edit = change.getText().toString();
                    if(!edit.equals("")){
                        final Map<String,Object> map = new HashMap<>();
                        map.put("codes",edit);
                        firestore.collection("Users").document(currentUser.getUid()).collection("Classroom").document(classname).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                try {
                                    details.put("codes",edit);
                                    FileOutputStream fileOutputStream = new FileOutputStream(path);
                                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                                    objectOutputStream.writeObject(details);
                                    objectOutputStream.close();
                                    fileOutputStream.close();
                                    startActivity(new Intent(view.getContext(), Classroom.class));
                                    getActivity().finish();
                                } catch (FileNotFoundException e) {
                                    progressButton.setProgress(-1);
                                    progressButton.setErrorText("Operation Failed.");
                                    progressButton.setTextColor(Color.WHITE);
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    progressButton.setProgress(-1);
                                    progressButton.setErrorText("Operation Failed.");
                                    progressButton.setTextColor(Color.WHITE);
                                    e.printStackTrace();
                            }    } else {
                                progressButton.setProgress(-1);
                                progressButton.setErrorText("Operation Failed.");
                                progressButton.setTextColor(Color.WHITE);
                            }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressButton.setProgress(-1);
                                progressButton.setErrorText("Operation Failed.");
                                progressButton.setTextColor(Color.WHITE);
                            }
                        });
                    }else {
                        progressButton.setProgress(-1);
                        progressButton.setErrorText("Fill the Field.");
                        progressButton.setTextColor(Color.WHITE);
                    }
                }
            }
        });
        return view;
    }

    private void requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle("Permission Needed")
                    .setMessage("This will help you to save attendance offline network issues..!!")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
                        }
                    }).setNegativeButton("Never", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else{
            ActivityCompat.requestPermissions(getActivity(),new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(),"Thanks for granting",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getContext(),"Permission Denied",Toast.LENGTH_LONG).show();
            }
        }
    }
}
