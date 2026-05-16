package org.example.tasktraker.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.Task;
import org.example.tasktraker.entity.User;
import org.example.tasktraker.network.NetworkClient;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;

import java.util.ArrayList;
import java.util.List;

public class AdminController {

    @FXML private Label adminInfoLabel;
    @FXML private ListView<Project> projectList;
    @FXML private ListView<User> projectUsersList;
    @FXML private TextField projectNameField;
    @FXML private TextArea projectDescriptionField;
    @FXML private ComboBox<User> userComboBox;
    @FXML private ComboBox<User> assigneeComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private TextField taskTitleField;
    @FXML private TextArea taskDescriptionField;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> colTaskTitle;
    @FXML private TableColumn<Task, String> colTaskStatus;
    @FXML private TableColumn<Task, String> colTaskPriority;
    @FXML private TableColumn<Task, String> colTaskAssignee;

    private int adminId;
    private List<Project> projects = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();

    public void initialize() {
        configureControls();
        loadProjects();
        loadUsers();
        loadTasks();
    }

    public void setUserId(int userId) {
        this.adminId = userId;
        adminInfoLabel.setText("Admin ID: " + adminId + " (Online)");
    }

    private void configureControls() {
        projectList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                setText(empty || project == null ? null : project.getName());
            }
        });

        projectUsersList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.toString());
            }
        });

        StringConverter<User> userConverter = new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.toString();
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        };
        userComboBox.setConverter(userConverter);
        assigneeComboBox.setConverter(userConverter);

        priorityComboBox.getItems().addAll("Low", "Normal", "High", "Critical");
        priorityComboBox.getSelectionModel().select("Normal");

        colTaskTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTaskStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTaskPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colTaskAssignee.setCellValueFactory(new PropertyValueFactory<>("assigneeName"));

        projectList.getSelectionModel().selectedItemProperty().addListener((obs, oldProject, newProject) -> {
            if (newProject != null) {
                loadProjectUsers(newProject.getId());
            } else {
                projectUsersList.getItems().clear();
            }
            applyTaskFilter();
        });
    }

    private void loadProjects() {
        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_ALL_PROJECTS", null));

        if (response != null && response.isSuccess()) {
            projects = (List<Project>) response.getData();
            projectList.getItems().setAll(projects);
        } else {
            showError(response != null ? response.getMessage() : "Cannot load projects");
        }
    }

    private void loadUsers() {
        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_ALL_USERS", null));

        if (response != null && response.isSuccess()) {
            users = (List<User>) response.getData();
            userComboBox.getItems().setAll(users);
            assigneeComboBox.getItems().setAll(
                    users.stream()
                            .filter(user -> "DEVELOPER".equals(user.getRole()))
                            .toList()
            );
        } else {
            showError(response != null ? response.getMessage() : "Cannot load users");
        }
    }

    private void loadTasks() {
        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_ALL_TASKS", null));

        if (response != null && response.isSuccess()) {
            tasks = (List<Task>) response.getData();
            applyTaskFilter();
        } else {
            showError(response != null ? response.getMessage() : "Cannot load tasks");
        }
    }

    private void loadProjectUsers(int projectId) {
        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_PROJECT_USERS", projectId));

        if (response != null && response.isSuccess()) {
            List<User> projectUsers = (List<User>) response.getData();
            projectUsersList.getItems().setAll(projectUsers);
        } else {
            showError(response != null ? response.getMessage() : "Cannot load project members");
        }
    }

    private void applyTaskFilter() {
        Project selectedProject = projectList.getSelectionModel().getSelectedItem();

        if (selectedProject == null) {
            tasksTable.setItems(FXCollections.observableArrayList(tasks));
            return;
        }

        tasksTable.setItems(FXCollections.observableArrayList(
                tasks.stream()
                        .filter(task -> task.getProjectId() == selectedProject.getId())
                        .toList()
        ));
    }

    @FXML
    private void handleCreateProject() {
        String name = projectNameField.getText().trim();
        String description = projectDescriptionField.getText().trim();

        if (name.isEmpty()) {
            showError("Project name is empty");
            return;
        }

        String[] payload = {name, description};
        Response response = NetworkClient.getInstance().sendRequest(new Request("CREATE_PROJECT", payload));

        if (response != null && response.isSuccess()) {
            projectNameField.clear();
            projectDescriptionField.clear();
            loadProjects();
            showInfo("Project created");
        } else {
            showError(response != null ? response.getMessage() : "Server error");
        }
    }

    @FXML
    private void handleAssignUser() {
        Project selectedProject = projectList.getSelectionModel().getSelectedItem();
        User user = userComboBox.getValue();

        if (selectedProject == null || user == null) {
            showError("Select project and user");
            return;
        }

        int[] payload = {user.getId(), selectedProject.getId()};
        Response response = NetworkClient.getInstance().sendRequest(new Request("ASSIGN_USER", payload));

        if (response != null && response.isSuccess()) {
            loadProjectUsers(selectedProject.getId());
            showInfo("User assigned to project");
        } else {
            showError(response != null ? response.getMessage() : "Server error");
        }
    }

    @FXML
    private void handleCreateTask() {
        Project selectedProject = projectList.getSelectionModel().getSelectedItem();
        User assignee = assigneeComboBox.getValue();
        String title = taskTitleField.getText().trim();
        String description = taskDescriptionField.getText().trim();
        int priorityId = priorityComboBox.getSelectionModel().getSelectedIndex() + 1;

        if (selectedProject == null) {
            showError("Select project");
            return;
        }

        if (assignee == null) {
            showError("Select developer");
            return;
        }

        if (!"DEVELOPER".equals(assignee.getRole())) {
            showError("Task assignee must be a developer");
            return;
        }

        if (title.isEmpty()) {
            showError("Task title is empty");
            return;
        }

        Object[] payload = {title, description, selectedProject.getId(), assignee.getId(), adminId, priorityId};
        Response response = NetworkClient.getInstance().sendRequest(new Request("CREATE_TASK", payload));

        if (response != null && response.isSuccess()) {
            taskTitleField.clear();
            taskDescriptionField.clear();
            priorityComboBox.getSelectionModel().select("Normal");
            loadTasks();
            showInfo("Task created");
        } else {
            showError(response != null ? response.getMessage() : "Server error");
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tasktraker/login_screen.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) adminInfoLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Cannot open login screen");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}
