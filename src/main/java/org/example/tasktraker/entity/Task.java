package org.example.tasktraker.entity;

import java.io.Serializable;

public class Task implements Serializable {
    private int id;
    private String title;
    private String status;
    private String priority;
    private String project;
    private String description;
    private int projectId;

    public Task(int id, String title, String status, String priority, String project, String description) {
        this(id, title, status, priority, project, description, 0);
    }

    public Task(int id, String title, String status, String priority, String project, String description, int projectId) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.project = project;
        this.description = description;
        this.projectId = projectId;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getProject() { return project; }
    public String getDescription() { return description; }
    public int getProjectId() { return projectId; }
}
