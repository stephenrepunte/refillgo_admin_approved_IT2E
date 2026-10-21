package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the tables required by the ReFillGo account application before
 * the command-line menu starts.
 */
public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE COLLATE NOCASE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL CHECK (roles IN ('admin/owner', 'staff', 'customer')),
                status TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'APPROVED', 'ACTIVE', 'DISABLED'))
            )
            """;

        String createActivityLogs = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT NOT NULL,
                details TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
            )
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createUsers);
            statement.execute(createActivityLogs);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize ReFillGo database: " + e.getMessage(), e);
        }

        createFirstRunAdministrator();
    }

    private static void createFirstRunAdministrator() {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement check = connection.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE roles = 'admin/owner'");
             ResultSet result = check.executeQuery()) {
            if (result.next() && result.getInt(1) == 0) {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO users(username, password, roles, status) VALUES (?, ?, ?, ?)")) {
                    insert.setString(1, "admin");
                    insert.setString(2, "admin123");
                    insert.setString(3, "admin/owner");
                    insert.setString(4, "ACTIVE");
                    insert.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create initial administrator: " + e.getMessage(), e);
        }
    }
}
