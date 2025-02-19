package docs.la_creme_des_cremes_docs.Classes;

import java.sql.Date;

public class Semester {
    private int semester;
    private Date begin;
    private Date end;
    private String name;

    public Semester(int semester, Date begin, Date end) {
        this.semester = semester;
        this.begin = begin;
        this.end = end;
        name = "Semestre " + semester;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
        name = "Semestre " + semester;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return name;
    }
}
