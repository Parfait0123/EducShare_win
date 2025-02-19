package docs.la_creme_des_cremes_docs.Classes;

import com.google.api.services.drive.model.About;
import docs.la_creme_des_cremes_docs.cloud.GoogleDriveService;

public class Essaie2 {
    public static void main(String[] args) {
        try {
            System.out.println("Avant 1");
//            GoogleDriveService.uploadFileToSpecificFolder("Docsgriwrzks.sqlite", "application/sqlite", null);
//            GoogleDriveService.uploadFileToSpecificFolder("Downloads.sqlite", "application/sqlite", null);
//            GoogleDriveService.uploadFileToSpecificFolder("Login.sqlite", "application/sqlite", null);


            GoogleDriveService.listAllFilesWithDetails();
//            About a = GoogleDriveService.getDriveService().about().get().setFields("storageQuota").execute();
//            System.out.println(a.getStorageQuota().getUsage()+" / "+a.getStorageQuota().getLimit());


            System.out.println("Après  ");
        } catch (Exception e) {
            System.out.println(e);
        }


    }


}


