package org.example.tasktraker.data;

import org.example.tasktraker.entity.Project;
import org.example.tasktraker.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectUserDao {

    public boolean addUserToProject(int userId, int projectId) {
        if (isUserAssignedToProject(userId, projectId)) {
            return true;
        }

        String sql = "INSERT INTO project_users (user_id, project_id) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isUserAssignedToProject(int userId, int projectId) {
        String sql = "SELECT 1 FROM project_users WHERE user_id = ? AND project_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, projectId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public List<Project> getProjectsByUser(int userId) {
        String sql =
                "SELECT p.id, p.name, p.description " +
                        "FROM projects p " +
                        "JOIN project_users pu ON p.id = pu.project_id " +
                        "WHERE pu.user_id = ?";

        List<Project> projects = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projects.add(new Project(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projects;
    }

    public List<User> getUsersByProject(int projectId) {
        String sql =
                "SELECT u.id, u.name, u.email, u.password, r.name AS role " +
                        "FROM users u " +
                        "JOIN roles r ON u.role_id = r.id " +
                        "JOIN project_users pu ON u.id = pu.user_id " +
                        "WHERE pu.project_id = ?";

        List<User> users = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}
