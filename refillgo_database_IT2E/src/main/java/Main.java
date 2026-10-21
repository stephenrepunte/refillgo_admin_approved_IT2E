import dao.DatabaseInitializer;
import dao.UserDao;
import ui.Authentication;

import java.util.Scanner;

/**
 * Single startup entry point: initialize the database, create shared
 * application resources, and delegate to Authentication.
 */
public class Main {
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();

            Scanner scanner = new Scanner(System.in);
            UserDao userDao = new UserDao();

            Authentication authentication = new Authentication(scanner, userDao);
            authentication.start();
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
        }
    }
}
