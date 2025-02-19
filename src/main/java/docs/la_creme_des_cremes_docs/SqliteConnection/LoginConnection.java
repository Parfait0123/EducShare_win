package docs.la_creme_des_cremes_docs.SqliteConnection;

import docs.la_creme_des_cremes_docs.Classes.User;
import docs.la_creme_des_cremes_docs.cloud.GoogleDriveService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;

public class LoginConnection {
    private Connection login;

    public LoginConnection() {
        String filePath = System.getProperty("user.home") + "\\.MyDocs\\database\\Login.sqlite";
        String url = "";
        if (Files.exists(Path.of(filePath))) {
            url = "jdbc:sqlite:" + filePath;
        } else {
            if (!GoogleDriveService.isInternetAvailable()) {

                Notifications n = Notifications.create().title("Connectez-vous à l'intenet svp !");
                n.owner(new Stage());
                n.showWarning();

                return;
            }
            try {
                if (GoogleDriveService.findAndDownload("Login.sqlite", filePath))
                    url = "jdbc:sqlite:" + filePath;
                else {
                    Notifications.create().text("Erreur ! Vérifiez votre connexion internet").showWarning();
                    return;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                //Notifications.create().text(e.getMessage()).title("Erreur !").showError();
                return;
            }
        }
        try {
            login = DriverManager.getConnection(url);
            System.out.println("Connection établie avec succès");
        } catch (
                SQLException e) {
            System.out.println(e);
            Notifications.create().title(" Erreur !").showError();
        }
    }

    public Connection getLogin() {
        return login;
    }

    public void setLogin(Connection login) {
        this.login = login;
    }

    public ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        Statement statement = null;
        try {
            statement = login.createStatement();
        } catch (Exception e) {
            Notifications.create().title("Connectez-vous à l'internet svp !").showWarning();
            return null;
        }
        try (ResultSet result = statement.executeQuery(query)) {

            while (result.next()) {
                String userFirstName = result.getString("first_name");
                String userLastName = result.getString("last_name");
                String userEmail = result.getString("email");
                String userPassword = result.getString("password");
                String userName = result.getString("username");
                String userRole = result.getString("user_type");
                String cLass = result.getString("class_name");
                String code = result.getString("authorizationCode");
                User user = new User(userFirstName, userLastName, userEmail, userPassword, userName, userRole, cLass);
                user.setAuthorizationCode(code);
                users.add(user);
            }
        } catch (SQLException e) {
            Notifications.create().title("Erreur").showError();
        }

        return users;
    }

    public boolean sendUser(User user) {
        String query = "INSERT INTO users (first_name, last_name, username, email, password, user_type, class_name,class_name,authorizationCode) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?,?,?)";
        try (PreparedStatement preparedStatement = login.prepareStatement(query)) {
            if (GoogleDriveService.isInternetAvailable()) {
                preparedStatement.setString(1, user.getFirstName());
                preparedStatement.setString(2, user.getLastName());
                preparedStatement.setString(3, user.getUserName());
                preparedStatement.setString(4, user.getEmail());
                preparedStatement.setString(5, user.getPassword());
                preparedStatement.setString(6, user.getRole());
                preparedStatement.setString(7, user.getcLass());
                preparedStatement.setString(8, user.getcLass());
                preparedStatement.setString(9, user.getAuthorizationCode());
                preparedStatement.executeUpdate();
                GoogleDriveService.update("Login.sqlite");
                return true;
            } else Notifications.create().text(" Connectez-vous à l'internet svp !").showWarning();
            return false;
        } catch (Exception e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
            String msg = "";
            if (e.getMessage().contains("UNIQUE constraint failed: users.username"))
                msg = "Changez d'identifiant svp !";
            Notifications.create().title("Erreur de création du compte ").text(msg).showError();
            return false;
        }
    }

    public String getUserClass(String id) {
        String class1 = "";
        try {
            class1 = id.split("-")[0].trim();
        } catch (Exception _) {
        }

        if (!class1.equals("rudze") && !class1.equals("jkluvek")) {
            Notifications.create().title("Code d'autorisation incorrecte !").showError();
            return null;
        }
        String query = "SELECT class_name FROM class WHERE id = ?";

        try (PreparedStatement statement = login.prepareStatement(query)) {
            statement.setString(1, id.replaceFirst(class1 + "-", "").trim());
            ResultSet result = statement.executeQuery();
            System.out.println(result.getString("class_name"));
            if (result.next()) return result.getString("class_name");
            else {
                Notifications.create().title("Code d'autorisation incorrecte ! ").showError();
                return null;
            }


        } catch (Exception e) {
            return null;
        }
    }

    public void updatePassword(String password, String userName) {
        String sql = "Update users set password=? where username=?";
        try (PreparedStatement statement = login.prepareStatement(sql)) {
            statement.setString(1, password);
            statement.setString(2, userName);
            statement.executeUpdate();
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String filePath = System.getProperty("user.home") + "\\.MyDocs\\database\\Login.sqlite";
                    GoogleDriveService.update(filePath);
                    return null;
                }

                @Override
                protected void succeeded() {
                    Platform.runLater(() -> Notifications.create().text("Mot de passe mdifié avec succès !").showInformation());
                }
            };
            new Thread(task).start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
