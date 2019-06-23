package turnoutcom.d_sanchay.myapplication.Classroom;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import turnoutcom.d_sanchay.myapplication.R;

public class StudentViewRecycler extends RecyclerView.Adapter<StudentViewRecycler.ViewHolder> {
ArrayList<StudentViewModel> list;

public  StudentViewRecycler(ArrayList<StudentViewModel> list){this.list=list;}
    @NonNull
    @Override
    public StudentViewRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.student_record_layout,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewRecycler.ViewHolder viewHolder, int i) {
    String network = list.get(i).getNetwork();
        if (network.equals("1")) {
            String status = list.get(i).getAttStatus();
            String recorded = list.get(i).getRecordedDate();
            String uploaded = list.get(i).getUploadedDate();
            String topic = list.get(i).getTopic();
            viewHolder.setData(status,recorded,uploaded,topic);
        } else {
            viewHolder.setErrorData(network);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView status,recorded,uploaded,states,recordedOn,uploadedOn,netWork,topic;
        View mView;
        CardView show,error;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            status = mView.findViewById(R.id.statusRecord);
            recorded = mView.findViewById(R.id.recordedDate);
            states = mView.findViewById(R.id.state);
            recordedOn = mView.findViewById(R.id.recordedOn);
            uploadedOn = mView.findViewById(R.id.uploadedOn);
            uploaded = mView.findViewById(R.id.uploadedDate);
            netWork = mView.findViewById(R.id.networkStatus);
            show = mView.findViewById(R.id.showAttStu);
            error = mView.findViewById(R.id.showStatus);
            topic = mView.findViewById(R.id.topic_student);
        }

        private void setData(String statuss,String recordeds,String uploadeds,String topics){
            show.setVisibility(View.VISIBLE);
        status.setText(statuss);
        recorded.setText(recordeds);
        uploaded.setText(setDate(Long.valueOf(uploadeds)));
        topic.setText(topics);
        }

        private void setErrorData(String data){
        error.setVisibility(View.VISIBLE);
        netWork.setText(data);
        }

    }

    private String setDate(long dt){
        Date date = new Date(dt);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy");
        return String.valueOf(simpleDateFormat.format(date));
    }
}
