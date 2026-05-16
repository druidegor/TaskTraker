package org.example.tasktraker.entity;

import java.io.Serializable;

public class Task implements Serializable {
    private int id;
    private String title;
    private String status;
    private String priority;
    private String project;
    private String description;

    public Task(int id, String title, String status, String priority, String project, String description) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.project = project;
        this.description = description;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getProject() { return project; }
    public String getDescription() { return description; }
}