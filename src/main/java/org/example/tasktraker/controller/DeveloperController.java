package org.example.tasktraker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.Task;
import org.example.tasktraker.network.NetworkClient;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;

import java.util.List;

public class DeveloperController {

    private int userId;
    private List<Project> userProjects;

    @FXML private ListView<String> projectList;
    @FXML private TextField projectNameField;
    @FXML private TextField projectDescriptionField;

    // 🔹 Новые поля для таблицы задач
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colStatus;
    @FXML private TableColumn<Task, String> colPriority;

    public void setUserId(int userId) {
        this.userId = userId;
        // Загружаем проекты и задачи, как только контроллер получает ID пользователя
        loadDeveloperProjects();
        loadDeveloperTasks();
    }

    public void initialize() {
        // Настраиваем привязку полей сущности Task к колонкам таблицы JavaFX
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        // 🔹 СОЗДАЕМ КОНТЕНТСТНОЕ МЕНЮ ДЛЯ ТАБЛИЦЫ ЗАДАЧ
        ContextMenu contextMenu = new ContextMenu();
        MenuItem inProgressItem = new MenuItem("Start Work (In Progress)");
        MenuItem doneItem = new MenuItem("Complete (Ready for Testing)");

        contextMenu.getItems().addAll(inProgressItem, doneItem);

        // Привязываем контекстное меню к строкам таблицы
        tasksTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });

        // Назначаем действия на элементы меню (например: 2 - В работе, 5 - Готово к тестированию)
        inProgressItem.setOnAction(event -> {
            Task selected = tasksTable.getSelectionModel().getSelectedItem();
            if (selected != null) handleStatusChange(selected.getId(), 2);
        });

        doneItem.setOnAction(event -> {
            Task selected = tasksTable.getSelectionModel().getSelectedItem();
            if (selected != null) handleStatusChange(selected.getId(), 5);
        });
    }

    // 🔹 МЕТОД ОТПРАВКИ ИЗМЕНЕНИЯ СТАТУСА НА СЕРВЕР
    private void handleStatusChange(int taskId, int newStatusId) {
        int[] payload = {taskId, newStatusId};
        Request request = new Request("CHANGE_TASK_STATUS", payload);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            System.out.println("Статус задачи успешно обновлен разработчиком!");
            loadDeveloperTasks(); // Перезагружаем таблицу, чтобы увидеть изменения
        } else {
            System.err.println("Ошибка изменения статуса: " +
                    (response != null ? response.getMessage() : "Нет ответа"));
        }
    }

    private void loadDeveloperProjects() {
        projectList.getItems().clear();

        Request request = new Request("GET_USER_PROJECTS", this.userId);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            userProjects = (List<Project>) response.getData();
            for (Project p : userProjects) {
                projectList.getItems().add(p.getName());
            }
        } else {
            System.err.println("Ошибка загрузки проектов разработчика: " +
                    (response != null ? response.getMessage() : "Нет ответа"));
        }
    }

    // 🔹 Новый метод загрузки задач разработчика с сервера
    private void loadDeveloperTasks() {
        Request request = new Request("GET_TASKS_BY_ASSIGNEE", this.userId);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            List<Task> tasks = (List<Task>) response.getData();

            // Переносим полученный список в ObservableList и устанавливаем в таблицу
            ObservableList<Task> taskList = FXCollections.observableArrayList(tasks);
            tasksTable.setItems(taskList);
        } else {
            System.err.println("Ошибка загрузки задач разработчика: " +
                    (response != null ? response.getMessage() : "Нет ответа"));
        }
    }

    @FXML
    private void handleCreateProject() {
        // Оставляем пока пустой
    }
}