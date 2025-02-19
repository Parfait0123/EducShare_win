package docs.la_creme_des_cremes_docs.live;

import animatefx.animation.ZoomOutDown;
import docs.la_creme_des_cremes_docs.Classes.Comment;
import docs.la_creme_des_cremes_docs.Classes.Document;
import docs.la_creme_des_cremes_docs.SqliteConnection.SqliteConnection;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.Rating;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class new_commentController implements Initializable {

    @FXML
    private TextArea comment_textArea;
    @FXML
    private ProgressIndicator progress;

    @FXML
    private Pane rating_pane;
    private long docId;
    private String authorId;
    private String author;
    private SqliteConnection connection;
    private Rating rating;
    private Document doc;
    private TableView<Document> doc_table;
    private Boolean canContinue;


    @FXML
    void return_to_menu(ActionEvent event) {
        canContinue = false;
        close(event);
    }


    @FXML
    void valid(ActionEvent event) {


        if (comment_textArea.getText().trim().isEmpty()) {
            Notifications.create().title("Entrez un commentaire").showWarning();
            return;
        }

        progress.setVisible(true);
        Task<Void> task = new Task<>() {

            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    updateMessage(String.valueOf(connection.sendComment(new Comment(author, comment_textArea.getText(), rating.getRating(), docId, authorId))));
                });
                return null;
            }

            @Override
            protected void updateMessage(String message) {
                if (message.contains("true")) {
                    Platform.runLater(() -> {
                        progress.setVisible(false);
                        double new_rating = rating.getRating();
                        int count = connection.getCommentCount(docId) + 2;
                        new_rating = (doc.getEtoile() + new_rating) / count;
                        if (connection.UpdateDocRate(docId, new_rating)) {
                            ArrayList<Document> docs = new ArrayList<>(doc_table.getItems());
                            for (Document d : docs)
                                if (d.getId() == docId) {
                                    d.setEtoile(new_rating);
                                    break;
                                }

                            doc_table.refresh();
                            close(event);
                        }
                    });
                } else {
                    Platform.runLater(() -> progress.setVisible(false));
                }
            }
        };
        new Thread(task).start();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rating = new Rating();
        rating.setRating(1);
        rating_pane.getChildren().add(rating);
    }

    public void setDocId(long docId) {
        this.docId = docId;
    }


    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    void close(ActionEvent event) {
        Node node = (Node) event.getSource();
        new ZoomOutDown(node.getParent()).play();
    }

    public void setConnection(SqliteConnection connection) {
        this.connection = connection;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
        setDocId(doc.getId());
    }

    public void setDoc_table(TableView<Document> doc_table) {
        this.doc_table = doc_table;
    }

    public void setCanContinue(Boolean canContinue) {
        this.canContinue = canContinue;
    }
}
