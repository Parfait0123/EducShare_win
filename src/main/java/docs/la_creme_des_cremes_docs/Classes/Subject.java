package docs.la_creme_des_cremes_docs.Classes;

public class Subject {
    private int semester;
    private String name;
    private int ueNbr;
    private int loan;
    private String teacherSName;
    private String id;
    private int n;

    public Subject(int semester, String name, int ueNbr, int loan, String teacherSName, String id) {
        this.semester = semester;
        this.name = name;
        this.ueNbr = ueNbr;
        this.loan = loan;
        this.teacherSName = teacherSName;
        this.id = id;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUeNbr() {
        return ueNbr;
    }

    public void setUeNbr(int ueNbr) {
        this.ueNbr = ueNbr;
    }

    public int getLoan() {
        return loan;
    }

    public void setLoan(int loan) {
        this.loan = loan;
    }

    public String getTeacherSName() {
        return teacherSName;
    }

    public void setTeacherSName(String teacherSName) {
        this.teacherSName = teacherSName;
    }

    @Override
    public String toString() {
        return name.trim();
    }
}
