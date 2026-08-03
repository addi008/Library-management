# Library Management System

A robust, layered **Library Management System** built with **Java 17+** and **JDBC**, featuring a clean separation of concerns (**Model-DAO-Service-UI** architecture), transactional borrowing logic, automated fine calculations, reservation management with return auto-notifications, aggregate SQL analytics, and a polished menu-driven console application with input validation.

---

## 🌟 Key Features & Phased Architecture

### 📁 Phase 1 — Database Design & Connection Infrastructure
- **Relational MySQL Schema (`schema.sql`)**: Structured tables for `books`, `members`, `transactions`, `fines`, and `reservations` with strict Foreign Key constraints, `NOT NULL` validations, and `UNIQUE` indexes on `isbn` and `email`.
- **Singleton Connection Utility (`DBConnection`)**: Thread-safe database connection manager using externalized `db.properties` credentials.
- **Embedded Test Fallback**: Automatic failover to an in-memory H2 database (MySQL compatibility mode) if a local MySQL instance is unavailable, enabling zero-setup verification.

### 📦 Phase 2 — Core Domain Models & DAO Layer
- **POJO Entities**: Domain models (`Book`, `Member`, `Transaction`, `Fine`, `Reservation`) equipped with encapsulated properties, constructors, getters/setters, and `toString()` formatters.
- **Custom Exceptions**: Domain-specific exception handling (`BookNotFoundException`, `MemberNotFoundException`).
- **Data Access Objects**: `BookDAO` and `MemberDAO` interfaces and JDBC implementations using parameterized `PreparedStatement` queries to guarantee SQL injection safety and try-with-resources resource management.

### 🔄 Phase 3 — Borrowing, Returning & Business Logic
- **Transactional Borrowing (`TransactionService`)**: Enforces business rules:
  - **Borrowing Limit**: A member can borrow a maximum of **3 books** concurrently (`MAX_BORROW_LIMIT = 3`). Exceeding this throws `MaxBooksBorrowedException`.
  - **Inventory Stock Check**: Borrowing requires `available_copies > 0`. Out-of-stock requests raise `BookNotAvailableException`.
- **Overdue Fine Calculation (`FineService`)**: Calculates fines at **$1.00 / day** for overdue returns and automatically creates pending `Fine` records upon book return.
- **Fine Payments**: Fine tracking and payment processing (`payFine`, `getUnpaidFines`).

### 🔖 Phase 4 — Reservations & Search/Reporting
- **Reservation System**: Members can place reservations on out-of-stock books (`available_copies == 0`).
- **Automated Return Alerts**: Returning a book automatically scans for the earliest pending reservation and emits an **auto-notification alert** in the console.
- **Search & Filtering**: Partial keyword matching (`LIKE`) across titles, authors, categories, and ISBNs.
- **SQL Aggregate Reports (`ReportDAO`)**:
  - Most borrowed books (`GROUP BY`, `ORDER BY DESC`).
  - Most active library members.
  - Monthly fine collection metrics (`SUM`).
  - Members with outstanding unpaid fines.

### 🛡️ Phase 5 — Console UI Polish & Input Validation
- **Interactive Menu UI (`MainMenu`)**: Intuitive loop + switch-case console application featuring organized sub-menus for Books, Members, Transactions, Fines, Reservations, and Analytics.
- **Robust Input Validation (`InputValidator`)**: Regex email validation, phone number formatting, positive integer checks, and non-empty text validation to prevent crashes (`NumberFormatException`, `InputMismatchException`).
- **Centralized Logging (`AppLogger`)**: Configured logging via `java.util.logging` saving operations to `library.log`.

---

## 🛠️ Tech Stack

- **Language**: Java 17+ (Compatible up to Java 26)
- **Database**: MySQL Server 8.x / Embedded H2 (via JDBC)
- **Driver**: `mysql-connector-j-8.3.0.jar`
- **Build Tool**: Maven (`pom.xml`)
- **Design Patterns**: DAO (Data Access Object), Singleton, DTO (Data Transfer Object)

---

## 📂 Project Structure

