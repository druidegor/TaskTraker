package org.example.tasktraker.entity;

import java.io.Serializable;

public class Comment implements Serializable {
    private int id;
    private int taskId;
    private String text;
    private String authorName;

    public Comment(int id, int taskId, String text, String authorName) {
        this.id = id;
        this.taskId = taskId;
        this.text = text;
        this.authorName = authorName;
    }

    public int getId() { return id; }
    public int getTaskId() { return taskId; }
    public String getText() { return text; }
    public String getAuthorName() { return authorName; }

    // Переопределяем toString, чтобы ListView в JavaFX сразу красиво выводил текст
    @Override
    public String toString() {
        return authorName + ": " + text;
    }
}