module docs.la_creme_des_cremes_docs {
    requires javafx.controls;
    requires javafx.fxml;
    requires animatefx;
    requires org.controlsfx.controls;
    requires java.sql;
    requires java.desktop;
    requires google.api.client;
    requires google.api.services.drive.v3.rev197;
    requires com.google.api.client.json.jackson2;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.http.apache.v2;


    opens docs.la_creme_des_cremes_docs.login;
    opens docs.la_creme_des_cremes_docs.Classes;
    opens docs.la_creme_des_cremes_docs.Security;
    opens docs.la_creme_des_cremes_docs.cloud;
    opens docs.la_creme_des_cremes_docs.SqliteConnection;
    opens docs.la_creme_des_cremes_docs.subject;
    opens docs.la_creme_des_cremes_docs.live;
}