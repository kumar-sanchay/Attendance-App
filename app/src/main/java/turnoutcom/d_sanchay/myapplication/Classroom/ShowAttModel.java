package turnoutcom.d_sanchay.myapplication.Classroom;

public class ShowAttModel {
   private String fileName,topic,totalStudents,presentStudents,absentStudents,persent,classname,recorded;

    public ShowAttModel(String fileName, String topic, String totalStudents, String presentStudents, String absentStudents, String persent,String name,String recorded) {
        this.fileName = fileName;
        this.topic = topic;
        this.totalStudents = totalStudents;
        this.presentStudents = presentStudents;
        this.absentStudents = absentStudents;
        this.persent = persent;
        classname = name;
        this.recorded = recorded;
    }

    public String getClassname() {
        return classname;
    }

    public ShowAttModel() {
    }

    public String getRecorded() {
        return recorded;
    }

    public String getTotalStudents() {
        return totalStudents;
    }

    public String getPresentStudents() {
        return presentStudents;
    }

    public String getAbsentStudents() {
        return absentStudents;
    }

    public String getPersent() {
        return persent;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTopic() {
        return topic;
    }
}
