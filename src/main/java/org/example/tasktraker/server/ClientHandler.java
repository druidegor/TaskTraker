package org.example.tasktraker.server;

import org.example.tasktraker.entity.User;
import org.example.tasktraker.network.Request;
import org.example.tasktraker.network.Response;
import org.example.tasktraker.service.UserService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final UserService userService;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.userService = new UserService(); // Подключаем твой сервис
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                // 1. Читаем запрос от клиента
                Request request = (Request) in.readObject();
                System.out.println("Сервер получил команду: " + request.getCommand());

                // 2. Обрабатываем запрос
                Response response = processRequest(request);

                // 3. Отправляем ответ обратно клиенту
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
            // Позже мы добавим сюда REGISTER, GET_PROJECTS и т.д.
            default:
                return new Response(false, "Неизвестная команда", null);
        }
    }

    private Response handleLogin(Object payload) {
        try {
            // Ожидаем, что клиент пришлет массив из двух строк: [email, password]
            String[] credentials = (String[]) payload;
            String email = credentials[0];
            String password = credentials[1];

            // Вызываем ТВОЙ метод из UserService
            User user = userService.login(email, password);

            if (user != null) {
                return new Response(true, "Успешный вход", user);
            } else {
                return new Response(false, "Неверный email или пароль", null);
            }
        } catch (Exception e) {
            return new Response(false, e.getMessage(), null);
        }
    }
}