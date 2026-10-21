# ReFillGo Account System (Java + SQLite)

## Architecture
- `Main.java` is the single application entry point.
- `dao/DatabaseInitializer.java` creates the required tables before the CLI starts.
- `dao/DatabaseConnection.java` enables SQLite foreign-key enforcement for every connection.
- `ui/Authentication.java` owns registration, login, and role routing.
- `ui/MenuUI.java` displays role-specific menus for Owner/Admin, Staff, and Customer.
- A single shared `Scanner` and `UserDao` are passed from `Main` through the UI classes.

## Features
- Register as `admin/owner`, `staff`, or `customer`.
- New accounts default to `PENDING` and cannot log in until activated.
- Owner/Admin can list accounts and change account status.
- Staff and Customer are routed to their own menus after successful login.
- `activity_logs` is initialized with a foreign key to `users`.

## First-run administrator
If no Owner/Admin account exists, the initializer creates:
- Username: `admin`
- Password: `admin123`
- Role: `admin/owner`
- Status: `ACTIVE`

Change this demo password before using the application beyond a classroom demo.

## Database location
The existing relative JDBC URL is preserved: `jdbc:sqlite:refillgo.db`. Maven creates/opens `refillgo.db` in the application's working directory (normally the project root when you run Maven there).

## Run
Use JDK 17+ and Maven. From the project root:

```bash
mvn clean compile
mvn exec:java
```

## Security note
This classroom version stores passwords as plain text to match the existing project. A real system should store salted password hashes instead.
