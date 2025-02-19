package docs.la_creme_des_cremes_docs.Classes;

public class Comment {
    private String author;
    private String commentText;
    private Double rate;
    private int id;
    private long docId;
    private String authorId;

    public Comment(String author, String commentText, Double rate, long docId, String authorId) {
        this.author = author;
        this.commentText = commentText;
        this.rate = rate;
        this.docId = docId;
        this.authorId = authorId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDocId() {
        return docId;
    }

    public void setDocId(long docId) {
        this.docId = docId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}
