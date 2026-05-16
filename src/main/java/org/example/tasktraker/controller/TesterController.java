package org.example.tasktraker.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.tasktraker.entity.Task;
import org.example.tasktraker.network.NetworkClient;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;

import java.util.List;

public class TesterController {

    private int userId;

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

    public void initialize() {
        // 1. Привязываем колонки таблицы к полям класса Task
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDeveloper.setCellValueFactory(new PropertyValueFactory<>("project")); // Пока выводим проект
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        // 2. Загружаем данные с сервера
        loadTasks();

        // 3. Добавляем слушатель: при клике на задачу в таблице выводим её описание вниз
        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                descriptionArea.setText(newSelection.getDescription());
            } else {
                descriptionArea.clear();
            }
        });
    }

    private void loadTasks() {
        // Создаем запрос на сервер
        Request request = new Request("GET_ALL_TASKS", null);
        Response response = NetworkClient.getInstance().sendRequest(request);

        // Если сервер ответил успешно
        if (response != null && response.isSuccess()) {
            List<Task> tasks = (List<Task>) response.getData();

            // Заполняем JavaFX таблицу данными
            ObservableList<Task> taskList = FXCollections.observableArrayList(tasks);
            tasksTable.setItems(taskList);
        } else {
            System.err.println("Ошибка загрузки задач: " + (response != null ? response.getMessage() : "Нет ответа"));
        }
    }
}