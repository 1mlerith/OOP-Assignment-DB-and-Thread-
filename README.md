# Library Borrowing System 

## Project Structure

```text
java-library-thread-jdbc/
├── .vscode/
│   └── settings.json           # Automatically wires lib/**/*.jar in VS Code
├── lib/
│   └── mysql-connector-j.jar   # MySQL JDBC driver (download & place here)
├── src/
|   ├── /db
        └── schema.sql
│   ├── DatabaseConnection.java # MySQL connection & automatic DB/table setup
│   ├── Book.java               # Book entity model (POJO)
│   ├── BookDAO.java            # CRUD, search, and atomic borrow transaction
│   └── LibraryApp.java         # Swing GUI + Thread & invokeLater pattern
├── screenshot.png              # Application screenshot
└── README.md
```

## Code Overview

| File | Primary Responsibility | Key Components |
| :--- | :--- | :--- |
| `src/DatabaseConnection.java` | Manages MySQL connections and automatically initializes the database schema on startup. | `DriverManager.getConnection(...)`, `initializeDatabase()` creates `library_db` along with the `books` and `borrow_logs` tables if they do not exist. |
| `src/Book.java` | Object representation (Model/POJO) for a single row in the `books` table. | `id`, `title`, `author`, and `availableCopies` fields with getters. |
| `src/BookDAO.java` | Data Access Object layer handling all SQL queries. | `PreparedStatement`, `ResultSet`, CRUD (`addBook`, `findAll`, `updateBook`, `deleteBook`), and the transactional `borrowBook` method. |
| `src/LibraryApp.java` | Swing GUI with a search header, vertical sidebar form, main data table, and status bar. | `new Thread(...)`, [`SwingUtilities.invokeLater`](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/SwingUtilities.html), and `setBusy(true/false, ...)` to disable buttons during DB work. |

### 1. Automatic Database Initialization (`DatabaseConnection.java`)

When the application launches, `LibraryApp` invokes `DatabaseConnection.initializeDatabase()` on a background thread (`db-init-thread`). This method:
- Creates the `library_db` database if it does not already exist (`CREATE DATABASE IF NOT EXISTS`).
- Creates the `books` and `borrow_logs` tables with a Foreign Key constraint (`ON DELETE CASCADE`).

No manual SQL script execution in MySQL Workbench is required.

### 2. CRUD & Atomic Transactions (`BookDAO.java`)

All database operations use `PreparedStatement` to prevent SQL injection and `ResultSet` to map rows into `List<Book>`.

The **Borrow Book (`borrowBook`)** action uses an explicit JDBC transaction:
1. Disables auto-commit (`conn.setAutoCommit(false)`).
2. Verifies that `available_copies` is greater than `0`.
3. Decrements stock in the `books` table (`available_copies = available_copies - 1`).
4. Inserts a borrowing record into `borrow_logs`.
5. Calls `conn.commit()` when every step succeeds, or `conn.rollback()` if stock is empty or any SQL error occurs.

### 3. Threading Pattern & Responsive GUI (`LibraryApp.java`)

Swing processes all user input and screen repaints on a single [Event Dispatch Thread (EDT)](https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html). To keep the window from freezing while waiting on MySQL, every action follows this lifecycle:

1. **Click Handler (EDT):** Validates inputs, disables action buttons (`setBusy(true, "Saving...")`), and updates the footer status label.
2. **Background Thread:** Executes `BookDAO` calls (`addBook`, `updateBook`, `deleteBook`, `borrowBook`, or `findAll`) inside `new Thread(...)`.
3. **UI Update (`SwingUtilities.invokeLater`):** Safely refreshes the `JTable`, book counter, and status label on the EDT, and re-enables buttons (`setBusy(false, ...)`) on both success and failure.

### 4. Screenshots

## Prerequisites & How to Run (VS Code)

1. **Install JDK** (Java 11 or newer) and ensure **MySQL Server** is running.
2. **Download the JDBC Driver:** Grab the *Platform Independent* archive from [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/), extract `mysql-connector-j-x.x.x.jar`, and copy it into the `lib/` folder.
3. **Verify in VS Code:** Open the project folder in VS Code (with the *Extension Pack for Java*) and confirm the `.jar` appears under **JAVA PROJECTS -> Referenced Libraries** (pre-configured via `.vscode/settings.json`).
4. **Configure MySQL Credentials:** Update `USER` and `PASSWORD` in `src/DatabaseConnection.java` to match your local MySQL server.
5. **Run the App:** Open `src/LibraryApp.java` and click **Run** above the `main` method.
