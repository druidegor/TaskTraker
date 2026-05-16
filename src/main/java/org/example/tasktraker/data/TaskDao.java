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
}