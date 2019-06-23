package turnoutcom.d_sanchay.myapplication.Classroom;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.HashMap;

import turnoutcom.d_sanchay.myapplication.R;
public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
    private ArrayList<RollNo> list;
    private ArrayList<String> absent;
    private FirebaseUser currentUser;
    private Context context;
    private OnItemClickListener listener;
    SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();
    public AttendanceAdapter(ArrayList<RollNo> list){this.list=list;}
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setItemOnClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    @NonNull
    @Override
    public AttendanceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.attendance,viewGroup,false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        absent = new ArrayList<>();
        context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AttendanceAdapter.ViewHolder viewHolder, int i) {
    final String roll = list.get(i).getRollno();
    if(currentUser!=null){
    viewHolder.sheet(roll);
    }
    viewHolder.bind(i);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView rollNo;
        RelativeLayout relativeLayout;
        View mView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            cardView = mView.findViewById(R.id.classCard);
            rollNo = mView.findViewById(R.id.rollNo);
            relativeLayout =  mView.findViewById(R.id.rollNoProgress);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if(listener != null){
                            int position = getAdapterPosition();
                            if(position != RecyclerView.NO_POSITION){
                                if(!sparseBooleanArray.get(position,false)){
                                    relativeLayout.setVisibility(View.VISIBLE);
                                    sparseBooleanArray.put(position,true);
                                }else {
                                    relativeLayout.setVisibility(View.GONE);
                                    sparseBooleanArray.put(position,false);
                                }
                                listener.onItemClick(position);
                            }
                        }

                }
            });
        }

        public void sheet(String roll){
            rollNo.setText(roll);
        }

        void bind(int position){
            if(!sparseBooleanArray.get(position,false)){
                relativeLayout.setVisibility(View.GONE);
            }else {
                relativeLayout.setVisibility(View.VISIBLE);
            }

        }

    }
}
