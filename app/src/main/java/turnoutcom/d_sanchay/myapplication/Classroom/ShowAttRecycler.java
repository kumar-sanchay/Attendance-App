package turnoutcom.d_sanchay.myapplication.Classroom;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import turnoutcom.d_sanchay.myapplication.R;

public class ShowAttRecycler extends RecyclerView.Adapter<ShowAttRecycler.ViewHolder> {
    ArrayList<ShowAttModel> list;
    DeleteAtt deleteAtt;
    ViewRecord viewRecordAtt;
    Upload uploads;
    public ShowAttRecycler(ArrayList<ShowAttModel> list){this.list=list;}

    public interface DeleteAtt{
        void onClick(int position);
    }
    public interface ViewRecord{
        void viewRecord(int position);
    }
    public interface Upload{
        void upload(int position);
    }
    public void setViewRecord(ViewRecord viewRecordAtt){this.viewRecordAtt=viewRecordAtt;}
    public void setOnClick(DeleteAtt deleteAtt){
        this.deleteAtt = deleteAtt;
    }
    public void setUpload(Upload uploads){this.uploads=uploads;}
    @NonNull
    @Override
    public ShowAttRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.att_record,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowAttRecycler.ViewHolder viewHolder, int i) {
    String fileName = list.get(i).getFileName();
    String topic = "Topic : "+list.get(i).getTopic();
    String total = "Total Students : "+list.get(i).getTotalStudents();
    String presnt = "Presnt Students : "+list.get(i).getPresentStudents();
    String absent = "Absent Students : "+list.get(i).getAbsentStudents();
    String persent = list.get(i).getPersent()+"%";
    viewHolder.setText(fileName,topic,total,presnt,absent,persent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView filename,topic,total,present,absent,persent;
        Button delete,viewRecord,upload;
        View mView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            filename = mView.findViewById(R.id.filename);
            topic = mView.findViewById(R.id.topic_showAtt);
            total = mView.findViewById(R.id.totalStudents);
            present = mView.findViewById(R.id.present_students);
            absent = mView.findViewById(R.id.absent_students);
            persent = mView.findViewById(R.id.percent);
            delete = mView.findViewById(R.id.deleteAttRecord);
            viewRecord = mView.findViewById(R.id.viewRecord);
            upload = mView.findViewById(R.id.upload);

            viewRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if(viewRecordAtt!=null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        viewRecordAtt.viewRecord(position);
                    }
                }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(deleteAtt!=null){
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                    deleteAtt.onClick(position);}
                }}
            });

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(upload!=null){
                        int position = getAdapterPosition();
                        if(position!=RecyclerView.NO_POSITION){
                            uploads.upload(position);
                        }
                    }
                }
            });

        }

        public void setText(String text,String topics,String totals,String presents,String absents,String persents){
            filename.setText(text);
            topic.setText(topics);
            total.setText(totals);
            present.setText(presents);
            absent.setText(absents);
            persent.setText(persents);
        }
    }
}
