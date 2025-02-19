package docs.la_creme_des_cremes_docs.Classes;

public class Downloads {
    private long docID;
    private boolean isDownloaded;
    private String fileName;

    public Downloads(long docID, boolean isDownloaded, String fileName) {
        this.docID = docID;
        this.isDownloaded = isDownloaded;
        this.fileName = fileName;
    }

    public long getDocID() {
        return docID;
    }

    public void setDocID(long docID) {
        this.docID = docID;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
