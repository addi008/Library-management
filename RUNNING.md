# RUNNING.md — LibVerse Library Management System

## Quick Reference

| Component      | Start Command                                                | Port / Access                    |
|----------------|--------------------------------------------------------------|----------------------------------|
| REST API Server| `java -cp "target/classes;lib/*" com.library.api.ApiServer`  | `http://localhost:4567/api`      |
| Web Frontend   | Open `frontend/index.html` in your browser                   | `file://…/frontend/index.html`   |
| Console App    | `java -cp "target/classes;lib/*" com.library.Main`           | Interactive terminal              |
| Health Check   | `curl http://localhost:4567/api/health`                      | Should return `{"status":"UP"}`  |

---

## Prerequisites

1. **Java 17+** — verify: `java -version`
2. **MySQL 8.x running** with database `library_db` created
3. **Schema loaded** — run once to create tables and seed data:
   ```bash
   mysql -u root -p library_db < schema.sql
   ```
4. **Credentials configured** in `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC
   db.user=root
   db.password=your_password
   ```
   > If MySQL is unavailable, the system automatically falls back to an embedded H2 in-memory database for testing.

---

## Step 1 — Compile

```powershell
# From project root c:\Library
mvn compile
```

Expected output: `BUILD SUCCESS`

---

## Step 2 — Start the REST API Server

```powershell
java -cp "target/classes;lib/*" com.library.api.ApiServer
```

Expected console output:
```
==================================================
   Library Management System REST API Started
   Endpoint: http://localhost:4567/api/health
==================================================
```

> **Leave this terminal window open.** The API server runs until you press Ctrl+C.

### Verify the API is running:
```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:4567/api/health"

# curl
curl http://localhost:4567/api/health
```

Expected: `{"status":"UP","service":"Library Management REST API","port":"4567"}`

---

## Step 3 — Open the Web Frontend

1. Open **`frontend/index.html`** directly in your browser (Chrome, Firefox, Edge).
2. The sidebar will show **"Connected (4567)"** with a green dot if the API is reachable.
3. If the server is offline, a red **offline banner** appears at the top — click **Retry Now** after starting the server.

> No build step or npm needed — pure HTML/CSS/JavaScript.

---

## Step 4 (Optional) — Run the Console App

The original console application works completely independently of the REST API and web frontend. Both read from the same MySQL database simultaneously.

```powershell
java -cp "target/classes;lib/*" com.library.Main
```

Follow the numbered menu to issue books, return books, pay fines, etc.

---

## Ports Used

| Port | Service             |
|------|---------------------|
| 4567 | Spark Java REST API |
| 3306 | MySQL (default)     |

No other ports are occupied.

---

## REST API Endpoint Reference

### Books
| Method | Endpoint             | Description                  |
|--------|----------------------|------------------------------|
| GET    | `/api/books`         | List all books               |
| GET    | `/api/books/search?q=` | Search by title/author/ISBN |
| GET    | `/api/books/:id`     | Get book by ID               |
| POST   | `/api/books`         | Add a new book               |
| PUT    | `/api/books/:id`     | Update book                  |
| DELETE | `/api/books/:id`     | Delete book                  |

### Members
| Method | Endpoint             | Description              |
|--------|----------------------|--------------------------|
| GET    | `/api/members`       | List all members         |
| GET    | `/api/members/:id`   | Get member by ID         |
| POST   | `/api/members`       | Register a new member    |
| PUT    | `/api/members/:id`   | Update member            |
| DELETE | `/api/members/:id`   | Delete member            |

### Transactions
| Method | Endpoint                     | Body / Params              | Description        |
|--------|------------------------------|----------------------------|--------------------|
| GET    | `/api/transactions`          |                            | All transactions   |
| GET    | `/api/transactions/overdue`  |                            | Overdue list       |
| POST   | `/api/transactions/issue`    | `{memberId, bookId}`       | Issue a book       |
| POST   | `/api/transactions/return`   | `{transactionId}`          | Return a book      |

