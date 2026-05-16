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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;

import java.util.List;

public class TesterController {

    private int userId;
    private Task selectedTask;

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
    @FXML private Button sendCommentButton;

    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDeveloper.setCellValueFactory(new PropertyValueFactory<>("project"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        loadTasks();

        // Обновленный слушатель выбора задачи
        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTask = newSelection;
                descriptionArea.setText(newSelection.getDescription());
                loadComments(newSelection.getId()); // Загружаем комментарии
            } else {
                selectedTask = null;
                descriptionArea.clear();
                commentsList.getItems().clear();
            }
        });

        createBugButton.setOnAction(e -> handleCreateBug());
        // Слушатель для кнопки отправки комментария
        sendCommentButton.setOnAction(e -> handleSendComment());
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

    private void loadComments(int taskId) {
        commentsList.getItems().clear();

        Request request = new Request("GET_COMMENTS_BY_TASK", taskId);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            // Так как мы добавили toString() в Comment, можно просто вывести их как строки
            List<org.example.tasktraker.entity.Comment> comments =
                    (List<org.example.tasktraker.entity.Comment>) response.getData();

            for (org.example.tasktraker.entity.Comment c : comments) {
                commentsList.getItems().add(c.toString());
            }
        } else {
            System.err.println("Ошибка загрузки комментариев: " +
                    (response != null ? response.getMessage() : "Нет ответа"));
        }
    }

    private void handleSendComment() {
        if (selectedTask == null) {
            System.out.println("Выберите задачу сначала!");
            return;
        }

        String text = commentField.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        // Формируем пакет данных: [taskId, authorId, text]
        // ВАЖНО: убедись, что при логине в TesterController передается userId!
        // Пока используем userId (убедись, что он у тебя устанавливается, как в AdminController)
        Object[] payload = {selectedTask.getId(), this.userId, text};
        Request request = new Request("ADD_COMMENT", payload);
        Response response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            commentField.clear(); // Очищаем поле ввода
            loadComments(selectedTask.getId()); // Сразу перезагружаем список комментариев
        } else {
            System.err.println("Ошибка отправки комментария: " +
                    (response != null ? response.getMessage() : "Нет ответа"));
        }
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private void handleCreateBug() {
        // Создаем кастомное диалоговое окно
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создать баг");
        dialog.setHeaderText("Пожалуйста, опишите найденный баг");

        // Кнопки "Создать" и "Отмена"
        ButtonType createButtonType = new ButtonType("Создать", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Сетка для расположения элементов (GridPane)
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20px;");

        // Поля ввода
        TextField titleField = new TextField();
        titleField.setPromptText("Название бага (кратко)");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Шаги воспроизведения, ожидаемый и фактический результат...");
        descArea.setPrefRowCount(4);

        TextField projectIdField = new TextField();
        projectIdField.setPromptText("ID проекта (число)");

        // Добавляем элементы в сетку
        grid.add(new Label("Название:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("ID проекта:"), 0, 2);
        grid.add(projectIdField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Ждем, пока пользователь нажмет кнопку
        dialog.showAndWait().ifPresent(response -> {
            if (response == createButtonType) {
                try {
                    String title = titleField.getText().trim();
                    String desc = descArea.getText().trim();
                    int projectId = Integer.parseInt(projectIdField.getText().trim());

                    if (title.isEmpty()) {
                        System.err.println("Название не может быть пустым!");
                        return;
                    }

                    // Отправляем запрос на сервер: [title, description, projectId, authorId]
                    Object[] payload = {title, desc, projectId, this.userId};
                    Request request = new Request("CREATE_BUG", payload);
                    Response netResponse = NetworkClient.getInstance().sendRequest(request);

                    if (netResponse != null && netResponse.isSuccess()) {
                        System.out.println("Баг успешно создан!");
                        loadTasks(); // Сразу обновляем таблицу задач, чтобы увидеть баг
                    } else {
                        System.err.println("Ошибка сервера: " + (netResponse != null ? netResponse.getMessage() : "Нет ответа"));
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Ошибка: ID проекта должен быть числом!");
                    // Опционально: можно добавить показ Alert'а с ошибкой для пользователя
                    Alert alert = new Alert(Alert.AlertType.ERROR, "ID проекта должен быть числом!");
                    alert.show();
                }
            }
        });
    }
}