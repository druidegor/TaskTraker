package org.example.tasktraker.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    // Приватный конструктор (часть паттерна Singleton)
    private NetworkClient() {
        try {
            // Подключаемся к серверу
            this.socket = new Socket(HOST, PORT);

            // ВАЖНО: ObjectOutputStream всегда создается первым!
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Клиент успешно подключился к серверу!");
        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    // Метод для получения единственного экземпляра класса
    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    // Универсальный метод для отправки запросов и получения ответов
    public Response sendRequest(Request request) {
        try {
            if (out == null || in == null) {
                return new Response(false, "Нет соединения с сервером", null);
            }

            // Отправляем запрос
            out.writeObject(request);
            out.flush();

            // Ждем и возвращаем ответ от сервера
            return (Response) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            return new Response(false, "Ошибка сети: " + e.getMessage(), null);
        }
    }
}