### Fines
| Method | Endpoint                  | Params              | Description         |
|--------|---------------------------|---------------------|---------------------|
| GET    | `/api/fines`              |                     | All fines           |
| GET    | `/api/fines/unpaid`       | `?memberId=`        | Unpaid fines        |
| POST   | `/api/fines/:id/pay`      |                     | Mark fine as paid   |

### Reservations
| Method | Endpoint            | Body                  | Description           |
|--------|---------------------|-----------------------|-----------------------|
| GET    | `/api/reservations` |                       | All reservations      |
| POST   | `/api/reservations` | `{memberId, bookId}`  | Place reservation     |

### Reports
| Method | Endpoint                         | Description                        |
|--------|----------------------------------|------------------------------------|
| GET    | `/api/reports/most-borrowed`     | Top 10 most borrowed books         |
| GET    | `/api/reports/active-members`    | Top 10 most active members         |
| GET    | `/api/reports/fines-collected`   | Total fines collected this month   |
| GET    | `/api/reports/unpaid-fines`      | Members with outstanding fines     |

---

## End-to-End Test Walkthrough (Phase 9)

Follow these steps to verify the full system integration — from frontend to MySQL and back.

### Test 1 — Issue a Book via Web Frontend
1. Open `frontend/index.html` in browser, confirm **Connected (4567)** status.
2. Navigate to **Transactions** → click **📖 Issue Book**.
3. Select any member and any book with available copies > 0.
4. Click **Confirm Issue**. Expect a green toast: *"Book issued successfully! Due in 14 days."*

**Verify in MySQL:**
```sql
SELECT * FROM transactions ORDER BY transaction_id DESC LIMIT 1;
-- Should show a new row with status='ISSUED', return_date=NULL
```

### Test 2 — Return the Book & Confirm Fine Logic
1. In **Transactions** view, find the just-issued row (status: ISSUED).
2. Click **↩️ Return**.
3. If return date > due date, a second toast appears: *"A late return fine was automatically generated."*

**Verify in MySQL:**
```sql
SELECT * FROM transactions WHERE status = 'RETURNED' ORDER BY transaction_id DESC LIMIT 1;
SELECT * FROM fines ORDER BY fine_id DESC LIMIT 1;
-- Fine amount should equal ($1.00 × days_overdue) if late
```

### Test 3 — Pay the Fine via Web Frontend
1. Navigate to **Fines** view.
2. Find the unpaid fine (red UNPAID badge).
3. Click **💳 Pay $X.XX**.
4. Expect status changes to **PAID** badge (green).

**Verify in MySQL:**
```sql
SELECT fine_id, paid, paid_date FROM fines ORDER BY fine_id DESC LIMIT 1;
-- paid=1, paid_date=today
```

### Test 4 — Confirm Console App Reads Same Data
1. Run `java -cp "target/classes;lib/*" com.library.Main` in a second terminal.
2. Choose **View All Transactions** — the returned transaction from Test 2 should appear.
3. Choose **View Fines** — the paid fine from Test 3 should show as PAID.

✅ Both the web frontend and the console app read from the same database with no conflicts.

### Test 5 — Offline Detection
1. Stop the REST API server (Ctrl+C in the API terminal).
2. Wait up to 30 seconds, or navigate to another view in the browser.
3. The **red offline banner** should appear at the top.
4. Restart the server, click **Retry Now** — banner clears and data reloads.

---

## Troubleshooting

| Issue | Cause | Fix |
|---|---|---|
| `Connection refused` on API call | Server not started | Run Step 2 |
| `Address already in use :4567` | Previous instance still running | Kill with `Stop-Process -Name java` |
| H2 fallback mode warning in console | MySQL unreachable | Check MySQL service and `db.properties` credentials |
| CORS error in browser console | Wrong port | Ensure API runs on 4567, frontend fetches `http://localhost:4567/api/...` |
| `BUILD FAILURE` in mvn compile | Java/Maven not on PATH | Verify `java -version` and `mvn -version` both work |
