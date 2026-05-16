package org.example.tasktraker.data;

import org.example.tasktraker.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public List<User> getAllUsers() {
        String query =
                "SELECT users.id, users.email, users.password, users.name, roles.name AS role\n" +
                        "FROM users\n" +
                        "JOIN roles ON users.role_id = roles.id";

        List<User> users = new ArrayList<User>();
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {


            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                users.add( new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public User login(String email, String password) {
        String query =
                "SELECT users.id, users.email, users.password, users.name, roles.name AS role\n" +
                        "FROM users\n" +
                        "JOIN roles ON users.role_id = roles.id\n" +
                        "WHERE email = ? AND password = ? ";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getRoleIdByName(String role) {
        String sql = "SELECT id FROM roles WHERE name = ?";

        try(Connection connection = Database.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1,role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean save(User user, int roleId) {
        String sql = "INSERT INTO users (name, email, password, role_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setInt(4, roleId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public User getUserById(int id) {
        String sql =
                "SELECT u.id, u.name, u.email, u.password, r.name AS role " +
                        "FROM users u JOIN roles r ON u.role_id = r.id " +
                        "WHERE u.id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("password")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}