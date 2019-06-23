package turnoutcom.d_sanchay.myapplication.Classroom;

public class StudentViewModel {
   private String attStatus,recordedDate,uploadedDate,network,topic;

    public StudentViewModel() {
    }

    public StudentViewModel(String network,String attStatus, String recordedDate, String uploadedDate,String topic) {
        this.attStatus = attStatus;
        this.recordedDate = recordedDate;
        this.uploadedDate = uploadedDate;
        this.network = network;
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public String getNetwork() {
        return network;
    }

    public String getAttStatus() {
        return attStatus;
    }

    public String getRecordedDate() {
        return recordedDate;
    }

    public String getUploadedDate() {
        return uploadedDate;
    }

}
