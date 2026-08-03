# 📚 Library Management System (Java + JDBC + MySQL + REST API + Web UI)

A full-stack, enterprise-layered **Library Management System** built with **Core Java (Java 17+)**, **JDBC**, **MySQL / H2 Database**, **Spark Java REST API**, and a modern **Dark Glassmorphism Web Frontend (HTML/CSS/JS)**.

The system handles books, members, transactions, fine calculations, reservations, and aggregate analytical reports using clean design patterns (**DAO**, **Singleton**, **Service Layer**, **DTO**, and **REST Controllers**).

---

## 🌟 Key Features

- **📚 Book & Member Management**: Complete CRUD operations using safe `PreparedStatement` queries to prevent SQL injection.
- **🔄 Transaction Lifecycle**: Issue & Return workflow with automatic stock tracking and 14-day due date calculation.
- **⚠️ Business Constraints**:
  - Max borrowing limit: A member can borrow at most **3 active books** concurrently (`MAX_BORROW_LIMIT = 3`).
  - Stock validation: Books with 0 available copies cannot be issued.
- **💰 Automatic Fine Calculation**: Calculates **$1.00 / day** overdue fines automatically on return and records `Fine` entities.
- **🔖 Reservations & Auto-Notifications**: Reserve out-of-stock books; returning a copy automatically emits a notification for the earliest `PENDING` reservation.
- **📊 Aggregate SQL Reporting**:
  - Top Most Borrowed Books
  - Top Most Active Members
  - Monthly Fine Revenue Collection
  - Members with Unpaid Fines
- **📱 Dual User Interfaces**:
  1. **Menu-Driven Console UI** (`com.library.ui.MainMenu` / `com.library.Main`) with robust regex input validation and crash protection.
  2. **Dark Glassmorphism Web Application** (`frontend/index.html`) connected to the Java REST API server (`http://localhost:4567`).

---

## 🏗️ System Architecture & Package Structure

```text
c:\Library\
├── pom.xml                                     # Maven build configuration
├── schema.sql                                  # MySQL / H2 database schema script & seed data
├── library.log                                 # Application event log file
├── lib/                                        # Standard JAR dependencies (JDBC, Spark, Gson)
├── frontend/                                   # Standalone Web Frontend
│   ├── index.html                              # Single Page Application HTML shell
│   ├── style.css                               # Dark Glassmorphism CSS design system
│   └── main.js                                 # SPA JavaScript REST client & controller
└── src/main/
    ├── java/com/library/
    │   ├── Main.java                           # Console App entry point
    │   ├── Phase1Test.java - Phase5Test.java   # Automated test suites per phase
    │   ├── api/
    │   │   └── ApiServer.java                  # Spark Java REST API server (Port 4567)
    │   ├── model/                              # Domain POJOs (Book, Member, Transaction, Fine, Reservation)
    │   ├── dto/                                # Data Transfer Objects for reports
    │   ├── exception/                          # Domain Exception hierarchy
    │   ├── dao/                                # Data Access Interfaces & PreparedStatements
    │   │   └── impl/                           # JDBC DAO implementations
    │   ├── service/                            # Business Logic Service Interfaces
    │   │   └── impl/                           # Transaction & Fine business logic implementations
    │   ├── util/
    │   │   ├── DBConnection.java               # Singleton Connection Manager (MySQL + H2 Fallback)
    │   │   ├── InputValidator.java              # Regex & numeric validator with auto-retry
    │   │   └── AppLogger.java                   # Centralized logger initializer
    │   └── ui/
    │       └── MainMenu.java                   # Polished console menu interface
    └── resources/
        └── db.properties                       # Database configuration file
```

---

## 🗄️ Database Schema (`schema.sql`)

The backend works with both **MySQL Server** and **embedded H2 Database**:

