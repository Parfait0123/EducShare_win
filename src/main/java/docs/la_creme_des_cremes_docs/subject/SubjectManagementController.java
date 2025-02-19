package docs.la_creme_des_cremes_docs.subject;

import animatefx.animation.FadeInLeft;
import animatefx.animation.FadeInRight;
import animatefx.animation.Flash;
import animatefx.animation.ZoomInDown;
import docs.la_creme_des_cremes_docs.Classes.Semester;
import docs.la_creme_des_cremes_docs.Classes.Subject;
import docs.la_creme_des_cremes_docs.Classes.UE;
import docs.la_creme_des_cremes_docs.Classes.User;
import docs.la_creme_des_cremes_docs.SqliteConnection.SqliteConnection;
import docs.la_creme_des_cremes_docs.live.confirmController;
import docs.la_creme_des_cremes_docs.live.mean_interfaceController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class SubjectManagementController implements Initializable {

    @FXML
    private TableColumn<Subject, String> subject_database_id_column;
    @FXML
    private TextField search_with_name_field;
    @FXML
    private Button add_button;
    @FXML
    private TextField new_semester_id_field;

    @FXML
    private ImageView current_ImageView;

    @FXML
    private AnchorPane menu_view;

    @FXML
    private HBox modification_hbox;

    @FXML
    private VBox semester_view_vbox;

    @FXML
    private Button modify_button;

    @FXML
    private Pane new_semester_pane;

    @FXML
    private Button remove_button;

    @FXML
    private Button see_or_return_button;

    @FXML
    private DatePicker semester_begining_dateOicker;

    @FXML
    private DatePicker semester_end_dateOicker;

    @FXML
    private ChoiceBox<Semester> semester_list_choiceBox;

    @FXML
    private TableColumn<Subject, Integer> subject_UE_number_column;

    @FXML
    private TableColumn<Subject, String> subject__teacher_name_column;

    @FXML
    private TableColumn<Subject, String> subject_id_column;

    @FXML
    private TableColumn<Subject, Integer> subject_loan_column;

    @FXML
    private TableColumn<Subject, String> subject_name_column;

    @FXML
    private TableView<Subject> subject_tableView;

    @FXML
    private TableColumn<UE, Integer> ue_id_column;

    @FXML
    private TableColumn<UE, Integer> ue_loan_column;

    @FXML
    private TableColumn<UE, String> ue_name_column;

    @FXML
    private TableView<UE> ue_tableView;
    @FXML
    private Label class_field;

    @FXML
    private TableColumn<UE, String> ue_teacher_name_column;
    private SqliteConnection connection;
    private ArrayList<Semester> semesters;
    private ArrayList<Subject> subjects;
    private ArrayList<UE> ues;
    private Subject subjectSelected;
    private UE ueSelected;
    private Semester currentSemester;
    private boolean changeSemester = false;
    private User user;
    private Boolean canContinue = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        subject_database_id_column.setCellValueFactory(new PropertyValueFactory<>("id"));
        subject_UE_number_column.setCellValueFactory(new PropertyValueFactory<>("ueNbr"));
        subject__teacher_name_column.setCellValueFactory(new PropertyValueFactory<>("teacherSName"));
        subject_id_column.setCellValueFactory(new PropertyValueFactory<>("n"));
        subject_loan_column.setCellValueFactory(new PropertyValueFactory<>("loan"));
        subject_name_column.setCellValueFactory(new PropertyValueFactory<>("name"));
        ue_id_column.setCellValueFactory(new PropertyValueFactory<>("n"));
        ue_loan_column.setCellValueFactory(new PropertyValueFactory<>("loan"));
        ue_name_column.setCellValueFactory(new PropertyValueFactory<>("name"));
        ue_teacher_name_column.setCellValueFactory(new PropertyValueFactory<>("teacherSName"));
    }

    @FXML
    public void add_new_matter() throws IOException {
        if (!user.getRole().contains("admin")) {
            Notifications.create().text(" Seuls les responsales sont autorisés à éffectuer cette action ").showWarning();
            return;
        }

        if (canContinue) {
            canContinue = false;
            FXMLLoader loader = addView();
            if (add_button.getText().contains("UE")) {
                ueDetailsController controller = loader.getController();
                controller.setSubject(subjectSelected);
                controller.setLoan(subjectSelected.getLoan());
                controller.setTechears_name_field(subjectSelected.getTeacherSName());
                controller.setConnection(connection);
                controller.setUe_tableView(ue_tableView);
                ues = connection.getUEs(subjectSelected.getId());
                controller.setUe_list(ues);

            } else {
                currentSemester = semester_list_choiceBox.getSelectionModel().getSelectedItem();
                if (currentSemester != null) {
                    subjectDetailsController controller = loader.getController();
                    controller.setSemesterId(currentSemester.getSemester());
                    controller.setSubjectTable(subject_tableView);
                    controller.setConnection(connection);
                    controller.setModify(false);
                } else {
                    Notifications.create().text(" Semestre invalide").showWarning();
                }
            }
        } else verify(1);
    }

    @FXML
    public void add_new_semester_to_semesters_list() throws IOException {
        if (!user.getRole().contains("admin")) {
            Notifications.create().text(" Seuls les responsales sont autorisés à ajouter un semestre ").showWarning();
            return;
        }
        if (canContinue)
            changeView(semester_view_vbox, new_semester_pane);
        else verify(4);
    }

    @FXML
    public void create_new_semester() throws IOException {
        if (!user.getRole().contains("admin")) {
            Notifications.create().text(" Seuls les responsales sont autorisés à ajouter un semestre ").showWarning();
            return;
        }

        canContinue = false;
        try {
            int semesterID = Integer.parseInt(new_semester_id_field.getText());
            for (Semester s : semesters)
                if (semesterID == s.getSemester()) {
                    Notifications.create().text(" Semestre existant").showWarning();
                    semester_list_choiceBox.getSelectionModel().select(s);
                    return;
                }

            Semester s = new Semester(semesterID, Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now()));
            semesters.add(s);
            if (connection.sendSemester(s)) {
                semester_list_choiceBox.getItems().add(s);
                changeView(new_semester_pane, semester_view_vbox);
                semester_list_choiceBox.getSelectionModel().select(s);
            }


        } catch (NumberFormatException e) {
            Notifications.create().text(" Entré invalide").showWarning();
        }
    }

    @FXML
    void leave(ActionEvent event) {

    }

    @FXML
    public void modify_current_matter() throws IOException {

        if (!user.getRole().contains("admin")) {
            Notifications.create().text(" Seuls les responsales sont autorisés à effectuer des modifications ").showWarning();
            return;
        }


        if (add_button.getText().contains("UE")) {

            ueSelected = ue_tableView.getSelectionModel().getSelectedItem();
            subjectSelected = subject_tableView.getSelectionModel().getSelectedItem();
            if (ueSelected != null) {
                if (canContinue) {
                    canContinue = false;
                    FXMLLoader loader = addView();
                    ueDetailsController controller = loader.getController();
                    controller.setSubject(subjectSelected);
                    controller.setName_field(ueSelected.getName());
                    controller.setLoan(subjectSelected.getLoan());
                    controller.setTechears_name_field(subjectSelected.getTeacherSName());
                    controller.setConnection(connection);
                    controller.setUe_tableView(ue_tableView);
                    controller.setUe_list(ues);
                    controller.setUe(ueSelected);
                    controller.setModification(true);
                    ues = connection.getUEs(ueSelected.getId());
                } else verify(2);
            } else {
                Notifications.create().text("Sélectionnez une UE").showWarning();
            }
        } else {
            currentSemester = semester_list_choiceBox.getSelectionModel().getSelectedItem();
            subjectSelected = subject_tableView.getSelectionModel().getSelectedItem();
            if (subjectSelected != null) {

                if (canContinue) {
                    canContinue = false;
                    FXMLLoader loader = addView();
                    subjectDetailsController controller = loader.getController();
                    controller.setConnection(connection);
                    controller.setSubjectTable(subject_tableView);
                    controller.setSemesterId(currentSemester.getSemester());
                    controller.setCurrentSubject(subjectSelected);
                    controller.setLoan(String.valueOf(subjectSelected.getLoan()));
                    controller.setTechears_name(subjectSelected.getTeacherSName());
                    controller.setName(subjectSelected.getName());
                    controller.setUe_number(String.valueOf(subjectSelected.getUeNbr()));
                    controller.setModify(true);
                    subjects = connection.getSubjects(currentSemester.getSemester());
                } else verify(2);
            } else {
                Notifications.create().text("Sélectionnez une matière").showWarning();
            }

        }

    }


    @FXML
    public void remove_current_matter() {
        if (!user.getRole().contains("admin")) {
            Notifications.create().text(" Seuls les responsales sont autorisés à effectuer des modifications ").showWarning();
            return;
        }

        if (canContinue) {
            canContinue = false;
            if (add_button.getText().contains("UE")) {
                ueSelected = ue_tableView.getSelectionModel().getSelectedItem();
                if (ueSelected != null) {
                    connection.removeUE(ueSelected);
                    ues = connection.getUEs(subjectSelected.getId());
                    ue_tableView.getItems().clear();
                    ue_tableView.getItems().addAll(ues);
                    ue_tableView.refresh();
                } else Notifications.create().text("Sélectionnez une UE").showWarning();
            } else {
                subjectSelected = null;
                currentSemester = semester_list_choiceBox.getSelectionModel().getSelectedItem();
                subjectSelected = subject_tableView.getSelectionModel().getSelectedItem();
                try {
                    ues = connection.getUEs(subjectSelected.getId());
                    connection.removeSubject(subjectSelected);
                    subjects = connection.getSubjects(currentSemester.getSemester());
                    subject_tableView.getItems().clear();
                    subject_tableView.getItems().addAll(subjects);
                    subject_tableView.refresh();
                    for (UE ue : ues) connection.removeUE(ue);
                } catch (Exception e) {
                    Notifications.create().text("Erreur de suppression ").showError();
                }
            }
        } else verify(3);
    }

    @FXML
    void remove_current_semester() {
        if (!user.getRole().contains("admin")) {
            Notifications.create().text(" Seuls les responsales sont autorisés à effectuer cette action ").showWarning();
            return;
        }
    }

    @FXML
    void return_to_semester_view(ActionEvent event) {
        changeView(new_semester_pane, semester_view_vbox);

    }

    @FXML
    void see_or_return() {

        subjectSelected = subject_tableView.getSelectionModel().getSelectedItem();
        if (subjectSelected == null && add_button.getText().contains("matière")) {
            Notifications.create().text("Sélectionnez une matière").showWarning();
        } else {

            ues = connection.getUEs(subjectSelected.getId());
            int count = 0;
            for (UE u : ues) u.setN(++count);

            ue_tableView.setItems(FXCollections.observableArrayList(ues));
            Node newNode;
            Node oldNode;
            if (see_or_return_button.getText().contains("Voir les UE")) {
                newNode = ue_tableView;
                oldNode = subject_tableView;
                see_or_return_button.setText("Retour aux matières");
                add_button.setText("Ajouter une UE");
                ue_tableView.refresh();
            } else {
                newNode = subject_tableView;
                oldNode = ue_tableView;
                see_or_return_button.setText("Voir les UE");
                add_button.setText("Ajouter une matière");
                subject_tableView.refresh();
            }
            changeView(oldNode, newNode);
        }
    }

    private void changeView(Node oldNode, Node newNode) {
        oldNode.setVisible(false);
        newNode.setVisible(true);
        new FadeInRight(newNode).play();
    }

    private FXMLLoader addView() throws IOException {
        Pane pane;
        FXMLLoader loader;
        if (add_button.getText().contains("Ajouter une matière")) {
            loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("subjectDetails.fxml")));
            pane = loader.load();
        } else {
            loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("ueDetails.fxml")));
            pane = loader.load();
        }
        new ZoomInDown(pane).play();
        pane.setLayoutX(475);
        pane.setLayoutY(200);
        menu_view.getChildren().add(pane);
        return loader;
    }

    @FXML
    public void search_with_name(ActionEvent event) {
        if (add_button.getText().contains("Ajouter une matière")) {
            ArrayList<Subject> subjects1 = new ArrayList<>();
            for (Subject subject : subjects) {
                if (subject.getName().toUpperCase().contains(search_with_name_field.getText().toUpperCase()) &&
                    subject.getSemester() == semester_list_choiceBox.getSelectionModel().getSelectedItem().getSemester())
                    subjects1.add(subject);
            }
            subject_tableView.getItems().clear();
            subject_tableView.getItems().addAll(subjects1);
            subject_tableView.refresh();
        } else if (add_button.getText().contains("UE")) {
            ues = connection.getUEs(subjectSelected.getId());
            ArrayList<UE> ues1 = new ArrayList<>();
            for (UE u : ues) {
                if (u.getName().toUpperCase().contains(search_with_name_field.getText().toUpperCase())) ues1.add(u);
                ue_tableView.getItems().clear();
                ue_tableView.getItems().addAll(ues1);
                ue_tableView.refresh();
            }
        }
    }

    @FXML
    public void return_to_menu(ActionEvent event) throws IOException {
        openNewWindow(new FXMLLoader(mean_interfaceController.class.getResource("mean_interface.fxml")));
    }

    void openNewWindow(FXMLLoader loader) throws IOException {
        menu_view.getChildren().clear();
        AnchorPane pane = loader.load();
        menu_view.getChildren().setAll(pane);
        mean_interfaceController controller = loader.getController();
        controller.setUser(user);
    }

    @FXML
    void change_semester_begin(ActionEvent event) {
        if (changeSemester) {
            if (!user.getRole().contains("admin")) {
                Notifications.create().text(" Seuls les responsales sont autorisés à effectuer cette action ").showWarning();
                return;
            }
            currentSemester = semester_list_choiceBox.getSelectionModel().getSelectedItem();
            currentSemester.setBegin(Date.valueOf(semester_begining_dateOicker.getValue()));
            connection.updateSemesterDate1(currentSemester);
        }

    }

    @FXML
    void change_semester_end(ActionEvent event) {
        if (changeSemester) {
            if (!user.getRole().contains("admin")) {
                Notifications.create().text(" Seuls les responsales sont autorisés à effectuer cette action ").showWarning();
                return;
            }
            currentSemester = semester_list_choiceBox.getSelectionModel().getSelectedItem();
            currentSemester.setEnd(Date.valueOf(semester_end_dateOicker.getValue()));
            connection.updateSemesterDate2(currentSemester);
        }
    }


    private void verify(int methodID) {
        FXMLLoader loader = new FXMLLoader(mean_interfaceController.class.getResource("confirm.fxml"));
        Pane root = new Pane();
        try {
            root = loader.load();
        } catch (Exception _) {
        }
        root.setLayoutX(5);
        root.setLayoutY(2);
        boolean temp = true;
        for (Node node : menu_view.getChildren()) {
            if (node.getLayoutX() == root.getLayoutX() && node.getLayoutY() == root.getLayoutY()) {
                menu_view.getChildren().remove(node);
                break;
            }
        }
        menu_view.getChildren().add(root);
        new FadeInLeft(root).play();
        confirmController controller = loader.getController();
        controller.setUser(user);
        controller.setValid(canContinue);
        controller.setSourceController2(this);
        controller.setMethodID(methodID);

    }

    public void setCanContinue(Boolean b) {
        this.canContinue = b;
    }

    public void setUser(User user) {
        this.user = user;
        initiate();
        class_field.setText(user.getcLass());
    }


    private void initiate() {
        try {
            connection = new SqliteConnection(user);
            semesters = connection.getSemesters();

            subjects = connection.getSubjects(semesters.getFirst().getSemester());
            subject_tableView.setItems(FXCollections.observableArrayList(subjects));

            int count = 0;
            for (Subject s : subjects) s.setN(++count);
            semester_list_choiceBox.setItems(FXCollections.observableArrayList(semesters));
            semester_list_choiceBox.getSelectionModel().select(0);

            semester_begining_dateOicker.setValue(LocalDate.parse(String.valueOf(semesters.getFirst().getBegin())));
            semester_end_dateOicker.setValue(LocalDate.parse(String.valueOf(semesters.getFirst().getEnd())));
            semester_list_choiceBox.getSelectionModel().selectedItemProperty().addListener(_ -> {
                see_or_return_button.setText("Voir les UE");
                add_button.setText("Ajouter une matière");
                changeSemester = false;
                Semester semester = semester_list_choiceBox.getSelectionModel().getSelectedItem();
                subjects = connection.getSubjects(semester.getSemester());
                subject_tableView.getItems().clear();
                subject_tableView.getItems().addAll(subjects);
                subject_tableView.refresh();
                ue_tableView.setVisible(false);
                subject_tableView.setVisible(true);
                new Flash(subject_tableView).play();
                semester_begining_dateOicker.setValue(LocalDate.parse(String.valueOf(semester.getBegin())));
                semester_end_dateOicker.setValue(LocalDate.parse(String.valueOf(semester.getEnd())));
                changeSemester = true;
            });
        } catch (Exception _) {
        }

    }
}
