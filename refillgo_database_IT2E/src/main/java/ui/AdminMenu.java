package ui;

import dao.UserDao;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner;
    private final UserDao userDao;

    public AdminMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void start(User admin) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- OWNER/ADMIN MENU ---");
            System.out.println("Signed in as: " + admin.username());
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. Logout");
            int choice = readIntInput("Choose: ");
            switch (choice) {
                case 1 -> showUsers(false);
                case 2 -> showUsers(true);
                case 3 -> approveUser();
                case 4 -> updateUserRoleAndStatus(admin);
                case 5 -> running = false;
                default -> System.out.println("Invalid choice. Please choose 1-5.");
            }
        }
    }

    private void showUsers(boolean pendingOnly) {
        try {
            List<User> users = pendingOnly ? userDao.getPendingUsers() : userDao.getAllUsers();
            printUserTable(users);
            if (users.isEmpty()) System.out.println(pendingOnly ? "No pending registrations." : "No registered users.");
        } catch (SQLException e) {
            System.out.println("Could not load users: " + e.getMessage());
        }
    }

    private void approveUser() {
        try {
            List<User> pending = userDao.getPendingUsers();
            printUserTable(pending);
            if (pending.isEmpty()) {
                System.out.println("There are no pending accounts to approve.");
                return;
            }
            int id = readIntInput("Enter target User ID to approve (0 to cancel): ");
            if (id == 0) {
                System.out.println("Approval cancelled.");
                return;
            }
            boolean existsInPendingList = pending.stream().anyMatch(user -> user.id() == id);
            if (!existsInPendingList) {
                System.out.println("That ID is not in the pending registrations list.");
                return;
            }
            if (userDao.approveUser(id)) System.out.println("User ID " + id + " approved successfully. Status: APPROVED");
            else System.out.println("Approval failed. The account may no longer be pending.");
        } catch (SQLException e) {
            System.out.println("Could not approve user: " + e.getMessage());
        }
    }

    private void updateUserRoleAndStatus(User currentAdmin) {
        try {
            List<User> users = userDao.getAllUsers();
            printUserTable(users);
            if (users.isEmpty()) {
                System.out.println("No registered users to update.");
                return;
            }

            int targetUserId = readIntInput("Enter target User ID to update (0 to cancel): ");
            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            User target = users.stream()
                    .filter(user -> user.id() == targetUserId)
                    .findFirst().orElse(null);
            if (target == null) {
                System.out.println("User ID not found.");
                return;
            }

            if (targetUserId == currentAdmin.id()
                    && "APPROVED".equalsIgnoreCase(currentAdmin.status())) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                return;
            }

            String newRole = readValidRole();
            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            String newStatus = readValidStatus();
            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            if (userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus)) {
                System.out.println("User role and status updated successfully.");
            } else {
                System.out.println("Update failed. User ID may no longer exist.");
            }
        } catch (SQLException e) {
            System.out.println("Could not update user: " + e.getMessage());
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new role (OWNER/ADMIN, STAFF, CUSTOMER; 0 to cancel): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("0")) return null;
            if (role.equals("OWNER/ADMIN") || role.equals("STAFF") || role.equals("CUSTOMER")) {
                return role;
            }
            System.out.println("Invalid role. Enter OWNER/ADMIN, STAFF, or CUSTOMER.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new status (APPROVED or PENDING; 0 to cancel): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("0")) return null;
            if (status.equals("APPROVED") || status.equals("PENDING")) return status;
            System.out.println("Invalid status. Enter APPROVED or PENDING.");
        }
    }

    private void printUserTable(List<User> users) {
        String border = "+-------+-----------------+--------------+------------+";
        System.out.println(border);
        System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(border);
        for (User user : users) {
            System.out.printf("| %-5d | %-15.15s | %-12.12s | %-10.10s |%n",
                    user.id(), user.username(), user.roles(), user.status());
        }
        System.out.println(border);
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
