package org.example.tasktraker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.User;
import org.example.tasktraker.network.NetworkClient;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;

import java.util.List;

public class AdminController {

    @FXML private Label adminInfoLabel;
    @FXML private ListView<String> projectList;
    @FXML private TextField projectNameField;
    @FXML private TextField projectDescriptionField;
    @FXML private ComboBox<User> userComboBox;

    private List<Project> projects;
    private int adminId;

    public void setUserId(int userId) {
        this.adminId = userId;
        adminInfoLabel.setText("Admin ID: " + adminId + " (Online)");
    }

    public void initialize() {
        loadProjects();
        loadUsers();
    }

    private void loadProjects() {
        projectList.getItems().clear();

        // Отправляем запрос на получение всех проектов
        Request request = new Request("GET_ALL_PROJECTS", null);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            // Распаковываем ответ
            projects = (List<Project>) response.getData();
            for (Project p : projects) {
                projectList.getItems().add(p.getName());
            }
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
            List<User> users = (List<User>) response.getData();
            userComboBox.getItems().addAll(users);
        } else {
            showError("Ошибка загрузки пользователей: " + (response != null ? response.getMessage() : "Нет ответа"));
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
        int selectedIndex = projectList.getSelectionModel().getSelectedIndex();
        User user = userComboBox.getValue();

        if (selectedIndex == -1 || user == null) {
            showError("Выберите проект и пользователя");
            return;
        }

        int projectId = projects.get(selectedIndex).getId();

        // Упаковываем ID пользователя и ID проекта в массив чисел
        int[] payload = {user.getId(), projectId};
        Request request = new Request("ASSIGN_USER", payload);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Пользователь успешно назначен!");
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