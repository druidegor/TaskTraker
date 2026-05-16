package org.example.tasktraker.service;

import org.example.tasktraker.data.TaskDao;
import org.example.tasktraker.entity.Task;

import java.util.List;

public class TaskService {
    private final TaskDao taskDao = new TaskDao();

    public List<Task> getAllTasks() {
        return taskDao.getAllTasks();
    }
}