package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Owns the initial CLI authentication flow and routes active users to
 * their role-specific menu. Scanner and UserDao are shared with MenuUI.
 */
public class Authentication {
    private final Scanner scanner;
    private final UserDao userDao;
    private final MenuUI menuUI;

    public Authentication(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.menuUI = new MenuUI(scanner, userDao);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\\n==================================");
            System.out.println("       REFILLGO ACCOUNT SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            switch (readInt()) {
                case 1 -> registerAccount();
                case 2 -> login();
                case 3 -> {
                    System.out.println("Application closed. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please choose 1-3.");
            }
        }
    }

    private void registerAccount() {
        System.out.println("\\n--- Register Account ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isBlank()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password.isBlank()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        System.out.println("Select role:");
        System.out.println("1. OWNER/ADMIN");
        System.out.println("2. STAFF");
        System.out.println("3. CUSTOMER");
        System.out.print("Role: ");

        String role = switch (readInt()) {
            case 1 -> "admin/owner";
            case 2 -> "staff";
            case 3 -> "customer";
            default -> null;
        };
        if (role == null) {
            System.out.println("Invalid role. Registration cancelled.");
            return;
        }

        try {
            if (userDao.register(username, password, role)) {
                System.out.println("Account registered successfully.");
                System.out.println("Role: " + role);
                System.out.println("Status: PENDING");
                System.out.println("Please wait for an administrator to activate your account.");
            } else {
                System.out.println("That username is already in use. Please choose another.");
            }
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void login() {
        System.out.println("\\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            Optional<User> result = userDao.findByCredentials(username, password);
            if (result.isEmpty()) {
                System.out.println("Invalid username or password.");
                return;
            }

            User user = result.get();
            if (!("ACTIVE".equalsIgnoreCase(user.status()) || "APPROVED".equalsIgnoreCase(user.status()))) {
                System.out.println("Login denied. Your account status is " + user.status() + ".");
                return;
            }

            System.out.println("Login successful. Welcome, " + user.username() + "!");
            System.out.println("Role: " + user.roles());
            menuUI.openRoleMenu(user);
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private int readInt() {
        String value = scanner.nextLine().trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
