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
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ueDetailsController implements Initializable {

    @FXML
    private TextField loan_field;

    @FXML
    private TextField name_field;

    @FXML
    private TextField techears_name_field;
    private TableView<UE> ue_tableView;
    private ArrayList<UE> ue_list;
    private Subject subject;
    private SqliteConnection connection;
    private UE ue;
    private boolean isModification = false;

    @FXML
    void return_to_view(ActionEvent event) {
        Node node = (Node) event.getSource();
        new ZoomOutDown(node.getParent()).play();
    }

    @FXML
    void save(ActionEvent event) {
        Notifications loanNotification = Notifications.create().title("Error").text(" Crédit invalide");
        int loan = 0;
        try {
            loan = Integer.parseInt(loan_field.getText());
            if (loan <= 0) {
                loanNotification.showError();
                return;
            }
        } catch (NumberFormatException _) {
            loanNotification.showError();
            return;
        }

        if (name_field.getText().trim().isEmpty() || techears_name_field.getText().trim().isEmpty() || loan_field.getText().trim().isEmpty()) {
            Notifications.create().title("Remplissez les informations").showWarning();
            return;
        }
        boolean verifyName = true;
        for (UE ue : ue_list) {
            if (ue.getName().trim().equals(name_field.getText().trim())) {
                verifyName = false;
                break;
            }
        }
        if (verifyName) {
            if (!isModification) {
                String tempID = String.valueOf(Instant.now().toEpochMilli());
                UE ue = new UE(name_field.getText(), subject.getId(), tempID, loan, techears_name_field.getText());
                ue.setN(ue_list.size() + 1);
                if (connection.sendUE(ue)) {
                    subject.setUeNbr(subject.getUeNbr() + 1);
                    connection.updateSubject1(subject);
                    ue_list.add(ue);
                    ue_tableView.getItems().clear();
                    ue_tableView.getItems().addAll(ue_list);
                    ue_tableView.refresh();
                    Notifications.create().title("Succès").text(" UE ajouté avec succès").showInformation();

                    Node node = (Node) event.getSource();
                    new ZoomOutDown(node.getParent()).play();
                }

            } else {
                String tempID = ue.getId();
                UE ue = new UE(name_field.getText(), subject.getId(), tempID, loan, techears_name_field.getText());
                if (connection.updateUE(ue)) {
                    ue_list = connection.getUEs(subject.getId());
                    ue_tableView.getItems().clear();
                    ue_tableView.getItems().addAll(ue_list);
                    ue_tableView.refresh();
                    Node node = (Node) event.getSource();
                    new ZoomOutDown(node.getParent()).play();
                }
            }
        } else {
            Notifications.create().title("UE existant .").text(" L'UE existe déjà ").showError();
        }
        ue_tableView.refresh();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setLoan(int loan) {
        loan_field.setText(String.valueOf(loan));
    }

    public void setName_field(String name) {
        name_field.setText(name);
    }

    public void setTechears_name_field(String techears_name) {
        techears_name_field.setText(techears_name);
    }

    public void setUe_tableView(TableView<UE> ue_tableView) {
        this.ue_tableView = ue_tableView;
    }

    public void setUe_list(ArrayList<UE> ue_list) {
        this.ue_list = ue_list;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setConnection(SqliteConnection connection) {
        this.connection = connection;
    }


    public void setUe(UE ue) {
        this.ue = ue;
    }

    public void setModification(boolean modification) {
        isModification = modification;
    }
}
