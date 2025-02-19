package docs.la_creme_des_cremes_docs.Security;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Security {

    // Clé AES standard sous forme d'une chaîne de 16 caractères (128 bits)
    private static final String CLE_STANDARD = "1234567890abcdef"; // Exemple à personnaliser

    // Méthode pour chiffrer la base de données SQLite
    public static void chiffrerBaseDeDonnees(String cheminFichierBD) throws Exception {
        File fichierDB = new File(cheminFichierBD);

        // Vérifier si le fichier est déjà chiffré (vérifier l'extension .encrypted)
        if (fichierDB.getName().endsWith(".encrypted")) {
            System.out.println("La base de données est déjà chiffrée.");
            return;
        }

        // Vérifier que la base de données existe
        if (!fichierDB.exists()) {
            System.out.println("Le fichier de base de données n'existe pas.");
            return;
        }

        // Convertir la clé standard en SecretKey
        SecretKey cle = convertirCle(CLE_STANDARD);

        // Lire le contenu de la base de données SQLite
        byte[] contenuBD = Files.readAllBytes(fichierDB.toPath());

        // Chiffrer le contenu de la base de données
        byte[] fichierChiffre = chiffrerDonnees(contenuBD, cle);

        // Écrire le contenu chiffré dans un fichier avec l'extension .encrypted
        Path cheminChiffre = Paths.get(cheminFichierBD + ".encrypted");
        Files.write(cheminChiffre, fichierChiffre);

        // Supprimer le fichier original non chiffré
        fichierDB.delete();

        // Cacher le fichier chiffré sous Windows
        cacherFichierSousWindows(cheminChiffre.toFile());

        System.out.println("La base de données a été chiffrée et sauvegardée à : " + cheminChiffre);
    }

    // Méthode pour déchiffrer la base de données SQLite
    public static void dechiffrerBaseDeDonnees(String cheminFichierChiffre) throws Exception {
        File fichierChiffre = new File(cheminFichierChiffre);

        // Vérifier si le fichier est déjà déchiffré (vérifier l'absence de l'extension .encrypted)
        if (!fichierChiffre.getName().endsWith(".encrypted")) {
            System.out.println("Le fichier n'est pas chiffré ou a déjà été déchiffré.");
            return;
        }

        // Vérifier que le fichier chiffré existe
        if (!fichierChiffre.exists()) {
            System.out.println("Le fichier chiffré n'existe pas.");
            return;
        }

        // Convertir la clé standard en SecretKey
        SecretKey cle = convertirCle(CLE_STANDARD);

        // Lire le contenu du fichier chiffré
        byte[] contenuChiffre = Files.readAllBytes(fichierChiffre.toPath());

        // Déchiffrer le contenu pour obtenir la base de données SQLite
        byte[] fichierDechiffre = dechiffrerDonnees(contenuChiffre, cle);

        // Écrire le fichier déchiffré sous le nom original (sans .encrypted)
        String cheminOriginal = cheminFichierChiffre.replace(".encrypted", "");
        Path cheminDechiffre = Paths.get(cheminOriginal);
        Files.write(cheminDechiffre, fichierDechiffre);

        // Supprimer le fichier chiffré
        fichierChiffre.delete();

        // Cacher le fichier déchiffré sous Windows
        cacherFichierSousWindows(cheminDechiffre.toFile());

        System.out.println("La base de données a été déchiffrée et restaurée à : " + cheminDechiffre);
    }

    // Convertir une chaîne de caractères en SecretKey
    public static SecretKey convertirCle(String cle) {
        return new SecretKeySpec(cle.getBytes(), "AES");
    }

    // Chiffrer des données avec la clé AES
    public static byte[] chiffrerDonnees(byte[] donnees, SecretKey cle) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, cle);
        return cipher.doFinal(donnees);
    }

    // Déchiffrer des données avec la clé AES
    public static byte[] dechiffrerDonnees(byte[] donneesChiffrees, SecretKey cle) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, cle);
        return cipher.doFinal(donneesChiffrees);
    }

    // Méthode pour cacher un fichier sous Windows
    public static void cacherFichierSousWindows(File fichier) throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            Path path = fichier.toPath();
            Files.setAttribute(path, "dos:hidden", true);
            System.out.println("Fichier caché sous Windows.");
        }
    }

    // Méthode de test pour chiffrer et déchiffrer
    public static void main(String[] args) throws IOException {
        File fichier = new File("C:\\Users\\lenovo\\.MyDocs\\database\\Downloads.sqlite");
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            Path path = fichier.toPath();
            Files.setAttribute(path, "dos:hidden", false);
            System.out.println("Fichier caché sous Windows.");
        }
    }
}
