package turnoutcom.d_sanchay.myapplication.Classroom;

public class RollNo {
    String rollno,status;

    public RollNo(String rollno) {
        this.rollno = rollno;
    }

    public RollNo() {
    }
    public RollNo(String rollno,String status){
        this.rollno = rollno;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getRollno() {
        return rollno;
    }

    public void setRollno(String rollno) {
        this.rollno = rollno;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
