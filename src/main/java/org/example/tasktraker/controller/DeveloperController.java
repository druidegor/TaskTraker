package org.example.tasktraker.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.tasktraker.entity.Comment;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.Task;
import org.example.tasktraker.network.NetworkClient;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeveloperController {

    private int userId;
    private List<Project> userProjects = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private Task selectedTask;

    @FXML private Label developerInfoLabel;
    @FXML private ListView<Project> projectList;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colProject;
    @FXML private TableColumn<Task, String> colStatus;
    @FXML private TableColumn<Task, String> colPriority;
    @FXML private Label selectedTaskLabel;
    @FXML private TextArea taskDescriptionArea;
    @FXML private ListView<String> commentsList;
    @FXML private TextField commentField;

    public void initialize() {
        configureProjects();
        configureTasks();
    }

    public void setUserId(int userId) {
        this.userId = userId;
        developerInfoLabel.setText("Developer ID: " + userId + " (Online)");
        refreshData();
    }

    private void configureProjects() {
        projectList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                setText(empty || project == null ? null : project.getName());
            }
        });

        projectList.getSelectionModel().selectedItemProperty().addListener((obs, oldProject, newProject) -> applyProjectFilter());
    }

    private void configureTasks() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colProject.setCellValueFactory(new PropertyValueFactory<>("project"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldTask, newTask) -> showTaskDetails(newTask));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem startWorkItem = new MenuItem("Start Work");
        MenuItem readyForTestingItem = new MenuItem("Ready for Testing");
        MenuItem createBugItem = new MenuItem("Create Bug");
        contextMenu.getItems().addAll(startWorkItem, readyForTestingItem, createBugItem);

        startWorkItem.setOnAction(event -> changeSelectedTaskStatus(2));
        readyForTestingItem.setOnAction(event -> changeSelectedTaskStatus(5));
        createBugItem.setOnAction(event -> handleCreateBug());

        tasksTable.setRowFactory(table -> {
            TableRow<Task> row = new TableRow<>();
            row.setContextMenu(contextMenu);
            return row;
        });
    }

    @FXML
    private void handleRefresh() {
        refreshData();
    }

    @FXML
    private void handleShowAllTasks() {
        projectList.getSelectionModel().clearSelection();
        applyProjectFilter();
    }

    @FXML
    private void handleStartWork() {
        changeSelectedTaskStatus(2);
    }

    @FXML
    private void handleReadyForTesting() {
        changeSelectedTaskStatus(5);
    }

    @FXML
    private void handleSendComment() {
        if (selectedTask == null) {
            showError("Select a task first");
            return;
        }

        String text = commentField.getText().trim();
        if (text.isEmpty()) {
            showError("Comment is empty");
            return;
        }

        Object[] payload = {selectedTask.getId(), userId, text};
        Response response = NetworkClient.getInstance().sendRequest(new Request("ADD_COMMENT", payload));

        if (response != null && response.isSuccess()) {
            commentField.clear();
            loadComments(selectedTask.getId());
        } else {
            showError(response != null ? response.getMessage() : "Server is not responding");
        }
    }

    @FXML
    private void handleCreateBug() {
        int projectId = resolveCurrentProjectId();
        if (projectId <= 0) {
            showError("Select a project or task first");
            return;
        }

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Create Bug");
        titleDialog.setHeaderText("Bug title");
        titleDialog.setContentText("Title:");
        Optional<String> titleResult = titleDialog.showAndWait();

        if (titleResult.isEmpty() || titleResult.get().trim().isEmpty()) {
            return;
        }

        TextInputDialog descriptionDialog = new TextInputDialog();
        descriptionDialog.setTitle("Create Bug");
        descriptionDialog.setHeaderText("Bug description");
        descriptionDialog.setContentText("Description:");
        Optional<String> descriptionResult = descriptionDialog.showAndWait();

        Object[] payload = {
                titleResult.get().trim(),
                descriptionResult.orElse("").trim(),
                projectId,
                userId,
                selectedTask != null ? selectedTask.getAssigneeId() : userId
        };

        Response response = NetworkClient.getInstance().sendRequest(new Request("CREATE_BUG", payload));

        if (response != null && response.isSuccess()) {
            showInfo("Bug created");
            refreshData();
        } else {
            showError(response != null ? response.getMessage() : "Server is not responding");
        }
    }

    private void refreshData() {
        loadDeveloperProjects();
        loadDeveloperTasks();
    }

    private void loadDeveloperProjects() {
        projectList.getItems().clear();

        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_USER_PROJECTS", userId));

        if (response != null && response.isSuccess()) {
            userProjects = (List<Project>) response.getData();
            projectList.getItems().addAll(userProjects);
        } else {
            showError(response != null ? response.getMessage() : "Cannot load projects");
        }
    }

    private void loadDeveloperTasks() {
        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_TASKS_BY_ASSIGNEE", userId));

        if (response != null && response.isSuccess()) {
            allTasks = (List<Task>) response.getData();
            applyProjectFilter();
        } else {
            showError(response != null ? response.getMessage() : "Cannot load tasks");
        }
    }

    private void applyProjectFilter() {
        Project selectedProject = projectList.getSelectionModel().getSelectedItem();

        if (selectedProject == null) {
            tasksTable.setItems(FXCollections.observableArrayList(allTasks));
            return;
        }

        List<Task> filtered = allTasks.stream()
                .filter(task -> task.getProjectId() == selectedProject.getId())
                .toList();
        tasksTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showTaskDetails(Task task) {
        selectedTask = task;
        commentsList.getItems().clear();

        if (task == null) {
            selectedTaskLabel.setText("No task selected");
            taskDescriptionArea.clear();
            return;
        }

        selectedTaskLabel.setText(task.getTitle() + " | " + task.getStatus() + " | " + task.getProject());
        taskDescriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");
        loadComments(task.getId());
    }

    private void loadComments(int taskId) {
        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_COMMENTS_BY_TASK", taskId));

        if (response != null && response.isSuccess()) {
            List<Comment> comments = (List<Comment>) response.getData();
            commentsList.getItems().setAll(comments.stream().map(Comment::toString).toList());
        } else {
            showError(response != null ? response.getMessage() : "Cannot load comments");
        }
    }

    private void changeSelectedTaskStatus(int statusId) {
        if (selectedTask == null) {
            showError("Select a task first");
            return;
        }

        int[] payload = {selectedTask.getId(), statusId};
        Response response = NetworkClient.getInstance().sendRequest(new Request("CHANGE_TASK_STATUS", payload));

        if (response != null && response.isSuccess()) {
            loadDeveloperTasks();
        } else {
            showError(response != null ? response.getMessage() : "Cannot change task status");
        }
    }

    private int resolveCurrentProjectId() {
        if (selectedTask != null && selectedTask.getProjectId() > 0) {
            return selectedTask.getProjectId();
        }

        Project selectedProject = projectList.getSelectionModel().getSelectedItem();
        return selectedProject != null ? selectedProject.getId() : 0;
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
            Stage stage = (Stage) developerInfoLabel.getScene().getWindow();
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
