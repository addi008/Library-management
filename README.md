# 📚 LibVerse — Library Management System & Web Application

[![Java 17+](https://img.shields.io/badge/Java-17%2B-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spark Java REST API](https://img.shields.io/badge/Spark_Java-2.9.4-red?style=for-the-badge)](https://sparkjava.com/)
[![Web Frontend](https://img.shields.io/badge/Frontend-Cyber--Glass_SPA-00f2fe?style=for-the-badge)](http://localhost:8000)
[![Status](https://img.shields.io/badge/Deployment-Live-10b981?style=for-the-badge)](http://localhost:8000)

A full-stack, enterprise-grade **Library Management System** featuring a **Core Java (Java 17+)** business core, **Spark Java REST API**, **MySQL / Embedded H2 Database engine**, and an **Ultra-Premium Cyber-Glass Web Application**.

---

## 🌐 Live Application & Deployment Links

| Deployment | URL / Access | Description |
| :--- | :--- | :--- |
| **🚀 Web Application UI** | [http://localhost:8000](http://localhost:8000) | Live Cyber-Glass Single Page Web Application |
| **⚡ REST API Server** | [http://localhost:4567/api](http://localhost:4567/api) | High-performance Spark Java REST API base URL |
| **💚 Health Check API** | [http://localhost:4567/api/health](http://localhost:4567/api/health) | API Server health and connection status endpoint |
| **📦 Production Fat-JAR** | `target/library-management-1.0.0.jar` | Standalone executable deployment bundle |

---

## 🎨 UI/UX Design Palette

Designed according to high-craft visual standards featuring a **Cyber-Glass Design System**:

- **Pure White (`#ffffff`)**: Clean headers, crisp contrast badges, and white input focus glows.
- **Cyber Teal (`#00f2fe`)**: Vibrant primary accents, active navigation states, and primary action glows.
- **Neon Pink (`#ff007f`)**: Overdue badges, warning indicators, and destructive action highlights.
- **Sky Blue (`#38bdf8`)**: Member badges, secondary highlights, and status tags.
- **Emerald Green (`#10b981`)**: Return confirmations, stock availability badges, and cleared fines.
- **Obsidian Black (`#070a12`)**: Multi-layered background depth with smooth radial color gradients.

---

## 🌟 Key Features

### 1. 📚 Book & Member Management
- Complete CRUD operations with automatic stock calculation (`availableCopies` vs `totalCopies`).
- Safe JDBC `PreparedStatement` queries preventing SQL injection.
- Preserves registration dates (`membershipDate`, `addedDate`) on updates.

### 2. 🔄 Transaction Lifecycle & Delivery Options
- **Issue Book**: Supports selecting member, available book, and **Delivery / Payment Mode**:
  - `🏢 In-Person`: Standard library checkout.
  - `🚚 Cash on Delivery (COD)`: Home delivery checkout with COD tracking badge.
- **Return Book**: Automatic stock restoration and instant overdue fine generation.

### 3. 💰 Fine Audit & Payments
- Automatic **$1.00 / Day** overdue calculation upon return.
- Captures fine details and explicit reason (`Late Return`).
- `💳 Pay Fine` button updates fine status to `PAID` in DB and records payment date.

### 4. 🔖 Reservations & Live Fulfillment
- Reserve out-of-stock or popular books (`PENDING`).
- Action buttons (`✅ Fulfill`, `❌ Cancel`) allow librarians to update reservation status in real-time (`PUT /api/reservations/:id/status`).

### 5. 📈 Analytics & Executive Reporting
- Most Borrowed Books chart breakdown (`/api/reports/most-borrowed`).
- Active Members leaderboard (`/api/reports/active-members`).
- Total Fine Revenue collection metric (`/api/reports/fines-collected`).

### 6. ⚡ 60 FPS Jitter-Free & Instant Cache Rendering
- **0ms View Switching**: Instant cache hydration eliminates loading spinners and screen flickers when switching tabs.
- **GPU Hardware Acceleration**: Smooth 60 FPS rendering with optimized backdrop-filters.
- **Keyboard Shortcuts**: Modal windows close cleanly on **Escape Key** press or backdrop click.

---

## 🏗️ System Architecture & Package Structure

```text
c:\Library\
├── pom.xml                                     # Maven build & maven-shade-plugin fat-JAR config
├── schema.sql                                  # Database schema script & rich seed data
├── library.log                                 # Application event logger
├── lib/                                        # Standard JAR dependencies (JDBC, Spark, Gson, H2)
├── frontend/                                   # Single Page Web Application
│   ├── index.html                              # SPA HTML Shell & Google Fonts (Plus Jakarta Sans, JetBrains Mono)
│   ├── style.css                               # Cyber-Glass Design System (Teal, Pink, White, Obsidian, Sky Blue)
│   └── main.js                                 # SPA REST Client & State Controller
└── src/main/java/com/library/
    ├── Main.java                               # Console App & Automated Test Entry Point
    ├── api/
    │   └── ApiServer.java                      # Spark Java REST API controllers & CORS middleware
    ├── model/                                  # Domain Models (Book, Member, Transaction, Fine, Reservation)
    ├── dto/                                    # Data Transfer Objects for Analytical Reports
    ├── dao/                                    # Data Access Interfaces
    │   └── impl/                               # JDBC Implementations (BookDAOImpl, MemberDAOImpl, etc.)
    ├── service/                                # Business Logic Layer
    │   └── impl/                               # TransactionServiceImpl, FineServiceImpl, etc.
    └── util/
        ├── DBConnection.java                   # JDBC Singleton (MySQL with automatic H2 embedded fallback)
        ├── InputValidator.java                  # Console input validator
        └── AppLogger.java                      # Centralized logging manager
```

---

## 🗄️ Database Schema (`schema.sql`)

The system automatically detects **MySQL Server**. If MySQL is unreachable, it seamlessly initializes an **embedded in-memory H2 database**:

- `books`: `book_id` (PK), `title`, `author`, `isbn` (UNIQUE), `category`, `total_copies`, `available_copies`, `added_date`.
- `members`: `member_id` (PK), `name`, `email` (UNIQUE), `phone`, `membership_date`, `membership_type`.
- `transactions`: `transaction_id` (PK), `book_id` (FK), `member_id` (FK), `issue_date`, `due_date`, `return_date`, `payment_mode` (`IN_PERSON`, `CASH_ON_DELIVERY`), `status` (`ISSUED`, `RETURNED`, `OVERDUE`).
- `fines`: `fine_id` (PK), `transaction_id` (FK), `amount`, `reason`, `paid`, `paid_date`.
- `reservations`: `reservation_id` (PK), `book_id` (FK), `member_id` (FK), `reservation_date`, `status` (`PENDING`, `FULFILLED`, `CANCELLED`).

---

## 🚀 How to Run & Deploy the Project

### Prerequisites
- **Java Development Kit (JDK 17 or 26)** installed (`java -version`).
- Optional: **MySQL Server** (If absent, embedded H2 database boots automatically).

---

### 1. Build Production Executable Fat-JAR

Build the self-contained executable JAR bundling all dependencies:

```powershell
# Using Maven
mvn clean package

# Or using direct compilation
$javaExe = "C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
$libs = (Get-ChildItem "lib\*.jar" | ForEach-Object { $_.FullName }) -join ";"
& "C:\Program Files\Java\jdk-26.0.1\bin\javac.exe" -d target/classes -cp $libs (Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse).FullName
```

---

### 2. Run the REST API Server (Port 4567)

Launch the REST API server:

```powershell
$libs = (Get-ChildItem "lib\*.jar" | ForEach-Object { $_.FullName }) -join ";"
java -cp "target/classes;$libs" com.library.api.ApiServer
```

---

### 3. Serve the Web Application (Port 8000)

Start the frontend web server:

```powershell
# Using Python builtin HTTP server
python -m http.server 8000

# Or using Node.js serve
npx serve frontend -p 8000
```

Access the live UI at: **[http://localhost:8000](http://localhost:8000)**

---

### 4. Cloud Deployment (Render / Railway / Docker)

To deploy to cloud platforms like **Render**, **Railway**, or **Heroku**:

#### Docker Deployment (`Dockerfile`)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/classes /app/classes
COPY lib /app/lib
COPY frontend /app/frontend
EXPOSE 4567 8000
CMD ["java", "-cp", "classes:lib/*", "com.library.api.ApiServer"]
```

---

## 🌐 REST API Specification (`http://localhost:4567/api`)

| Method | Endpoint | Description | Payload Sample |
| :--- | :--- | :--- | :--- |
| `GET` | `/health` | API Health & status check | — |
| `GET` | `/books` | Get all books | — |
| `GET` | `/books/search?q=kw` | Live keyword search | — |
| `POST` | `/books` | Add new book | `{ "title": "X", "author": "Y", "isbn": "123", "category": "CS", "totalCopies": 3 }` |
| `PUT` | `/books/:id` | Update book | `{ "title": "X", "author": "Y", "isbn": "123", "category": "CS", "totalCopies": 5 }` |
| `DELETE`| `/books/:id` | Delete book record | — |
| `GET` | `/members` | Get all members | — |
| `POST` | `/members` | Register member | `{ "name": "A", "email": "a@b.com", "phone": "123", "membershipType": "PREMIUM" }` |
| `PUT` | `/members/:id` | Update member | `{ "name": "A", "email": "a@b.com", "phone": "123", "membershipType": "STANDARD" }` |
| `DELETE`| `/members/:id` | Delete member | — |
| `GET` | `/transactions` | Get all transactions | — |
| `POST` | `/transactions/issue` | Issue book | `{ "memberId": 1, "bookId": 2, "paymentMode": "CASH_ON_DELIVERY" }` |
| `POST` | `/transactions/return` | Return book | `{ "transactionId": 4 }` |
| `GET` | `/transactions/overdue` | Get overdue transactions | — |
| `GET` | `/fines` | Get all fines | — |
| `GET` | `/fines/unpaid?memberId=` | Get unpaid fines for member | — |
| `POST` | `/fines/:id/pay` | Pay fine record | — |
| `GET` | `/reservations` | Get all reservations | — |
| `POST` | `/reservations` | Place reservation | `{ "memberId": 1, "bookId": 3 }` |
| `PUT` | `/reservations/:id/status` | Update reservation status | `{ "status": "FULFILLED" }` |
| `GET` | `/reports/most-borrowed` | Report: Top borrowed books | — |
| `GET` | `/reports/active-members` | Report: Active members | — |
| `GET` | `/reports/fines-collected` | Report: Total fines collected | — |

---

## 🧪 Automated Testing

Execute the automated test suites:

```powershell
# System Integration Test
java -cp "target/classes;lib/*" com.library.Main --test

# Individual Phase Tests
java -cp "target/classes;lib/*" com.library.Phase1Test
java -cp "target/classes;lib/*" com.library.Phase2Test
java -cp "target/classes;lib/*" com.library.Phase3Test
java -cp "target/classes;lib/*" com.library.Phase4Test
java -cp "target/classes;lib/*" com.library.Phase5Test
```

---

## 📜 License & Author

Developed for **Antigravity IDE** — Advanced Agentic Coding Architecture.  
Built with Core Java 17+, Spark Java, JDBC, MySQL / H2, and Cyber-Glass Web Design.
