package org.example.tasktraker.network;

public class ResponseFactory {

    // Фабричный метод для создания успешного ответа с данными
    public static Response createSuccess(String message, Object data) {
        return new Response(true, message, data);
    }

    // Фабричный метод для создания успешного ответа без данных (просто подтверждение)
    public static Response createSuccess(String message) {
        return new Response(true, message, null);
    }

    // Фабричный метод для создания ответа об ошибке
    public static Response createError(String errorMessage) {
        return new Response(false, errorMessage, null);
    }
}
