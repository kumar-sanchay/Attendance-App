package turnoutcom.d_sanchay.myapplication.Classroom;

public class ClassModel {
    private String className,total,to,from,codes,status,ramdom;

    public ClassModel(String className, String total, String from, String to, String codes,String status,String random) {
        this.className = className;
        this.total = total;
        this.to = to;
        this.from = from;
        this.codes = codes;
        this.status= status;
        this.ramdom = random;
    }

    public ClassModel() {
    }

    public String getRamdom() {
        return ramdom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getCodes() {
        return codes;
    }

    public void setCodes(String codes) {
        this.codes = codes;
    }
}
