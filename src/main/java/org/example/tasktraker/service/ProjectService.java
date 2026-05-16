package org.example.tasktraker.service;

import org.example.tasktraker.data.ProjectDao;
import org.example.tasktraker.data.ProjectUserDao;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.User;

import java.util.List;

public class ProjectService {

    private final ProjectDao projectDao = new ProjectDao();
    private ProjectUserDao projectUserDao = new ProjectUserDao();

    public void createProject(String name, String description) {

        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Project name is empty");
        }

        boolean created = projectDao.createProject(name, description);

        if (!created) {
            throw new RuntimeException("Project creation failed");
        }
    }

    public void assignUserToProject(int userId, int projectId) {
        boolean success = projectUserDao.addUserToProject(userId, projectId);

        if (!success) {
            throw new RuntimeException("Cannot assign user to project");
        }
    }

    public List<Project> getUserProjects(int userId) {
        return projectUserDao.getProjectsByUser(userId);
    }

    public List<User> getProjectUsers(int projectId) {
        return projectUserDao.getUsersByProject(projectId);
    }

    public List<Project> getAllProjects() {
        return projectDao.getAllProjects();
    }
}
