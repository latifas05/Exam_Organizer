module org.example.organizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.organizer to javafx.fxml;
    exports org.example.organizer;
    exports org.example.organizer.controller;
    opens org.example.organizer.controller to javafx.fxml;

    exports org.example.organizer.model;
}
