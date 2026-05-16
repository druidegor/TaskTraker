package org.example.tasktraker;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.tasktraker.entity.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeveloperController {


    @FXML private ListView<String> projectList;
    @FXML private TextField projectNameField;
    @FXML private TextField projectDescriptionField;
    @FXML
    private void handleCreateProject() {

    }


    public void initialize() {
    }
}