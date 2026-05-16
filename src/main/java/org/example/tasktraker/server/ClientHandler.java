package org.example.tasktraker.server;

import org.example.tasktraker.entity.Comment;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.Task;
import org.example.tasktraker.entity.User;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;
import org.example.tasktraker.network.ResponseFactory;
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
            case "REGISTER":
                return handleRegister(request.getPayload());
            case "GET_ALL_PROJECTS":
                return handleGetAllProjects();
            case "GET_ALL_USERS":
                return handleGetAllUsers();
            case "CREATE_PROJECT":
                return handleCreateProject(request.getPayload());
            case "ASSIGN_USER":
                return handleAssignUser(request.getPayload());
            case "GET_PROJECT_USERS":
                return handleGetProjectUsers(request.getPayload());
            case "CREATE_TASK":
                return handleCreateTask(request.getPayload());
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
            case "GET_TASKS_BY_ASSIGNEE":
                return handleGetTasksByAssignee(request.getPayload());
            case "GET_TASKS_BY_TESTER_PROJECTS":
                return handleGetTasksByTesterProjects(request.getPayload());
            case "GET_USER_PROJECTS":
                return handleGetUserProjects(request.getPayload());
            default:
                return ResponseFactory.createError("Неизвестная команда");
        }
    }

    private Response handleLogin(Object payload) {
        try {
            String[] credentials = (String[]) payload;
            User user = userService.login(credentials[0], credentials[1]);
            if (user != null) {
                return ResponseFactory.createSuccess("Успешный вход", user);
            }
            return ResponseFactory.createError("Неверный логин или пароль");
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleRegister(Object payload) {
        try {
            String[] data = (String[]) payload;
            userService.register(data[0], data[1], data[2], data[3], data[4]);
            return ResponseFactory.createSuccess("User registered");
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    // --- НОВЫЕ МЕТОДЫ ---

    private Response handleGetAllProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            return ResponseFactory.createSuccess("Проекты получены", projects);
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleGetAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseFactory.createSuccess("Пользователи получены", users);
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleCreateProject(Object payload) {
        try {
            String[] data = (String[]) payload; // [name, description]
            projectService.createProject(data[0], data[1]);
            return ResponseFactory.createSuccess("Проект успешно создан");
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleAssignUser(Object payload) {
        try {
            int[] data = (int[]) payload; // [userId, projectId]
            projectService.assignUserToProject(data[0], data[1]);
            return ResponseFactory.createSuccess("Пользователь назначен на проект");
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleGetProjectUsers(Object payload) {
        try {
            int projectId = (int) payload;
            List<User> users = projectService.getProjectUsers(projectId);
            return ResponseFactory.createSuccess("Участники проекта получены", users);
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleCreateTask(Object payload) {
        try {
            Object[] data = (Object[]) payload;
            String title = (String) data[0];
            String description = (String) data[1];
            int projectId = (int) data[2];
            int assigneeId = (int) data[3];
            int authorId = (int) data[4];
            int priorityId = (int) data[5];

            taskService.createTask(title, description, projectId, assigneeId, authorId, priorityId);
            return ResponseFactory.createSuccess("Задача успешно создана");
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleGetAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            return ResponseFactory.createSuccess("Задачи успешно получены", tasks);
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
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
                return ResponseFactory.createSuccess("Комментарий добавлен");
            }
            return ResponseFactory.createError("Не удалось добавить комментарий в БД");
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleGetCommentsByTask(Object payload) {
        try {
            int taskId = (int) payload; // Ожидаем просто число (ID задачи)
            List<Comment> comments = commentService.getCommentsByTaskId(taskId);
            return ResponseFactory.createSuccess("Комментарии загружены", comments);
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleCreateBug(Object payload) {
        try {
            // [title, description, projectId, authorId, optional assigneeId]
            Object[] data = (Object[]) payload;
            String title = (String) data[0];
            String description = (String) data[1];
            int projectId = (int) data[2];
            int authorId = (int) data[3];
            int assigneeId = data.length > 4 ? (int) data[4] : 0;

            boolean success = taskService.createBug(title, description, projectId, authorId, assigneeId);

            if (success) {
                return ResponseFactory.createSuccess("Баг успешно создан");
            }
            return ResponseFactory.createError("Не удалось сохранить баг в базу");

        } catch (Exception e) {
            return ResponseFactory.createError("Ошибка сервера: " + e.getMessage());
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
                return ResponseFactory.createSuccess("Статус задачи обновлен");
            }
            return ResponseFactory.createError("Не удалось обновить статус в базе");
        } catch (Exception e) {
            return ResponseFactory.createError("Ошибка сервера: " + e.getMessage());
        }
    }

    private Response handleGetTasksByAssignee(Object payload) {
        try {
            int assigneeId = (int) payload;
            List<org.example.tasktraker.entity.Task> tasks = taskService.getTasksByAssignee(assigneeId);
            return ResponseFactory.createSuccess("Задачи разработчика успешно получены", tasks);
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleGetTasksByTesterProjects(Object payload) {
        try {
            int testerId = (int) payload;
            List<Task> tasks = taskService.getTasksByTesterProjects(testerId);
            return ResponseFactory.createSuccess("Задачи проектов тестировщика успешно получены", tasks);
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }

    private Response handleGetUserProjects(Object payload) {
        try {
            int userId = (int) payload;
            List<org.example.tasktraker.entity.Project> projects = projectService.getUserProjects(userId);
            return ResponseFactory.createSuccess("Проекты пользователя успешно получены", projects);
        } catch (Exception e) {
            return ResponseFactory.createError(e.getMessage());
        }
    }
}
