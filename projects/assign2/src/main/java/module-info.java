module application.assign {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;

    opens application.Controllers to javafx.fxml;
    exports application;
}