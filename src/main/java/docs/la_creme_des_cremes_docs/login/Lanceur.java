package docs.la_creme_des_cremes_docs.login;

import docs.la_creme_des_cremes_docs.Security.Security;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


public class Lanceur extends Application {
    public static void main(String[] args) {
        launch();
    }

    public static void createFolder(String cheminDossier) {
        Path dossier = Paths.get(cheminDossier);

        try {
            if (!Files.exists(dossier)) {
                Files.createDirectory(dossier);
            }
        } catch (IOException _) {
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));

        Scene scene = new Scene(fxmlLoader.load());
        scene.getRoot().setEffect(new DropShadow(10, Color.rgb(100, 100, 100)));
        scene.setFill(Color.TRANSPARENT);

        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon.png"))));
        stage.setScene(scene);
        stage.setTitle("EducShare");
        stage.show();

        createFolder(System.getProperty("user.home") + "\\.MyDocs");
        createFolder(System.getProperty("user.home") + "\\.MyDocs\\database");
        try {
            Security.cacherFichierSousWindows(new File(System.getProperty("user.home") + "\\.MyDocs\\database\\Login.sqlite"));
            Security.cacherFichierSousWindows(new File(System.getProperty("user.home") + "\\.MyDocs\\database\\a.json"));
            Security.cacherFichierSousWindows(new File(System.getProperty("user.home") + "\\.MyDocs\\database\\Downloads.sqlite"));
        } catch (Exception _) {
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        try {
            int a=1;
            Security.cacherFichierSousWindows(new File(System.getProperty("user.home") + "\\.MyDocs\\database\\Login.sqlite"));
            Security.cacherFichierSousWindows(new File(System.getProperty("user.home") + "\\.MyDocs\\database\\a.json"));
            Security.cacherFichierSousWindows(new File(System.getProperty("user.home") + "\\.MyDocs\\database\\Downloads.sqlite"));
        } catch (Exception _) {
        }
    }
}