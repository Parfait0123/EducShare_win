package docs.la_creme_des_cremes_docs.live;

import animatefx.animation.FadeInRight;
import animatefx.animation.ZoomOutDown;
import docs.la_creme_des_cremes_docs.Classes.*;
import docs.la_creme_des_cremes_docs.SqliteConnection.DownloadDatabase;
import docs.la_creme_des_cremes_docs.SqliteConnection.SqliteConnection;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class new_docController implements Initializable {

    @FXML
    private ChoiceBox<Category> category;
    @FXML
    private Pane pane;

    @FXML
    private TextField category_name_field;

    @FXML
    private TextField export_name_field;

    @FXML
    private ChoiceBox<Semester> semester;

    @FXML
    private ChoiceBox<Subject> subject;
    @FXML
    private ProgressIndicator progress;

    @FXML
    private ChoiceBox<UE> ue;
    private ArrayList<Semester> semesters;
    private ArrayList<Subject> subjects;
    private ArrayList<UE> ues;
    private ArrayList<Category> categories;
    private SqliteConnection connection;
    private boolean newCategory = false;
    private File file;
    private Category currentCategory;
    private User user;
    private TableView<Document> doc_table;
    private Document docToModify;
    private Boolean canContinue;
    private DownloadDatabase downloadCon;

    @FXML
    void export(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisissez un fichier");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"), new FileChooser.ExtensionFilter("Fichier pdf", "*.pdf"));
        file = fileChooser.showSaveDialog(null);

        if (file != null) {
            System.out.println(file.getAbsolutePath());
            String name = file.getName();
            if (name.contains(".")) {
                int index = name.lastIndexOf(".");
                name = name.substring(0, index);
            }
            export_name_field.setText(name);
        }
    }

    @FXML
    void new_category(ActionEvent event) {
        category_name_field.setText("");
        newCategory = true;
        category.setVisible(false);
        category_name_field.setVisible(true);
        new FadeInRight(category_name_field).play();
    }

    @FXML
    void return_to_menu(ActionEvent event) {
        canContinue = false;
        Node node = (Node) event.getSource();
        new ZoomOutDown(node.getParent()).play();
    }

    @FXML
    void valid(ActionEvent event) {

        boolean v = false;
        currentCategory = category.getSelectionModel().getSelectedItem();
        try {
            if (newCategory && category_name_field.getText() != null) {
                currentCategory = new Category(category_name_field.getText().trim());
                boolean temp = false;
                for (Category c : categories) {
                    if (c.getCategory().trim().equalsIgnoreCase(currentCategory.getCategory().trim())) {
                        temp = true;
                        category.getSelectionModel().select(c);
                        break;
                    }
                }
                if (!temp) v = connection.sendCategory(currentCategory);
                categories.add(currentCategory);
                category.setItems(FXCollections.observableArrayList(categories));
            }
            String msg = "";
            boolean temp = true;
            if (export_name_field.getText().trim().isEmpty()) {
                temp = false;
                msg = msg + "\n Nom invalide ";
            }
            if (ue.getSelectionModel().getSelectedItem() == null) {
                temp = false;
                msg = msg + "\n " + " Sélectionnez une UE ";
            }
            if (file == null) {
                temp = false;
                msg = msg + "\n " + " Fichier invalide ";
            }
            if (currentCategory == null && !newCategory) {
                temp = false;
                msg = msg + "\n " + " Sélectionnez une catégorie ";
            }
            if (newCategory && !v) {
                for (Category c : categories) {
                    if (c.equals(currentCategory)) {

                    }
                }
                temp = false;
                msg = msg + "\n " + " Catégorie invalide ";
            }

            if (temp) {
                long id = Instant.now().toEpochMilli();
                String name = export_name_field.getText().trim();
                if (name.trim().equals("Login")) {
                    Notifications.create().text(" Nom non pris en charge ! ").showWarning();
                    return;
                }

                int semester_id = semester.getSelectionModel().getSelectedItem().getSemester();
                Subject s = subject.getSelectionModel().getSelectedItem();
                UE u = ue.getSelectionModel().getSelectedItem();
                String format = Arrays.stream(file.getName().split("\\.")).toList().getLast();
                String sendBy = user.getFirstName() + " " + user.getLastName();
                Date sendDate = Date.valueOf(LocalDate.now());

                if (docToModify == null) {
                    Document doc = new Document(id, name, semester_id, s.getId(), s.getName(), u.getId(), u.getName(), currentCategory.getCategory(), 1.0, format, sendBy, sendDate, file.length());
                    doc.setDownloadUrl("");
                    doc.setSize(file.length());
                    doc.setLocalFilename(file.getAbsolutePath());
                    progress.setVisible(true);
                    connection.sendDocument(doc, user, doc_table, this);

                } else {

                    docToModify = new Document(docToModify.getId(), name, semester_id, s.getId(), s.getName(), u.getId(), u.getName(), currentCategory.getCategory(), docToModify.getEtoile(), format, sendBy, sendDate, file.length());
                    docToModify.setDownloadUrl("");
                    docToModify.setLocalFilename(file.getAbsolutePath());
                    progress.setVisible(true);
                    connection.UpdateDocument(docToModify, user, doc_table, this);
                    downloadCon = new DownloadDatabase();
                    downloadCon.setDocLocalFileName(file.getAbsolutePath(), docToModify.getId());

                }

            } else {
                Notifications.create().title("Erreur d'insertion ").text(msg).showError();
            }
        } catch (Exception e) {
            System.out.println(e);
        }


    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        semester.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            subjects = connection.getSubjects(newValue.getSemester());
            subject.getItems().clear();
            subject.getItems().addAll(subjects);
        });
        subject.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ues = connection.getUEs(newValue.getId());
            ue.getItems().clear();
            ue.getItems().addAll(ues);
        });
        if (docToModify != null) {
            export_name_field.setText(docToModify.getName());
        }
    }

    public void setConnection(SqliteConnection connection) {
        this.connection = connection;
        semesters = connection.getSemesters();
        semester.setItems(FXCollections.observableArrayList(semesters));
        categories = connection.getCategories();
        category.setItems(FXCollections.observableArrayList(categories));
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDoc_table(TableView<Document> doc_table) {
        this.doc_table = doc_table;
    }

    public void setSubject(Subject s) {
        subject.getSelectionModel().select(s);
    }

    public void setCategory(Category c) {
        category.getSelectionModel().select(c);
    }

    public void setUE(UE u) {
        ue.getSelectionModel().select(u);
    }

    public void setSemester(Semester s) {
        semester.getSelectionModel().select(s);
    }

    public void setDocToModify(Document doc) {
        this.docToModify = doc;
        semesters = connection.getSemesters();
        semester.setItems(FXCollections.observableArrayList(semesters));
        for (Semester s : semesters) {
            if (s.getSemester() == doc.getSemester()) {
                semester.getSelectionModel().select(s);
                System.out.println(" Semestre trouvé");
                break;
            }
        }
        subjects = connection.getSubjects(doc.getSemester());
        subject.getItems().clear();
        subject.setItems(FXCollections.observableArrayList(subjects));
        for (Subject s : subjects) {
            System.out.println("'" + s.getId() + "'" + "     '" + doc.getSubjectID() + "'");
            if (s.getId().trim().equals(doc.getSubjectID().trim())) {
                subject.getSelectionModel().select(s);
                System.out.println("matière trouvée ");
                break;
            }
        }
        ues = connection.getUEs(doc.getSubjectID());
        ue.getItems().clear();
        ue.setItems(FXCollections.observableArrayList(ues));
        for (UE u : ues) {
            System.out.println("'" + u.getId() + "'" + "     '" + doc.getUeID() + "'");
            System.out.println(u.getId());
            if (u.getId().trim().equals(doc.getUeID().trim())) {
                ue.getSelectionModel().select(u);
                System.out.println("UE trouvé ");
                break;
            }
        }
        categories = connection.getCategories();
        category.getItems().clear();
        category.setItems(FXCollections.observableArrayList(categories));
        for (Category c : categories) {
            System.out.println("'" + c.getCategory() + "'" + "     '" + doc.getCategory() + "'");
            if (c.getCategory().trim().equals(doc.getCategory().trim())) {
                category.getSelectionModel().select(c);
                break;
            }
        }

    }

    public void returnn() {
        canContinue = false;
        new ZoomOutDown(pane).play();
    }

    public void setCanContinue(Boolean canContinue) {
        this.canContinue = canContinue;
    }
}