package org.example.tasktraker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.User;
import org.example.tasktraker.service.ProjectService;
import org.example.tasktraker.service.UserService;

import java.util.List;

public class AdminController {

    @FXML private Label adminInfoLabel;
    @FXML private ListView<String> projectList;
    @FXML private TextField projectNameField;
    @FXML private TextField projectDescriptionField;
    @FXML private ComboBox<User> userComboBox;

    private final ProjectService projectService = new ProjectService();
    private final UserService userService = new UserService();

    private List<Project> projects;

    private int adminId;

    public void setUserId(int userId) {
        this.adminId = userId;
        showAdminInfo();
    }


    public void initialize() {
        loadProjects();
        loadUsers();
    }

    private void showAdminInfo() {
        User admin = userService.getUserById(adminId);

        if (admin != null) {
            adminInfoLabel.setText(
                    "Admin: " + admin.getName() + " (" + admin.getRole() + ")"
            );
        }
    }

    private void loadProjects() {
        projectList.getItems().clear();
        projects = projectService.getAllProjects();

        for (Project p : projects) {
            projectList.getItems().add(p.getName());
        }
    }

    private void loadUsers() {
        List<User> users = userService.getAllUsers();
        userComboBox.getItems().addAll(users);
    }

    @FXML
    private void handleCreateProject() {
        String name = projectNameField.getText();
        String description = projectDescriptionField.getText();

        try {
            projectService.createProject(name, description);
            loadProjects();

            projectNameField.clear();
            projectDescriptionField.clear();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleAssignUser() {
        int selectedIndex = projectList.getSelectionModel().getSelectedIndex();
        User user = userComboBox.getValue();

        if (selectedIndex == -1 || user == null) {
            showError("Select project and user");
            return;
        }

        int projectId = projects.get(selectedIndex).getId();

        projectService.assignUserToProject(user.getId(), projectId);
    }


    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}