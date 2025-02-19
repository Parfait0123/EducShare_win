package docs.la_creme_des_cremes_docs.live;

import animatefx.animation.ZoomOutDown;
import docs.la_creme_des_cremes_docs.Classes.Document;
import docs.la_creme_des_cremes_docs.SqliteConnection.DownloadDatabase;
import docs.la_creme_des_cremes_docs.cloud.GoogleDriveService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.controlsfx.control.Notifications;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class downloadController implements Initializable {

    @FXML
    private Label name_label;
    @FXML
    private Label statut_label;

    @FXML
    private ProgressBar progressbar;
    private Document document;
    private TableView<Document> table;
    @FXML
    private ProgressIndicator progress;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progress.setVisible(false);
    }

    @FXML
    public void return_to_menu(ActionEvent event) {
        Node node = (Node) event.getSource();
        new ZoomOutDown(node.getParent()).play();
    }

    @FXML
    void start(ActionEvent event) {
        Button button = (Button) event.getSource();
        if (button.getText().equals("Quitter")) {
            progress.setVisible(false);
            return_to_menu(event);
            return;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> progress.setVisible(true));
                if (!GoogleDriveService.isInternetAvailable()) {
                    updateMessage("Connectez-vous à l'intenet svp !");
                    Platform.runLater(() -> progress.setVisible(false));
                    return null;
                }
                String destination = System.getProperty("user.home") + "\\.MyDocs\\" + document.getName() + document.getId() + "." + document.getFormat();
                GoogleDriveService.downloadFileWithProgress(document.getSize(), document.getDownloadUrl().trim(), progressbar, destination);
                updateMessage("Téléchargement en cours .......");


                return null;
            }

            @Override
            protected void updateMessage(String message) {
                Platform.runLater(() -> Notifications.create().text(message).show());
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progress.setVisible(false);
                    Notifications.create().title("Erreur de téléchargement ").showError();

                });
            }

            @Override
            protected void succeeded() {

                progress.setVisible(false);
                String destination = System.getProperty("user.home") + "\\.MyDocs\\" + document.getName() + document.getId() + "." + document.getFormat();
                Button b = (Button) event.getSource();
                b.setText("Quitter");
                DownloadDatabase d = new DownloadDatabase();
                d.sendDocStatus(document.getId(), true, destination);
                d.setDocLocalFileName(destination, document.getId());
                Platform.runLater(() -> {
                    ArrayList<Document> docs = new ArrayList<>(table.getItems());
                    d.makeTheLink(docs);
                    table.getItems().clear();
                    table.getItems().addAll(docs);
                    table.refresh();
                    table.sort();
                });


            }
        };
        new Thread(task).start();
    }


    public void setName(String name) {
        name_label.setText(name);
    }

    public void setDocument(Document document) {
        this.document = document;
        double size = document.getSize();
        if (size < 1024 * 1024) {
            size = (double) document.getSize() / (1024);
            statut_label.setText("    " + size + " Ko ");
        } else {
            size = (double) document.getSize() / (1024 * 1024);
            statut_label.setText("    " + size + " Mo ");
        }
        System.out.println(document.getDownloadUrl());
    }

    public void setTable(TableView<Document> table) {
        this.table = table;
    }
}
