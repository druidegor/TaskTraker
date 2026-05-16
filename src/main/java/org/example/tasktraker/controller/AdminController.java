package org.example.tasktraker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.User;
import org.example.tasktraker.network.NetworkClient;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;

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

    private List<Project> projects;
    private List<User> users;
    private int adminId;

    public void setUserId(int userId) {
        this.adminId = userId;
        adminInfoLabel.setText("Admin ID: " + adminId + " (Online)");
    }

    public void initialize() {
        configureControls();
        loadProjects();
        loadUsers();
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

        projectList.getSelectionModel().selectedItemProperty().addListener((obs, oldProject, newProject) -> {
            if (newProject != null) {
                loadProjectUsers(newProject.getId());
            } else {
                projectUsersList.getItems().clear();
            }
        });
    }

    private void loadProjects() {
        projectList.getItems().clear();

        // Отправляем запрос на получение всех проектов
        Request request = new Request("GET_ALL_PROJECTS", null);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            // Распаковываем ответ
            projects = (List<Project>) response.getData();
            projectList.getItems().addAll(projects);
        } else {
            showError("Ошибка загрузки проектов: " + (response != null ? response.getMessage() : "Нет ответа"));
        }
    }

    private void loadUsers() {
        userComboBox.getItems().clear();

        // Отправляем запрос на получение всех пользователей
        Request request = new Request("GET_ALL_USERS", null);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            users = (List<User>) response.getData();
            userComboBox.getItems().addAll(users);
            assigneeComboBox.getItems().addAll(users);
        } else {
            showError("Ошибка загрузки пользователей: " + (response != null ? response.getMessage() : "Нет ответа"));
        }
    }

    private void loadProjectUsers(int projectId) {
        projectUsersList.getItems().clear();

        Request request = new Request("GET_PROJECT_USERS", projectId);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            List<User> projectUsers = (List<User>) response.getData();
            projectUsersList.getItems().addAll(projectUsers);
        } else {
            showError("Ошибка загрузки участников проекта: " + (response != null ? response.getMessage() : "Нет ответа"));
        }
    }

    @FXML
    private void handleCreateProject() {
        String name = projectNameField.getText().trim();
        String description = projectDescriptionField.getText().trim();

        if (name.isEmpty()) {
            showError("Введите название проекта");
            return;
        }

        // Упаковываем данные проекта в массив
        String[] payload = {name, description};
        Request request = new Request("CREATE_PROJECT", payload);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            // Если успешно, перезагружаем список проектов с сервера
            loadProjects();
            projectNameField.clear();
            projectDescriptionField.clear();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Проект успешно создан!");
            alert.show();
        } else {
            showError(response != null ? response.getMessage() : "Ошибка сервера");
        }
    }

    @FXML
    private void handleAssignUser() {
        Project selectedProject = projectList.getSelectionModel().getSelectedItem();
        User user = userComboBox.getValue();

        if (selectedProject == null || user == null) {
            showError("Выберите проект и пользователя");
            return;
        }

        // Упаковываем ID пользователя и ID проекта в массив чисел
        int[] payload = {user.getId(), selectedProject.getId()};
        Request request = new Request("ASSIGN_USER", payload);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            loadProjectUsers(selectedProject.getId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Пользователь успешно назначен!");
            alert.show();
        } else {
            showError(response != null ? response.getMessage() : "Ошибка сервера");
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
            showError("Выберите проект");
            return;
        }

        if (assignee == null) {
            showError("Выберите исполнителя");
            return;
        }

        if (title.isEmpty()) {
            showError("Введите название задачи");
            return;
        }

        Object[] payload = {title, description, selectedProject.getId(), assignee.getId(), adminId, priorityId};
        Request request = new Request("CREATE_TASK", payload);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            taskTitleField.clear();
            taskDescriptionField.clear();
            priorityComboBox.getSelectionModel().select("Normal");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Задача успешно создана!");
            alert.show();
        } else {
            showError(response != null ? response.getMessage() : "Ошибка сервера");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}
