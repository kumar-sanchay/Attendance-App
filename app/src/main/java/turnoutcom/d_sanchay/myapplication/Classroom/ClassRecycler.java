package turnoutcom.d_sanchay.myapplication.Classroom;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Random;

import turnoutcom.d_sanchay.myapplication.R;

public class ClassRecycler extends RecyclerView.Adapter<ClassRecycler.ViewHolder> {
    ArrayList<ClassModel> list;
    Context context;
    int imgNum;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;
    OnClickListener listener;
    DeleteClass deleteClass;
    ChangeRoll changeRoll;
    public ClassRecycler(ArrayList<ClassModel> list,int imgNum){this.list = list;this.imgNum = imgNum;}

    public interface DeleteClass{
        void OnDelete(int position);
    }
    public interface OnClickListener{
        void OnClick(int position);
    }
    public interface ChangeRoll{
        void Change(int position);
    }
    public void setChangeRoll(ChangeRoll changeRoll){this.changeRoll=changeRoll;}
    public void setOnDelete(DeleteClass deleteClass){this.deleteClass=deleteClass;}
    public void setItemOnClickListener(OnClickListener listener){
        this.listener = listener;
    }
    @NonNull
    @Override
    public ClassRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.class_layout,viewGroup,false);
        context = viewGroup.getContext();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Background");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassRecycler.ViewHolder viewHolder, int i) {
        final String classname = list.get(i).getClassName();
        final String total = list.get(i).getTotal();
        String status = list.get(i).getStatus();
        String random = list.get(i).getRamdom();

    viewHolder.setClass(classname,total,status,random);

    }
    public void update(ArrayList<ClassModel> newList){
    this.list = newList;
    notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView className,totalStudents;
        View mView;
        ImageView imageView,delete,settings;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            className = mView.findViewById(R.id.className);
            totalStudents = mView.findViewById(R.id.totalAtt);
            imageView = mView.findViewById(R.id.classImageView);
            delete = mView.findViewById(R.id.takeAtt);
            settings = mView.findViewById(R.id.settingRoll);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(deleteClass!=null){
                        int position = getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){
                            deleteClass.OnDelete(position);
                        }
                    }
                }
            });
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.OnClick(position);
                        }
                    }
                }
            });

            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(changeRoll!=null){
                        int position = getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){
                            changeRoll.Change(position);
                        }
                    }
                }
            });
        }


        public void setClass(String name, String total, String status, final String random){
            className.setText(name);
            if(status.equals("b")){
                totalStudents.setText(total+" Students");
            }else {
                totalStudents.setText(total);
                settings.setVisibility(View.VISIBLE);
            }

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String url = dataSnapshot.child(random).getValue().toString();
                    Glide.with(context).load(url).into(imageView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
}
