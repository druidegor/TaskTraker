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

    public boolean updateTaskStatus(int taskId, int statusId) {
        return taskDao.updateTaskStatus(taskId, statusId);
    }
}