module application.assign {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;

    opens application.Controllers to javafx.fxml;
    opens application.Models to javafx.base;
    exports application;
}