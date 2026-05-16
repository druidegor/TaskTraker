package org.example.tasktraker.service;

import org.example.tasktraker.data.TaskDao;
import org.example.tasktraker.data.ProjectUserDao;
import org.example.tasktraker.entity.Task;

import java.util.List;

public class TaskService {
    private final TaskDao taskDao = new TaskDao();
    private final ProjectUserDao projectUserDao = new ProjectUserDao();

    public List<Task> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public boolean createBug(String title, String description, int projectId, int authorId) {
        return createBug(title, description, projectId, authorId, 0);
    }

    public boolean createBug(String title, String description, int projectId, int authorId, int assigneeId) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Bug title is empty");
        }

        if (projectId <= 0) {
            throw new RuntimeException("Project is not selected");
        }

        if (assigneeId > 0) {
            projectUserDao.addUserToProject(assigneeId, projectId);
        }

        return taskDao.createBug(
                title.trim(),
                description != null ? description.trim() : "",
                projectId,
                authorId,
                assigneeId
        );
    }

    public void createTask(String title, String description, int projectId, int assigneeId, int authorId, int priorityId) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Task title is empty");
        }

        if (projectId <= 0) {
            throw new RuntimeException("Project is not selected");
        }

        if (assigneeId <= 0) {
            throw new RuntimeException("Assignee is not selected");
        }

        if (priorityId <= 0) {
            throw new RuntimeException("Priority is not selected");
        }

        projectUserDao.addUserToProject(assigneeId, projectId);

        boolean created = taskDao.createTask(
                title.trim(),
                description != null ? description.trim() : "",
                projectId,
                assigneeId,
                authorId,
                priorityId
        );

        if (!created) {
            throw new RuntimeException("Task creation failed");
        }
    }

    public boolean updateTaskStatus(int taskId, int statusId) {
        return taskDao.updateTaskStatus(taskId, statusId);
    }

    public void deleteTask(int taskId) {
        if (taskId <= 0) {
            throw new RuntimeException("Task is not selected");
        }

        boolean deleted = taskDao.deleteTask(taskId);
        if (!deleted) {
            throw new RuntimeException("Task deletion failed");
        }
    }

    public List<Task> getTasksByAssignee(int assigneeId) {
        return taskDao.getTasksByAssignee(assigneeId);
    }

    public List<Task> getTasksByTesterProjects(int testerId) {
        return taskDao.getTasksByTesterProjects(testerId);
    }
}
