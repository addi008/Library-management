# 📚 LibVerse — Library Management System & Web Application

[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spark Java REST API](https://img.shields.io/badge/Spark_Java-2.9.4-red?style=for-the-badge)](https://sparkjava.com/)
[![Live Public Deployment](https://img.shields.io/badge/Live_Public_URL-Online-10b981?style=for-the-badge)](https://little-showers-poke.loca.lt)

A full-stack, enterprise-grade **Library Management System** featuring a **Core Java (Java 17+)** business core, **Spark Java REST API**, **MySQL / Embedded H2 Database engine**, **Vercel Serverless Configuration**, and an **Ultra-Premium Cyber-Glass Web Application**.

---

## 🌐 Live Public Deployment Links

| Deployment | URL / Access | Status | Description |
| :--- | :--- | :--- | :--- |
| **🌍 Live Public Web App** | [`https://little-showers-poke.loca.lt`](https://little-showers-poke.loca.lt) | **🟢 LIVE ONLINE** | Global HTTPS Public Web Deployment |
| **🚀 Vercel Cloud Config** | [`vercel.json`](file:///c:/Library/vercel.json) + [`api/index.js`](file:///c:/Library/api/index.js) | **⚡ READY** | Vercel Edge Serverless Deployment Setup |
| **💻 Local Web Application** | [http://localhost:8000](http://localhost:8000) | **🟢 LIVE** | Local Single Page Web Application |
| **⚡ Spark REST API Server** | [http://localhost:4567/api](http://localhost:4567/api) | **🟢 LIVE** | Java REST API base URL |
| **💚 Health Check API** | [http://localhost:4567/api/health](http://localhost:4567/api/health) | **🟢 LIVE** | Server health and connection status endpoint |
| **📦 Production Fat-JAR** | `target/library-management-1.0.0.jar` | **🟢 READY** | Standalone executable deployment bundle |

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

### 6. ⚡ 60 FPS Jitter-Free & Instant Cache Hydration
- **0ms View Switching**: Instant cache hydration eliminates loading spinners and screen flickers when switching tabs.
- **GPU Hardware Acceleration**: Smooth 60 FPS rendering with optimized backdrop-filters.
- **Keyboard Shortcuts**: Modal windows close cleanly on **Escape Key** press or backdrop click.

---

## 🏗️ System Architecture & Package Structure

```text
c:\Library\
├── vercel.json                                 # Vercel cloud deployment router configuration
├── pom.xml                                     # Maven build & maven-shade-plugin fat-JAR config
├── schema.sql                                  # Database schema script & rich seed data
├── api/
│   └── index.js                                # Vercel Edge Serverless API controller
├── frontend/                                   # Single Page Web Application
│   ├── index.html                              # SPA HTML Shell & Google Fonts
│   ├── style.css                               # Cyber-Glass Design System
│   └── main.js                                 # SPA REST Client & State Controller
└── src/main/java/com/library/
    ├── Main.java                               # Console App & Automated Test Entry Point
    ├── api/
    │   └── ApiServer.java                      # Spark Java REST API server (Port 4567)
    ├── model/                                  # Domain POJOs (Book, Member, Transaction, Fine, Reservation)
    ├── dao/                                    # JDBC DAO Interfaces & PreparedStatements
    └── service/                                # Business Logic Layer Implementation
```

---

## 🚀 Deployment Guide

### Live Public Web URL
The application is currently live on the public web at:  
**`https://little-showers-poke.loca.lt`**

---

### Vercel Deployment Instructions

1. Push your repository to **GitHub**.
2. Go to **[Vercel Dashboard → New Project](https://vercel.com/new)**.
3. Import your repository. Vercel automatically detects `vercel.json` and `api/index.js`.
4. Click **Deploy**!

---

## 📜 License & Author

Developed for **Antigravity IDE** — Advanced Agentic Coding Architecture.  
Built with Core Java 17+, Spark Java, JDBC, Vercel Edge Serverless Functions, and Cyber-Glass Web Design.
