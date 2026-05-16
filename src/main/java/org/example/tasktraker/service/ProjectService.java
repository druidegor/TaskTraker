package org.example.tasktraker.service;

import org.example.tasktraker.data.ProjectDao;
import org.example.tasktraker.data.ProjectUserDao;
import org.example.tasktraker.data.UserDao;
import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.User;

import java.util.List;

public class ProjectService {

    private final ProjectDao projectDao = new ProjectDao();
    private final ProjectUserDao projectUserDao = new ProjectUserDao();
    private final UserDao userDao = new UserDao();

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
        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Admin cannot be assigned to a project");
        }

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

    public void removeUserFromProject(int userId, int projectId) {
        boolean success = projectUserDao.removeUserFromProject(userId, projectId);

        if (!success) {
            throw new RuntimeException("Cannot remove user from project");
        }
    }

    public void deleteProject(int projectId) {
        if (projectId <= 0) {
            throw new RuntimeException("Project is not selected");
        }

        boolean deleted = projectDao.deleteProject(projectId);
        if (!deleted) {
            throw new RuntimeException("Project deletion failed");
        }
    }
}
