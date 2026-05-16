package org.example.tasktraker.service;

import org.example.tasktraker.data.TaskDao;
import org.example.tasktraker.entity.Task;

import java.util.List;

public class TaskService {
    private final TaskDao taskDao = new TaskDao();

    public List<Task> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public boolean createBug(String title, String description, int projectId, int authorId) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Название бага не может быть пустым");
        }
        return taskDao.createBug(title.trim(), description != null ? description.trim() : "", projectId, authorId);
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

    public List<Task> getTasksByAssignee(int assigneeId) {
        return taskDao.getTasksByAssignee(assigneeId);
    }
}
