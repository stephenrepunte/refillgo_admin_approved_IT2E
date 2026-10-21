package ui;

import dao.UserDao;
import model.User;
import java.util.Scanner;

public class MenuUI {
    private final Scanner scanner;
    private final UserDao userDao;

    public MenuUI(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void openRoleMenu(User user) {
        switch (user.roles().toLowerCase()) {
            case "admin/owner" -> new AdminMenu(scanner, userDao).start(user);
            case "staff" -> staffMenu(user);
            case "customer" -> customerMenu(user);
            default -> System.out.println("Unknown role. Access denied.");
        }
        System.out.println("Logged out: " + user.username());
    }

    private void staffMenu(User user) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- STAFF MENU ---");
            System.out.println("Welcome, " + user.username() + "!");
            System.out.println("1. View Staff Account");
            System.out.println("2. Logout");
            System.out.print("Choose: ");
            switch (readInt()) {
                case 1 -> System.out.printf("Username: %s%nRole: %s%nStatus: %s%n", user.username(), user.roles(), user.status());
                case 2 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void customerMenu(User user) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- CUSTOMER MENU ---");
            System.out.println("Welcome, " + user.username() + "!");
            System.out.println("1. View Customer Account");
            System.out.println("2. Logout");
            System.out.print("Choose: ");
            switch (readInt()) {
                case 1 -> System.out.printf("Username: %s%nRole: %s%nStatus: %s%n", user.username(), user.roles(), user.status());
                case 2 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private int readInt() {
        String value = scanner.nextLine().trim();
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return -1; }
    }
}