- `books`: `book_id` (PK), `title`, `author`, `isbn` (UNIQUE, INDEX), `category`, `total_copies`, `available_copies`, `added_date`.
- `members`: `member_id` (PK), `name`, `email` (UNIQUE, INDEX), `phone`, `membership_date`, `membership_type`.
- `transactions`: `transaction_id` (PK), `book_id` (FK), `member_id` (FK), `issue_date`, `due_date`, `return_date`, `status` (`ISSUED`, `RETURNED`, `OVERDUE`).
- `fines`: `fine_id` (PK), `transaction_id` (FK), `amount`, `paid`, `paid_date`.
- `reservations`: `reservation_id` (PK), `book_id` (FK), `member_id` (FK), `reservation_date`, `status` (`PENDING`, `FULFILLED`, `CANCELLED`).

---

## 🚀 How to Run the Application

### Prerequisites
- **Java Development Kit (JDK 17+)** installed (`java -version`)
- Optional: **MySQL Server** (if absent, system automatically uses embedded H2 fallback for zero-setup execution!)

---

### Option 1: Run the Interactive Console Menu Application

1. **Compile the Java sources:**
   ```powershell
   javac -d target/classes -cp "lib/*" (Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse).FullName
   Copy-Item -Path "src/main/resources/*", "schema.sql" -Destination "target/classes" -Force
   ```

2. **Launch Main Console App:**
   ```powershell
   java -cp "target/classes;lib/*" com.library.Main
   ```

---

### Option 2: Run the REST API & Web Frontend

1. **Start the REST API Server (Port 4567):**
   ```powershell
   java -cp "target/classes;lib/*" com.library.api.ApiServer
   ```
   *The server starts listening on `http://localhost:4567/api/health`.*

2. **Open the Web Application:**
   - Double-click [`frontend/index.html`](file:///c:/Library/frontend/index.html) or open it in any web browser.
   - Alternatively, serve it via `npx serve frontend` or VS Code Live Server.

---

## 🌐 REST API Endpoints Reference (`http://localhost:4567`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/health` | Check API health status |
| `GET` | `/api/books` | Get list of all books |
| `GET` | `/api/books/search?q=kw` | Search books by keyword or category |
| `POST` | `/api/books` | Create a new book record |
| `PUT` | `/api/books/:id` | Update an existing book |
| `DELETE`| `/api/books/:id` | Delete a book record |
| `GET` | `/api/members` | Get list of all registered members |
| `POST` | `/api/members` | Register a new member |
| `PUT` | `/api/members/:id` | Update member details |
| `DELETE`| `/api/members/:id` | Delete a member record |
| `GET` | `/api/transactions` | Get list of all transactions |
| `POST` | `/api/transactions/issue` | Issue a book (`{ "memberId": 1, "bookId": 2 }`) |
| `POST` | `/api/transactions/return` | Return a book (`{ "transactionId": 5 }`) |
| `GET` | `/api/transactions/overdue` | Get list of overdue transactions |
| `GET` | `/api/fines` | Get list of all fines |
| `GET` | `/api/fines/unpaid?memberId=` | Get unpaid fines for a member |
| `POST` | `/api/fines/:id/pay` | Pay fine record |
| `GET` | `/api/reservations` | Get list of all reservations |
| `POST` | `/api/reservations` | Place reservation (`{ "memberId": 1, "bookId": 3 }`) |
| `GET` | `/api/reports/most-borrowed` | Report: Top borrowed books |
| `GET` | `/api/reports/active-members` | Report: Top active members |
| `GET` | `/api/reports/fines-collected` | Report: Total fines collected |
| `GET` | `/api/reports/unpaid-fines` | Report: Members with unpaid fines |

---

## 🧪 Running Automated System Verification

You can execute the complete end-to-end automated test suites anytime:

- **System Integration Test:**
  ```powershell
  java -cp "target/classes;lib/*" com.library.Main --test
  ```

- **Phase Specific Tests:**
  ```powershell
  java -cp "target/classes;lib/*" com.library.Phase1Test
  java -cp "target/classes;lib/*" com.library.Phase2Test
  java -cp "target/classes;lib/*" com.library.Phase3Test
  java -cp "target/classes;lib/*" com.library.Phase4Test
  java -cp "target/classes;lib/*" com.library.Phase5Test
  ```

---

## 📜 License & Author

Developed for **Antigravity IDE** — Advanced Agentic Coding Architecture.
Built with Java 17+, JDBC, Spark Java, and Vanilla HTML5/CSS3/JavaScript.
