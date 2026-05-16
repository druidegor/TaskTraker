package org.example.tasktraker.network;

import java.io.Serializable;

public class Response implements Serializable {
    private boolean success; // Успешно или нет?
    private String message;  // Сообщение (например, текст ошибки)
    private Object data;     // Возвращаемые данные (например, объект User)

    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
