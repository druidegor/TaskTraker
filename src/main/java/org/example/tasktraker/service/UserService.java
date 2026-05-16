package org.example.tasktraker.service;

import org.example.tasktraker.data.UserDao;
import org.example.tasktraker.entity.User;

import java.util.List;

public class UserService {

    private final UserDao userDao = new UserDao();

    public User login(String email, String password) {

        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email is empty");
        }

        if (password == null || password.isEmpty()) {
            throw new RuntimeException("Password is empty");
        }

        return userDao.login(email.trim(), password.trim());
    }

    public void register(String name, String email, String password, String confirmPassword, String role) {

        if (name == null || name.isEmpty() ||
                email == null || email.isEmpty() ||
                password == null || password.isEmpty() ||
                role == null) {
            throw new RuntimeException("Fill all fields");
        }

        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userDao.existsByEmail(email)) {
            throw new RuntimeException("User already exists");
        }

        int roleId = userDao.getRoleIdByName(role);
        if (roleId == -1) {
            throw new RuntimeException("Role not found");
        }

        User user = new User(0, name, email, role, password);

        boolean saved = userDao.save(user, roleId);

        if (!saved) {
            throw new RuntimeException("Registration failed");
        }
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public User getUserById(int id) {
        return userDao.getUserById(id);
    }
}