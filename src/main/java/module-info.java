module com.campasian {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.google.gson;
    requires org.postgresql.jdbc;
    requires flyway.core;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires kotlin.stdlib;

    opens com.campasian.controller to javafx.fxml;
    opens com.campasian to javafx.fxml;
    exports com.campasian;
}
