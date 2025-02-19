package docs.la_creme_des_cremes_docs.Classes;

import java.sql.Date;
import java.util.ArrayList;

public class Document {
    private int n;
    private String name;
    private int semester;
    private String subject;
    private String ue;
    private String category;
    private Double etoile;
    private String sendBy;
    private Date sendDate;
    private String format;
    private Boolean isDownload;
    private long id;
    private ArrayList<Comment> comments;
    private String subjectID;
    private String ueID;
    private String downloadUrl;
    private String localFilename;
    private long size;

    public Document(long id, String name, int semester, String subjectID, String subject, String ueID, String ue, String category, Double etoile, String format, String sendBy, Date sendDate, long size) {
        this.id = id;
        this.name = name;
        this.semester = semester;
        this.subjectID = subjectID;
        this.ueID = ueID;
        this.category = category;
        etoile = etoile * 100;
        etoile = etoile.intValue() / 100.0;
        this.etoile = etoile;
        this.format = format;
        this.sendBy = sendBy;
        this.sendDate = sendDate;
        this.subject = subject;
        this.ue = ue;
        this.size = size;
    }

    public boolean getIsDownload() {
        return isDownload;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUe() {
        return ue;
    }

    public void setUe(String ue) {
        this.ue = ue;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getEtoile() {
        return etoile;
    }

    public void setEtoile(Double etoile) {
        etoile = etoile * 100;
        etoile = etoile.intValue() / 100.0;
        this.etoile = etoile;
    }

    public String getSendBy() {
        return sendBy;
    }

    public void setSendBy(String sendBy) {
        this.sendBy = sendBy;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public Boolean getDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public void setDownload(Boolean download) {
        isDownload = download;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public String getUeID() {
        return ueID;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getLocalFilename() {
        return localFilename;
    }

    public void setLocalFilename(String localFilename) {
        this.localFilename = localFilename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
