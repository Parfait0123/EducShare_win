package docs.la_creme_des_cremes_docs.SqliteConnection;

import docs.la_creme_des_cremes_docs.Classes.Document;
import docs.la_creme_des_cremes_docs.cloud.GoogleDriveService;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;

public class DownloadDatabase {
    private Connection conn;

    public DownloadDatabase() {
        String filePath = System.getProperty("user.home") + "\\.MyDocs\\database\\Downloads.sqlite";
        String url = "";
        if (Files.exists(Path.of(filePath))) {
            url = "jdbc:sqlite:" + filePath;
        } else {
            if (!GoogleDriveService.isInternetAvailable()) {
                Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
                return;
            }
            try {
                if (GoogleDriveService.findAndDownload("Downloads.sqlite", filePath))
                    url = "jdbc:sqlite:" + filePath;
                else {
                    Notifications.create().text("Erreur ! Vérifiez votre connexion internet").showWarning();
                    return;
                }
            } catch (Exception e) {
                Notifications.create().text(e.getMessage()).title("Erreur !").showError();
                return;
            }
        }
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection établie avec succès");
        } catch (
                SQLException e) {
            Notifications.create().title(" Erreur !").text(e.getMessage()).showError();
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public void makeTheLink(ArrayList<Document> docs) {

        for (Document doc : docs) {
            String local = getDocLocalFileName(doc.getId());
            if (local != null) {
                doc.setDownload(new File(local).exists());
                doc.setLocalFilename(local);
            } else {
                doc.setDownload(false);
                doc.setLocalFilename("");
            }
        }

    }

    public boolean updateDocStatus(long id, boolean status) {
        String sql = "update downloadsDatabase set isDownload=" + status + " where id=" + id;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setBoolean(1, status);
            statement.setLong(2, id);
            statement.executeUpdate();
            return true;
        } catch (SQLException _) {
            return false;
        }
    }

    public boolean sendDocStatus(long id, boolean status, String fileName) {
        String sql = "insert into downloadsDatabase values(?,?,?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, id);
            pst.setBoolean(2, status);
            pst.setString(3, fileName);
            pst.executeUpdate();
            return true;
        } catch (SQLException _) {
            return false;
        }
    }

    public void setDocLocalFileName(String fileName, long id) {
        String sql = "update downloadsDatabase set localFileName='" + fileName + "' where doc_Id=" + id;
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.executeUpdate();
        } catch (Exception _) {
            System.out.println(" Failed");
        }
    }

    public String getDocLocalFileName(long id) {
        String sql = "select localFileName from downloadsDatabase where doc_Id=" + id;
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            ResultSet r = st.executeQuery();
            return r.getString("localFileName");
        } catch (Exception _) {
            return null;
        }
    }


}
