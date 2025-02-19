package docs.la_creme_des_cremes_docs.live;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import docs.la_creme_des_cremes_docs.Classes.User;
import docs.la_creme_des_cremes_docs.SqliteConnection.LoginConnection;
import docs.la_creme_des_cremes_docs.cloud.GoogleDriveService;
import docs.la_creme_des_cremes_docs.subject.SubjectManagementController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class confirmController implements Initializable {
    @FXML
    private Label label;
    @FXML
    private TextField newPass;
    @FXML
    private PasswordField password;
    @FXML
    private Pane pane;
    private User user;
    private Pane paneToClose;
    private Boolean isValid = false;
    private mean_interfaceController controller1;
    private SubjectManagementController controller2;
    private int methodID;
    private boolean modify;
    private LoginConnection connection;

    @FXML
    void leave(ActionEvent event) {
        new FadeOut(pane).play();
        pane.setVisible(false);
    }

    @FXML
    void save(ActionEvent event) {

        if (modify && label.getText().contains("votre")) {
            if (user.getPassword().equals(password.getText())) {
                label.setText("Entrez le nouveau mot de passe");
                newPass.setVisible(true);
                password.setVisible(false);
                new FadeIn(newPass).play();
                new FadeIn(label).play();
                return;
            } else {
                Notifications.create().title("Mot de passe incorrect").showError();
                return;
            }
        }
        if (modify && label.getText().contains("nouveau")) {

            if (!newPass.getText().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
                String passMsg = """
                         Votre mot de passe doit contenir :\s

                        1. Au moins 8 caractères.
                        2. Au moins une lettre majuscule.
                        3. Au moins une lettre minuscule.
                        4. Au moins un chiffre.
                        5. Au moins un caractère spécial (comme `@`, `#`, `$`, etc.).""";
                Notifications.create().text(passMsg).showWarning();
                return;
            } else {
                if (!GoogleDriveService.isInternetAvailable()) {
                    Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
                    return;
                }
                this.user.setPassword(newPass.getText());
                connection = new LoginConnection();
                connection.updatePassword(newPass.getText(), user.getUserName());
                Notifications.create().text("Traitement en cours ....").showWarning();
                leave(event);
            }

        }


        if (user.getPassword().equals(password.getText())) {
            new FadeOut(pane).play();
            pane.setVisible(false);
            if (controller1 != null) controller1.setCanContinue(true);
            if (controller2 != null) controller2.setCanContinue(true);
            confirm();
        } else Notifications.create().title("Mot de passe incorrect").showError();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPaneToClose(Pane paneToClose) {
        this.paneToClose = paneToClose;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    public void setSourceController1(mean_interfaceController controller1) {
        this.controller1 = controller1;
    }

    public void setSourceController2(SubjectManagementController controller2) {
        this.controller2 = controller2;
    }

    public void setMethodID(int methodID) {
        this.methodID = methodID;
    }

    public void confirm() {
        if (controller1 != null) {
            if (methodID == 1) {
                controller1.note_file();
            } else if (methodID == 2) {
                controller1.add_doc();
            } else if (methodID == 3) {
                controller1.update_doc();
            } else if (methodID == 4) {
                controller1.remove_doc();
            }
        }
        if (controller2 != null) {

            try {
                if (methodID == 1) {
                    controller2.add_new_matter();
                } else if (methodID == 2) {
                    controller2.modify_current_matter();
                } else if (methodID == 3) {
                    controller2.remove_current_matter();
                } else if (methodID == 4) {
                    controller2.add_new_semester_to_semesters_list();
                }
            } catch (IOException _) {

            }
        }
    }

    public void setModify(boolean modify) {
        this.modify = modify;
    }

    public void setConnection(LoginConnection connection) {
        this.connection = connection;
    }
}
