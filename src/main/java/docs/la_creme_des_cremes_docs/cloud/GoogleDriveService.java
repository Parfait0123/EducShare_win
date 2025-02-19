package docs.la_creme_des_cremes_docs.cloud;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import docs.la_creme_des_cremes_docs.Security.Security;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import org.controlsfx.control.Notifications;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class GoogleDriveService {

    private static final String APPLICATION_NAME = "Docs";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String SERVICE_ACCOUNT_KEY_FILE = System.getProperty("user.home") + "\\.MyDocs\\database\\a.json";


    public static Drive getDriveService() throws Exception {
        verify();
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_ACCOUNT_KEY_FILE))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));


        return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    public static void downloadFileFromMyDrive(String fileId, String destinationPath) throws Exception {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                Drive driveService = getDriveService();
                OutputStream outputStream = new FileOutputStream(destinationPath);
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                outputStream.close();
                System.out.println("Fichier téléchargé avec succès.");
                return null;
            }
        };
        new Thread(task).start();
    }

    public static void listAllFiles() throws Exception {

        Drive driveService = getDriveService();
        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setPageToken(pageToken)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();

            List<File> files = result.getFiles();

            if (files == null || files.isEmpty()) {
                System.out.println("Aucun fichier trouvé dans Google Drive.");
            } else {
                System.out.println("Fichiers trouvés dans Google Drive :");
                for (File file : files) {
                    System.out.printf("Nom : %s, ID : %s%n", file.getName(), file.getId());
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

    }

    public static void listAllFilesWithDetails() throws Exception {

        Drive driveService = getDriveService();
        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setPageToken(pageToken)
                    .setFields("nextPageToken, files(id, name, mimeType, size, parents, createdTime, modifiedTime, owners)")
                    .execute();

            List<File> files = result.getFiles();

            if (files == null || files.isEmpty()) {
                System.out.println("Aucun fichier trouvé dans Google Drive.");
            } else {
                System.out.println("Fichiers trouvés dans Google Drive :");
                for (File file : files) {
                    System.out.printf("Nom : %s%n", file.getName());
                    System.out.printf("ID : %s%n", file.getId());
                    System.out.printf("Type MIME : %s%n", file.getMimeType());
                    System.out.printf("Taille : %s%n", file.getSize());
                    System.out.printf("Parents : %s%n", file.getParents());
                    System.out.printf("Créé le : %s%n", file.getCreatedTime());
                    System.out.printf("Modifié le : %s%n", file.getModifiedTime());
                    System.out.printf("Propriétaires : %s%n", file.getOwners());
                    System.out.println("--------------------------------------------------");
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

    }

    public static boolean findAndDownload(String folderName, String destinationPath) throws Exception {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Drive driveService = GoogleDriveService.getDriveService();
                String query = "mimeType = '" + "application/sqlite" + "' and name = '" + folderName + "'";
                FileList result = driveService.files().list()
                        .setQ(query)
                        .setFields("files(id, name)")
                        .execute();
                List<File> folders = result.getFiles();
                System.out.println(folders.size());
                if (!folders.isEmpty()) {
                    downloadFileFromMyDrive(folders.getFirst().getId(), destinationPath);
                    Security.cacherFichierSousWindows(new java.io.File(destinationPath));
                    return true;
                }
                return false;
            }
        };
        new Thread(task).start();
        while (!task.isDone()) ;
        return task.get();
    }

    public static String findFolderByName2(String folderName) throws Exception {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                Drive driveService = GoogleDriveService.getDriveService();
                String query = "mimeType = '" + "application/sqlite" + "' and name = '" + folderName + "'";
                FileList result = driveService.files().list()
                        .setQ(query)
                        .setFields("files(id, name)")
                        .execute();
                List<File> folders = result.getFiles();
                if (!folders.isEmpty()) {
                    return folders.getFirst().getId();
                } else return "";
            }
        };
        new Thread(task).start();
        while (!task.isDone()) System.out.print("");
        return task.get();
    }


    public static void update(String filePath) throws Exception {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String fileID = findFolderByName2("Login.sqlite");
                return getaVoid(fileID, filePath);
            }
        };
        new Thread(task).start();

    }

    public static void update2(String filePath, String code) throws Exception {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String fileName = "Docs" + code.substring(0, 8) + ".sqlite";
                String fileID = findFolderByName2(fileName);
                return getaVoid(fileID, filePath);
            }
        };
        new Thread(task).start();
    }

    private static Void getaVoid(String fileID, String filePath) throws Exception {
        java.io.File newFile = new java.io.File(filePath);
        FileContent mediaContent = new FileContent("application/sqlite", newFile);
        File updatedFile = GoogleDriveService.getDriveService().files().update(fileID, null, mediaContent).execute();
        System.out.println("Le fichier a été mis à jour : " + updatedFile.getName());
        return null;
    }

    public static void downloadFileWithProgress(long fileSize, String fileId, ProgressBar progressBar, String destinationPath) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Drive driveService = getDriveService();
                // Flux de sortie pour écrire dans le fichier local
                OutputStream outputStream = new FileOutputStream(destinationPath);

                // Obtenir l'InputStream du fichier sur Google Drive
                InputStream inputStream = driveService.files().get(fileId).executeMediaAsInputStream();

                byte[] buffer = new byte[1024]; // Taille du buffer pour la lecture
                int bytesRead;
                long totalBytesRead = 0;

                // Boucle pour lire les données et suivre la progression
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead); // Écriture des données dans le fichier local
                    totalBytesRead += bytesRead; // Mise à jour du total téléchargé
                    // Calcul de la progression
                    double progress = (double) totalBytesRead / fileSize;
                    updateMessage(String.valueOf(progress));

                }

                // Fermer les flux
                outputStream.close();
                inputStream.close();
                updateMessage("Téléchargement terminé !");

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> Notifications.create().text("Erreur de téléchargement").showError());
            }

            @Override
            protected void updateMessage(String message) {
                if (message.contains("terminé")) Platform.runLater(() -> Notifications.create().text(message).show());
                else {
                    try {
                        Platform.runLater(() -> progressBar.setProgress(Double.parseDouble(message)));
                    } catch (Exception _) {
                    }
                }


            }
        };
        new Thread(task).start();


    }

    public static boolean isInternetAvailable() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    URL url = new URL("https://clients3.google.com/generate_204");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("HEAD");
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(4000);
                    return conn.getResponseCode() == 204;
                } catch (IOException e) {
                    return false;
                }
            }
        };
        new Thread(task).start();
        while (!task.isDone()) System.out.print("");
        try {
            return task.get();
        } catch (Exception _) {
            return false;
        }
    }

    public static void deleteFile(String fileId) throws Exception {
        try {
            Drive d = getDriveService();
            d.files().delete(fileId).execute();
        } catch (Exception _) {
            System.out.println("Erreur de suppression");
        }
    }

    public static void uploadFile(java.io.File filePath) throws Exception {
        // Créer le fichier sur Google Drive
        File fileMetadata = new File();
        fileMetadata.setName(filePath.getName()); // Nom du fichier sur Drive

        // Spécifier le type de contenu
        FileContent mediaContent = new FileContent("application/sqlite", filePath);

        // Envoyer le fichier
        File uploadedFile = getDriveService().files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        System.out.println("Fichier envoyé avec succès. ID : " + uploadedFile.getId());
    }
