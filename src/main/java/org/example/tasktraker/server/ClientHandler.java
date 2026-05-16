package org.example.tasktraker.server;

import org.example.tasktraker.entity.Comment;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.Task;
import org.example.tasktraker.entity.User;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;
import org.example.tasktraker.service.CommentService;
import org.example.tasktraker.service.ProjectService;
import org.example.tasktraker.service.TaskService;
import org.example.tasktraker.service.UserService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final UserService userService;
    private final ProjectService projectService; // Добавили сервис проектов
    private final TaskService taskService; // <-- ДОБАВИТЬ ЭТО
    private final CommentService commentService;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.userService = new UserService();
        this.projectService = new ProjectService();
        this.taskService = new TaskService();
        this.commentService = new CommentService(); // <-- Инициализируем
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                Request request = (Request) in.readObject();
                System.out.println("Сервер получил команду: " + request.getCommand());

                Response response = processRequest(request);

                out.writeObject(response);
                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Клиент отключился.");
        }
    }

    private Response processRequest(Request request) {
        switch (request.getCommand()) {
            case "LOGIN":
                return handleLogin(request.getPayload());
            case "GET_ALL_PROJECTS":
                return handleGetAllProjects();
            case "GET_ALL_USERS":
                return handleGetAllUsers();
            case "CREATE_PROJECT":
                return handleCreateProject(request.getPayload());
            case "ASSIGN_USER":
                return handleAssignUser(request.getPayload());
            case "GET_ALL_TASKS":         // <-- ДОБАВИТЬ ЭТО
                return handleGetAllTasks();
            case "ADD_COMMENT":
                return handleAddComment(request.getPayload());
            case "GET_COMMENTS_BY_TASK":
                return handleGetCommentsByTask(request.getPayload());
            case "CREATE_BUG":
                return handleCreateBug(request.getPayload());
            case "CHANGE_TASK_STATUS":
                return handleChangeTaskStatus(request.getPayload());
            default:
                return new Response(false, "Неизвестная команда", null);
        }
    }

    private Response handleLogin(Object payload) {
        try {
            String[] credentials = (String[]) payload;
            User user = userService.login(credentials[0], credentials[1]);
            if (user != null) {
                return new Response(true, "Успешный вход", user);
            }
            return new Response(false, "Неверный логин или пароль", null);
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    // --- НОВЫЕ МЕТОДЫ ---

    private Response handleGetAllProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            return new Response(true, "Проекты получены", projects);
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleGetAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return new Response(true, "Пользователи получены", users);
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleCreateProject(Object payload) {
        try {
            String[] data = (String[]) payload; // [name, description]
            projectService.createProject(data[0], data[1]);
            return new Response(true, "Проект успешно создан", null);
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleAssignUser(Object payload) {
        try {
            int[] data = (int[]) payload; // [userId, projectId]
            projectService.assignUserToProject(data[0], data[1]);
            return new Response(true, "Пользователь назначен на проект", null);
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleGetAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            return new Response(true, "Задачи успешно получены", tasks);
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleAddComment(Object payload) {
        try {
            // Ожидаем массив Object: [taskId (int), authorId (int), text (String)]
            Object[] data = (Object[]) payload;
            int taskId = (int) data[0];
            int authorId = (int) data[1];
            String text = (String) data[2];

            boolean success = commentService.addComment(taskId, authorId, text);
            if (success) {
                return new Response(true, "Комментарий добавлен", null);
            }
            return new Response(false, "Не удалось добавить комментарий в БД", null);
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleGetCommentsByTask(Object payload) {
        try {
            int taskId = (int) payload; // Ожидаем просто число (ID задачи)
            List<Comment> comments = commentService.getCommentsByTaskId(taskId);
            return new Response(true, "Комментарии загружены", comments);
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }

    private Response handleCreateBug(Object payload) {
        try {
            // Ожидаем массив: [title (String), description (String), projectId (int), authorId (int)]
            Object[] data = (Object[]) payload;
            String title = (String) data[0];
            String description = (String) data[1];
            int projectId = (int) data[2];
            int authorId = (int) data[3];

            boolean success = taskService.createBug(title, description, projectId, authorId);

            if (success) {
                return new Response(true, "Баг успешно создан", null);
            }
            return new Response(false, "Не удалось сохранить баг в базу", null);

        } catch (Exception e) {
            return new Response(false, "Ошибка сервера: " + e.getMessage(), null);
        }
    }

    private Response handleChangeTaskStatus(Object payload) {
        try {
            // Ожидаем массив int: [taskId, statusId]
            int[] data = (int[]) payload;
            int taskId = data[0];
            int statusId = data[1];

            boolean success = taskService.updateTaskStatus(taskId, statusId);
            if (success) {
                return new Response(true, "Статус задачи обновлен", null);
            }
            return new Response(false, "Не удалось обновить статус в базе", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка сервера: " + e.getMessage(), null);
        }
    }
}