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
        // Простой запрос к базе
        String sql = "SELECT id, title, status_id, priority_id, project_id, description FROM tasks";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        "Status " + rs.getInt("status_id"),     // Временно преобразуем ID в строку
                        "Priority " + rs.getInt("priority_id"), // Позже сделаем JOIN с таблицей словарей
                        "Project " + rs.getInt("project_id"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public boolean createBug(String title, String description, int projectId, int authorId) {
        // Задаем значения по умолчанию: status_id = 1 (Open), priority_id = 1 (Normal), type_id = 2 (Bug)
        String sql = "INSERT INTO tasks (title, description, status_id, priority_id, type_id, project_id, author_id) " +
                "VALUES (?, ?, 1, 1, 2, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, projectId);
            stmt.setInt(4, authorId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}