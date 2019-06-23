package turnoutcom.d_sanchay.myapplication.Classroom;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import turnoutcom.d_sanchay.myapplication.R;

public class ShowAttRecordRecycler extends RecyclerView.Adapter<ShowAttRecordRecycler.ViewHolder> {
    ArrayList<RollNo> list;
    SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();
    ChangeRecord changeRecord;
    public interface ChangeRecord{
        void onClickRecord(int position);
    }
    public void setChangeRecord(ChangeRecord changeRecord){this.changeRecord=changeRecord;}
    public ShowAttRecordRecycler(ArrayList<RollNo> list){this.list=list;}
    @NonNull
    @Override
    public ShowAttRecordRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.attendance,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowAttRecordRecycler.ViewHolder viewHolder, int i) {
    String roll  = list.get(i).getRollno();
    String status = list.get(i).getStatus();
    viewHolder.setRecord(roll,status);
    if(status.equals("Absent")){
        sparseBooleanArray.put(i,false);
      }else {
        sparseBooleanArray.put(i,true);
    }

      viewHolder.bind(i);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void update(ArrayList<RollNo> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public void absent(ArrayList<RollNo> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView rollno,status;
        View mView;
        RelativeLayout relativeLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            rollno = mView.findViewById(R.id.rollNo);
            status = mView.findViewById(R.id.att_record_status);
            relativeLayout = mView.findViewById(R.id.red_record);
            status.setVisibility(View.VISIBLE);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(changeRecord!=null){
                        int position = getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){
                            changeRecord.onClickRecord(position);
                        }
                    }
                }
            });
        }

        void setRecord(String roll,String Status){
            rollno.setText(roll);
            status.setText(Status);
        }

        void bind(int position){
            if(!sparseBooleanArray.get(position,false)){
                relativeLayout.setBackgroundColor(Color.RED);
                rollno.setTextColor(Color.WHITE);
                status.setTextColor(Color.WHITE);
            }else {
                relativeLayout.setBackgroundColor(Color.WHITE);
                rollno.setTextColor(Color.BLACK);
                status.setTextColor(Color.BLACK);
            }
        }


    }

}
