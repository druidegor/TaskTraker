package org.example.tasktraker.data;

import org.example.tasktraker.entity.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDao {

    public boolean addComment(int taskId, int authorId, String text) {
        String sql = "INSERT INTO comments (task_id, author_id, text) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.setInt(2, authorId);
            stmt.setString(3, text);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Comment> getCommentsByTaskId(int taskId) {
        List<Comment> comments = new ArrayList<>();
        // Используем JOIN для получения имени автора комментария
        String sql = "SELECT c.id, c.task_id, c.text, u.name AS author_name " +
                "FROM comments c JOIN users u ON c.author_id = u.id " +
                "WHERE c.task_id = ? ORDER BY c.created_at ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(new Comment(
                        rs.getInt("id"),
                        rs.getInt("task_id"),
                        rs.getString("text"),
                        rs.getString("author_name")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }
}