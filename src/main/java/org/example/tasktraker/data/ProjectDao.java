package org.example.tasktraker.data;

import org.example.tasktraker.entity.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao {
    private final TaskDao taskDao = new TaskDao();

    public boolean createProject(String name, String description) {
        String sql = "INSERT INTO projects (name, description) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, description);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Project> getAllProjects() {
        String sql = "SELECT * FROM projects";

        List<Project> projects = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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

    public boolean deleteProject(int projectId) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            taskDao.deleteTasksByProject(conn, projectId);

            try (PreparedStatement usersStmt = conn.prepareStatement("DELETE FROM project_users WHERE project_id = ?")) {
                usersStmt.setInt(1, projectId);
                usersStmt.executeUpdate();
            }

            int deleted;
            try (PreparedStatement projectStmt = conn.prepareStatement("DELETE FROM projects WHERE id = ?")) {
                projectStmt.setInt(1, projectId);
                deleted = projectStmt.executeUpdate();
            }

            conn.commit();
            return deleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
