module com.example.offlinedocumenthubserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires io.javalin;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.services.drive;
    requires com.google.auth.oauth2;

    opens com.example.offlinedocumenthubserver to javafx.fxml;
    exports com.example.offlinedocumenthubserver.dto to com.fasterxml.jackson.databind;
    exports com.example.offlinedocumenthubserver;
}