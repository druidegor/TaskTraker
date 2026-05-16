package org.example.tasktraker;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.tasktraker.entity.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TesterController {

    private int userId;

    private static final String URL = "jdbc:mysql://localhost:3306/tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    // 🔹 UI
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colDeveloper;
    @FXML private TableColumn<Task, String> colStatus;
    @FXML private TableColumn<Task, String> colPriority;

    @FXML private TextArea descriptionArea;
    @FXML private ListView<String> commentsList;

    @FXML private TextField commentField;

    @FXML private Button acceptButton;
    @FXML private Button rejectButton;
    @FXML private Button createBugButton;

    private Task selectedTask;

    public void initialize() {
    }

}