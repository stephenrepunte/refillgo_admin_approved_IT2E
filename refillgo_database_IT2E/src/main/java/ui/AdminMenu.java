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
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            switch (readInt()) {
                case 1 -> showUsers(false);
                case 2 -> showUsers(true);
                case 3 -> approveUser();
                case 4 -> running = false;
                default -> System.out.println("Invalid choice. Please choose 1-4.");
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
            System.out.print("Enter target User ID to approve (0 to cancel): ");
            int id = readInt();
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

    private int readInt() {
        String value = scanner.nextLine().trim();
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return -1; }
    }
}