```
c:\Library\
├── pom.xml                                  # Maven project configuration
├── schema.sql                               # Complete Database Schema & Seed Data
├── db.properties                            # Database connection credentials
├── lib/                                     # Bundled JDBC JAR dependencies
│   ├── mysql-connector-j-8.3.0.jar
│   └── h2-2.2.224.jar
├── src/
│   └── main/
│       ├── java/
│       │   └── com/library/
│       │       ├── Main.java                # Application entry point
│       │       ├── Phase1Test.java          # Phase 1 DB Connection deliverable test
│       │       ├── Phase2Test.java          # Phase 2 DAO CRUD test suite
│       │       ├── Phase3Test.java          # Phase 3 Borrow/Return/Fine test suite
│       │       ├── Phase4Test.java          # Phase 4 Reservations & Reports test suite
│       │       ├── Phase5Test.java          # Phase 5 Input Validation & Logger test suite
│       │       ├── dto/                     # Data Transfer Objects for Reports
│       │       │   ├── BookBorrowReportDTO.java
│       │       │   ├── MemberActivityReportDTO.java
│       │       │   └── MemberWithUnpaidFineDTO.java
│       │       ├── exception/               # Custom Domain Exceptions
│       │       │   ├── BookNotFoundException.java
│       │       │   ├── BookNotAvailableException.java
│       │       │   ├── MaxBooksBorrowedException.java
│       │       │   ├── MemberNotFoundException.java
│       │       │   ├── InvalidTransactionException.java
│       │       │   ├── FineNotFoundException.java
│       │       │   └── ReservationException.java
│       │       ├── model/                   # Domain Model POJOs
│       │       │   ├── Book.java
│       │       │   ├── Member.java
│       │       │   ├── Transaction.java
│       │       │   ├── Fine.java
│       │       │   └── Reservation.java
│       │       ├── dao/                     # DAO Interfaces & JDBC Implementations
│       │       │   ├── BookDAO.java
│       │       │   ├── MemberDAO.java
│       │       │   ├── TransactionDAO.java
│       │       │   ├── FineDAO.java
│       │       │   ├── ReservationDAO.java
│       │       │   ├── ReportDAO.java
│       │       │   └── impl/
│       │       │       ├── BookDAOImpl.java
│       │       │       ├── MemberDAOImpl.java
│       │       │       ├── TransactionDAOImpl.java
│       │       │       ├── FineDAOImpl.java
│       │       │       ├── ReservationDAOImpl.java
│       │       │       └── ReportDAOImpl.java
│       │       ├── service/                 # Business Service Layer
│       │       │   ├── TransactionService.java
│       │       │   ├── FineService.java
│       │       │   ├── ReservationService.java
│       │       │   ├── SearchReportService.java
│       │       │   └── impl/
│       │       │       ├── TransactionServiceImpl.java
│       │       │       ├── FineServiceImpl.java
│       │       │       ├── ReservationServiceImpl.java
│       │       │       └── SearchReportServiceImpl.java
│       │       ├── ui/                      # Presentation Layer
│       │       │   └── MainMenu.java        # Interactive Console Menu App
│       │       └── util/                    # System Utilities
│       │           ├── DBConnection.java    # Singleton DB Connection Manager
│       │           ├── InputValidator.java  # Input Validation & Sanitization
│       │           └── AppLogger.java       # Centralized Application Logging
│       └── resources/
│           └── db.properties                # Resource classpath configuration
```

---

## 🗄️ Database Schema Overview (`schema.sql`)

- **`books`**: `book_id` (PK), `title`, `author`, `isbn` (UNIQUE, INDEX), `category`, `total_copies`, `available_copies`, `added_date`.
- **`members`**: `member_id` (PK), `name`, `email` (UNIQUE, INDEX), `phone`, `membership_date`, `membership_type`.
- **`transactions`**: `transaction_id` (PK), `book_id` (FK), `member_id` (FK), `issue_date`, `due_date`, `return_date`, `status` (`ISSUED`, `RETURNED`, `OVERDUE`).
- **`fines`**: `fine_id` (PK), `transaction_id` (FK), `amount`, `paid` (BOOLEAN), `paid_date`.
- **`reservations`**: `reservation_id` (PK), `book_id` (FK), `member_id` (FK), `reservation_date`, `status` (`PENDING`, `FULFILLED`, `CANCELLED`).

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java SE Development Kit (JDK 17 or higher)** installed.
- **Git** installed.
- *(Optional)* **MySQL Server 8.0+** (If MySQL is not running locally, the application automatically uses embedded H2 memory mode).

### 2. Configuration (`db.properties`)
Configure database parameters in `src/main/resources/db.properties` or `db.properties`:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
```

### 3. Compilation
Compile all Java source files:
```powershell
javac -d target/classes -cp "lib/*" (Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse).FullName
Copy-Item -Path "src/main/resources/*", "schema.sql" -Destination "target/classes" -Force
```

### 4. Running the Interactive Application
Launch the menu-driven console app:
```powershell
java -cp "target/classes;lib/*" com.library.Main
```

### 5. Running Automated Verification Suites
Run phase test suites directly:
```powershell
# Run system end-to-end verification
java -cp "target/classes;lib/*" com.library.Main --test

# Run Phase 5 Input Validation & Logger Test
java -cp "target/classes;lib/*" com.library.Phase5Test
```

---

## 📝 License

This project is open source and available under the [MIT License](LICENSE).
