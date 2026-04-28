# Project Overview: File Explorer with Encryption

This project is a Java-based File Explorer application that features hybrid encryption (RSA + AES) for securing files, as well as tracking recent files and user favorites.

## Core Technologies
- **Language:** Java
- **Database:** SQLite (managed via JDBC)
- **Encryption:** RSA-OAEP for key wrapping and AES-256-GCM for file encryption.
- **Persistence:** DAO (Data Access Object) pattern for database operations.

## Architecture & Structure
The project is organized into several packages:
- `db`: Contains the database management logic, schema, DAOs, and database tests.
  - `DatabaseManager.java`: Singleton managing the SQLite connection and schema initialization.
  - `EncryptedFileDAO.java`: Manages storage and retrieval of RSA-wrapped AES keys and IVs.
  - `RecentFilesDAO.java`: Tracks recently opened files (limited to 50 entries).
  - `FavoritesDAO.java`: Manages user-bookmarked paths.
  - `schema.sql`: Defines the SQLite table structure.
  - `DBTest.java`: A standalone test suite for verifying the `db` package.
- `model`: Data models (e.g., `EncryptedFileRecord`, `FileItem` hierarchy).
- `operations`: Core file operations (e.g., Copy, Move, Delete, Zip).
- `services`: High-level application logic (e.g., `FileExplorerService`).
- `threading`: Background task management using `ThreadPoolManager` and `SwingWorker`.
- `ui`: Swing user interface components (`ModernFileManagerUI.java`).

## Building and Running

### Prerequisites
- Java JDK 11 or higher.
- SQLite JDBC Driver (e.g., `sqlite-jdbc-*.jar`).

### Key Commands
- **Run Database Tests:**
  Execute the `db.DBTest` class. Ensure the SQLite JDBC driver is on your classpath.
  ```bash
  # Example (adjust classpath as needed)
  javac -d bin src/db/*.java src/model/EncryptedFileRecord.java src/services/*.java src/threading/*.java src/operations/*.java
  java -cp bin:lib/sqlite-jdbc.jar db.DBTest
  ```
- **Build Project:**
  TODO: Document build system (Maven/Gradle) once configured.

## Development Conventions
- **Database:** All database interactions should go through `DatabaseManager.getInstance().getConnection()`.
- **Encryption Metadata:** Never store raw AES keys. Always use the `EncryptedFileDAO` to store RSA-wrapped keys and IVs.
- **Comments:** Follow the established Javadoc style for documenting DAO responsibilities and usage.
- **Testing:** Add test cases to `DBTest.java` (or new test classes) for any changes to the persistence layer.
