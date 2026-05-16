package org.example.tasktraker.data;

import org.example.tasktraker.entity.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql =
                "SELECT t.id, t.title, t.status_id, t.priority_id, t.project_id, t.description, " +
                        "p.name AS project_name, t.assignee_id, u.name AS assignee_name " +
                        "FROM tasks t " +
                        "JOIN projects p ON t.project_id = p.id " +
                        "LEFT JOIN users u ON t.assignee_id = u.id";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        getStatusName(rs.getInt("status_id")),
                        getPriorityName(rs.getInt("priority_id")),
                        rs.getString("project_name"),
                        rs.getString("description"),
                        rs.getInt("project_id"),
                        rs.getInt("assignee_id"),
                        rs.getString("assignee_name")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public boolean createBug(String title, String description, int projectId, int authorId) {
        return createBug(title, description, projectId, authorId, 0);
    }

    public boolean createBug(String title, String description, int projectId, int authorId, int assigneeId) {
        String sql = "INSERT INTO tasks (title, description, status_id, priority_id, type_id, project_id, assignee_id, author_id) " +
                "VALUES (?, ?, 1, 2, 2, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, projectId);
            if (assigneeId > 0) {
                stmt.setInt(4, assigneeId);
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            stmt.setInt(5, authorId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createTask(String title, String description, int projectId, int assigneeId, int authorId, int priorityId) {
        String sql = "INSERT INTO tasks (title, description, status_id, priority_id, type_id, project_id, assignee_id, author_id) " +
                "VALUES (?, ?, 1, ?, 1, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, priorityId);
            stmt.setInt(4, projectId);
            stmt.setInt(5, assigneeId);
            stmt.setInt(6, authorId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateTaskStatus(int taskId, int statusId) {
        String sql = "UPDATE tasks SET status_id = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, statusId);
            stmt.setInt(2, taskId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Task> getTasksByAssignee(int assigneeId) {
        List<Task> tasks = new ArrayList<>();
        String sql =
                "SELECT t.id, t.title, t.status_id, t.priority_id, t.project_id, t.description, " +
                        "p.name AS project_name, t.assignee_id, u.name AS assignee_name " +
                        "FROM tasks t " +
                        "JOIN projects p ON t.project_id = p.id " +
                        "LEFT JOIN users u ON t.assignee_id = u.id " +
                        "WHERE t.assignee_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assigneeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            getStatusName(rs.getInt("status_id")),
                            getPriorityName(rs.getInt("priority_id")),
                            rs.getString("project_name"),
                            rs.getString("description"),
                            rs.getInt("project_id"),
                            rs.getInt("assignee_id"),
                            rs.getString("assignee_name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public List<Task> getTasksByTesterProjects(int testerId) {
        List<Task> tasks = new ArrayList<>();
        String sql =
                "SELECT t.id, t.title, t.status_id, t.priority_id, t.project_id, t.description, " +
                        "p.name AS project_name, t.assignee_id, u.name AS assignee_name " +
                        "FROM tasks t " +
                        "JOIN projects p ON t.project_id = p.id " +
                        "JOIN project_users pu ON p.id = pu.project_id " +
                        "LEFT JOIN users u ON t.assignee_id = u.id " +
                        "WHERE pu.user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, testerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new Task(
                            rs.getInt("id"),
                            rs.getString("title"),
                            getStatusName(rs.getInt("status_id")),
                            getPriorityName(rs.getInt("priority_id")),
                            rs.getString("project_name"),
                            rs.getString("description"),
                            rs.getInt("project_id"),
                            rs.getInt("assignee_id"),
                            rs.getString("assignee_name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    private String getStatusName(int statusId) {
        return switch (statusId) {
            case 1 -> "Open";
            case 2 -> "In Progress";
            case 3 -> "Accepted";
            case 4 -> "Rejected";
            case 5 -> "Ready for Testing";
            default -> "Status " + statusId;
        };
    }

    private String getPriorityName(int priorityId) {
        return switch (priorityId) {
            case 1 -> "Low";
            case 2 -> "Normal";
            case 3 -> "High";
            case 4 -> "Critical";
            default -> "Priority " + priorityId;
        };
    }
}
