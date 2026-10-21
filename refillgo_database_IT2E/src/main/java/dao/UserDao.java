package dao;

import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    public boolean register(String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, password);
            statement.setString(3, role);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19 || (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique"))) return false;
            throw e;
        }
    }

    public Optional<User> findByCredentials(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, password);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(mapUser(rs));
            }
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() throws SQLException {
        return queryUsers("SELECT id, username, password, roles, status FROM users ORDER BY id", null);
    }

    public List<User> getPendingUsers() throws SQLException {
        return queryUsers("SELECT id, username, password, roles, status FROM users WHERE status = ? ORDER BY id", "PENDING");
    }

    private List<User> queryUsers(String sql, String status) throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (status != null) statement.setString(1, status);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) users.add(mapUser(rs));
            }
        }
        return users;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                rs.getString("roles"), rs.getString("status"));
    }

    public boolean approveUser(int userId) throws SQLException {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ? AND status = 'PENDING'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        }
    }
}
