package com.library.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.dao.impl.*;
import com.library.model.*;
import com.library.service.*;
import com.library.service.impl.*;
import com.library.util.AppLogger;
import spark.Spark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static spark.Spark.*;

/**
 * REST API Server exposing existing business services over HTTP on port 4567.
 * Pure additive bridge layer without changing any underlying model/dao/service classes.
 */
public class ApiServer {

    private static final Logger LOGGER = AppLogger.getLogger(ApiServer.class.getName());
    private static final int PORT = 4567;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                    LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString()))
            .create();

    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        port(PORT);

        // Static files mapping for embedded web server if desired
        staticFiles.externalLocation("frontend");

        // Enable CORS
        enableCORS();

        // ── Global exception handler: always return JSON, never HTML ──
        exception(Exception.class, (exception, request, response) -> {
            response.status(500);
            response.type("application/json");
            Map<String, String> errBody = new HashMap<>();
            errBody.put("error", exception.getMessage() != null
                    ? exception.getMessage() : "Internal server error");
            response.body(gson.toJson(errBody));
            LOGGER.warning("[API] Unhandled exception on " + request.requestMethod()
                    + " " + request.pathInfo() + ": " + exception.getMessage());
        });

        // Service Dependencies (Using existing business logic layer)
        BookDAO bookDAO = new BookDAOImpl();
        MemberDAO memberDAO = new MemberDAOImpl();
        TransactionService transactionService = new TransactionServiceImpl();
        FineService fineService = new FineServiceImpl();
        ReservationService reservationService = new ReservationServiceImpl();
        SearchReportService searchReportService = new SearchReportServiceImpl();

        // ----------------------------------------------------
        // HEALTH CHECK
        // ----------------------------------------------------
        get("/api/health", (req, res) -> {
            res.type("application/json");
            Map<String, String> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "Library Management REST API");
            health.put("port", String.valueOf(PORT));
            return gson.toJson(health);
        });

        // ----------------------------------------------------
        // BOOKS ENDPOINTS
        // ----------------------------------------------------
        get("/api/books", (req, res) -> {
            res.type("application/json");
            return gson.toJson(bookDAO.getAllBooks());
        });

        get("/api/books/search", (req, res) -> {
            res.type("application/json");
            String query = req.queryParams("q");
            String category = req.queryParams("category");
            List<Book> results = searchReportService.searchBooks(query, category);
            return gson.toJson(results);
        });

        get("/api/books/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            try {
                return gson.toJson(bookDAO.getBookById(id));
            } catch (Exception e) {
                res.status(404);
                return errorJson("Book not found with ID: " + id);
            }
        });

        post("/api/books", (req, res) -> {
            res.type("application/json");
            Book incoming = gson.fromJson(req.body(), Book.class);
            if (incoming.getAddedDate() == null) incoming.setAddedDate(LocalDate.now());
            if (incoming.getAvailableCopies() <= 0) incoming.setAvailableCopies(incoming.getTotalCopies());

            if (bookDAO.addBook(incoming)) {
                res.status(201);
                return gson.toJson(incoming);
            }
            res.status(400);
            return errorJson("Failed to add book.");
        });

        put("/api/books/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            try {
                Book incoming = gson.fromJson(req.body(), Book.class);
                incoming.setBookId(id);
                // Preserve addedDate and calculate availableCopies delta
                try {
                    Book existing = bookDAO.getBookById(id);
                    if (incoming.getAddedDate() == null) {
                        incoming.setAddedDate(existing.getAddedDate());
                    }
                    // Recalculate available copies: keep checked-out count constant
                    int delta = incoming.getTotalCopies() - existing.getTotalCopies();
                    int newAvail = Math.max(0, existing.getAvailableCopies() + delta);
                    incoming.setAvailableCopies(newAvail);
                } catch (Exception fetchEx) {
                    if (incoming.getAddedDate() == null) incoming.setAddedDate(LocalDate.now());
                    if (incoming.getAvailableCopies() <= 0)
                        incoming.setAvailableCopies(incoming.getTotalCopies());
                }
                if (bookDAO.updateBook(incoming)) {
                    return gson.toJson(incoming);
                }
                res.status(400);
                return errorJson("Failed to update book ID: " + id);
            } catch (Exception e) {
                res.status(400);
                return errorJson(e.getMessage() != null ? e.getMessage() : "Invalid request");
            }
        });

        delete("/api/books/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            if (bookDAO.deleteBook(id)) {
                return successJson("Book ID " + id + " deleted successfully.");
            }
            res.status(400);
            return errorJson("Could not delete book ID " + id + ".");
        });

        // ----------------------------------------------------
        // MEMBERS ENDPOINTS
        // ----------------------------------------------------
        get("/api/members", (req, res) -> {
            res.type("application/json");
            return gson.toJson(memberDAO.getAllMembers());
        });

        get("/api/members/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            try {
                return gson.toJson(memberDAO.getMemberById(id));
            } catch (Exception e) {
                res.status(404);
                return errorJson("Member not found with ID: " + id);
            }
        });

        post("/api/members", (req, res) -> {
            res.type("application/json");
            Member incoming = gson.fromJson(req.body(), Member.class);
            if (incoming.getMembershipDate() == null) incoming.setMembershipDate(LocalDate.now());
            if (memberDAO.addMember(incoming)) {
                res.status(201);
                return gson.toJson(incoming);
            }
            res.status(400);
            return errorJson("Failed to create member.");
        });

        put("/api/members/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            try {
                Member incoming = gson.fromJson(req.body(), Member.class);
                incoming.setMemberId(id);
                // Preserve membershipDate from existing record
                if (incoming.getMembershipDate() == null) {
                    try {
                        Member existing = memberDAO.getMemberById(id);
                        incoming.setMembershipDate(existing.getMembershipDate());
                    } catch (Exception fetchEx) {
                        incoming.setMembershipDate(LocalDate.now());
                    }
                }
                if (memberDAO.updateMember(incoming)) {
                    return gson.toJson(incoming);
                }
                res.status(400);
                return errorJson("Failed to update member ID: " + id);
            } catch (Exception e) {
                res.status(400);
                return errorJson(e.getMessage() != null ? e.getMessage() : "Invalid request");
            }
        });

        delete("/api/members/:id", (req, res) -> {
            res.type("application/json");
            int id = Integer.parseInt(req.params(":id"));
            if (memberDAO.deleteMember(id)) {
                return successJson("Member ID " + id + " deleted successfully.");
            }
            res.status(400);
            return errorJson("Could not delete member ID " + id + ".");
        });

        // ----------------------------------------------------
        // TRANSACTIONS ENDPOINTS (Borrowing / Returning)
        // ----------------------------------------------------
        get("/api/transactions", (req, res) -> {
            res.type("application/json");
            return gson.toJson(transactionService.getAllTransactions());
        });

        get("/api/transactions/overdue", (req, res) -> {
            res.type("application/json");
            return gson.toJson(transactionService.getOverdueBooks());
        });

        post("/api/transactions/issue", (req, res) -> {
            res.type("application/json");
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            int memberId = ((Number) body.get("memberId")).intValue();
            int bookId = ((Number) body.get("bookId")).intValue();
            String paymentMode = body.containsKey("paymentMode") ? String.valueOf(body.get("paymentMode")) : "IN_PERSON";

            try {
                Transaction t = transactionService.issueBook(memberId, bookId);
                if (t != null && paymentMode != null && !paymentMode.isEmpty()) {
                    t.setPaymentMode(paymentMode);
                    new TransactionDAOImpl().updateTransaction(t);
                }
                res.status(201);
                return gson.toJson(t);
            } catch (Exception e) {
                res.status(400);
                return errorJson(e.getMessage());
            }
        });

        post("/api/transactions/return", (req, res) -> {
            res.type("application/json");
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            int transactionId = ((Number) body.get("transactionId")).intValue();

            try {
                Transaction t = transactionService.returnBook(transactionId);
                return gson.toJson(t);
            } catch (Exception e) {
                res.status(400);
                return errorJson(e.getMessage());
            }
        });

        // ----------------------------------------------------
        // FINES ENDPOINTS
        // ----------------------------------------------------
        get("/api/fines", (req, res) -> {
            res.type("application/json");
            return gson.toJson(fineService.getAllFines());
        });

        get("/api/fines/unpaid", (req, res) -> {
            res.type("application/json");
            String mIdStr = req.queryParams("memberId");
            if (mIdStr != null && !mIdStr.isEmpty()) {
                int memberId = Integer.parseInt(mIdStr);
                return gson.toJson(fineService.getUnpaidFines(memberId));
            }
            return gson.toJson(fineService.getAllFines());
        });

        post("/api/fines/:id/pay", (req, res) -> {
            res.type("application/json");
            int fineId = Integer.parseInt(req.params(":id"));
            try {
                if (fineService.payFine(fineId)) {
                    return successJson("Fine ID " + fineId + " marked as paid successfully.");
                }
                res.status(400);
                return errorJson("Failed to update fine ID: " + fineId);
            } catch (Exception e) {
                res.status(400);
                return errorJson(e.getMessage());
            }
        });

        // ----------------------------------------------------
        // RESERVATIONS ENDPOINTS
        // ----------------------------------------------------
        get("/api/reservations", (req, res) -> {
            res.type("application/json");
            return gson.toJson(reservationService.getAllReservations());
        });

        post("/api/reservations", (req, res) -> {
            res.type("application/json");
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            int memberId = ((Number) body.get("memberId")).intValue();
            int bookId = ((Number) body.get("bookId")).intValue();

            try {
                Reservation r = reservationService.reserveBook(memberId, bookId);
                res.status(201);
                return gson.toJson(r);
            } catch (Exception e) {
                res.status(400);
                return errorJson(e.getMessage());
            }
        });

        // ----------------------------------------------------
        // REPORTS ENDPOINTS
        // ----------------------------------------------------
        get("/api/reports/most-borrowed", (req, res) -> {
            res.type("application/json");
            return gson.toJson(searchReportService.getMostBorrowedBooks(10));
        });

        get("/api/reports/active-members", (req, res) -> {
            res.type("application/json");
            return gson.toJson(searchReportService.getMostActiveMembers(10));
        });

        get("/api/reports/fines-collected", (req, res) -> {
            res.type("application/json");
            BigDecimal total = searchReportService.getTotalFinesCollectedThisMonth();
            Map<String, Object> map = new HashMap<>();
            map.put("totalCollected", total);
            return gson.toJson(map);
        });

        get("/api/reports/unpaid-fines", (req, res) -> {
            res.type("application/json");
            return gson.toJson(searchReportService.getMembersWithUnpaidFines());
        });

        LOGGER.info("REST API Server initialized and listening on http://localhost:" + PORT);
        System.out.println("==================================================");
        System.out.println("   Library Management System REST API Started    ");
        System.out.println("   Endpoint: http://localhost:" + PORT + "/api/health   ");
        System.out.println("==================================================");
    }

    public static void stopServer() {
        Spark.stop();
    }

    private static void enableCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
        });
    }

    private static String errorJson(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("error", message);
        return gson.toJson(map);
    }

    private static String successJson(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        return gson.toJson(map);
    }
}
