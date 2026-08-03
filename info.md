# Prompt for Antigravity IDE — Library Management System (Java + SQL)

> Copy everything below into Antigravity IDE as your project prompt/task description.

---

## Project Overview

Build a **Library Management System** using **Java** (core Java + JDBC) with a **MySQL/PostgreSQL** database backend. The system should manage books, members, borrowing/returning transactions, fines, and reservations, with a clean layered architecture (DAO pattern, separation of concerns) so it can later be extended with a GUI (Swing/JavaFX) or REST API.

**Tech stack:**
- Language: Java 17+
- Database: MySQL (via JDBC) — schema provided below
- Build tool: Maven
- Architecture: Layered — `model`, `dao`, `service`, `ui` (console first, GUI optional later)
- Design patterns: DAO, Singleton (DB connection), Factory (optional)

Build this project in **phases**. Complete and verify each phase before moving to the next. After each phase, run the code, test it against sample data, and summarize what was built before proceeding.

---

## Phase 1 — Project Setup & Database Design

1. Initialize a Maven project with folders: `model`, `dao`, `service`, `util`, `ui`.
2. Add dependency: `mysql-connector-j` (JDBC driver).
3. Design and create the SQL schema (`schema.sql`) with these tables:
   - `books` (book_id PK, title, author, isbn, category, total_copies, available_copies, added_date)
   - `members` (member_id PK, name, email UNIQUE, phone, membership_date, membership_type)
   - `transactions` (transaction_id PK, book_id FK, member_id FK, issue_date, due_date, return_date, status ENUM('ISSUED','RETURNED','OVERDUE'))
   - `fines` (fine_id PK, transaction_id FK, amount, paid BOOLEAN, paid_date)
   - `reservations` (reservation_id PK, book_id FK, member_id FK, reservation_date, status ENUM('PENDING','FULFILLED','CANCELLED'))
4. Add appropriate foreign keys, `NOT NULL` constraints, and indexes on `isbn` and `email`.
5. Create a `DBConnection` utility class (Singleton) using a `db.properties` file for credentials (never hardcode credentials).
6. Insert 5–10 sample rows into `books` and `members` for testing.

**Deliverable:** Working DB connection test (`SELECT 1`) printed successfully from Java, plus the `schema.sql` file.

---

## Phase 2 — Core Models & DAO Layer

1. Create POJO model classes: `Book`, `Member`, `Transaction`, `Fine`, `Reservation` — with constructors, getters/setters, `toString()`.
2. Create DAO interfaces + implementations for each entity:
   - `BookDAO`: addBook, updateBook, deleteBook, getBookById, getAllBooks, searchByTitle/Author/ISBN
   - `MemberDAO`: addMember, updateMember, deleteMember, getMemberById, getAllMembers
3. Use `PreparedStatement` everywhere (no string concatenation — prevent SQL injection).
4. Implement proper try-with-resources for connections/statements/result sets.
5. Add basic custom exceptions (e.g., `BookNotFoundException`, `MemberNotFoundException`).

**Deliverable:** Console test class that adds, updates, fetches, and deletes a book and a member, confirming DAO layer works end-to-end.

---

## Phase 3 — Borrowing, Returning & Business Logic

1. Create `TransactionDAO` and `TransactionService`:
   - `issueBook(memberId, bookId)` — checks `available_copies > 0`, decrements it, creates a transaction row with due date (e.g., +14 days).
   - `returnBook(transactionId)` — sets return_date, increments `available_copies`, marks status `RETURNED`.
   - `getOverdueBooks()` — lists transactions where `due_date < today` and status is `ISSUED`.
2. Implement fine calculation logic: e.g., ₹5/day (or $1/day) after due date, auto-create a `Fine` record on return if overdue.
3. Add validation: a member cannot borrow more than N books at once (e.g., 3); a book with 0 available copies cannot be issued.
4. Add a `FineService` with `payFine(fineId)` and `getUnpaidFines(memberId)`.

**Deliverable:** Console-driven demo simulating: issue → overdue check → return → fine generated → fine paid.

---

## Phase 4 — Reservations & Search/Reporting

1. Implement `ReservationService`: if a book has 0 available copies, a member can reserve it; when a copy is returned, auto-notify (console message) the earliest pending reservation.
2. Add search/filter features: search books by title/author/category (partial match using `LIKE`), list all books currently issued to a member, list members with unpaid fines.
3. Add simple reports (via SQL aggregate queries): most borrowed books, most active members, total fines collected this month.

**Deliverable:** Menu-driven console app exposing all features from Phases 2–4.

---

## Phase 5 — Console UI Polish & Input Validation

1. Build a clean console menu system (`ui/MainMenu.java`) using a loop + switch-case, with sub-menus for Books, Members, Transactions, Fines, Reservations, Reports.
2. Add input validation (e.g., email format regex, phone number length, no negative IDs) with friendly error messages and no crashes on bad input.
3. Add logging (simple `java.util.logging` or a `Logger` util) for key actions (issue, return, fine created) instead of scattered `System.out.println` for errors.

**Deliverable:** A fully working console application that doesn't crash on invalid input and covers the full workflow.

---

## Phase 6 (Optional/Stretch) — GUI & Extras

1. Convert the console UI to a Java Swing (or JavaFX) desktop GUI with forms/tables (`JTable` bound to DAO results).
2. Add authentication (Librarian/Admin login) with a `users` table and hashed passwords (BCrypt via a small library).
3. Add CSV/PDF export for reports.
4. Add unit tests (JUnit 5 + an in-memory or test schema) for the service layer.

**Deliverable:** GUI version of the app, or a documented list of what's implemented if skipped.

---

## Instructions to Antigravity IDE

- Work through the phases **in order**; do not skip ahead.
- After each phase, show me the file structure changes and run a quick test/demo before continuing.
- Keep code in a layered structure (`model/`, `dao/`, `service/`, `ui/`, `util/`) — don't put SQL logic in the UI layer.
- Use `PreparedStatement` for every query — flag anything that isn't safely parameterized.
- Ask me before making schema changes once Phase 1 is complete.
