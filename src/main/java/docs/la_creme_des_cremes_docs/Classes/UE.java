package docs.la_creme_des_cremes_docs.Classes;

public class UE {
    private int n;
    private String name;
    private String id;
    private int loan;
    private String teacherSName;
    private String subjectId;

    public UE(String name, String id, String subjectId) {
        this.name = name;
        this.id = id;
        this.subjectId = subjectId;
    }

    public UE(String name, String subjectId, String id, int loan, String teacherSName) {
        this.name = name;
        this.subjectId = subjectId;
        this.id = id;
        this.loan = loan;
        this.teacherSName = teacherSName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
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

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name.trim();
    }
}
