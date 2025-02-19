package docs.la_creme_des_cremes_docs.subject;

import animatefx.animation.ZoomOutDown;
import docs.la_creme_des_cremes_docs.Classes.Subject;
import docs.la_creme_des_cremes_docs.Classes.UE;
import docs.la_creme_des_cremes_docs.SqliteConnection.SqliteConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.controlsfx.control.Notifications;

import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;

public class subjectDetailsController implements Initializable {

    @FXML
    private TextField loan_field;

    @FXML
    private TextField name_field;

    @FXML
    private TextField techears_name_field;

    @FXML
    private TextField ue_number_field;
    private int semesterId;
    private SqliteConnection connection;
    private TableView<Subject> subjectTable;
    private Subject currentSubject;
    private boolean isModify;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (isModify) {
            name_field.setText(currentSubject.getName());
            loan_field.setText(String.valueOf(currentSubject.getLoan()));
            techears_name_field.setText(currentSubject.getTeacherSName());
            ue_number_field.setText(String.valueOf(currentSubject.getUeNbr()));
        } else {
            ue_number_field.setText("1");
        }
        ue_number_field.setEditable(false);
    }

    @FXML
    void return_to_view(ActionEvent event) {
        close(event);
    }

    @FXML
    void save(ActionEvent event) {
        boolean s = false;
        if (name_field.getText().isEmpty() || techears_name_field.getText().isEmpty() || loan_field.getText().isEmpty()) {
            Notifications.create().title("Remplissez toutes les informations ").showWarning();
        } else {
            int loan = 0;
            try {
                loan = Integer.parseInt(loan_field.getText());
            } catch (NumberFormatException _) {
                Notifications.create().title("Crédit invalide").showError();
                return;
            }
            if (loan <= 0) {
                Notifications.create().title("Crédit invalide").showError();
                return;
            }

            boolean temp = false;
            if (isModify) {
                connection.updateSubject(new Subject(semesterId, name_field.getText().trim(), currentSubject.getUeNbr(), loan, techears_name_field.getText().trim(), currentSubject.getId()));
            } else {
                String subjectId = String.valueOf(Instant.now().toEpochMilli());
                String ueID = String.valueOf(Instant.now().toEpochMilli());
                temp = connection.sendSubject(new Subject(semesterId, name_field.getText().trim(), 1, loan, techears_name_field.getText().trim(), subjectId));
                temp = temp && connection.sendUE(new UE(name_field.getText().trim(), ueID, subjectId));
            }

            if (temp) {
                subjectTable.getItems().clear();
                subjectTable.getItems().addAll(connection.getSubjects(semesterId));
                subjectTable.refresh();
                String msg = "";
                if (isModify) msg = name_field.getText() + " modifiée avec succès";
                else msg = name_field.getText() + " ajoutée avec succès";
                Notifications.create().title(msg).showInformation();
                close(event);
            }

        }
    }

    private void close(ActionEvent event) {
        Node node = (Node) event.getSource();
        new ZoomOutDown(node.getParent()).play();
    }

    void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }

    public void setConnection(SqliteConnection connection) {
        this.connection = connection;
    }

    public void setSubjectTable(TableView<Subject> subjectTable) {
        this.subjectTable = subjectTable;
    }

    public void setLoan(String loan) {
        this.loan_field.setText(loan);
    }

    public void setName(String name) {
        this.name_field.setText(name);
    }

    public void setTechears_name(String techears_name) {
        this.techears_name_field.setText(techears_name);
    }

    public void setUe_number(String ue_number) {
        this.ue_number_field.setText(ue_number);
    }

    public void setCurrentSubject(Subject currentSubject) {
        this.currentSubject = currentSubject;
    }

    public void setModify(boolean modify) {
        isModify = modify;
    }
}