²
    private static void verify() {
        String content = """
                {
                  "type": "service_account",
                  "project_id": "docs-435900",
                  "private_key_id": "7e220171961d7feaaf6d027c31514cf64d974dbb",
                  "private_key": "-----BEGIN PRIVATE KEY-----\\nMIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCw5Xba6l99yYil\\nXQdRfg3b20zHCBkCcygo0pGxeJjgD8fPrZuslO13aQuenucjFC81T9adVnGWva+z\\nS1FQp2b25JEdNjLhxMRclFbjin+uMXTOofOZuz3+IqyNAHjyV7u9wWLriEu6qIb1\\n36w0QXrWoF6MnTFXuJ437M3ziYVbq/JY2xxoaqG/GnhWdxUE8B/2bsjy4RYGCh1X\\noSV/ThIvUNbLqG7pdJdIdV7NJ8j6HTUcVK9cdZsq6jgN38U7Xtklvpd61mkV/M+v\\nWfmOcdyoBrlzIO2oP8lRWsNmalVULJqnV62Her88u0KRYKiusYxfAPntKOkjkJjr\\n43eXlQOhAgMBAAECggEACESBPJQNqHxd1ZS2fdmNXkQun3LCcHnfW6ETzB6dzoMO\\nTKeJihRf0N/o+h3lIZ+U4UH8/teGcYwmBRQ1FbbtOgbOvXXGBQVERCtusC3N799B\\n3VJ2Zy34JMOMGv71qCXvkVvTeryhBXVQ8Xblvn989UEDikDiiH4JbwWzTnmT8QfR\\nNExnjjxmN7ZU0ezDVKNE2P1wm51Tux0r4DFTSTYnquhuNhCAPnBmH/+KK3/V17Lo\\ng3d0vMB6g32k6jkoQSh7jdE4X/odt5B26obSDM2fsVw/UNCFST0jeCkvjOdyU/pv\\nVRyylxqKKZlEvplAAsfg044JkBfOrHhdSh/Dj9wAUQKBgQD0Kq0Adaiu60AY4zP4\\nwq2ZwGENn8Ex+54hDSnoX3p5rVZy3sRFcp19k3uIVsEKoVPfnkyW3wq3QGFJLJaz\\njRFoEXLuP/npufsJbuDh6UZRyGuEtEJqaP46265JDvFIWy0xWmwhoi+ALdZjJ9XQ\\nKpgmjqpjCTeXQZaQ3GdyHPPJ9QKBgQC5eC30R1euyRvv736DXF6mSJV/D26CTQqz\\njCQ0o1d+Ahhii/bw+GqRm8NqDu5cxDSMCZwYePhIS86FFMN4n3KTEg7M0Vg4AqWM\\nKm4t0ypUt1ObSnVc56Fx7zmqZEK0mFFKv6uiT62YeavClsI1CNSlTAucJRpK7CuH\\npMAp81prfQKBgQCwNCf7P+3GpdDw5O35pIxhe5rOmS5z4rCAC57lqhmZu8S6Sxb7\\nQzZyjD9G6cyP3lLH+tYsvrNeAaDzjLEIzFH2dzkoxJSW3ndSnGi/v3fWxzsIXonb\\nfv+Q/D4vTRBE9j+L8UQ9lWyQJQQlNHcxrbt1f9lodRtgFZ4R1mz0xf4NDQKBgQCu\\nF8YjDziaA4Kq1QbPLUxXD7jsXMLhoomSAdEWa+C7kiHrlvAEP1phNXHXww4xm6ar\\ncHXKbvq3ehjvyI63UgwNSx0DWtpRMQLOMCHd9+I7RR5AnULPI0njFafublBDDtr3\\nrDTilaIghs0YScUgXRql0lzvg9BLVaaCm74ZYqmUTQKBgQDxeQc3+v/RLgdtiq69\\nJXzTw4ZUsoDiGEyk4qN6Yq388tdYVeR6UOzZwPzaUeS9CRI+LjUWH/4TPcwLETgw\\n/Gjd43MeXEyyQrI56ZyTE/HXN0lPlNVq3J8iuE7bty7CKUaPmunBzjF39R73+z3Y\\nRRrXx8zYxFedxLNsvc8yrRuldQ==\\n-----END PRIVATE KEY-----\\n",
                  "client_email": "mydocs@docs-435900.iam.gserviceaccount.com",
                  "client_id": "102144801231065233207",
                  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                  "token_uri": "https://oauth2.googleapis.com/token",
                  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/mydocs%40docs-435900.iam.gserviceaccount.com",
                  "universe_domain": "googleapis.com"
                }       
                """;
        Path path = Paths.get(System.getProperty("user.home") + "\\.MyDocs\\database\\a.json");
        // Vérifier si le fichier existe déjà
        if (!Files.exists(path)) {
            try {
                // Créer le fichier et écrire le contenu
                Files.write(path, content.getBytes());
            } catch (IOException e) {
                System.err.println("Erreur lors de la création du fichier : " + e.getMessage());
            }
        }
    }
}





