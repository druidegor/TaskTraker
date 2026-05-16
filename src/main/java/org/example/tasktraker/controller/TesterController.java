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
import java.util.Set;
import java.util.stream.Collectors;

public class TesterController {

    private int userId;
    private Task selectedTask;
    private List<Project> userProjects = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();

    @FXML private Label testerInfoLabel;
    @FXML private ListView<Project> projectList;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colProject;
    @FXML private TableColumn<Task, String> colStatus;
    @FXML private TableColumn<Task, String> colPriority;
    @FXML private Label selectedTaskLabel;
    @FXML private TextArea descriptionArea;
    @FXML private ListView<String> commentsList;
    @FXML private TextField commentField;

    public void initialize() {
        configureProjects();
        configureFilters();
        configureTasks();
    }

    public void setUserId(int userId) {
        this.userId = userId;
        testerInfoLabel.setText("Tester ID: " + userId + " (Online)");
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

        projectList.getSelectionModel().selectedItemProperty().addListener((obs, oldProject, newProject) -> applyFilters());
    }

    private void configureFilters() {
        statusFilterComboBox.getItems().addAll(
                "All",
                "Ready for Testing",
                "Open",
                "In Progress",
                "Accepted",
                "Rejected"
        );
        statusFilterComboBox.getSelectionModel().select("Ready for Testing");
        statusFilterComboBox.setOnAction(event -> applyFilters());
    }

    private void configureTasks() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colProject.setCellValueFactory(new PropertyValueFactory<>("project"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        tasksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldTask, newTask) -> showTaskDetails(newTask));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem acceptItem = new MenuItem("Accept");
        MenuItem rejectItem = new MenuItem("Reject");
        MenuItem createBugItem = new MenuItem("Create Bug");
        contextMenu.getItems().addAll(acceptItem, rejectItem, createBugItem);

        acceptItem.setOnAction(event -> acceptSelectedTask());
        rejectItem.setOnAction(event -> rejectSelectedTask());
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
    private void handleShowAllProjects() {
        projectList.getSelectionModel().clearSelection();
        applyFilters();
    }

    @FXML
    private void handleAccept() {
        acceptSelectedTask();
    }

    @FXML
    private void handleReject() {
        rejectSelectedTask();
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

        TextInputDialog descriptionDialog = new TextInputDialog(buildBugDescriptionPrefix());
        descriptionDialog.setTitle("Create Bug");
        descriptionDialog.setHeaderText("Bug description");
        descriptionDialog.setContentText("Description:");
        Optional<String> descriptionResult = descriptionDialog.showAndWait();

        Object[] payload = {
                titleResult.get().trim(),
                descriptionResult.orElse("").trim(),
                projectId,
                userId,
                selectedTask != null ? selectedTask.getAssigneeId() : 0
        };

        Response response = NetworkClient.getInstance().sendRequest(new Request("CREATE_BUG", payload));

        if (response != null && response.isSuccess()) {
            showInfo("Bug created");
            refreshData();
        } else {
            showError(response != null ? response.getMessage() : "Server is not responding");
        }
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

        addComment(selectedTask.getId(), text);
        commentField.clear();
        loadComments(selectedTask.getId());
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tasktraker/login_screen.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) testerInfoLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            showError("Cannot open login screen");
        }
    }

    private void refreshData() {
        loadTesterProjects();
        loadTasks();
    }

    private void loadTesterProjects() {
        projectList.getItems().clear();

        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_USER_PROJECTS", userId));

        if (response != null && response.isSuccess()) {
            userProjects = (List<Project>) response.getData();
            projectList.getItems().addAll(userProjects);
        } else {
            showError(response != null ? response.getMessage() : "Cannot load projects");
        }
    }

    private void loadTasks() {
        Response response = NetworkClient.getInstance().sendRequest(new Request("GET_TASKS_BY_TESTER_PROJECTS", userId));

        if (response != null && response.isSuccess()) {
            allTasks = (List<Task>) response.getData();
            applyFilters();
        } else {
            showError(response != null ? response.getMessage() : "Cannot load tasks");
        }
    }

    private void applyFilters() {
        Set<Integer> projectIds = userProjects.stream()
                .map(Project::getId)
                .collect(Collectors.toSet());

        Project selectedProject = projectList.getSelectionModel().getSelectedItem();
        String selectedStatus = statusFilterComboBox.getValue();

        List<Task> filtered = allTasks.stream()
                .filter(task -> projectIds.isEmpty() || projectIds.contains(task.getProjectId()))
                .filter(task -> selectedProject == null || task.getProjectId() == selectedProject.getId())
                .filter(task -> selectedStatus == null || "All".equals(selectedStatus) || selectedStatus.equals(task.getStatus()))
                .toList();

        tasksTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showTaskDetails(Task task) {
        selectedTask = task;
        commentsList.getItems().clear();

        if (task == null) {
            selectedTaskLabel.setText("No task selected");
            descriptionArea.clear();
            return;
        }

        selectedTaskLabel.setText(task.getTitle() + " | " + task.getStatus() + " | " + task.getProject());
        descriptionArea.setText(task.getDescription() != null ? task.getDescription() : "");
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

    private void acceptSelectedTask() {
        if (selectedTask == null) {
            showError("Select a task first");
            return;
        }

        changeSelectedTaskStatus(3);
    }

    private void rejectSelectedTask() {
        if (selectedTask == null) {
            showError("Select a task first");
            return;
        }

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Reject Task");
        reasonDialog.setHeaderText("Reason for rejection");
        reasonDialog.setContentText("Comment:");
        Optional<String> reason = reasonDialog.showAndWait();

        if (reason.isPresent() && !reason.get().trim().isEmpty()) {
            addComment(selectedTask.getId(), "Rejected: " + reason.get().trim());
        }

        changeSelectedTaskStatus(4);
    }

    private void changeSelectedTaskStatus(int statusId) {
        int[] payload = {selectedTask.getId(), statusId};
        Response response = NetworkClient.getInstance().sendRequest(new Request("CHANGE_TASK_STATUS", payload));

        if (response != null && response.isSuccess()) {
            refreshData();
            tasksTable.getSelectionModel().clearSelection();
        } else {
            showError(response != null ? response.getMessage() : "Cannot change task status");
        }
    }

    private void addComment(int taskId, String text) {
        Object[] payload = {taskId, userId, text};
        Response response = NetworkClient.getInstance().sendRequest(new Request("ADD_COMMENT", payload));

        if (response == null || !response.isSuccess()) {
            showError(response != null ? response.getMessage() : "Cannot send comment");
        }
    }

    private int resolveCurrentProjectId() {
        if (selectedTask != null && selectedTask.getProjectId() > 0) {
            return selectedTask.getProjectId();
        }

        Project selectedProject = projectList.getSelectionModel().getSelectedItem();
        return selectedProject != null ? selectedProject.getId() : 0;
    }

    private String buildBugDescriptionPrefix() {
        if (selectedTask == null) {
            return "";
        }

        return "Found while testing task #" + selectedTask.getId() + " (" + selectedTask.getTitle() + "): ";
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}
