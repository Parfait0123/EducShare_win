package docs.la_creme_des_cremes_docs.live;

import animatefx.animation.*;
import docs.la_creme_des_cremes_docs.Classes.Comment;
import docs.la_creme_des_cremes_docs.SqliteConnection.SqliteConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;

public class see_commentController implements Initializable {

    @FXML
    private TextArea comment;
    @FXML
    private Button next_button;
    @FXML
    private Button prev_button;
    @FXML
    private StackPane stackpane;

    @FXML
    private Label author_label;

    @FXML
    private Label note;
    @FXML
    private Label msg_label;
    @FXML
    private Pane comments_pane;
    @FXML
    private Pane msg_pane;
    @FXML
    private SqliteConnection connection;
    private long docID;
    private ArrayList<Comment> comments;
    private int count;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        count = 0;
        comments = new ArrayList<>();


//        //NumberBinding n=when(count.greaterThan(comments.size() - 1)).then(0.25).otherwise(1);
//        NumberBinding p=when(count.lessThan(0)).then(0.25).otherwise(1);
//        //next_button.opacityProperty().bind(n);
//        prev_button.opacityProperty().bind(p);

    }

    @FXML
    void next_comment(ActionEvent event) {
        if (count < comments.size() - 1) {
            count++;
            seeComment();
            if (count == comments.size() - 1) next_button.setOpacity(0.3);
            prev_button.setOpacity(1);
        }
    }

    @FXML
    void prev_comment(ActionEvent event) {
        if (count > 0) {
            count--;
            seeComment();
            if (count == 0) prev_button.setOpacity(0.3);
            next_button.setOpacity(1);
        }
    }

    @FXML
    void return_to_menu(ActionEvent event) {
        new ZoomOutDown(stackpane).play();
    }


    @FXML
    void go_to_comment_view() throws IOException {
        if (comments.size() == 1) {
            next_button.setOpacity(0.3);
            prev_button.setOpacity(0.3);
            next_button.setDisable(true);
            prev_button.setDisable(true);
        }
        if (!comments.isEmpty()) {
            msg_pane.setVisible(false);
            comments_pane.setVisible(true);
            new FadeInRight(comments_pane.getParent()).play();
            styleView();
        } else {
            Notifications.create().title("Aucun commentaire n'est disponible ").showWarning();
        }
    }

    public void setConnection(SqliteConnection connection) {
        this.connection = connection;
        comments = connection.getComments(docID);

        if (comments.size() <= 1) msg_label.setText(comments.size() + " commentaire disponible ");
        else msg_label.setText(comments.size() + " commentaires disponibles ");
        if (!comments.isEmpty()) {
            seeComment();
        }

    }

    public void setDocID(long docID) {
        this.docID = docID;
    }

    void seeComment() {
        new FadeInRight(comment).play();
        new FadeInRight(note).play();
        new FadeInRight(author_label).play();
        author_label.setText(comments.get(count).getAuthor());
        note.setText(comments.get(count).getRate() + "/5");
        comment.setText(comments.get(count).getCommentText());
    }


    private void styleView() {
        for (Node node : comments_pane.getChildren()) {
            ArrayList<AnimationFX> list = new ArrayList<>(Arrays.asList(new FadeInRight(node), new FadeInLeft(node), new FadeInUp(node), new FadeInDown(node), new Flash(node), new Bounce(node)));
            list.get(new Random().nextInt(list.size())).play();
        }
    }
}
