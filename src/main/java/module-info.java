module org.example.tasktraker {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.controlsfx.controls;

    opens org.example.tasktraker to javafx.fxml;
    exports org.example.tasktraker;
    exports org.example.tasktraker.entity;
    opens org.example.tasktraker.entity to javafx.fxml;
    exports org.example.tasktraker.controller;
    opens org.example.tasktraker.controller to javafx.fxml;
}