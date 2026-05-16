package org.example.tasktraker.service;

import org.example.tasktraker.data.CommentDao;
import org.example.tasktraker.entity.Comment;
import java.util.List;

public class CommentService {
    private final CommentDao commentDao = new CommentDao();

    public boolean addComment(int taskId, int authorId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("Комментарий не может быть пустым");
        }
        return commentDao.addComment(taskId, authorId, text.trim());
    }

    public List<Comment> getCommentsByTaskId(int taskId) {
        return commentDao.getCommentsByTaskId(taskId);
    }
}