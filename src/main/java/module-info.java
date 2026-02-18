module com.abir.demo {
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

    opens com.abir.demo.controllers to javafx.fxml;  // 👈 plural, matches your folder
    opens com.abir.demo to javafx.fxml;
    exports com.abir.demo;
}
