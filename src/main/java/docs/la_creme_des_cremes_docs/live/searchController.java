package docs.la_creme_des_cremes_docs.live;

import animatefx.animation.ZoomOutDown;
import docs.la_creme_des_cremes_docs.Classes.*;
import docs.la_creme_des_cremes_docs.SqliteConnection.SqliteConnection;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class searchController implements Initializable {

    @FXML
    private ChoiceBox<Category> category;

    @FXML
    private ChoiceBox<Semester> semester;

    @FXML
    private ChoiceBox<Subject> subject;

    @FXML
    private ChoiceBox<UE> ue;

    private SqliteConnection connection;
    private TableView<Document> tableView;
    private ArrayList<Document> documents;
    private ArrayList<Semester> semesters;
    private ArrayList<Subject> subjects;
    private ArrayList<UE> ues;
    private ArrayList<Category> categories;

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

    }

    @FXML
    void return_to_menu(ActionEvent event) {
        close(event);
    }

    @FXML
    void search(ActionEvent event) {
        int semesterID = 0;
        String subjectID = "";
        String ueID = "";
        String categoryName = "";
        if (semester.getSelectionModel().getSelectedItem() != null) {
            semesterID = semester.getSelectionModel().getSelectedItem().getSemester();
        }
        if (subject.getSelectionModel().getSelectedItem() != null) {
            subjectID = subject.getSelectionModel().getSelectedItem().getId().trim();
        }
        if (ue.getSelectionModel().getSelectedItem() != null) {
            ueID = ue.getSelectionModel().getSelectedItem().getId().trim();
        }
        if (category.getSelectionModel().getSelectedItem() != null) {
            categoryName = category.getSelectionModel().getSelectedItem().getCategory().trim();
        }
        ArrayList<Document> documentsFound = new ArrayList<>();
        for (Document d : documents)
            if ((semesterID == 0 || d.getSemester() == semesterID) &&
                d.getSubjectID().contains(subjectID) &&
                d.getUeID().contains(ueID) &&
                d.getCategory().contains(categoryName)) documentsFound.add(d);

        int count = 0;
        for (Document d : documentsFound) d.setN(++count);

        tableView.getItems().clear();
        tableView.setItems(FXCollections.observableArrayList(documentsFound));
        tableView.refresh();
        close(event);
    }


    public void setConnection(SqliteConnection connection) {
        this.connection = connection;
        semesters = connection.getSemesters();
        subjects = connection.getSubjects();
        ues = connection.getUEs();
        categories = connection.getCategories();
        semester.setItems(FXCollections.observableArrayList(semesters));
        subject.setItems(FXCollections.observableArrayList(subjects));
        ue.setItems(FXCollections.observableArrayList(ues));
        category.setItems(FXCollections.observableArrayList(categories));

    }

    public void setTableView(TableView<Document> tableView) {
        this.tableView = tableView;
        documents = new ArrayList<>();
        documents.addAll(connection.getDocuments());
    }

    public void setDocuments(ArrayList<Document> documents) {
        this.documents = documents;
    }

    void close(ActionEvent event) {

        Node node = (Node) event.getSource();
        new ZoomOutDown(node.getParent()).play();
    }
}
