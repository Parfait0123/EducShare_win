package docs.la_creme_des_cremes_docs.live;

import animatefx.animation.FadeInLeft;
import animatefx.animation.ZoomInDown;
import docs.la_creme_des_cremes_docs.Classes.Document;
import docs.la_creme_des_cremes_docs.Classes.User;
import docs.la_creme_des_cremes_docs.SqliteConnection.CitationsBase;
import docs.la_creme_des_cremes_docs.SqliteConnection.SqliteConnection;
import docs.la_creme_des_cremes_docs.cloud.GoogleDriveService;
import docs.la_creme_des_cremes_docs.subject.SubjectManagementController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class mean_interfaceController implements Initializable {

    @FXML
    private Text text;
    @FXML
    private ImageView image;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Button chrono;
    @FXML
    private TableColumn<Document, String> doc_category_column;
    @FXML
    private TableColumn<Document, Boolean> doc_is_Download_column;

    @FXML
    private TableColumn<Document, String> doc_etoile_column;

    @FXML
    private TableColumn<Document, String> doc_format_column;

    @FXML
    private TableColumn<Document, String> doc_name_column;

    @FXML
    private TableColumn<Document, Integer> doc_semester_column;

    @FXML
    private TableColumn<Document, String> doc_sendBy_column;

    @FXML
    private TableColumn<Document, Date> doc_send_date_column;

    @FXML
    private TableColumn<Document, String> doc_subject_column;

    @FXML
    private TableView<Document> doc_table;

    @FXML
    private TableColumn<Document, String> doc_ue_column;

    @FXML
    private TableColumn<Document, Integer> n_column;

    @FXML
    private TextField search_with_sector_field;
    @FXML
    private Label classe_field;
    @FXML
    private AnchorPane view_Anchropane;
    private ArrayList<Document> documents;
    private SqliteConnection connection;
    private Document selectedDocument;
    private User user;
    private Boolean canContinue = false;
    private String filePath2;
    private CitationsBase citationsBase;
    private Random r;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        doc_category_column.setCellValueFactory(new PropertyValueFactory<>("category"));
        doc_etoile_column.setCellValueFactory(new PropertyValueFactory<>("etoile"));
        doc_format_column.setCellValueFactory(new PropertyValueFactory<>("format"));
        doc_name_column.setCellValueFactory(new PropertyValueFactory<>("name"));
        doc_semester_column.setCellValueFactory(new PropertyValueFactory<>("semester"));
        doc_send_date_column.setCellValueFactory(new PropertyValueFactory<>("sendDate"));
        doc_subject_column.setCellValueFactory(new PropertyValueFactory<>("subject"));
        doc_ue_column.setCellValueFactory(new PropertyValueFactory<>("ue"));
        n_column.setCellValueFactory(new PropertyValueFactory<>("n"));
        doc_sendBy_column.setCellValueFactory(new PropertyValueFactory<>("sendBy"));
        doc_is_Download_column.setCellValueFactory(new PropertyValueFactory<>("isDownload"));
        doc_send_date_column.setSortType(TableColumn.SortType.DESCENDING);
        doc_table.getSortOrder().add(doc_send_date_column);
        progress.setVisible(false);
        citationsBase = new CitationsBase();
        r = new Random();
        int n = new Random().nextInt(1, 3);
        if (n != 1) {
            image.setImage(new Image(Objects.requireNonNull(getClass().getResource("menu.png")).toExternalForm()));
            text.setFill(javafx.scene.paint.Color.BLACK);
        }
        text.setText(citationsBase.getCitation(r.nextInt(1, 101)));
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(20), event -> {
            text.setText(citationsBase.getCitation(r.nextInt(1, 101)));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // Répéter indéfiniment
        timeline.play();
    }


    @FXML
    void add_doc() {

        if (!canContinue) verify(2);
        else {
            canContinue = false;
            new_docController controller = addView("new_doc.fxml").getController();
            controller.setUser(user);
            controller.setDoc_table(doc_table);
            controller.setConnection(connection);
        }

    }


    @FXML
    void download_file() {
        selectedDocument = doc_table.getSelectionModel().getSelectedItem();
        if (selectedDocument != null) {
            if (selectedDocument.getIsDownload())
                Notifications.create().text("Le document est téléchargé. Vous etes sur le point de le retélécharger !").showWarning();
            downloadController controller = addView("download.fxml").getController();
            controller.setName(selectedDocument.getName());
            controller.setDocument(selectedDocument);
            controller.setTable(doc_table);
        } else {
            Notifications.create().title("Sélectionnez un documment svp ").showWarning();
        }
    }

    @FXML
    void note_file() {

        selectedDocument = null;
        selectedDocument = doc_table.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) Notifications.create().title("Sélectionnez un document").showWarning();
        else {
            if (!canContinue) verify(1);
            else {
                canContinue = false;
                new_commentController controller = Objects.requireNonNull(addView("new_comment.fxml")).getController();
                controller.setCanContinue(canContinue);
                controller.setConnection(connection);
                controller.setDoc(selectedDocument);
                controller.setDoc_table(doc_table);
                controller.setAuthor(user.getLastName().concat(" ").concat(user.getFirstName()));
                controller.setAuthorId(user.getUserName());
            }
        }
    }

    @FXML
    void recharger_le_tableau(ActionEvent event) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return;
        }


        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {

                String c = user.getAuthorizationCode();
                c = System.getProperty("user.home") + "\\.MyDocs\\database\\Docs" + c.substring(0, 8) + ".sqlite";
                File file = new File(c);
                Platform.runLater(() -> {
                    Notifications.create().text("Patientez 15 secondes tout en laissant votre connexion active").showInformation();
                    chrono.setVisible(true);
                    chrono.setText("0");
                });
                connection.closeConnection();
                try {
                    file.delete();
                } catch (Exception _) {
                }
                new Thread(() -> {
                    try {
                        for (int i = 1; i < 16; i++) {
                            Thread.sleep(1000);
                            updateMessage(String.valueOf(i));
                        }
                    } catch (InterruptedException _) {
                        updateMessage("15");
                    }
                }).start();
                connection = new SqliteConnection(user);

                return null;
            }


            @Override
            protected void updateMessage(String message) {
                System.out.println(message);
                Platform.runLater(() -> {
                    if (message.contains("15")) {
                        chrono.setVisible(false);
                        Platform.runLater(() -> {
                            connection = new SqliteConnection(user);
                            documents = connection.getDocuments();
                            progress.setVisible(false);
                            doc_table.getItems().clear();
                            doc_table.getItems().addAll(documents);
                            Notifications.create().text("Données rechargées avec succès !").showWarning();
                            doc_table.sort();
                        });
                    } else {

                        chrono.setText(message);
                    }

                });
            }
        };
        new Thread(task).start();
    }

    @FXML
    void remove_doc() {
        if (!user.getRole().contains("admin")) {
            Notifications.create().title(" Seuls les responsales peuvent supprimer un document ").showWarning();
            return;
        }
        selectedDocument = doc_table.getSelectionModel().getSelectedItem();
        if (selectedDocument != null) {
            progress.setVisible(true);
            try {
                if (!canContinue) verify(4);
                else {
                    if (!GoogleDriveService.isInternetAvailable()) {
                        Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
                        progress.setVisible(false);
                        return;
                    }
                    canContinue = false;
                    if (connection.removeDocument(selectedDocument)) {
                        String c = user.getAuthorizationCode();
                        c = System.getProperty("user.home") + "\\.MyDocs\\database\\Docs" + c.substring(0, 8) + ".sqlite";
                        GoogleDriveService.update2(c, user.getAuthorizationCode());
                        doc_table.getItems().clear();
                        doc_table.getItems().addAll(connection.getDocuments());
                        doc_table.refresh();
                        Notifications.create().title("Document supprimé avec succès ").showInformation();
                        progress.setVisible(false);
                    } else Notifications.create().title("Erreur de suppression").showError();
                }
            } catch (Exception e) {
                Notifications.create().title(" Connectez-vous à l'internet ").showWarning();
            }
        } else Notifications.create().title("Sélectionnez un documment ").showWarning();
        progress.setVisible(false);
    }

    @FXML
    void search_more(ActionEvent event) {
        searchController controller = addView("search.fxml").getController();
        controller.setConnection(connection);
        controller.setTableView(doc_table);
        doc_table.sort();
    }

    @FXML
    void search_with_field(ActionEvent event) {
        documents = connection.getDocuments();
        if (!search_with_sector_field.getText().trim().isEmpty()) {
            ArrayList<Document> documents1 = new ArrayList<>();
            for (Document d : documents) {
                if (d.getName().toUpperCase().contains(search_with_sector_field.getText().toUpperCase().trim())) {
                    documents1.add(d);
                }
            }
            int count = 0;
            for (Document d : documents1) d.setN(++count);
            doc_table.getItems().clear();
            doc_table.getItems().addAll(documents1);
            doc_table.refresh();
        } else {
            doc_table.getItems().clear();
            int count = 0;
            for (Document doc : documents) doc.setN(++count);
            doc_table.getItems().addAll(documents);
            doc_table.refresh();
        }


    }

    @FXML
    void see_docs_comment(ActionEvent event) {
        selectedDocument = doc_table.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            Notifications.create().title("Sélectionnez un document").showWarning();
        } else {
            see_commentController controller = addView("see_comments.fxml").getController();
            controller.setDocID(selectedDocument.getId());
            controller.setConnection(connection);

        }

    }

    @FXML
    void see_file(ActionEvent event) {
        selectedDocument = doc_table.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            Notifications.create().title("Sélectionnez un document ").showWarning();
            return;
        }

        if (selectedDocument.getIsDownload()) {
            try {
                File file = new File(selectedDocument.getLocalFilename());
                if (Desktop.isDesktopSupported() && file.exists()) {
                    Desktop d = Desktop.getDesktop();
                    d.open(file);
                } else Notifications.create().title("Erreur d'ouverture").showError();
            } catch (IOException e) {
                Notifications.create().title("Erreur d'ouverture ").showError();
            }
        } else Notifications.create().title("Document non téléchargé !").showWarning();
    }

    @FXML
    void send_file(ActionEvent event) {
        selectedDocument = doc_table.getSelectionModel().getSelectedItem();


        if (selectedDocument != null) {
            try {
                File file = new File(selectedDocument.getLocalFilename());
                if (!file.exists()) {
                    Notifications.create().title(" Erreur !").showError();
                    return;
                }
            } catch (Exception e) {
                return;
            }

            if (selectedDocument.getIsDownload()) {
                String desktopPath = System.getProperty("user.home") + "\\Desktop";

                // Commande pour copier le fichier vers le Bureau
                String command = "cmd /c copy \"" + selectedDocument.getLocalFilename() + "\" \"" + desktopPath + "\"";

                try {
                    // Exécuter la commande
                    Runtime.getRuntime().exec(command);
                    Notifications.create().text("Fichier envoyé vers le bureau.").showInformation();
                } catch (IOException e) {
                    Notifications.create().title("Erreur de l'envoie ").showError();
                }
            } else Notifications.create().title("Document non téléchargé !").showError();
        } else {
            Notifications.create().title("Sélectionnez un document ").showWarning();
        }
    }

    @FXML
    void update_doc() {
        if (!user.getRole().contains("admin")) {
            Notifications.create().title(" Seuls les responsales peuvent modifier un document ").showWarning();
            return;
        }
        selectedDocument = doc_table.getSelectionModel().getSelectedItem();
        if (selectedDocument != null) {
            if (!canContinue) verify(3);
            else {
                canContinue = false;
                new_docController controller = addView("new_doc.fxml").getController();
                controller.setDoc_table(doc_table);
                controller.setConnection(connection);
                controller.setUser(user);
                controller.setDocToModify(selectedDocument);
            }
        } else {
            Notifications.create().title(" Sélectionnez un document ").showWarning();
        }
    }

    @FXML
    void administration(ActionEvent event) throws IOException {
        openNewWindow(new FXMLLoader(SubjectManagementController.class.getResource("subjectManagement.fxml")));

    }

    void openNewWindow(FXMLLoader loader) throws IOException {
        view_Anchropane.getChildren().clear();
        AnchorPane pane = loader.load();
        view_Anchropane.getChildren().setAll(pane);
        SubjectManagementController controller = loader.getController();
        controller.setUser(user);
    }

    private FXMLLoader addView(String file) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(file));

        Pane pane = new Pane();
        try {
            pane = loader.load();
        } catch (IOException e) {
            System.out.println(e);
        }
        new ZoomInDown(pane).play();
        pane.setLayoutX(475);
        pane.setLayoutY(200);
        if (view_Anchropane.getChildren().contains(pane) && !pane.isVisible()) pane.setVisible(true);
        else if (!view_Anchropane.getChildren().contains(pane)) view_Anchropane.getChildren().add(pane);
        return loader;
    }

    public void setUser(User user) {
        this.user = user;
        connection = new SqliteConnection(user);
        documents = connection.getDocuments();
        doc_table.getItems().addAll(documents);
        doc_table.sort();

        classe_field.setText(user.getcLass());
    }

    private void verify(int methodID) {
        confirmController controller = openVerifyView();
        controller.setValid(canContinue);
        controller.setSourceController1(this);
        controller.setMethodID(methodID);
    }

    public void setCanContinue(Boolean canContinue) {
        this.canContinue = canContinue;
    }

    @FXML
    void modifyPass() {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return;
        }
        confirmController controller = openVerifyView();
        controller.setModify(true);
    }

    private confirmController openVerifyView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("confirm.fxml"));
        Pane root = new Pane();
        try {
            root = loader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        root.setLayoutX(5);
        root.setLayoutY(2);
        boolean temp = true;
        for (Node node : view_Anchropane.getChildren()) {
            if (node.getLayoutX() == root.getLayoutX() && node.getLayoutY() == root.getLayoutY()) {
                view_Anchropane.getChildren().remove(node);
                break;
            }
        }
        view_Anchropane.getChildren().add(root);
        new FadeInLeft(root).play();
        confirmController controller = loader.getController();
        controller.setUser(user);
        return controller;
    }
}
