# Prompt for Antigravity IDE — Frontend for Library Management System

> This continues your existing project. Copy this in as a **new task** — it only ADDS a new layer on top of what you already built (Phases 1–5). None of your existing `model/`, `dao/`, `service/`, or `ui/` (console) code needs to change.

---

## Context (tell Antigravity this first)

I already have a working Java + JDBC + MySQL Library Management System with this structure:
- `model/` — Book, Member, Transaction, Fine, Reservation
- `dao/` — BookDAO, MemberDAO, TransactionDAO, FineDAO, ReservationDAO (JDBC, PreparedStatements)
- `service/` — BookService/TransactionService/FineService/ReservationService (business logic: issue, return, fines, reservations)
- `ui/` — a console-based menu app

**Do not modify or refactor any existing model/dao/service classes.** Only add new files/folders. The goal is to expose the existing service layer over HTTP and build a web frontend on top of it, so the console app keeps working exactly as-is.

---

## Phase 7 — REST API Layer (bridge, no changes to existing code)

1. Add a new `api/` package and add these Maven dependencies: `spark-core` (Spark Java, lightweight — no need for full Spring Boot conversion) and `gson` (JSON serialization).
   - If Spark feels too minimal, use **Spring Boot** instead, but only as an *additive* module: create `@RestController` classes that internally call your existing `service` classes — don't touch the services themselves.
2. Create REST controllers/routes that call your **existing service methods directly** (just wrap them, don't reimplement logic):
   - `GET /api/books` → BookService.getAllBooks()
   - `GET /api/books/search?q=` → BookService.search(...)
   - `POST /api/books` , `PUT /api/books/:id`, `DELETE /api/books/:id`
   - `GET /api/members`, `POST /api/members`, `PUT /api/members/:id`, `DELETE /api/members/:id`
   - `POST /api/transactions/issue` → body `{memberId, bookId}`
   - `POST /api/transactions/return` → body `{transactionId}`
   - `GET /api/transactions/overdue`
   - `GET /api/fines/unpaid?memberId=`
   - `POST /api/fines/:id/pay`
   - `GET /api/reservations`, `POST /api/reservations`
   - `GET /api/reports/most-borrowed`, `GET /api/reports/active-members`
3. Serialize all responses as JSON. Add CORS headers so a browser frontend on a different port can call this API.
4. Keep this running on its own port (e.g., `4567` for Spark, or `8080` if Spring Boot) — separate from nothing else, since your console app doesn't use a port at all.

**Deliverable:** All existing functionality reachable via REST endpoints, testable with `curl` or Postman, with zero changes to `model/dao/service`.

---

## Phase 8 — Frontend Setup

Build the frontend as a **separate folder** (`frontend/`) at the project root — completely decoupled from the Java code, communicating only via the REST API from Phase 7.

Use **plain HTML/CSS/JavaScript (Vite, no framework)** for a fast, dependency-light build that's easy to demo:
- `frontend/index.html`, `frontend/style.css`, `frontend/main.js`
- Use `fetch()` to call the REST API (`http://localhost:4567/api/...` or your chosen port)
- Dark glassmorphism UI style: dark background, frosted-glass panels (`backdrop-filter: blur()`, translucent `rgba` cards), subtle accent color, rounded corners, soft shadows

Pages/views (single-page app, tab or sidebar based navigation):
1. **Dashboard** — quick stats: total books, total members, books currently issued, unpaid fines total (from `/api/reports/*`)
2. **Books** — table of all books, search bar, add/edit/delete forms (modal or inline)
3. **Members** — table of members, add/edit/delete forms
4. **Transactions** — issue book form (select member + book), return book action, overdue list highlighted in red
5. **Fines** — list of unpaid fines per member, "Mark as Paid" button
6. **Reservations** — list + create reservation form

**Deliverable:** A working single-page frontend that lists real data pulled from your Java backend via REST, with working add/issue/return/pay actions.

---

## Phase 9 — Polish & Integration Testing

1. Add loading states and error handling in the frontend (e.g., show a message if the API is unreachable, disable buttons while a request is in-flight).
2. Add simple client-side validation (matching what your service layer already validates) so bad requests are caught before hitting the API.
3. Write a short `RUNNING.md` explaining: how to start the Java REST API (Phase 7), how to start the frontend (`npm run dev` or just open `index.html`), and which ports are used.
4. Do a full end-to-end test: issue a book from the frontend → confirm it reflects in MySQL → return it from the frontend → confirm fine logic still works exactly as it did in the console version.

**Deliverable:** Fully working full-stack app (Java/MySQL backend + HTML/CSS/JS frontend) where the original console app and the new web frontend both work off the same database and service logic, without any conflicts.

---

## Instructions to Antigravity IDE

- Treat this as **additive only** — do not edit, rename, or refactor anything inside existing `model/`, `dao/`, `service/`, or `ui/` packages.
- Confirm before adding any new dependency to `pom.xml`.
- Keep the REST layer as a thin wrapper — all business logic (fines, availability checks, validation) must stay in the existing `service/` classes, not be duplicated in controllers.
- After Phase 7, pause and let me test the API with Postman/curl before starting the frontend.
- After Phase 8, pause and let me see the UI before Phase 9 polish.
