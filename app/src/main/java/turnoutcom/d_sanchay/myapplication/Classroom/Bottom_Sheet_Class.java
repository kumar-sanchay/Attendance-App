package turnoutcom.d_sanchay.myapplication.Classroom;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.text.DateFormat;
import java.util.Calendar;

import turnoutcom.d_sanchay.myapplication.Connection;
import turnoutcom.d_sanchay.myapplication.R;
import turnoutcom.d_sanchay.myapplication.SharedPrefManager.SharedPrefManager;

import static android.support.constraint.Constraints.TAG;

public class Bottom_Sheet_Class extends BottomSheetDialogFragment implements MonthYearPickerDialog.DataSet {
CardView takeAtt,viewAtt,editClass,share;
FirebaseUser currentUser;
FirebaseFirestore firestore;
Connection connection;
AlertDialog.Builder alertDialog;
int progress = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View  view = inflater.inflate(R.layout.bottom_nav_classroom,container,false);
        takeAtt = view.findViewById(R.id.takeAttendance);
        viewAtt = view.findViewById(R.id.viewAtt);
        editClass = view.findViewById(R.id.editClass);
        share = view.findViewById(R.id.shareClass);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        connection = new Connection(getContext());
        alertDialog = new AlertDialog.Builder(getContext());
        final String className = getArguments().getString("classname");
        final String total = getArguments().getString("total");
        final String from = getArguments().getString("from");
        final String to = getArguments().getString("to");
        final String codes = getArguments().getString("codes");

        takeAtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),AttSheet.class);
                intent.putExtra("name",className);
                intent.putExtra("total",total);
                intent.putExtra("from",from);
                intent.putExtra("to",to);
                intent.putExtra("codes",codes);
                getActivity().startActivity(intent);
            }
        });

        viewAtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            showDate();

            }
        });

        editClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(getContext(),Edit_Classroom.class);
            intent.putExtra("name",className);
            intent.putExtra("total",total);
            intent.putExtra("from",from);
            intent.putExtra("to",to);
            intent.putExtra("codes",codes);
            getActivity().startActivity(intent);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthYearPickerDialog pd = new MonthYearPickerDialog();
                pd.setListener(Bottom_Sheet_Class.this);
                pd.show(getActivity().getSupportFragmentManager(), "MonthYearPickerDialog");
            }
        });


        return view;
    }

    public boolean delete(File file){
        if(file.isDirectory()){
            for(File child:file.listFiles()){
                delete(child);
            }
        }
        return file.delete();
    }
    private void showDate() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        Calendar calendar = Calendar.getInstance();
        Bundle bundle = new Bundle();
        bundle.putInt("year",calendar.get(Calendar.YEAR));
        bundle.putInt("month",calendar.get(Calendar.MONTH));
        bundle.putInt("day",calendar.get(Calendar.DAY_OF_MONTH));

        datePickerFragment.setArguments(bundle);
        datePickerFragment.setCallback(ondate);
        datePickerFragment.show(getFragmentManager(),"Date Picker");

    }
    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.YEAR,year);
      calendar.set(Calendar.MONTH,month);
      calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
      String date = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
      String className = getArguments().getString("classname");
      String total = getArguments().getString("total");
      Intent intent = new Intent(getContext(),ShowAttentance.class);
      intent.putExtra("fileName",date);
      intent.putExtra("name",className);
      intent.putExtra("total",total);
      intent.putExtra("mode","date");
      getActivity().startActivity(intent);
        }
    };



    @Override
    public void onDataSet(int month, int year) {
        String className = getArguments().getString("classname");
        String total = getArguments().getString("total");
        Intent intent = new Intent(getContext(),ShowAttentance.class);
        intent.putExtra("month",String.valueOf(month));
        intent.putExtra("year",String.valueOf(year));
        intent.putExtra("mode","month");
        intent.putExtra("name",className);
        intent.putExtra("total",total);
        getActivity().startActivity(intent);
    }
}
