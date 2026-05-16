package org.example.tasktraker.network;

import java.io.Serializable;

public class Request implements Serializable {
    private String command; // Название команды, например "LOGIN"
    private Object payload; // Сами данные (например, логин и пароль)

    public Request(String command, Object payload) {
        this.command = command;
        this.payload = payload;
    }

    public String getCommand() { return command; }
    public Object getPayload() { return payload; }
}