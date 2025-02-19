package docs.la_creme_des_cremes_docs.SqliteConnection;

import com.google.api.client.http.FileContent;
import docs.la_creme_des_cremes_docs.Classes.*;
import docs.la_creme_des_cremes_docs.cloud.GoogleDriveService;
import docs.la_creme_des_cremes_docs.live.new_docController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import org.controlsfx.control.Notifications;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;

public class SqliteConnection {
    private Connection connection;
    private User user;
    private String filePath;

    public SqliteConnection(User user) {
        this.user = user;
        String c = user.getAuthorizationCode();
        filePath = System.getProperty("user.home") + "\\.MyDocs\\database\\Docs" + c.substring(0, 8) + ".sqlite";
        String url = "";
        if (Files.exists(Path.of(filePath))) {
            url = "jdbc:sqlite:" + filePath;
        } else {
            try {
                if (!GoogleDriveService.isInternetAvailable()) {
                    Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
                    return;
                }
                if (GoogleDriveService.findAndDownload("Docs" + c.substring(0, 8) + ".sqlite", filePath))
                    url = "jdbc:sqlite:" + filePath;
                else {
                    Notifications.create().text("Erreur ! Vérifiez votre connexion internet").showWarning();
                    return;
                }
            } catch (Exception e) {
                System.out.println(e);
                Notifications.create().text(e.getMessage()).title("Erreur !").showError();
                return;
            }
        }
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Connection établie avec succès");
        } catch (
                SQLException e) {
            Notifications.create().title(" Erreur !").text(e.getMessage()).showError();
        }
    }

    public boolean connectionIsEstablished() {
        return connection != null;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connexion fermée avec succès");
            } catch (SQLException e) {
                System.out.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }

    public ArrayList<Document> getDocuments() {
        ArrayList<Document> documents = new ArrayList<>();
        String query = "SELECT * FROM documents";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query)) {
            int compt = 0;
            while (result.next()) {
                long id = result.getLong("id");
                String title = result.getString("title").split("°°")[0];
                int semesterId = result.getInt("semester_id");
                String subjectId = result.getString("subject_id");
                String ueId = result.getString("ue_id");
                String category = result.getString("category");
                Double rating = result.getDouble("rating");
                String senderName = result.getString("sender_name");
                String sentDate = result.getString("sent_date");
                String fileFormat = result.getString("file_format");
                String downloadUrl = result.getString("download_url");
                String subject = result.getString("subject_name");
                String ue = result.getString("ue_name");
                long size = result.getLong("doc_size");
                Document doc = new Document(id, title, semesterId, subjectId, subject, ueId, ue, category, rating, fileFormat, senderName, Date.valueOf(sentDate), size);
                doc.setN(++compt);
                doc.setDownload(false);
                doc.setDownloadUrl(downloadUrl);
                doc.setId(id);
                documents.add(doc);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            Notifications.create().title("Erreur").showError();

        }
        DownloadDatabase db = new DownloadDatabase();
        db.makeTheLink(documents);
        documents.sort((d1, d2) -> d2.getSendDate().compareTo(d1.getSendDate()));
        int count = 0;
        for (Document doc : documents) doc.setN(++count);
        return documents;
    }

    public boolean sendDocument(Document doc, User user, TableView<Document> doc_table, new_docController controller) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> Notifications.create().text("En cours d'envoie .....").showInformation());

                String query = "INSERT INTO documents VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setLong(1, doc.getId());
                statement.setString(2, doc.getName());
                statement.setInt(3, doc.getSemester());
                statement.setString(4, doc.getSubjectID());
                statement.setString(5, doc.getUeID());
                statement.setString(6, doc.getCategory());
                statement.setDouble(7, doc.getEtoile());
                statement.setString(8, doc.getSendBy());
                statement.setString(9, String.valueOf(doc.getSendDate()));
                statement.setString(10, doc.getFormat());

                statement.setString(12, doc.getLocalFilename());
                statement.setString(13, doc.getSubject());
                statement.setString(14, doc.getUe());
                statement.setLong(15, doc.getSize());


                String mimeType;
                if (doc.getFormat().contains("txt")) mimeType = "text/txt";
                else if (doc.getFormat().contains("img") || doc.getFormat().contains("png"))
                    mimeType = "image/" + doc.getFormat();
                else mimeType = "application/" + doc.getFormat();


                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(new java.io.File(doc.getLocalFilename()).getName());

                FileContent fileContent = new FileContent(mimeType, new java.io.File(doc.getLocalFilename()));

                com.google.api.services.drive.model.File uploadedFile = GoogleDriveService.getDriveService().files().create(fileMetadata, fileContent)
                        .setFields("id") // Obtenez l'ID du fichier créé
                        .execute();

                String downloadID = uploadedFile.getId();

                doc.setDownloadUrl(downloadID);
                System.out.println(" fichier envoyé " + downloadID);

                statement.setString(11, downloadID);
                statement.executeUpdate();
                DownloadDatabase db = new DownloadDatabase();
                db.sendDocStatus(doc.getId(), true, doc.getLocalFilename());
                System.out.println("mise à jour éffectué ");

                GoogleDriveService.update2(filePath, user.getAuthorizationCode());

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    Notifications.create().text("Document envoyé avec succés .").showInformation();
                    controller.returnn();
                    doc_table.getItems().clear();
                    doc_table.setItems(FXCollections.observableArrayList(getDocuments()));
                    doc_table.getSelectionModel().select(doc);
                    doc_table.refresh();

                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> Notifications.create().text(" Echec de l'ajout ").showError());
            }
        };
        new Thread(task).start();

        return true;
    }

    public boolean UpdateDocument(Document doc, User user, TableView<Document> doc_table, new_docController controller) {
        return (removeDocument(doc) && sendDocument(doc, user, doc_table, controller));
    }

    public boolean UpdateDocRate(long id, double rate) {
        String query = "UPDATE documents SET rating = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, rate);
            statement.setLong(2, id);
            statement.executeUpdate();

            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public int getCommentCount(long id) {
        String query = "SELECT COUNT(*) FROM comments WHERE docId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            return statement.executeUpdate();
        } catch (Exception _) {
            return 0;
        }
    }

    public boolean removeDocument(Document document) {

        String query = "DELETE FROM documents WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, document.getId());
            statement.executeUpdate();

            GoogleDriveService.deleteFile(document.getDownloadUrl());
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public ArrayList<Semester> getSemesters() {
        ArrayList<Semester> semesters = new ArrayList<>();
        String query = "SELECT * FROM semesters";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query)) {

            while (result.next()) {
                int id = result.getInt("id");
                Date begin = Date.valueOf(result.getString("start_date"));
                Date end = Date.valueOf(result.getString("end_date"));
                semesters.add(new Semester(id, begin, end));
            }
        } catch (SQLException e) {
            Notifications.create().title("Erreur").showError();
        }
        return semesters;
    }

    public boolean sendSemester(Semester semester) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "INSERT INTO semesters (id,start_date, end_date) VALUES (?,?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, semester.getSemester());
            statement.setString(2, String.valueOf(semester.getBegin()));
            statement.setString(3, String.valueOf(semester.getEnd()));
            statement.executeUpdate();

            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            Notifications.create().title(" Semestre ajouté avec succès ").showInformation();
            return true;
        } catch (Exception e) {
            Notifications.create().title("Erreur lors de l'insertion du semestre ").text(e.getMessage()).showError();
            return false;
        }
    }

    public boolean updateSemesterDate1(Semester semester) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "UPDATE  semesters SET start_date=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(semester.getBegin()));
            statement.setInt(2, semester.getSemester());
            statement.executeUpdate();

            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            Notifications.create().title(" Semestre modifié avec succès ").showInformation();
            return true;
        } catch (Exception e) {
            Notifications.create().title("Erreur lors de la modification de la date ").text(e.getMessage()).showError();
            return false;
        }
    }

    public boolean updateSemesterDate2(Semester semester) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "UPDATE  semesters SET end_date=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, String.valueOf(semester.getEnd()));
            statement.setInt(2, semester.getSemester());
            statement.executeUpdate();

            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            Notifications.create().title(" Semestre modifié avec succès ").showInformation();
            return true;
        } catch (Exception e) {
            Notifications.create().title("Erreur lors de la modification de la date ").text(e.getMessage()).showError();
            return false;
        }
    }

    public ArrayList<Subject> getSubjects(int semesterId) {
        ArrayList<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects WHERE semester_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, semesterId);
            ResultSet result = statement.executeQuery();
            int count = 0;
            while (result.next()) {
                String name = result.getString("name");
                int ueNbr = result.getInt("number_of_ues");
                int loan = result.getInt("loan");
                String teacherLastName = result.getString("teacher_last_name");
                String id = result.getString("id");
                int semester = result.getInt("semester_id");
                Subject subject = new Subject(semester, name, ueNbr, loan, teacherLastName, id);
                subject.setN(++count);
                subjects.add(subject);
            }
        } catch (SQLException e) {
            Notifications.create().title("Erreur").showError();
        }
        return subjects;
    }

    public ArrayList<Subject> getSubjects() {
        ArrayList<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet result = statement.executeQuery();
            int count = 0;
            while (result.next()) {
                String name = result.getString("name");
                int ueNbr = result.getInt("number_of_ues");
                int loan = result.getInt("loan");
                String teacherLastName = result.getString("teacher_last_name");
                String id = result.getString("id");
                int semester = result.getInt("semester_id");
                Subject subject = new Subject(semester, name, ueNbr, loan, teacherLastName, id);
                subject.setN(++count);
                subjects.add(subject);
            }
        } catch (SQLException e) {
            Notifications.create().title("Erreur").showError();
        }
        return subjects;
    }

    public boolean sendSubject(Subject subject) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "INSERT INTO subjects (name, number_of_ues, loan, teacher_last_name, id, semester_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subject.getName());
            statement.setInt(2, subject.getUeNbr());
            statement.setInt(3, subject.getLoan());
            statement.setString(4, subject.getTeacherSName());
            statement.setString(5, subject.getId());
            statement.setInt(6, subject.getSemester());
            statement.executeUpdate();

            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            return true;
        } catch (Exception e) {
            Notifications.create().title("Erreur de l'ajout ").text(e.getMessage()).showError();
            return false;
        }
    }

    public ArrayList<UE> getUEs(String subjectId) {
        ArrayList<UE> ues = new ArrayList<>();
        String query = "SELECT * FROM ues WHERE subject_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subjectId);
            getUEsMethode(ues, statement);
        } catch (SQLException e) {
            Notifications.create().title("Erreur").showError();
        }
        return ues;
    }

    public ArrayList<UE> getUEs() {
        ArrayList<UE> ues = new ArrayList<>();
        String query = "SELECT * FROM ues ";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            getUEsMethode(ues, statement);
        } catch (SQLException e) {
            Notifications.create().title("Erreur").showError();
        }
        return ues;
    }

    private void getUEsMethode(ArrayList<UE> ues, PreparedStatement statement) throws SQLException {
        ResultSet result = statement.executeQuery();
        ArrayList<Subject> subjects = getSubjects();
        int count = 0;
        while (result.next()) {
            String name = result.getString("name");
            String id = result.getString("id");
            String subjectId1 = result.getString("subject_id");
            UE ue = new UE(name, id, subjectId1);
            for (Subject s : subjects) {
                if (subjectId1.trim().equals(s.getId().trim())) {
                    ue.setLoan(s.getLoan());
                    ue.setTeacherSName(s.getTeacherSName());
                    break;
                }
            }
            ue.setN(++count);
            ues.add(ue);

        }
    }

    public boolean sendUE(UE ue) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "INSERT INTO ues (name, id, subject_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ue.getName());
            statement.setString(2, ue.getId());
            statement.setString(3, ue.getSubjectId());
            statement.executeUpdate();

            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            return true;
        } catch (Exception e) {
            Notifications.create().title("Erreur lors de l'insertion de la UE").text(e.getMessage()).showError();
            return false;
        }
    }

    public boolean updateUE(UE ue) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "DELETE FROM ues WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ue.getId());
            statement.executeUpdate();

            if (sendUE(ue)) {
                GoogleDriveService.update2(filePath, user.getAuthorizationCode());
                Notifications.create().title("Succès ").text(" UE moidifé avec succès ").showInformation();
                return true;
            }
        } catch (Exception e) {
            Notifications.create().title("Erreur ").text("Erreur lors de la mise à jour de  l'UE ;" + e.getMessage()).showError();
            return false;
        }
        return false;
    }

    public boolean updateUE1(UE ue) {
        String query = "DELETE FROM ues WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ue.getId());
            statement.executeUpdate();
            return sendUE(ue);
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean removeUE(UE ue) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "DELETE FROM ues WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ue.getId());
            statement.executeUpdate();
            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            Notifications.create().title("UE supprimé avec succès").showInformation();
            return true;
        } catch (Exception e) {
            Notifications.create().title("Erreur de suppression ").text(e.getMessage()).showError();
            return false;
        }
    }

    public boolean updateSubject(Subject subject) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "DELETE FROM subjects WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subject.getId());

            statement.executeUpdate();
            if (sendSubject(subject)) {
                GoogleDriveService.update2(filePath, user.getAuthorizationCode());
                Notifications.create().title("Succès ").text(" Matière moidifée avec succès ").showInformation();
                return true;
            }
        } catch (Exception e) {
            Notifications.create().title("Erreur ").text("Erreur lors de la mise à jour de  la matière ;\n" + e.getMessage()).showError();
            return false;
        }
        return false;
    }

    public boolean updateSubject1(Subject subject) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }
        String query = "DELETE FROM subjects WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subject.getId());
            statement.executeUpdate();

            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            return sendSubject(subject);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean removeSubject(Subject subject) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }
        String query = "DELETE FROM subjects WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subject.getId());

            statement.executeUpdate();
            Notifications.create().title("Matière supprimée avec succès").showInformation();
            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            return true;
        } catch (Exception e) {
            Notifications.create().title("Erreur de suppression ").text(e.getMessage()).showError();
            return false;
        }
    }

    public boolean sendComment(Comment comment) {
        if (!GoogleDriveService.isInternetAvailable()) {
            Notifications.create().title("Connectez-vous à l'intenet svp !").showWarning();
            return false;
        }

        String query = "INSERT INTO comments (comment_text, rate,author,authorId,docId) VALUES (?, ?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, comment.getCommentText());
            statement.setDouble(2, comment.getRate());
            statement.setString(3, comment.getAuthor());
            statement.setString(4, comment.getAuthorId());
            statement.setLong(5, comment.getDocId());

            statement.executeUpdate();
            GoogleDriveService.update2(filePath, user.getAuthorizationCode());
            Platform.runLater(() -> Notifications.create().title(" Commentaire ajouté avec succès ").showInformation());

            return true;
        } catch (Exception e) {
            System.out.println(e);
            Notifications.create().title("Erreur d'insertion du commentaire ").text(e.getMessage()).showError();
            return false;
        }
    }

    public ArrayList<Comment> getComments(long documentId) {
        String query = "SELECT * FROM comments WHERE docId = ?";
        ArrayList<Comment> comments = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, documentId);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                int docId = result.getInt("docId");
                double rate = result.getDouble("rate");
                String comment_text = result.getString("comment_text");
                String author = result.getString("author");
                String authorId = result.getString("authorId");
                comments.add(new Comment(author, comment_text, rate, docId, authorId));
            }
        } catch (Exception _) {
        }
        return comments;
    }

    public ArrayList<Category> getCategories() {
        String query = "SELECT * FROM category";
        ArrayList<Category> categories = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String category = result.getString("category_name");
                categories.add(new Category(category));
            }
        } catch (Exception _) {
        }
        return categories;
    }

    public boolean sendCategory(Category category) {
        String query = "INSERT INTO category (category_name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, category.getCategory());
            statement.executeUpdate();
            return true;
        } catch (Exception _) {
            return false;
        }
    }
}
