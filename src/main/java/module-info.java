module com.campasian {
    requires java.net.http;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.google.gson;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires kotlin.stdlib;
    requires java.prefs; // এই লাইনটি যোগ করুন
    requires java.sql;   // যদি ডাটাবেস ব্যবহার করেন তবে এটিও লাগতে পারে

    opens com.campasian.controller to javafx.fxml;
    opens com.campasian to javafx.fxml;
    exports com.campasian;
}
